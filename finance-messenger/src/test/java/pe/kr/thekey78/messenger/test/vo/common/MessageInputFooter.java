package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pe.kr.thekey78.messenger.annotation.DefaultValue;
import pe.kr.thekey78.messenger.annotation.Length;

@Data
@EqualsAndHashCode(callSuper=false)
public class MessageInputFooter {
	@Length(2)
	@DefaultValue("@@")
	private String end;
}
