package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pe.kr.thekey78.messenger.annotation.Length;

@Data
@EqualsAndHashCode(callSuper=false)
public class MessageErrorHeader {
	@Length(8)
	private String messageCode;

	@Length(180)
	private String message;
}
