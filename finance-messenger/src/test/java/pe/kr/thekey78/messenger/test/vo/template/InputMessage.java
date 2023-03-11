package pe.kr.thekey78.messenger.test.vo.template;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pe.kr.thekey78.messenger.test.vo.common.MessageInputFooter;
import pe.kr.thekey78.messenger.test.vo.common.MessageInputHeader;
import pe.kr.thekey78.messenger.vo.AbstractVo;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class InputMessage<T extends AbstractVo> extends AbstractVo {
	private MessageInputHeader inputHeader = new MessageInputHeader();

	private T body;

	private MessageInputFooter inputFooter = new MessageInputFooter();

}
