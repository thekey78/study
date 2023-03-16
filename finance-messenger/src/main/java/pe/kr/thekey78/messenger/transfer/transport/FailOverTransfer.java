package pe.kr.thekey78.messenger.transfer.transport;

import java.io.IOException;

public interface FailOverTransfer {
    byte[] sendAndReceive(byte[] bytes) throws IOException;
}
