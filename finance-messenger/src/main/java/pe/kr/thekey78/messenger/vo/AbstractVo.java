package pe.kr.thekey78.messenger.vo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.lang.reflect.Field;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pe.kr.thekey78.messenger.annotation.DefaultValue;

@EqualsAndHashCode(callSuper = false)
@ToString
@Slf4j
public class AbstractVo implements Externalizable {
    {
        Class<?> clazz = getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);
            if (defaultValue != null) {
                if (!field.isAccessible())
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
                    } else if (type == java.math.BigInteger.class) {
                        field.set(this, NumberUtils.createBigInteger(value));
                    } else if (type == long.class || type == Long.class) {
                        field.set(this, NumberUtils.toLong(value));
                    } else if (type == float.class || type == Float.class) {
                        field.set(this, NumberUtils.toFloat(value));
                    } else if (type == double.class || type == Double.class) {
                        field.set(this, NumberUtils.toDouble(value));
                    } else if (type == java.math.BigDecimal.class) {
                        field.set(this, NumberUtils.createBigDecimal(value));
                    } else if (type == char.class || type == Character.class) {
                        if (value.length() > 0)
                            field.set(this, value.charAt(0));
                    } else if (type == boolean.class) {
                        field.setBoolean(this, Boolean.parseBoolean(value));
                    } else if (type == Boolean.class) {
                        field.set(this, Boolean.valueOf(value));
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     *
     */
    private static final long serialVersionUID = -3726761958425200856L;

    public String toJSON() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public static <E extends AbstractVo> E fromJSON(String str, Class<E> clazz) throws IOException, ClassNotFoundException {
        try {
            E e = clazz.newInstance();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(str.getBytes()));
            e.readExternal(ois);
            ois.close();
            return e;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error(e.getLocalizedMessage(), e);
            return null;
        }

    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(toJSON().getBytes());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
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
}
