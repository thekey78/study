package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import pe.kr.thekey78.messenger.annotation.DefaultValue;
import pe.kr.thekey78.messenger.annotation.Length;

@Data
public class MessageCommonFooter {
    @Length(2)
    @DefaultValue("@@")
    private String end;
}
