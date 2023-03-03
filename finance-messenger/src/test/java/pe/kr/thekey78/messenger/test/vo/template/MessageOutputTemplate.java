package pe.kr.thekey78.messenger.test.vo.template;

import pe.kr.thekey78.messenger.annotation.Condition;
import pe.kr.thekey78.messenger.test.vo.common.MessageErrorHeader;
import pe.kr.thekey78.messenger.test.vo.common.MessageOutputFooter;
import pe.kr.thekey78.messenger.test.vo.common.MessageOutputHeader;
import pe.kr.thekey78.messenger.vo.AbstractVo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class MessageOutputTemplate<T extends AbstractVo> extends AbstractVo {
	private MessageOutputHeader messageOutputHeader;

	@Condition(test = false)
	private MessageErrorHeader messageErrorHeader;

	@Condition(test = true)
	private T body;

	private MessageOutputFooter messageOutputFooter;

}
