package pe.kr.thekey78.messenger.test.vo.template;

import lombok.Data;
import pe.kr.thekey78.messenger.test.vo.common.MessageInputFooter;
import pe.kr.thekey78.messenger.test.vo.common.MessageInputHeader;

@Data
public class InputMessage<T> {
	private MessageInputHeader inputHeader = new MessageInputHeader();

	private T body;

	private MessageInputFooter inputFooter = new MessageInputFooter();

}
