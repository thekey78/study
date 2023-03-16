package pe.kr.thekey78.messenger.utils;

import java.util.UUID;

public class UUIDGenerator {
    private static class Singleton {
        private static UUIDGenerator instance;

        public static synchronized UUIDGenerator getInstance() {
            if(instance == null)
                instance = new UUIDGenerator();
            return instance;
        }
    }

    public static UUIDGenerator getInstance() {
        return Singleton.getInstance();
    }

    public String getNextUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
