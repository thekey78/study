package pe.kr.thekey78.messenger.test.vo.body;

import lombok.Data;
import pe.kr.thekey78.messenger.annotation.IOType;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.annotation.MessageId;
import pe.kr.thekey78.messenger.annotation.Reference;
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

            @Length(12)
            BigInteger balance;
        }
    }
}
