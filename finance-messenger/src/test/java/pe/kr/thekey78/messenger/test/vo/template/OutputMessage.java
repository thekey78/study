package pe.kr.thekey78.messenger.test.vo.template;

import lombok.Data;
import pe.kr.thekey78.messenger.annotation.Condition;
import pe.kr.thekey78.messenger.test.vo.common.MessageCommonFooter;
import pe.kr.thekey78.messenger.test.vo.common.MessageCommonHeader;
import pe.kr.thekey78.messenger.test.vo.common.MessageResponseHeader;

@Data
public class OutputMessage<T> {
	private MessageCommonHeader header;

	private MessageResponseHeader responseHeader;

	@Condition(el = "${ref eq '00000'}", ref = "responseHeader.resCd")
	private T body;

	private MessageCommonFooter footer;

}
