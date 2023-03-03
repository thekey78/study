package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pe.kr.thekey78.messenger.annotation.DefaultValue;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.vo.AbstractVo;

@Data
@EqualsAndHashCode(callSuper=false)
public class MessageOutputFooter extends AbstractVo {
	@Length(2)
	@DefaultValue("@@")
	private String end;
}
