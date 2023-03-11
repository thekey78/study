package pe.kr.thekey78.messenger;

public class MessageException extends RuntimeException{
    public MessageException() {
        super();
    }
    public MessageException(String msg) {
        super(msg);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageException(Throwable cause) {
        super(cause);
    }

    protected MessageException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
