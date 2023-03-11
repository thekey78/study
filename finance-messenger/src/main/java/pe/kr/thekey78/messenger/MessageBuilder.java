package pe.kr.thekey78.messenger;

import lombok.extern.slf4j.Slf4j;
import pe.kr.thekey78.messenger.annotation.*;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.function.MessageBuildHelper;
import pe.kr.thekey78.messenger.utils.ClassUtils;
import pe.kr.thekey78.messenger.utils.NumberUtils;
import pe.kr.thekey78.messenger.utils.ReflectionUtils;
import pe.kr.thekey78.messenger.utils.VoUtils;
import pe.kr.thekey78.messenger.vo.AbstractVo;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

@Slf4j
public class MessageBuilder {
    private static class Singletone {
        private static final MessageBuilder INSTANCE = new MessageBuilder();
    }

    private MessageBuilder() {}

    public static MessageBuilder getInstance() {
        return Singletone.INSTANCE;
    }

    public byte[] getBytes(AbstractVo vo) throws IllegalArgumentException {
        return vo.getBytes();
    }

    public <T extends AbstractVo> T fromBytes(final byte[] array, final Class<T> t) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T outputBody = t.getConstructor().newInstance();
        fromBytes(array, outputBody);
        return outputBody;
    }

    public <T extends AbstractVo> void fromBytes(final byte[] array, final T vo) {
        fromBytes(array, 0, vo);
    }

    public <T extends AbstractVo> void fromBytes(final byte[] array, int position, final T vo) {
        Class<? extends AbstractVo> clazz = vo.getClass();
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

    private static <T extends AbstractVo> Object findRefValue(T vo, String refValue) throws NoSuchFieldException, IllegalAccessException {
        String[] strRefs = refValue.split("[.]");
        Object refObj = vo;
        for (String strRef : strRefs) {
            Field refField = refObj.getClass().getDeclaredField(strRef);
            refField.setAccessible(true);
            refObj = refField.get(refObj);
        }
        return refObj;
    }

    private MessageBuildHelper<?> makeFunction(final AbstractVo vo, final Field field, final byte[] array, final int position) throws UnsupportedEncodingException {
        MessageBuildHelper<?> f = null;
        Supplier<?> sf = null;

        final LongAdder len = new LongAdder();

        Class<?> type = field.getType();
        if(type.isPrimitive() || type == BigInteger.class || type == BigDecimal.class) {
            Length a_length = field.getAnnotation(Length.class);
            DecimalPosition decimalPosition = field.getAnnotation(DecimalPosition.class);

            byte[] valueArr = new byte[a_length.value()];
            System.arraycopy(array, position, valueArr, 0, a_length.value());
            sf = getSupplier(decimalPosition, valueArr, type);
            len.add(a_length.value());
        }
        else if(AbstractVo.class.isAssignableFrom(type)) {
            sf = getSupplier(vo, field, array, type, position);
            len.add(((AbstractVo)sf.get()).length());
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

                    List<AbstractVo> result = null;
                    if(type.isInterface()) {
                        result = new ArrayList<>();
                    }
                    else if(type.isAnonymousClass()) {
                        result = new ArrayList<>();
                    }
                    else {
                        result = (List<AbstractVo>) ReflectionUtils.newInstance(type);
                    }

                    for(int i = 0; i < repeatCnt; i++) {
                        AbstractVo newVo = ReflectionUtils.newInstance(ReflectionUtils.getGenericClass(field.getGenericType()));
                        makeVo(newVo, array, position);
                        result.add(newVo);
                        len.add(newVo.length());
                    }

                    return result;
                } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException |
                         InvocationTargetException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            };
        }
        else {
            final String encoding = VoUtils.getCharEncoding(field.getAnnotation(CharEncoding.class));

            Length a_length = field.getAnnotation(Length.class);
            final byte[] valueArr = new byte[a_length.value()];
            System.arraycopy(array, position, valueArr, 0, a_length.value());

            Extension extension = field.getAnnotation(Extension.class);
            sf = () -> {
                try {
                    return VoUtils.doExtension(new String(valueArr, encoding).trim(), extension);
                } catch (UnsupportedEncodingException e) {
                    return VoUtils.doExtension(new String(valueArr).trim(), extension);
                }
            };
            len.add(a_length.value());
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

    private AbstractVo makeVo(AbstractVo subVo, byte[] array, int p) {
        if (subVo == null)
            throw new NumberFormatException("subVo is null");

        byte[] decoration = new byte[array.length - p];
        System.arraycopy(array, p, decoration, 0, decoration.length);
        fromBytes(decoration, subVo);
        return subVo;
    }

    private Supplier<Object> getSupplier(DecimalPosition decimalPosition, byte[] v, Class<?> type) {
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

    private Supplier<?> getSupplier(AbstractVo vo, Field field, byte[] array, Class<?> type, int p) {
        return () -> {
            try {
                AbstractVo subVo = (AbstractVo) field.get(vo);
                if(subVo == null) {
                    subVo = (AbstractVo) type.getConstructor().newInstance();
                }
                return makeVo(subVo, array, p);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new MessageException(e);
            }
        };
    }

    private String putDecimalPoint(DecimalPosition decimalPosition, byte[] value) {
        return putDecimalPoint(decimalPosition, new String(value));
    }
    private String putDecimalPoint(DecimalPosition decimalPosition, String value) {
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
