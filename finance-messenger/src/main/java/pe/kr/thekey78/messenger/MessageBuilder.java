package pe.kr.thekey78.messenger;

import org.apache.commons.lang3.StringUtils;
import pe.kr.thekey78.messenger.annotation.DefaultValue;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.vo.AbstractVo;

import java.lang.reflect.Field;
import java.util.Collection;

public class MessageBuilder {
    public byte[] buildMessage(AbstractVo vo) {
        Class<?> clazz = vo.getClass();
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder buff = new StringBuilder();
        for (Field field : fields) {
            if (!field.isAccessible())
                field.setAccessible(true);
            Length a_length = field.getAnnotation(Length.class);
            Align alignType = Align.LEFT;
            char padChar = ' ';
            int length = 0;
            if (a_length != null) {
                length = a_length.value();
                alignType = a_length.align();
                padChar = (char) a_length.pad();
            }

            DefaultValue a_defaultValue = field.getAnnotation(DefaultValue.class);
//			Delimiter a_delimiter = field.getAnnotation(Delimiter.class);
//          Encrypt a_encrypt = field.getAnnotation(Encrypt.class);
//			Order a_order = field.getAnnotation(Order.class);

            String defaultValue = "";
            if (a_defaultValue != null)
                defaultValue = a_defaultValue.value();

            try {
                Object obj = field.get(vo);
                if (obj != null && obj instanceof AbstractVo) {
                    // 필드가 VO인 경우
                    buff.append(buildMessage((AbstractVo) obj));
                } else if (obj != null && obj instanceof Collection) {
                    // TODO 필드가 List일 경우
                    buff.append(buildMessage((Collection<?>) obj, length));
                } else {
                    String str = "";
                    if (obj == null)
                        str = defaultValue;
                    else
                        str = StringUtils.isEmpty(obj.toString()) ? defaultValue : obj.toString();

                    if (a_length != null && length > 0) {
                        if (str.length() < length) {
                            if (alignType == Align.LEFT) {
                                str = StringUtils.rightPad(str, length, padChar);
                            }
                            if (alignType == Align.RIGHT) {
                                str = StringUtils.leftPad(str, length, padChar);
                            }
                        }
                    }

                    buff.append(str);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return buff.toString().getBytes();
    }

    protected byte[] buildMessage(Collection<?> collection, int length) {
        StringBuilder buff = new StringBuilder();
        buff.append(StringUtils.rightPad(String.valueOf(collection.size()), length, '0'));
        for (Object nextObj : collection) {
            if (nextObj instanceof AbstractVo) {
                buff.append(buildMessage((AbstractVo) nextObj));
            }
            if (nextObj instanceof Collection) {
                buff.append(buildMessage(collection, length));
            }
        }
        return buff.toString().getBytes();
    }
}
