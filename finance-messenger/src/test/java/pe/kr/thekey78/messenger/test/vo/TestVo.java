package pe.kr.thekey78.messenger.test.vo;

import org.junit.Test;
import pe.kr.thekey78.messenger.MessageBuilder;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.test.vo.body.MSG000000001;
import pe.kr.thekey78.messenger.test.vo.template.InputMessage;
import pe.kr.thekey78.messenger.test.vo.template.OutputMessage;
import pe.kr.thekey78.messenger.utils.ReflectionUtils;
import pe.kr.thekey78.messenger.utils.VoUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestVo {

    @Test
    public void testInputVo() throws NoSuchFieldException {
        MSG000000001 msg000000001 = new MSG000000001();
        msg000000001.getInput().setUserId("test001");


        InputMessage<MSG000000001.Input> inputMessage = MessageUtil.makeSendData(msg000000001);
        byte[] input = MessageBuilder.getInstance().getBytes(inputMessage);
        System.out.printf("%s:%s", "testInputVo", new String(input));
    }

    @Test
    public void testOutputVo() throws NoSuchFieldException {
        byte[] outputBytes = "00000000MSG000000001    SB01O0001ABCDEFGHIJKLM987654321098@@".getBytes();

        OutputMessage<MSG000000001.Output> outputMessage = MessageUtil.makeReceiveData(outputBytes, MSG000000001.Output.class);

        System.out.printf("%s:%s", "testOutputVo", outputMessage);
    }

    @Test
    public void testClass() throws Exception {
        assertEquals(String.class, "123".getClass());

        Object obj1 = Integer.parseInt("123");
        assertEquals(Integer.class, obj1.getClass());

        Object obj2 = Integer.parseInt("123");
        assertEquals(true, List.class.isAssignableFrom(List.class));
    }

    @Test
    public void testDecimalPoint() {
        String value = "002233";
        int p = 2;

        Align align = Align.LEFT;
        if(align == Align.RIGHT)
            assertEquals("0022.33", value.substring(0, value.length() - p) + "." + value.substring(value.length() - p));
        else
            assertEquals("00.2233", value.substring(0, p) + "." + value.substring(p));
    }

    @Test
    public void testType() throws NoSuchFieldException, ClassNotFoundException {
        MSG000000001 msg000000001 = new MSG000000001();
        Class<? extends MSG000000001.Output> clazz = msg000000001.getOutput().getClass();
        Field field = clazz.getDeclaredField("rec");
        System.out.printf("%s", ReflectionUtils.getGenericClass(field.getGenericType()));
    }

    @Test
    public void testMessageBytes() throws Exception {
        String src = "ABCDEFG";
        Length length = getLengthAnnotation();
        assertEquals("ABCDEF", new String(VoUtils.toMessageBytes(src, length)));
    }

    @Test
    public void testUnPad() throws Exception {
        String src = "000000";
        Length length = getLengthAnnotation();
        assertEquals("", new String(VoUtils.unPad(src.getBytes(), length)));
    }

    private Length getLengthAnnotation() {
        return new Length() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Length.class;
            }

            @Override
            public int value() {
                return 6;
            }

            @Override
            public Align align() {
                return Align.RIGHT;
            }

            @Override
            public byte pad() {
                return '0';
            }

            @Override
            public String ref() {
                return "";
            }
        };
    }
}
