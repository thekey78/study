package pe.kr.thekey78.transfer.adaptor;

import pe.kr.thekey78.transfer.util.AbstractTransferProperties;

public abstract class AbstractAdaptor implements Adaptor{
    private final AbstractTransferProperties transferProperties;

    private AbstractAdaptor(AbstractTransferProperties transferProperties) {
        this.transferProperties = transferProperties;
    }
}
