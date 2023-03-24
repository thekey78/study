package pe.kr.thekey78.transfer.adaptor;

public interface Adaptor {
    byte[] execute(String uuid, String channelId, byte[] request);
}
