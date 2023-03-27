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

	@Condition(el = "${ref[0] eq '0' and ref[1] eq '00000'}", ref = "responseHeader.rsltDvcd,responseHeader.resCd")
	private T body;

	private MessageCommonFooter footer;

}
