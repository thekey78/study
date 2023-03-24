package pe.kr.thekey78.transrate.test;

import pe.kr.thekey78.messenger.test.util.Target;

public class Translate {
    private static class Singleton {
        private static Translate INSTANCE;

        public synchronized static Translate getInstance() {
            if (INSTANCE == null)
                INSTANCE = new Translate();
            return INSTANCE;
        }
    }

    public static Translate getInstance() {
        return Singleton.getInstance();
    }

    public byte[] execute(byte[] bytes) {
        final FailOverSocket socket = new FailOverSocket();
        return socket.sendAndReceive(bytes);
    }
}
