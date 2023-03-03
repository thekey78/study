package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pe.kr.thekey78.messenger.annotation.DefaultValue;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.enumeration.Ascii;
import pe.kr.thekey78.messenger.vo.AbstractVo;

@Data
@EqualsAndHashCode(callSuper=false)
public class MessageOutputHeader extends AbstractVo {
	@Length(value = 8, align = Align.LEFT, pad = Ascii.ZERO)
	private int length;
	@Length(16)
	private String messageId;
	@Length(4)
	private String systemGb;
	@Length(1)
	@DefaultValue("O")
	private String ioGb;
	@Length(2)
	private String resultCode;
}
