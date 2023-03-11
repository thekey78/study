package pe.kr.thekey78.messenger.test.vo;

import pe.kr.thekey78.messenger.MessageBuilder;
import pe.kr.thekey78.messenger.annotation.MessageId;
import pe.kr.thekey78.messenger.test.vo.template.InputMessage;
import pe.kr.thekey78.messenger.test.vo.template.OutputMessage;
import pe.kr.thekey78.messenger.vo.AbstractVo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MessageUtil {
    public static <T extends AbstractVo> InputMessage<T> makeSendData(Object body) {
        InputMessage<T> inputTemplate = new InputMessage<>();

        MessageId messageId = body.getClass().getAnnotation(MessageId.class);
        if(messageId != null) {
            try {
                inputTemplate.getInputHeader().setMessageId(messageId.value());
                Method inputMethod =  body.getClass().getMethod("getInput");
                //noinspection unchecked
                inputTemplate.setBody((T) inputMethod.invoke(body));

                return inputTemplate;
            }
            catch (SecurityException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("Required MessageId");
    }

    public static <T extends AbstractVo> OutputMessage<T> makeReceiveData(byte[] bytes, Class<T> t) {
        try {
            OutputMessage<T> outputMessage = new OutputMessage<>();
            T outputBody = t.getConstructor().newInstance();
            outputMessage.setBody(outputBody);

            MessageBuilder.getInstance().fromBytes(bytes, outputMessage);
            return  outputMessage;
        }
        catch (SecurityException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
               InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
