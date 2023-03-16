package pe.kr.thekey78.messenger.transfer;

import java.util.Properties;

public abstract class AbstractAdaptor implements ExternalAdaptor{
    private final TransferProperties transferProperties;

    private AbstractAdaptor() {
        transferProperties = TransferProperties.getInstance();
    }

    @Override
    public byte[] execute(String uuid, String channelId, byte[] request) {
        return new byte[0];
    }

    protected byte[] exec(String uuid, String channelId, byte[] request) {
        Properties prop = transferProperties.getProperties(TransgerType.SOCKET);
        String ip = prop.getProperty("");
        return new byte[0];
    }
}
