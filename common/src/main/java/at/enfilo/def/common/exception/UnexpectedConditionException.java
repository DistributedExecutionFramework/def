package at.enfilo.def.common.exception;

/**
 * Created by mase on 24.10.2016.
 */
public class UnexpectedConditionException extends Exception {

    public UnexpectedConditionException(String message) {
        super(message);
    }

    public UnexpectedConditionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedConditionException(Throwable cause) {
        super(cause);
    }

    public UnexpectedConditionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
