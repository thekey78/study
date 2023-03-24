package pe.kr.thekey78.transfer.util;

import pe.kr.thekey78.transfer.info.TransgerType;

import java.util.Map;
import java.util.Properties;

public abstract class AbstractTransferProperties {
    private Map<TransgerType, Properties> properties;

    private AbstractTransferProperties(){
        init();
    }

    abstract protected void init();

    abstract public boolean reload(TransgerType transgerType);

    public void reload() {
        for(TransgerType transgerType : TransgerType.values()) {
            reload(transgerType);
        }
    }

    public Properties getProperties(TransgerType type) {
        return properties.get(type);
    }

    public Map<TransgerType, Properties> getAllProperties() {
        return properties;
    }
}
