package pe.kr.thekey78.messenger.test.vo.body;

import lombok.Getter;
import lombok.Setter;
import pe.kr.thekey78.messenger.annotation.IOType;
import pe.kr.thekey78.messenger.annotation.Length;
import pe.kr.thekey78.messenger.annotation.MessageId;
import pe.kr.thekey78.messenger.annotation.Reference;
import pe.kr.thekey78.messenger.enumeration.IoType;
import pe.kr.thekey78.messenger.vo.AbstractVo;

import java.math.BigInteger;
import java.util.List;

@MessageId("MSG000000001")
@Getter @Setter
public class MSG000000001 {
    private Input input = new Input();
    private Output output = new Output();
    @IOType(IoType.INPUT)
    @Getter @Setter
    public static class Input extends AbstractVo {
        @Length(10)
        private String userId;
    }


    @IOType(IoType.OUTPUT)
    @Getter @Setter
    public static class Output extends AbstractVo{
        @Length(2)
        private int ncnt;

        @Reference("ncnt")
        private List<Rec> rec;

        public static class Rec {
            @Length(13)
            String acno;

            @Length(12)
            BigInteger balance;
        }
    }
}
