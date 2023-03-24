package pe.kr.thekey78.messenger;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pe.kr.thekey78.messenger.annotation.*;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.utils.*;

import javax.el.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

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

    public <T> byte[] getBytes(@NonNull T vo) throws IllegalArgumentException {
        Class<?> clazz = vo.getClass();
        Field[] fields = clazz.getDeclaredFields();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // List에 대한 Reference 필드 처리
        for (Field field : fields) {
            // 필드가 List 일 경우
            if (List.class.isAssignableFrom(field.getType())) {
                boolean accessible = field.canAccess(vo);
                field.setAccessible(true);
                try {
                    Object obj = field.get(vo);
                    if (obj instanceof List) {
                        List<?> list = List.class.cast(obj);
                        doReference(vo, field, list.size());
                    }
                    if (obj instanceof Object[]) {
                        Object[] list = Object[].class.cast(obj);
                        doReference(vo, field, list.length);
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new MessageException(e);
                } finally {
                    field.setAccessible(accessible);
                }
            }
        }

        for (Field field : fields) {
            boolean accessible = field.canAccess(vo);
            field.setAccessible(true);

            Except except = field.getAnnotation(Except.class);
            if (except != null && except.value()) continue;

            try {
                Object obj = field.get(vo);
                if (ClassUtils.isPrimitive(field.getType()) || field.getType() == String.class) {
                    DefaultValue a_defaultValue = field.getAnnotation(DefaultValue.class);

                    String defaultValue = "";
                    if (a_defaultValue != null)
                        defaultValue = a_defaultValue.value();

                    String str = StringUtils.defaultIfEmpty(Objects.toString(obj, defaultValue), defaultValue);

                    Extension extension = field.getAnnotation(Extension.class);
                    str = VoUtils.doExtension(extension, str);

                    Length a_length = field.getAnnotation(Length.class);
                    String charEncoding = VoUtils.getCharEncoding(field.getAnnotation(CharEncoding.class));

                    if (StringUtils.isNotBlank(a_length.ref())) {
                        Object ref = findRefValue(vo, a_length.ref());
                        int refVal;
                        if (ref instanceof Number)
                            refVal = ((Number) ref).intValue();
                        else
                            refVal = NumberUtils.intValue(ref.toString());
                        a_length = VoUtils.newLength(a_length, refVal);
                    }

                    byte[] bytes = VoUtils.toMessageBytes(str, a_length, charEncoding);

                    baos.write(bytes);
                } else if (obj instanceof List) {
                    // 필드가 List 일 경우
                    List<?> list = List.class.cast(obj);
                    baos.write(getBytes(list));
                } else {
                    // 필드가 VO인 경우
                    baos.write(getBytes(obj));
                } // end if
            } catch (IllegalAccessException | IOException | NoSuchFieldException e) {
                throw new MessageException(e);
            } finally {
                field.setAccessible(accessible);
            }
        } // end for

        return baos.toByteArray();
    }

    private static <T> void doReference(@NonNull T vo, @NonNull Field field, int size) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = vo.getClass();
        Reference ref = field.getAnnotation(Reference.class);
        // 참조 필드가 같은 vo 안에 있는 경우만 처리 가능
        Field refField = clazz.getDeclaredField(ref.value());
        boolean b = refField.canAccess(vo);
        refField.setAccessible(true);

        // 참조필드에 List size set
        refField.set(vo, size);
        refField.setAccessible(b);
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
            if (except != null && except.value()) continue;

            try {
                Condition condition = field.getAnnotation(Condition.class);
                if (condition != null) {
                    try {
                        Object obj = findRefValue(vo, condition.ref());
                        ConditionWrapper conditionWrapper = new ConditionWrapper(condition.ref(), obj);

                        ExpressionFactory elFactory = ExpressionFactory.newInstance();
                        ELContext elContext = new StandardELContext(elFactory);
                        elContext.getELResolver().setValue(elContext, null, "ref", obj);

                        Object result = elFactory.createValueExpression(elContext, condition.el(), Boolean.class).getValue(elContext);
                        if (!(result instanceof Boolean)) {
                            throw new ELException("EL expression did not return a boolean result");
                        }

                        if (!(Boolean) result) {
                            continue;
                        }

                    } catch (NoSuchFieldException e) {
                        throw new MessageException(e);
                    }


                    // TODO
                }

                MessageBuildHelper<?> f = makeFunction(vo, field, array, position);
                field.set(vo, f.getElement());
                position += f.getLength();
            } catch (IllegalAccessException | UnsupportedEncodingException e) {
                throw new MessageException(e);
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
        Supplier<?> sf = null;

        final LongAdder len = new LongAdder();

        Class<?> type = field.getType();
        if (ClassUtils.isPrimitive(type) || ClassType.getClassType(type) == ClassType.STRING) {
            Length a_length = field.getAnnotation(Length.class);
            DecimalPosition decimalPosition = field.getAnnotation(DecimalPosition.class);

            byte[] valueArr = new byte[a_length.value()];
            System.arraycopy(array, position, valueArr, 0, a_length.value());

            if (StringUtils.isNotBlank(a_length.ref())) {
                Object findObj = null;
                try {
                    findObj = findRefValue(vo, a_length.ref());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new MessageException(e);
                }
                int refVal = 0;
                if (findObj instanceof Number)
                    refVal = ((Number) findObj).intValue();
                else
                    refVal = NumberUtils.intValue(findObj.toString());
                a_length = VoUtils.newLength(a_length, refVal);
            }
            valueArr = VoUtils.doExtension(field.getAnnotation(Extension.class), valueArr);

            final String encoding = VoUtils.getCharEncoding(field.getAnnotation(CharEncoding.class));
            sf = getSupplier(valueArr, type, decimalPosition, encoding);
            len.add(a_length.value());
        } else if (List.class.isAssignableFrom(type)) {
            // 필드가 List일 경우
            sf = getSupplier(vo, field, array, position, len, type);
        } else {
            sf = getSupplier(vo, field, array, type, position);
            len.add(VoUtils.length(sf.get()));
        }

        return new MessageBuildHelper<>(len.intValue(), sf.get());
    }

    private Supplier<?> getSupplier(@NonNull Object vo, @NonNull Field field, @NonNull byte[] array, int position, LongAdder len, Class<?> type) {
        return () -> {
            try {
                Reference ref = field.getAnnotation(Reference.class);

                // 참조 필드가 같은 vo 안에 있는 경우
                Field refField = vo.getClass().getDeclaredField(ref.value());
                boolean canAccess = refField.canAccess(vo);
                if (!canAccess)
                    refField.setAccessible(true);

                Object findObj = findRefValue(vo, ref.value());
                int repeatCnt = 0;
                if (findObj != null) {
                    if (ClassUtils.isWholeNumber(findObj.getClass())) {
                        repeatCnt = ((Number) findObj).intValue();
                    } else {
                        repeatCnt = NumberUtils.intValue(findObj.toString());
                    }
                }

                refField.setAccessible(canAccess);

                List<Object> result = null;
                if (type.isInterface()) {
                    result = new ArrayList<>();
                } else if (type.isAnonymousClass()) {
                    result = new ArrayList<>();
                } else {
                    result = (List<Object>) ReflectionUtils.newInstance(type);
                }

                for (int i = 0; i < repeatCnt; i++) {
                    Object newVo = ReflectionUtils.newInstance(ReflectionUtils.getGenericClass(field.getGenericType()));
                    result.add(makeVo(newVo, array, position));
                    len.add(VoUtils.length(newVo));
                }

                return result;
            } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException |
                     InvocationTargetException | InstantiationException e) {
                throw new MessageException(e);
            }
        };
    }

    private <T> T makeVo(@NonNull T subVo, @NonNull byte[] array, int p) {
        byte[] decoration = new byte[array.length - p];
        System.arraycopy(array, p, decoration, 0, decoration.length);
        fromBytes(decoration, subVo);
        return subVo;
    }

    private Supplier<Object> getSupplier(@NonNull byte[] v, @NonNull Class<?> type, DecimalPosition decimalPosition, String encoding) {
        Supplier<Object> sf = null;
        switch (ClassType.getClassType(type)) {
            case BYTE:
                sf = () -> NumberUtils.byteValue(v);
                break;
            case SHORT:
                sf = () -> NumberUtils.shortValue(v);
                break;
            case INTEGER:
                sf = () -> NumberUtils.intValue(v);
                break;
            case LONG:
                sf = () -> NumberUtils.longValue(v);
                break;
            case DOUBLE:
                sf = () -> NumberUtils.doubleValue(putDecimalPoint(decimalPosition, v));
                break;
            case FLOAT:
                sf = () -> NumberUtils.floatValue(putDecimalPoint(decimalPosition, v));
                break;
            case BIG_INTEGER:
                sf = () -> NumberUtils.createBigInteger(v);
                break;
            case BIG_DECIMAL:
                sf = () -> NumberUtils.createBigDecimal(putDecimalPoint(decimalPosition, v));
                break;
            case BOOLEAN:
                sf = () -> Boolean.valueOf(new String(v));
            case CHARACTER:
                sf = () -> {
                    try {
                        return new String(v, encoding).charAt(0);
                    } catch (UnsupportedEncodingException e) {
                        throw new MessageException(e);
                    }
                };
            case STRING:
                sf = () -> {
                    try {
                        return new String(v, encoding).trim();
                    } catch (UnsupportedEncodingException e) {
                        throw new MessageException(e);
                    }
                };
        }
        return sf;
    }

    private <T> Supplier<?> getSupplier(@NonNull T vo, @NonNull Field field, @NonNull byte[] array, @NonNull Class<?> type, int p) {
        return () -> {
            try {
                Object subVo = field.get(vo);
                if (subVo == null) {
                    subVo = type.getConstructor().newInstance();
                }
                return makeVo(subVo, array, p);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
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
        if (align == Align.RIGHT)
            builder.append(value.substring(0, value.length() - p))
                    .append(".")
                    .append(value.substring(value.length() - p));
        else
            builder.append(value.substring(0, p))
                    .append(".")
                    .append(value.substring(p));
        return builder.toString();
    }

    private int getLength(Object obj) {
        if (obj == null)
            return 0;
        else if (ClassUtils.isNumber(obj.getClass()))
            return obj.toString().length();
        else if (obj instanceof String)
            return ((String) obj).length();
        else if (obj instanceof List)
            return ((List) obj).size();
        else if (obj instanceof Map)
            return ((Map) obj).size();
        else
            return VoUtils.length(obj);
    }

    private static CompositeELResolver createELELResolver(ELContext context, ConditionWrapper obj) {
        CompositeELResolver resolver = new CompositeELResolver();
        resolver.add(new BeanELResolver());
        resolver.add(new ArrayELResolver());
        resolver.add(new ListELResolver());
//        resolver.setValue(context, obj, "this", obj);
        resolver.setValue(context, null, "ref", obj.value);

        return resolver;
    }


    @Getter
    @AllArgsConstructor
    @RequiredArgsConstructor
    private static class MessageBuildHelper<E> {
        private int length;
        private E element;
    }

    @Data
    public static class ConditionWrapper {
        private Object ref;
        private Object value;

        private ConditionWrapper(Object ref, Object value) {
            this.ref = ref;
            this.value = value;
        }
    }
}
