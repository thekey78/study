package pe.kr.thekey78.messenger.transfer;

public interface ExternalAdaptor {
    byte[] execute(String uuid, String channelId, byte[] request);
}
