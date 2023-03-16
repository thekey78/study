package pe.kr.thekey78.messenger.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.apache.commons.lang3.math.NumberUtils;
import pe.kr.thekey78.messenger.MessageBuilder;
import pe.kr.thekey78.messenger.MessageException;
import pe.kr.thekey78.messenger.MessageExtension;
import pe.kr.thekey78.messenger.annotation.*;
import pe.kr.thekey78.messenger.enumeration.Align;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

public class VoUtils {

    public static <T> T create(@NonNull Class<T> c) {
        try {
            T t = c.getConstructor().newInstance();
            for(Field field  : c.getDeclaredFields()) {
                DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);
                if (defaultValue != null) {
                    if (!field.canAccess(t))
                        field.setAccessible(true);

                    Class<?> type = field.getType();
                    String value = defaultValue.value();
                    if (type == String.class) {
                        field.set(t, value);
                    } else if (type == byte.class || type == Byte.class) {
                        field.set(t, NumberUtils.toByte(value));
                    } else if (type == short.class || type == Short.class) {
                        field.set(t, NumberUtils.toShort(value));
                    } else if (type == int.class || type == Integer.class) {
                        field.set(t, NumberUtils.toInt(value));
                    } else if (type == long.class || type == Long.class) {
                        field.set(t, NumberUtils.toLong(value));
                    } else if (type == float.class || type == Float.class) {
                        field.set(t, NumberUtils.toFloat(value));
                    } else if (type == double.class || type == Double.class) {
                        field.set(t, NumberUtils.toDouble(value));
                    } else if (type == char.class || type == Character.class) {
                        if (value.length() > 0)
                            field.set(t, value.charAt(0));
                    } else if (type == boolean.class) {
                        field.setBoolean(t, Boolean.parseBoolean(value));
                    } else if (type == Boolean.class) {
                        field.set(t, Boolean.valueOf(value));
                    } else if (type == java.math.BigInteger.class) {
                        field.set(t, NumberUtils.createBigInteger(value));
                    } else if (type == java.math.BigDecimal.class) {
                        field.set(t, NumberUtils.createBigDecimal(value));
                    }
                }
            }

            return t;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new MessageException(e);
        }
    }

    public static String toJson(@NonNull Object o) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(o);
    }

    public static <T> T fromJson(@NonNull String str, @NonNull Class<T> c) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(str, c);
    }

    /**
     * 문자열을 전문 전송 규칙에 맞게 변환한다.
     * @param src
     * @param a_length
     * @return
     * @throws MessageException
     */
    public static byte[] toMessageBytes(@NonNull String src, @NonNull Length a_length) throws MessageException {
        return toMessageBytes(src, a_length, null);
    }

    /**
     * 문자열을 전문 전송 규칙에 맞게 변환한다.
     * @param src
     * @param a_length
     * @param charEncoding 변환할 인코딩
     * @return
     * @throws MessageException 지원되지 않는 문자열 인코딩인 경우
     */
    public static byte[] toMessageBytes(@NonNull String src, @NonNull Length a_length, String charEncoding) throws MessageException {
        if (a_length != null && a_length.value() > 0) {
            byte[] deco;
            if(charEncoding == null)
                deco = src.getBytes();
            else {
                try {
                    deco = src.getBytes(charEncoding);
                } catch (UnsupportedEncodingException exception) {
                    throw new MessageException(exception);
                }
            }

            byte[] result = pad(a_length, deco);
            return result;
        }
        else if(charEncoding == null) {
            return src.getBytes();
        }
        else {
            try {
                return src.getBytes(charEncoding);
            } catch (UnsupportedEncodingException exception) {
                throw new MessageException(exception);
            }
        }
    }

    /**
     * Length에 정의된 패딩 규칙에 맞게 변환한다.
     * @param aLength
     * @param bytes
     * @return 패딩된 바이트 배열
     */
    public static byte[] pad(@NonNull Length aLength, @NonNull byte[] bytes) {
        int length = aLength.value();
        byte[] result = new byte[length];
        Arrays.fill(result, aLength.pad());

        if(aLength.align() == Align.LEFT) {
            System.arraycopy(bytes, 0, result, 0, Math.min(length, bytes.length));
        }
        else {
            System.arraycopy(bytes, 0, result, Math.max(result.length - bytes.length, 0), Math.min(length, bytes.length));
        }

        return result;
    }

    /**
     * Length에 정의된 패딩 규칙을 제거 한다.
     * @param aLength
     * @param bytes
     * @return 패딩규칙이 제거된 byte 배열
     */
    public static byte[] unPad(@NonNull Length aLength, @NonNull byte[] bytes) {
        if (aLength.align() == Align.RIGHT) {
            int i = 0;
            for (; i < bytes.length; i++) {
                if(bytes[i] != aLength.pad())
                    break;
            }

            return Arrays.copyOfRange(bytes, i, bytes.length);
        }
        else {
            int i = bytes.length-1;
            for (; i > 0; i--) {
                if(bytes[i] != aLength.pad())
                    break;
            }

            return Arrays.copyOfRange(bytes, 0, i+1);
        }
    }

    /**
     *
     * @param charEncoding
     * @return
     */
    public static String getCharEncoding(CharEncoding charEncoding) {
        String encoding = Charset.defaultCharset().name();
        if(charEncoding != null)
            encoding = charEncoding.value();

        return encoding;
    }

    /**
     * 문자열을 입력 받고 Extension으로 변환된 문자열을 반환한다.
     *
     * @param extension
     * @param str       원본 문자열
     * @return Extension으로 변환된 문자열
     */
    public static String doExtension(Extension extension, @NonNull String str) {
        String result = str;
        if(extension != null) {
            MessageBuilder messageBuilder = MessageBuilder.getInstance();
            //확장필드 처리
            for (String key : extension.value()) {
                MessageExtension messageExtension = messageBuilder.getMessageExtension(key);
                result = messageExtension.doExtension(str);
            }
        }
        return result;
    }


    /**
     * VO 객체의 Lentth Annotation을 이용하여 객체의 길이를 구한다.
     * @param vo
     * @return
     * @param <T>
     */
    public static <T> int length(@NonNull T vo) {
        LongAdder adder = new LongAdder();
        Class<?> clazz = vo.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields) {
            boolean accessible = field.canAccess(vo);
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
            else if (field.getType().isAssignableFrom(List.class)) {
                try {
                    List<Object> others = List.class.cast(field.get(vo)) ;
                    for (Object other : others) {
                        adder.add(length(other));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (ClassUtils.isPrimitive(field.getType()) || field.getType() == String.class){
                Length length = field.getAnnotation(Length.class);
                if(length != null) {
                    adder.add(length.value());
                }
            }
            else {
                try {
                    Object other = field.get(vo);
                    adder.add(length(other));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            field.setAccessible(accessible);
        }

        return adder.intValue();
    }
}
