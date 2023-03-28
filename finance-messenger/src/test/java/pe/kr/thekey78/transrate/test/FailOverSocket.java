package pe.kr.thekey78.transrate.test;

import lombok.extern.slf4j.Slf4j;
import pe.kr.thekey78.messenger.utils.NumberUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

@Slf4j
public class FailOverSocket {

    private final String host;
    private final int port;
    private final int readTimeout;
    private final int connectionTimeout;

    public FailOverSocket() {
        host = "223.1.1.11";
        port = 9015;
        readTimeout = 70;
        connectionTimeout = 3;
    }

    public byte[] sendAndReceive(byte[] bytes)  {
        try {
            SingleThread thread = new SingleThread(this);
            thread.setWriteBytes(bytes);
            thread.start();
            thread.join();
            if (thread.ioException != null)
                throw new RuntimeException(thread.ioException);
            return thread.getReadBytes();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class SingleThread extends Thread {
        FailOverSocket failOverSocket;

        boolean read = false;
        boolean write = false;

        byte[] writeBytes;
        byte[] readBytes;

        IOException ioException;

        SingleThread(FailOverSocket failOverSocket) {
            this.failOverSocket = failOverSocket;
        }

        public void setWriteBytes(byte[] writeBytes) {
            this.writeBytes = writeBytes;
        }

        public byte[] getReadBytes() {
            return readBytes;
        }

        @Override
        public void run() {
            try (Selector selector = Selector.open()){
                SocketChannel socketChannel = failOverSocket.getSocketChannel();
                socketChannel.register(selector, SelectionKey.OP_WRITE, new StringBuffer());
                //int repeatCount = 0;
                while (selector.isOpen() && selector.select() > 0) {
                    //System.out.println("selector repeat : " + ++repeatCount);
                    Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                    while (selectionKeys.hasNext()) {
                        SelectionKey selectionKey = selectionKeys.next();
                        selectionKeys.remove();

                        if(!selectionKey.isValid()) {
                            continue;
                        }

                        if(selectionKey.isReadable()) {
                            read(selector, selectionKey);
                            this.read = true;
                        }
                        else if (selectionKey.isWritable()) {
                            write(selector, selectionKey);
                            this.write = true;
                        }
                    }
                }
            }
            catch (IOException e) {
                ioException = e;
            }
        }
        private void write(Selector selector, SelectionKey selectionKey) {
            try {
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                socketChannel.configureBlocking(false);
                log.info(String.format("데이터 전송 [%s]", new String(writeBytes)));
                ByteBuffer buffer = ByteBuffer.wrap(writeBytes);
                socketChannel.write(buffer);
                socketChannel.register(selector, SelectionKey.OP_READ, selectionKey.attachment());
            }
            catch (IOException e) {
                ioException = e;
            }
        }

        private void read(Selector selector, SelectionKey selectionKey) {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            try {
                socketChannel.configureBlocking(false);

                byte[] totalLength = new byte[8];
                ByteBuffer buffer = ByteBuffer.wrap(totalLength);
                socketChannel.read(buffer);
                buffer.flip();
                buffer.get(totalLength);

                byte[] body = new byte[NumberUtils.intValue(totalLength)];
                buffer = ByteBuffer.wrap(body);
                socketChannel.read(buffer);
                buffer.flip();
                buffer.get(body);

                readBytes = new byte[totalLength.length + body.length];
                System.arraycopy(totalLength, 0, readBytes, 0, totalLength.length);
                System.arraycopy(body, 0, readBytes, totalLength.length, body.length);
                log.info(String.format("데이터 수신 [%s]", new String(readBytes)));
            }
            catch (IOException e) {
                ioException = e;
            }
            finally {
                close(selector);
                close(socketChannel);
                selectionKey.cancel();
                log.info(String.format("접속 종료 : %s:%s", failOverSocket.host, failOverSocket.port));
            }
        }

        public void close(Closeable socketChannel) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private SocketChannel getSocketChannel() throws IOException {
        log.info(String.format("서버 접속 : %s:%s", host, port));
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);

        Socket socket = socketChannel.socket();
        socket.setSoTimeout(readTimeout);
        socket.setSoLinger(true, readTimeout);
        socket.connect(new InetSocketAddress(host, port), connectionTimeout);

        socketChannel.configureBlocking(false);
        return socketChannel;
    }
}
