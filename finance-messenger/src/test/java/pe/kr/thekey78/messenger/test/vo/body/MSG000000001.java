package pe.kr.thekey78.messenger.test.vo.body;

import lombok.Data;
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

@Data
@MessageId("MSG000000001")
public class MSG000000001 {
    private Input input = new Input();
    private Output output = new Output();

    @IOType(IoType.INPUT)
    @Getter @Setter
    public static class Input extends AbstractVo {
        @Length(10)
        private String userId;

        @Override
        public String toString() {
            return "Input{" +
                    "userId='" + userId + '\'' +
                    '}';
        }
    }


    @IOType(IoType.OUTPUT)
    @Data
    public static class Output extends AbstractVo{
        @Length(2)
        private int ncnt;

        @Reference("ncnt")
        private List<Rec> rec;

        @Override
        public String toString() {
            return "Output{" +
                    "ncnt=" + ncnt +
                    ", rec=" + rec +
                    '}';
        }

        @Data
        public static class Rec extends AbstractVo {
            @Length(13)
            String acno;

            @Length(12)
            BigInteger balance;
        }
    }
}
