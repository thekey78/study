package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import pe.kr.thekey78.messenger.annotation.DefaultValue;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.enumeration.Ascii;

@Data
public class MessageCommonHeader {
    @Length(value = 8, align = Align.RIGHT, pad = Ascii.ZERO)
    private int messageLength;
    @Length(20)
    private String messageId;

    @Length(1)
    @DefaultValue("I")
    private String messageDirection;

    @Length(32)
    private String uuid;

    @Length(17)
    private String reqDateTime;

    @Length(17)
    private String resDateTime;
}
