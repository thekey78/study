package pe.kr.thekey78.messenger.function;

public abstract class MessageBuildHelper<E> {

    protected int length;

    public abstract E get();

    public int length() {
        return length;
    }
}
