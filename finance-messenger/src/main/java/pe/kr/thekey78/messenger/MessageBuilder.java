package pe.kr.thekey78.messenger;

import org.apache.commons.lang3.StringUtils;
import pe.kr.thekey78.messenger.annotation.*;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.function.MessageBuildHelper;
import pe.kr.thekey78.messenger.utils.ClassUtils;
import pe.kr.thekey78.messenger.utils.NumberUtils;
import pe.kr.thekey78.messenger.utils.ReflectionUtils;
import pe.kr.thekey78.messenger.utils.VoUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageBuilder {
    private static class Singleton {
        private static final MessageBuilder INSTANCE = new MessageBuilder();
    }

    private Map<String, MessageExtension> extensionMap;

    private MessageBuilder() {
        extensionMap = new HashMap<>();
    }

    public void setExtensionMap(@NonNull Map<String, @NonNull MessageExtension> extensionMap) {
        this.extensionMap = extensionMap;
    }

    public void setExtension(@NonNull String key, @NonNull MessageExtension messageExtension) {
        this.extensionMap.put(key, messageExtension);
    }

    public Map<String, MessageExtension> getExtensionMap() {
        return extensionMap;
    }

    public MessageExtension getMessageExtension(String key) {
        return extensionMap.get(key);
    }

    public static MessageBuilder getInstance() {
        return Singleton.INSTANCE;
    }

    private <T> void processList(@NonNull T vo) {
        Class<?> clazz = vo.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            boolean accessible = field.canAccess(vo);
            field.setAccessible(true);
            try{
                Object obj = field.get(vo);
                if (obj instanceof List) {
                    // 필드가 List 일 경우
                    Reference ref = field.getAnnotation(Reference.class);
                    // 참조 필드가 같은 vo 안에 있는 경우
                    Field refField = clazz.getDeclaredField(ref.value());
                    boolean b = refField.canAccess(vo);
                    refField.setAccessible(true);

                    List<?> list = List.class.cast(obj) ;
                    refField.set(vo, list.size());
                    refField.setAccessible(b);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new MessageException(e);
            } finally {
                field.setAccessible(accessible);
            }
        }
    }

    public <T> byte[] getBytes(@NonNull T vo) throws IllegalArgumentException {
        processList(vo);

        Class<?> clazz = vo.getClass();
        Field[] fields = clazz.getDeclaredFields();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (Field field:fields) {
            boolean accessible = field.canAccess(vo);
            field.setAccessible(true);

            Except except = field.getAnnotation(Except.class);
            if(except != null && except.value()) continue;

            try {
                Object obj = field.get(vo);
                if (obj instanceof List) {
                    // 필드가 List 일 경우
                    List<?> list = List.class.cast(obj) ;
                    baos.write(getBytes(list));
                } else if (ClassUtils.isPrimitive(field.getType()) || field.getType() == String.class){
                    DefaultValue a_defaultValue = field.getAnnotation(DefaultValue.class);

                    String defaultValue = "";
                    if (a_defaultValue != null)
                        defaultValue = a_defaultValue.value();

                    String str = StringUtils.defaultIfEmpty(Objects.toString(obj, defaultValue), defaultValue);

                    Extension extension = field.getAnnotation(Extension.class);
                    str = VoUtils.doExtension(extension, str);

                    Length a_length = field.getAnnotation(Length.class);
                    String charEncoding = VoUtils.getCharEncoding(field.getAnnotation(CharEncoding.class));

                    byte[] bytes = VoUtils.toMessageBytes(str, a_length, charEncoding);

                    baos.write(bytes);
                } else {
                    // 필드가 VO인 경우
                    baos.write(getBytes(obj));
                } // end if
            } catch (IllegalAccessException | IOException e) {
                throw new MessageException(e);
            } finally {
                field.setAccessible(accessible);
            }
        } // end for

        return baos.toByteArray();
    }

    private byte[] getBytes(@NonNull List<?> collection) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (Object nextObj : collection) {
            if (nextObj != null) {
                byteArrayOutputStream.write(getBytes(nextObj));
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public <T> T fromBytes(@NonNull final byte[] array, @NonNull final Class<T> t) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T outputBody = t.getConstructor().newInstance();
        fromBytes(array, outputBody);
        return outputBody;
    }

    public <T> void fromBytes(@NonNull final byte[] array, @NonNull final T vo) {
        fromBytes(array, 0, vo);
    }

    public <T> void fromBytes(@NonNull final byte[] array, int position, @NonNull final T vo) {
        Class<?> clazz = vo.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            boolean accessible = field.canAccess(vo);

            if (!accessible)
                field.setAccessible(true);
            // 전문에서 제외되는 필드
            Except except = field.getAnnotation(Except.class);
            if(except != null && except.value()) continue;

            try {
                Condition condition = field.getAnnotation(Condition.class);
                if(condition != null) {
                    Object refObj = findRefValue(vo, condition.ref());

                    if(!Objects.equals(condition.test(), refObj))
                        continue;
                }

                MessageBuildHelper<?> f = makeFunction(vo, field, array, position);
                field.set(vo, f.get());
                position += f.length();
            } catch (IllegalAccessException | UnsupportedEncodingException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            } finally {
                field.setAccessible(accessible);
            }
        }
    }

    private static <T> Object findRefValue(@NonNull T vo, @NonNull String refValue) throws NoSuchFieldException, IllegalAccessException {
        String[] strRefs = refValue.split("[.]");
        Object refObj = vo;
        for (String strRef : strRefs) {
            Field refField = refObj.getClass().getDeclaredField(strRef);
            refField.setAccessible(true);
            refObj = refField.get(refObj);
        }
        return refObj;
    }

    @SuppressWarnings("cast")
    private MessageBuildHelper<?> makeFunction(@NonNull final Object vo, final @NonNull Field field, @NonNull final byte[] array, final int position) throws UnsupportedEncodingException {
        MessageBuildHelper<?> f = null;
        Supplier<?> sf = null;

        final LongAdder len = new LongAdder();

        Class<?> type = field.getType();
        if(ClassUtils.isNumber(type)) {
            Length a_length = field.getAnnotation(Length.class);
            DecimalPosition decimalPosition = field.getAnnotation(DecimalPosition.class);

            byte[] valueArr = new byte[a_length.value()];
            System.arraycopy(array, position, valueArr, 0, a_length.value());
            sf = getSupplier(decimalPosition, valueArr, type);
            len.add(a_length.value());
        }
        else if(List.class.isAssignableFrom(type)) {
            // 필드가 List일 경우
            sf = () -> {
                try {
                    Reference ref = field.getAnnotation(Reference.class);

                    // 참조 필드가 같은 vo 안에 있는 경우
                    Field refField = vo.getClass().getDeclaredField(ref.value());
                    boolean canAccess = refField.canAccess(vo);
                    if(!canAccess)
                        refField.setAccessible(true);

                    Object findObj = findRefValue(vo, ref.value());
                    int repeatCnt = 0;
                    if(findObj != null) {
                        if(ClassUtils.isWholeNumber(findObj.getClass())) {
                            repeatCnt = ((Number)findObj).intValue();
                        }
                        else {
                            repeatCnt = NumberUtils.intValue(findObj.toString());
                        }
                    }

                    refField.setAccessible(canAccess);

                    List<Object> result = null;
                    if(type.isInterface()) {
                        result = new ArrayList<>();
                    }
                    else if(type.isAnonymousClass()) {
                        result = new ArrayList<>();
                    }
                    else {
                        result = (List<Object>) ReflectionUtils.newInstance(type);
                    }

                    for(int i = 0; i < repeatCnt; i++) {
                        Object newVo = ReflectionUtils.newInstance(ReflectionUtils.getGenericClass(field.getGenericType()));
                        makeVo(newVo, array, position);
                        result.add(newVo);
                        len.add(VoUtils.length(newVo));
                    }

                    return result;
                } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException |
                         InvocationTargetException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            };
        }
        else if(ClassUtils.isPrimitive(type) || type == String.class) {
            final String encoding = VoUtils.getCharEncoding(field.getAnnotation(CharEncoding.class));

            Length a_length = field.getAnnotation(Length.class);
            final byte[] valueArr = new byte[a_length.value()];
            System.arraycopy(array, position, valueArr, 0, a_length.value());

            Extension extension = field.getAnnotation(Extension.class);
            sf = () -> {
                try {
                    return VoUtils.doExtension(extension, new String(valueArr, encoding).trim());
                } catch (UnsupportedEncodingException e) {
                    return VoUtils.doExtension(extension, new String(valueArr).trim());
                }
            };
            len.add(a_length.value());
        }
        else {
            sf = getSupplier(vo, field, array, type, position);
            len.add(VoUtils.length(sf.get()));
        }

        Supplier<?> finalSf = sf;
        f = new MessageBuildHelper<>() {
            @Override
            public Object get() {
                this.length = len.intValue();
                return finalSf.get();
            }
        };

        return f;
    }

    private <T> T makeVo(@NonNull T subVo, @NonNull byte[] array, int p) {
        byte[] decoration = new byte[array.length - p];
        System.arraycopy(array, p, decoration, 0, decoration.length);
        fromBytes(decoration, subVo);
        return subVo;
    }

    private Supplier<Object> getSupplier(DecimalPosition decimalPosition, @NonNull byte[] v, @NonNull Class<?> type) {
        Supplier<Object> sf = null;
        if(type == Byte.class || type == byte.class)
            sf = () -> NumberUtils.byteValue(v);
        else if(type == Short.class || type == short.class)
            sf = () -> NumberUtils.shortValue(v);
        else if(type == Integer.class || type == int.class)
            sf = () ->  NumberUtils.intValue(v);
        else if(type == Long.class || type == long.class)
            sf = () ->  NumberUtils.longValue(v);
        else if(type == Double.class || type == double.class)
            sf = () ->  NumberUtils.doubleValue(putDecimalPoint(decimalPosition, v));
        else if(type == Float.class || type == float.class)
            sf = () ->  NumberUtils.floatValue(putDecimalPoint(decimalPosition, v));
        else if(type == BigInteger.class)
            sf = () ->  NumberUtils.createBigInteger(v);
        else if(type == BigDecimal.class)
            sf = () ->  NumberUtils.createBigDecimal(putDecimalPoint(decimalPosition, v));
        return sf;
    }

    private <T> Supplier<?> getSupplier(@NonNull T vo, @NonNull Field field, @NonNull byte[] array, @NonNull Class<?> type, int p) {
        return () -> {
            try {
                Object subVo = field.get(vo);
                if(subVo == null) {
                    subVo = type.getConstructor().newInstance();
                }
                return makeVo(subVo, array, p);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new MessageException(e);
            }
        };
    }

    private String putDecimalPoint(@NonNull DecimalPosition decimalPosition, @NonNull byte[] value) {
        return putDecimalPoint(decimalPosition, new String(value));
    }
    private String putDecimalPoint(@NonNull DecimalPosition decimalPosition, @NonNull String value) {
        int p = decimalPosition.position();
        Align align = decimalPosition.type();
        StringBuilder builder = new StringBuilder();
        if(align == Align.RIGHT)
            builder.append(value.substring(0, value.length() - p))
                    .append(".")
                    .append(value.substring(value.length() - p));
        else
            builder.append(value.substring(0, p))
                    .append(".")
                    .append(value.substring(p));
        return builder.toString();
    }
}
