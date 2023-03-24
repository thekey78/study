package pe.kr.thekey78.messenger.test.util;

import lombok.extern.slf4j.Slf4j;
import pe.kr.thekey78.messenger.MessageBuilder;
import pe.kr.thekey78.messenger.annotation.IOType;
import pe.kr.thekey78.messenger.annotation.MessageId;
import pe.kr.thekey78.messenger.enumeration.IoType;
import pe.kr.thekey78.messenger.test.vo.template.InputMessage;
import pe.kr.thekey78.messenger.test.vo.template.OutputMessage;
import pe.kr.thekey78.messenger.utils.UUIDGenerator;
import pe.kr.thekey78.messenger.utils.VoUtils;
import pe.kr.thekey78.transrate.test.Translate;

import java.lang.reflect.Field;

@Slf4j
public class MessageUtil {

    @SuppressWarnings("cast")
    public static <T, E> InputMessage<E> makeSendData(T body) {
        InputMessage<E> inputTemplate = new InputMessage<>();

        MessageId messageId = body.getClass().getAnnotation(MessageId.class);
        if(messageId != null) {
            try {
                inputTemplate.getHeader().setUuid(UUIDGenerator.getInstance().getNextUUID());
                inputTemplate.getHeader().setMessageDirection("I");
                inputTemplate.getHeader().setMessageId(messageId.value());
                inputTemplate.setBody((E) findObject(body, IoType.INPUT));
                inputTemplate.getHeader().setMessageLength(VoUtils.length(inputTemplate) - 8);
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

    private static Object findObject(Object o, IoType ioType) throws NoSuchFieldException, IllegalAccessException {
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

    public static <T, E> OutputMessage<E> send(T o) {
        return send(o, Target.MCI);
    }

    @SuppressWarnings("cast")
    public static <T, E> OutputMessage<E> send(T o, Target t) {
        InputMessage<?> inputMessage = makeSendData(o);
        byte[] input = MessageBuilder.getInstance().getBytes(inputMessage);
        byte[] output = null;

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
                log.info(String.format("발신전문[%s]", new String(input)));
                output = Translate.getInstance().execute(input);
                log.info(String.format("수신전문[%s]", new String(output)));
                break;
        }
        //output = "00000247SMPTRN000000002     Scfd07eca3d724810bd0305832de006822023020613560101120230206135601011 000009999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999990288888888888880164775213427777777777777336477521342@@".getBytes();
        //          00000247SMPTRN000000002     Scfd07eca3d724810bd0305832de006822023020613560101120230206135601011 000009999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999990288888888888880164775213427777777777777336477521342@@

        try {
            E outputBody = (E) findObject(o, IoType.OUTPUT);
            return makeReceiveData(output, outputBody);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
