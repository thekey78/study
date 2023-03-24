package pe.kr.thekey78.transfer.info;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinRule {
    private final AtomicInteger atomicInteger;

    private final int limit;

    public RoundRobinRule(int init, int limit) {
        this.atomicInteger = new AtomicInteger(init);
        this.limit = limit;
    }

    public synchronized int next() {
        int nextVal = atomicInteger.get();
        if(nextVal >= limit) {
            nextVal = 0;
            this.atomicInteger.set(nextVal);
        }
        return atomicInteger.getAndIncrement();
    }
}
