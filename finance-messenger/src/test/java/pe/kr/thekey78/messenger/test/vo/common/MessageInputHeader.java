package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pe.kr.thekey78.messenger.annotation.DefaultValue;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.enumeration.Ascii;

@Data
@EqualsAndHashCode(callSuper=false)
public class MessageInputHeader {
	@Length(value = 8, align = Align.RIGHT, pad = Ascii.ZERO)
	private int length;
	@Length(16)
	private String messageId;
	@Length(4)
	private String systemGb;
	@Length(1)
	@DefaultValue("I")
	private String ioGb;
}
