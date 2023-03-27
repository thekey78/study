package pe.kr.thekey78.messenger.test.vo.body;

import lombok.Data;
import pe.kr.thekey78.messenger.annotation.*;
import pe.kr.thekey78.messenger.enumeration.Align;
import pe.kr.thekey78.messenger.enumeration.Ascii;
import pe.kr.thekey78.messenger.enumeration.IoType;

import java.math.BigInteger;
import java.util.List;

@Data
@MessageId("MSG000000001")
public class MSG000000001 {
    @IOType(IoType.INPUT)
    private Input input = new Input();

    @IOType(IoType.OUTPUT)
    private Output output = new Output();

    @Data
    public static class Input {
        @Length(10)
        private String userId;

        @Length(128)
        @Extension({"enc", "base64encode"})
        private String password;
    }


    @Data
    public static class Output {
        @Length(2)
        private int ncnt;

        @Reference("ncnt")
        private List<Rec> rec;

        @Data
        public static class Rec {
            @Length(13)
            String acno;

            @Length(value = 12, align = Align.RIGHT, pad = Ascii.ZERO)
            BigInteger balance;
        }
    }
}
