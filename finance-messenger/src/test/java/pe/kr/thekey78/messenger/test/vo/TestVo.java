package pe.kr.thekey78.messenger.test.vo;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import pe.kr.thekey78.messenger.MessageBuilder;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.test.util.*;
import pe.kr.thekey78.messenger.test.vo.body.MSG000000001;
import pe.kr.thekey78.messenger.test.vo.common.MessageCommonFooter;
import pe.kr.thekey78.messenger.test.vo.template.InputMessage;
import pe.kr.thekey78.messenger.test.vo.template.OutputMessage;
import pe.kr.thekey78.messenger.utils.ClassType;
import pe.kr.thekey78.messenger.utils.ReflectionUtils;
import pe.kr.thekey78.messenger.utils.VoUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.Socket;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class TestVo {

    @Before
    public void before() {
        MessageBuilder builder = MessageBuilder.getInstance();
        builder.setExtension("toHex", new Byte2Hex());
        builder.setExtension("fromHex", new Hex2Byte());
        builder.setExtension("enc", new EncryptPassword());
        builder.setExtension("base64encode", new EncodeBase64Extension());
        builder.setExtension("hex-encode", new EncodeHexExtension());
        //System.setProperty("javax.el.ExpressionFactory","jakarta.el.ExpressionFactory");
    }
    @Test
    public void testInputVo() {
        MSG000000001 msg000000001 = getMsg000000001();


        InputMessage<MSG000000001.Input> inputMessage = MessageUtil.makeSendData(msg000000001);
        byte[] input = MessageBuilder.getInstance().getBytes(inputMessage);
        System.out.println(String.format("testInputVo:" + new String(input)));
    }

    @Test
    public void testOutputVo() {
        byte[] outputBytes = "00000247SMPTRN000000002     Scfd07eca3d724810bd0305832de0068220230206135601011202302061356010110000009999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999990288888888888880164775213427777777777777336477521342@@".getBytes();
//        byte[] outputBytes = "00000247SMPTRN000000002     Scfd07eca3d724810bd0305832de006822023020613560101120230206135601011 99999에러메세지 어쩌구 저쩌구~~~                      에러메세지 어쩌구 저쩌구~~~                      @@".getBytes();
        MSG000000001 msg000000001 = VoUtils.create(MSG000000001.class);
        OutputMessage<MSG000000001.Output> outputMessage = MessageUtil.makeReceiveData(outputBytes, msg000000001.getOutput());

        System.out.println(String.format("testOutputVo:" + outputMessage));
    }

    @Test
    public void testMci() throws Exception {
        MSG000000001 msg000000001 = getMsg000000001();

        OutputMessage<MSG000000001.Output> outputMessage = MessageUtil.send(msg000000001);

        System.out.println(String.format("%s:%s,%s", "testMci", "MSG000000001", msg000000001));
        System.out.println(String.format("%s:%s,%s", "testMci", "OutputMessage", outputMessage));
    }

    private static MSG000000001 getMsg000000001() {
        MSG000000001 msg000000001 = new MSG000000001();
        MSG000000001.Input input = msg000000001.getInput();
        input.setUserId("test001");
        input.setPassword("ABCD");
        return msg000000001;
    }

    @Test
    public void testClass() throws Exception {
        assertEquals(String.class, "123".getClass());

        Object obj1 = Integer.parseInt("123");
        assertEquals(Integer.class, obj1.getClass());

        Object obj2 = Integer.parseInt("123");
        assertTrue(List.class.isAssignableFrom(List.class));
    }

    @Test
    public void testDecimalPoint() {
        String value = "002233";
        int p = 2;

        Align align = Align.RIGHT;
        StringBuilder sb = new StringBuilder(value);

        if(align == Align.RIGHT)
            assertEquals("0022.33", sb.insert(value.length() - p, ".").toString());
        else
            assertEquals("00.2233", sb.insert(p, ".").toString());
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
        assertEquals("", new String(VoUtils.unPad(length, src.getBytes())));
    }

    @Test
    public void testClassName() {
        System.out.println(ClassType.getClassType(Socket.class));
    }

    @Test
    public void testByteToBigInteger() {
        byte[] bytes = "000123".getBytes();
        assertNotEquals(new BigInteger("123"), new BigInteger(bytes));
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

    @Test
    public void testVo111() {
        System.out.println(VoUtils.create(MessageCommonFooter.class));
        System.out.println(new MessageCommonFooter());
    }
}
