package pe.kr.thekey78.messenger.test.vo.template;

import lombok.Data;
import pe.kr.thekey78.messenger.annotation.Condition;
import pe.kr.thekey78.messenger.test.vo.common.MessageErrorHeader;
import pe.kr.thekey78.messenger.test.vo.common.MessageOutputFooter;
import pe.kr.thekey78.messenger.test.vo.common.MessageOutputHeader;
import pe.kr.thekey78.messenger.vo.AbstractVo;

@Data
public class OutputMessage<T extends AbstractVo> extends AbstractVo {
	private MessageOutputHeader messageOutputHeader;

	@Condition(test = "99", ref = "messageOutputHeader.resultCode")
	private MessageErrorHeader messageErrorHeader;

	@Condition(test = "00", ref = "messageOutputHeader.resultCode")
	private T body;

	private MessageOutputFooter messageOutputFooter;

}
