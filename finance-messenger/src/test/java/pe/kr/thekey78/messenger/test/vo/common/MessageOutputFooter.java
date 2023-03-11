package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import pe.kr.thekey78.messenger.annotation.DefaultValue;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.vo.AbstractVo;

@Data
public class MessageOutputFooter extends AbstractVo {
	@Length(2)
	@DefaultValue("@@")
	private String end;
}
