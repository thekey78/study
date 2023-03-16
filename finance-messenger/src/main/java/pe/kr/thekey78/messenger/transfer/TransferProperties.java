package pe.kr.thekey78.messenger.transfer;

import java.util.Map;
import java.util.Properties;

public class TransferProperties {
    private Map<TransgerType, Properties> properties;

    private TransferProperties(){
        init();
    }

    private static class Singleton {
        private static TransferProperties INSTANCE;

        public static synchronized TransferProperties getInstance() {
            if (INSTANCE == null)
                INSTANCE = new TransferProperties();
            return INSTANCE;
        }
    }

    private void init() {
        // TODO Properties 초기화
    }

    public boolean reload() {
        // TODO Properties 재로드
        return true;
    }

    public static TransferProperties getInstance() {
        return Singleton.getInstance();
    }

    public Properties getProperties(TransgerType type) {
        return properties.get(type);
    }

    public Map<TransgerType, Properties> getAllProperties() {
        return properties;
    }
}
