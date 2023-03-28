package pe.kr.thekey78.messenger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pe.kr.thekey78.messenger.annotation.*;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.utils.*;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
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
                    List<?> list = (List) field.get(vo);
                    setReferenceSize(vo, field.getAnnotation(Reference.class), list.size());
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new MessageException(e);
                } finally {
                    field.setAccessible(accessible);
                }
            } else if (field.getType().isArray()) {
                boolean accessible = field.canAccess(vo);
                field.setAccessible(true);
                try {
                    Object[] list = (Object[]) field.get(vo);
                    setReferenceSize(vo, field.getAnnotation(Reference.class), list.length);
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

                    Length length = field.getAnnotation(Length.class);

                    if (StringUtils.isNotBlank(length.ref())) {
                        Object ref = findRefValue(vo, length.ref());
                        int refVal;
                        if (ref instanceof Number)
                            refVal = ((Number) ref).intValue();
                        else
                            refVal = NumberUtils.intValue(ref.toString());
                        length = VoUtils.newLength(length, refVal);
                    }

                    byte[] bytes = VoUtils.toMessageBytes(str, length, field.getAnnotation(CharEncoding.class));

                    baos.write(bytes);
                } else if (obj instanceof List) {
                    // 필드가 List 일 경우
                    baos.write(getBytes((List) obj));
                } else if (obj.getClass().isArray()) {
                    // 필드가 List 일 경우
                    baos.write(getBytes(Arrays.asList(obj)));
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

    private <T> void setReferenceSize(@NonNull T vo, @NonNull Reference ref, int size) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = vo.getClass();
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

    public <T> T fromBytes(@NonNull final byte[] array, @NonNull final Class<T> t) {
        try {
            T newInstance = t.getConstructor().newInstance();
            fromBytes(array, newInstance);
            return newInstance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new MessageException(e);
        }
    }

    public <T> void fromBytes(@NonNull final byte[] array, @NonNull final T vo) {
        int position = 0;
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
                }

                MessageBuildHelper<?> f = getMessageBuildHelper(vo, field, array, position);
                field.set(vo, f.getElement());
                position += f.getLength();
            } catch (IllegalAccessException | UnsupportedEncodingException e) {
                throw new MessageException(e);
            } finally {
                field.setAccessible(accessible);
            }
        } // end for
    }

    private <T> Object findRefValue(@NonNull T vo, @NonNull String refValues) throws NoSuchFieldException, IllegalAccessException {
        String[] vs = refValues.split(",");
        if (vs.length > 1) {
            List<Object> result = new ArrayList<>();
            for (String v : vs) {
                result.add(getRefObj(vo, v.split("[.]")));
            }
            return result;
        } else {
            return getRefObj(vo, refValues.split("[.]"));
        }
    }

    private static <T> Object getRefObj(T vo, String[] strFields) throws NoSuchFieldException, IllegalAccessException {
        Object refObj = vo;
        for (String strRef : strFields) {
            Field refField = refObj.getClass().getDeclaredField(strRef.trim());
            refField.setAccessible(true);
            refObj = refField.get(refObj);
        }
        return refObj;
    }

    @SuppressWarnings("cast")
    private MessageBuildHelper<?> getMessageBuildHelper(@NonNull final Object vo, final @NonNull Field field, @NonNull final byte[] array, final int position) throws UnsupportedEncodingException {
        Supplier<?> sf;

        final LongAdder len = new LongAdder();

        Class<?> type = field.getType();
        if (ClassUtils.isPrimitive(type) || ClassType.getClassType(type) == ClassType.STRING) {
            Length a_length = field.getAnnotation(Length.class);
            DecimalPosition decimalPosition = field.getAnnotation(DecimalPosition.class);

            byte[] valueArr = new byte[a_length.value()];
            System.arraycopy(array, position, valueArr, 0, a_length.value());

            if (StringUtils.isNotBlank(a_length.ref())) {
                Object findObj;
                try {
                    findObj = findRefValue(vo, a_length.ref());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new MessageException(e);
                }
                int refVal;
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
            sf = getSupplierList(vo, field, array, position, len);
        } else if (type.isArray()) {
            // 필드가 Array일 경우
            sf = getSupplierArray(vo, field, array, position, len);
        } else {
            sf = getSupplier(vo, field, array, position);
            len.add(VoUtils.length(sf.get()));
        }

        return new MessageBuildHelper<>(len.intValue(), sf.get());
    }

    private <E> Supplier<List<E>> getSupplierList(@NonNull Object vo, @NonNull Field field, @NonNull byte[] array, int position, LongAdder len) {
        Class<?> type = field.getType();
        try {
            int repeatCnt = getRepeatCnt(vo, field);

            List<E> result;
            if (field.get(vo) != null) {
                result = (List<E>) field.get(vo);
            } else {
                if (type.isInterface() || type.isAnonymousClass()) {
                    result = new ArrayList<>();
                } else {
                    result = (List<E>) ReflectionUtils.newInstance(type);
                }
            }

            for (int i = 0; i < repeatCnt; i++) {
                Class<E> newVo = ReflectionUtils.getGenericClass(field.getGenericType());
                E e = makeVo(newVo, array, position);
                result.add(e);

                int voLength = VoUtils.length(e);
                len.add(voLength);
                position += voLength;
            }

            return () -> result;
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException |
                 InvocationTargetException | InstantiationException e) {
            throw new MessageException(e);
        }
    }

    @SuppressWarnings("cast")
    private <E> Supplier<E[]> getSupplierArray(@NonNull Object vo, @NonNull Field field, @NonNull byte[] array, int position, LongAdder len) {
        try {
            int repeatCnt = getRepeatCnt(vo, field);

            Class<E> eClass = (Class<E>) field.getType().getComponentType();
            E[] e = (E[]) Array.newInstance(eClass, repeatCnt);

            for (int i = 0; i < repeatCnt; i++) {
                e[i] = makeVo(eClass, array, position);
                int voLength = VoUtils.length(e[i]);
                len.add(voLength);
                position += voLength;
            }

            return () -> e;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MessageException(e);
        }
    }

    private int getRepeatCnt(Object vo, Field field) throws NoSuchFieldException, IllegalAccessException {
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
        return repeatCnt;
    }

    private <T> T makeVo(@NonNull T subVo, @NonNull byte[] array, int p) {
        byte[] decoration = new byte[array.length - p];
        System.arraycopy(array, p, decoration, 0, decoration.length);
        fromBytes(decoration, subVo);
        return subVo;
    }

    private <T> T makeVo(@NonNull Class<T> subVo, @NonNull byte[] array, int p) {
        byte[] decoration = new byte[array.length - p];
        System.arraycopy(array, p, decoration, 0, decoration.length);
        return fromBytes(decoration, subVo);
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

    private <E, T> Supplier<T> getSupplier(@NonNull E vo, @NonNull Field field, @NonNull byte[] array, int p) {
        Class<?> type = field.getType();
        return () -> {
            try {
                T subVo = (T) field.get(vo);
                if (subVo == null) {
                    subVo = (T) type.getConstructor().newInstance();
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
        StringBuilder builder = new StringBuilder(value);
        if (align == Align.RIGHT)
            builder.insert(value.length() - p, ".");
        else
            builder.insert(p, ".");
        return builder.toString();
    }

    @Getter
    @AllArgsConstructor
    @RequiredArgsConstructor
    private static class MessageBuildHelper<E> {
        private int length;
        private E element;
    }
}
