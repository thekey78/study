package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.vo.AbstractVo;

@Data
@EqualsAndHashCode(callSuper=false)
public class MessageOutputHeader extends AbstractVo {
	@Length(8)
	private int length;
	@Length(16)
	private String messageId;
	@Length(4)
	private String systemGb;
	@Length(1)
	private String ioGb;
}
