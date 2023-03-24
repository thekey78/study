package pe.kr.thekey78.messenger.test.vo.common;

import lombok.Data;
import pe.kr.thekey78.messenger.annotation.Length;

@Data
public class MessageResponseHeader {
    @Length(1)
    private String rsltDvcd;

    @Length(5)
    private String resCd;

    @Length(99)
    private String resMsg;
}
