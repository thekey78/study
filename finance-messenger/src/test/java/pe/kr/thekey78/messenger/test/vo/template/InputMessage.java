package pe.kr.thekey78.messenger.test.vo.template;

import lombok.Data;
import pe.kr.thekey78.messenger.test.vo.common.MessageCommonFooter;
import pe.kr.thekey78.messenger.test.vo.common.MessageCommonHeader;

@Data
public class InputMessage<T> {
	private MessageCommonHeader header = new MessageCommonHeader();

	private T body;

	private MessageCommonFooter footer = new MessageCommonFooter();

}
