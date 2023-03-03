package pe.kr.thekey78.messenger.test.vo;

import org.junit.Test;
import pe.kr.thekey78.messenger.MessageBuilder;
import pe.kr.thekey78.messenger.test.vo.body.MSG000000001;
import pe.kr.thekey78.messenger.test.vo.template.MessageInputTemplate;
import pe.kr.thekey78.messenger.test.vo.template.MessageOutputTemplate;

public class TestVo {

    @Test
    public void testInputVo() {
        MSG000000001 msg000000001 = new MSG000000001();
        msg000000001.getInput().setUserId("test001");

        MessageInputTemplate inputTemplate = new MessageInputTemplate();
        inputTemplate.getInputHeader().setMessageId("MSG000000001");
        inputTemplate.setBody(msg000000001.getInput());

        byte[] input = MessageBuilder.getInstance().toBytes(inputTemplate);
        System.out.println(new String(input));
    }

    @Test
    public void testOutputVo() {
        MSG000000001 msg000000001 = new MSG000000001();
        msg000000001.getInput().setUserId("test001");

        MessageOutputTemplate<MSG000000001.Output> outputTemplate = new MessageOutputTemplate();
        byte[] input = MessageBuilder.getInstance().toBytes(outputTemplate);
        System.out.println(new String(input));
    }
}
