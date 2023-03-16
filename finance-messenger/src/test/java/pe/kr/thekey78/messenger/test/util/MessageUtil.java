package pe.kr.thekey78.messenger.test.util;

import pe.kr.thekey78.messenger.MessageBuilder;
import pe.kr.thekey78.messenger.annotation.IOType;
import pe.kr.thekey78.messenger.annotation.MessageId;
import pe.kr.thekey78.messenger.enumeration.IoType;
import pe.kr.thekey78.messenger.test.vo.template.InputMessage;
import pe.kr.thekey78.messenger.test.vo.template.OutputMessage;
import pe.kr.thekey78.messenger.utils.VoUtils;

import java.lang.reflect.Field;

public class MessageUtil {

    @SuppressWarnings("cast")
    public static <T, E> InputMessage<T> makeSendData(E body) {
        InputMessage<T> inputTemplate = new InputMessage<>();

        MessageId messageId = body.getClass().getAnnotation(MessageId.class);
        if(messageId != null) {
            try {
                inputTemplate.getInputHeader().setSystemGb("SB01");
                inputTemplate.getInputHeader().setIoGb("I");
                inputTemplate.getInputHeader().setMessageId(messageId.value());
                inputTemplate.setBody((T) findField(body, IoType.INPUT));
                inputTemplate.getInputHeader().setLength(VoUtils.length(inputTemplate));
                return inputTemplate;
            }
            catch (SecurityException | IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("Required MessageId");
    }

    public static <T> OutputMessage<T> makeReceiveData(byte[] bytes, T t) {
        OutputMessage<T> outputMessage = new OutputMessage<>();
        outputMessage.setBody(t);

        MessageBuilder.getInstance().fromBytes(bytes, outputMessage);
        return  outputMessage;
    }

    private static Object findField(Object o, IoType ioType) throws NoSuchFieldException, IllegalAccessException {
        Field field = null;
        boolean accessible = false;
        try {
            field = findField(o.getClass(), ioType);
            accessible = field.canAccess(o);
            field.setAccessible(true);

            return field.get(o);
        } finally {
            if(field != null)
                field.setAccessible(accessible);
        }
    }

    private static Field findField(Class<?> c, IoType ioType) throws NoSuchFieldException {
        for(Field field : c.getDeclaredFields()) {
            IOType type = field.getAnnotation(IOType.class);
            if(type != null && type.value() == ioType) {
                return field;
            }
        }
        throw new NoSuchFieldException(ioType.toString());
    }

    public static <T> OutputMessage send(T o) {
        return send(o, Target.MCI);
    }
    public static <T> OutputMessage send(T o, Target t) {
        InputMessage<?> inputMessage = makeSendData(o);
        byte[] input = MessageBuilder.getInstance().getBytes(inputMessage);

        // TODO send input
        switch (t) {
            case EAI:
                // TODO do something
                break;
            case FEP:
                // TODO do something
                break;
            case MCI:
            default:
                // TODO do something
        }
        byte[] output = "00000000MSG000000001    SB01O0001ABCDEFGHIJKLM987654321098@@".getBytes();

        try {
            Object outputBody = findField(o, IoType.OUTPUT);
            return MessageUtil.makeReceiveData(output, outputBody);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
