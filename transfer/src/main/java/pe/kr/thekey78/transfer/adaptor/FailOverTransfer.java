package pe.kr.thekey78.transfer.adaptor;

import java.io.IOException;

public interface FailOverTransfer {
    byte[] sendAndReceive(byte[] bytes) throws IOException;
}
