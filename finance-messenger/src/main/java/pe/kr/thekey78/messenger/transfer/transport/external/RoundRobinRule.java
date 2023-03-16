package pe.kr.thekey78.messenger.transfer.transport.external;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinRule {
    private AtomicInteger atomicInteger;

    private int limit;

    public RoundRobinRule(int init, int limit) {
        this.atomicInteger = new AtomicInteger(init);
        this.limit = limit;
    }

    public int next() {
        int nextVal = atomicInteger.get();
        if(nextVal >= limit) {
            nextVal = 0;
            this.atomicInteger.set(nextVal);
        }
        return atomicInteger.getAndIncrement();
    }
}
