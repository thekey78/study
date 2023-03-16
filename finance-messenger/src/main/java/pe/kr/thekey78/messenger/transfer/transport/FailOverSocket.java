package pe.kr.thekey78.messenger.transfer.transport;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import pe.kr.thekey78.messenger.transfer.transport.external.HostInfo;
import pe.kr.thekey78.messenger.transfer.transport.external.RoundRobinRule;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

@Slf4j
public class FailOverSocket implements FailOverTransfer {
    private boolean failOver;
    private boolean roundRobin;

    private List<HostInfo> hosts;

    private RoundRobinRule roundRobinRule;

    public FailOverSocket(@NonNull boolean failOver, @NonNull boolean roundRobin, @NonNull List<HostInfo> hosts, @NonNull RoundRobinRule roundRobinRule) {
        this.failOver = failOver;
        this.roundRobin = roundRobin;
        this.hosts = hosts;
        this.roundRobinRule = roundRobinRule;
    }

    @Override
    public byte[] sendAndReceive(byte[] bytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return sendAndReceive(buffer);
    }

    public byte[] sendAndReceive(ByteBuffer buffer) throws IOException {
        byte[] data = null;
        SocketChannel socketChannel = null;
        HostInfo hostInfo = getHostInfo();
        try {
            log.info("서버 접속 : %s:%s", hostInfo.getHost(), hostInfo.getPort());
            socketChannel = getSocketChannel(hostInfo);

            // 2. 데이터 전송
            log.info("송신전문길이 : [%d]", buffer.position());
            socketChannel.write(buffer);

            // 3. 데이터 수신
            buffer.clear();
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                data = new byte[bytesRead];
                buffer.get(data, 0, bytesRead);
            }
            log.info("수신전문길이 : [%d]", bytesRead);
        }
        finally {
            // 4. SocketChannel 닫기
            IOUtils.close(socketChannel);
            log.info("서버 접속 종료 : %s:%s", hostInfo.getHost(), hostInfo.getPort());
        }
        return data;
    }

    private HostInfo getHostInfo() {
        int index = roundRobinRule.next();
        return hosts.get(index);
    }

    private InetSocketAddress getSocketAddress(HostInfo hostInfo) {
        return new InetSocketAddress(hostInfo.getHost(), hostInfo.getPort());

    }

    private SocketChannel getSocketChannel(HostInfo hostInfo) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);

        Socket socket = channel.socket();
        socket.setSoTimeout(hostInfo.getReadTimeout());
        socket.setSoLinger(true, hostInfo.getReadTimeout());
        socket.connect(getSocketAddress(hostInfo), hostInfo.getConnectionTimeout());

        channel.configureBlocking(true);

        return channel;
    }
}
