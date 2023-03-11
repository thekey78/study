package pe.kr.thekey78.messenger.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import pe.kr.thekey78.messenger.MessageException;
import pe.kr.thekey78.messenger.annotation.*;
import pe.kr.thekey78.messenger.utils.ClassUtils;
import pe.kr.thekey78.messenger.utils.VoUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

@EqualsAndHashCode(callSuper = false)
@ToString
public class AbstractVo implements Externalizable {
    {
        Class<?> clazz = getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);
            if (defaultValue != null) {
                if (!field.canAccess(this))
                    field.setAccessible(true);

                Class<?> type = field.getType();
                String value = defaultValue.value();
                try {
                    if (type == String.class) {
                        field.set(this, value);
                    } else if (type == byte.class || type == Byte.class) {
                        field.set(this, NumberUtils.toByte(value));
                    } else if (type == short.class || type == Short.class) {
                        field.set(this, NumberUtils.toShort(value));
                    } else if (type == int.class || type == Integer.class) {
                        field.set(this, NumberUtils.toInt(value));
                    } else if (type == long.class || type == Long.class) {
                        field.set(this, NumberUtils.toLong(value));
                    } else if (type == float.class || type == Float.class) {
                        field.set(this, NumberUtils.toFloat(value));
                    } else if (type == double.class || type == Double.class) {
                        field.set(this, NumberUtils.toDouble(value));
                    } else if (type == char.class || type == Character.class) {
                        if (value.length() > 0)
                            field.set(this, value.charAt(0));
                    } else if (type == boolean.class) {
                        field.setBoolean(this, Boolean.parseBoolean(value));
                    } else if (type == Boolean.class) {
                        field.set(this, Boolean.valueOf(value));
                    } else if (type == java.math.BigInteger.class) {
                        field.set(this, NumberUtils.createBigInteger(value));
                    } else if (type == java.math.BigDecimal.class) {
                        field.set(this, NumberUtils.createBigDecimal(value));
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new MessageException(e);
                }
            }
        }
    }

    /**
     *
     */
    private static final long serialVersionUID = -3726761958425200856L;

    @SuppressWarnings("cast")
    public byte[] getBytes() {
        Class<? extends AbstractVo> clazz = getClass();
        Field[] fields = clazz.getDeclaredFields();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (Field field:fields) {
            boolean accessible = field.canAccess(this);
            field.setAccessible(true);

            Except except = field.getAnnotation(Except.class);
            if(except != null && except.value()) continue;

            try {
                Object obj = field.get(this);
                if (obj instanceof AbstractVo) {
                    // 필드가 VO인 경우
                    baos.write(((AbstractVo) obj).getBytes());
                } else if (obj instanceof List) {
                    // 필드가 List 일 경우
                    Reference ref = field.getAnnotation(Reference.class);
                    // 참조 필드가 같은 vo 안에 있는 경우
                    Field refField = clazz.getField(ref.value());

                    List<? extends AbstractVo> list = List.class.cast(obj) ;
                    refField.set(this, list.size());

                    baos.write(getBytes(list));
                } else {
                    DefaultValue a_defaultValue = field.getAnnotation(DefaultValue.class);

                    String defaultValue = "";
                    if (a_defaultValue != null)
                        defaultValue = a_defaultValue.value();

                    String str = StringUtils.defaultIfEmpty(Objects.toString(obj, defaultValue), defaultValue);

                    Extension extension = field.getAnnotation(Extension.class);
                    str = VoUtils.doExtension(str, extension);

                    Length a_length = field.getAnnotation(Length.class);
                    String charEncoding = VoUtils.getCharEncoding(field.getAnnotation(CharEncoding.class));

                    byte[] bytes = VoUtils.toMessageBytes(str, a_length, charEncoding);

                    baos.write(bytes);
                } // end if
            } catch (IllegalAccessException | IOException | NoSuchFieldException e) {
                throw new MessageException(e);
            } finally {
                field.setAccessible(accessible);
            }
        } // end for
        return baos.toByteArray();
    }

    protected byte[] getBytes(List<? extends AbstractVo> collection) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (AbstractVo nextObj : collection) {
            if (nextObj != null) {
                byteArrayOutputStream.write(nextObj.getBytes());
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public String toJSON() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public static <E extends AbstractVo> E fromJSON(String str, Class<E> clazz) throws IOException {
        try {
            E e = clazz.getConstructor().newInstance();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(str.getBytes()));
            e.readExternal(ois);
            ois.close();
            return e;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            throw new MessageException(exception);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(toJSON().getBytes());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        final int BUFFER_SIZE = 4096;

        ByteArrayOutputStream output = new ByteArrayOutputStream(BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];

        for (int n = -1; -1 != (n = in.read(buffer));) {
            output.write(buffer, 0, n);
        }

        byte[] data = output.toByteArray();

        ObjectMapper objectMapper = new ObjectMapper();
        AbstractVo vo = objectMapper.readValue(data, getClass());
        BeanUtils.copyProperties(vo, this);
    }

    @SuppressWarnings("cast")
    public int length() {
        LongAdder adder = new LongAdder();
        Class<?> clazz = getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields) {
            boolean accessible = field.canAccess(this);
            field.setAccessible(true);

            Except except = field.getAnnotation(Except.class);
            if(except != null && except.value())
                continue;

            if(ClassUtils.isPrimitive(field.getType())) {
                Length length = field.getAnnotation(Length.class);
                if(length != null) {
                    adder.add(length.value());
                }
            }
            else if (field.getType() == String.class) {
                Length length = field.getAnnotation(Length.class);
                if(length != null) {
                    adder.add(length.value());
                }
            }
            else if (field.getType().isAssignableFrom(AbstractVo.class)) {
                try {
                    AbstractVo other = AbstractVo.class.cast(field.get(this));
                    adder.add(other.length());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (field.getType().isAssignableFrom(List.class)) {
                try {
                    List<AbstractVo> others = List.class.cast(field.get(this)) ;
                    for (AbstractVo other : others) {
                        adder.add(other.length());
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            field.setAccessible(accessible);
        }

        return adder.intValue();
    }
}
