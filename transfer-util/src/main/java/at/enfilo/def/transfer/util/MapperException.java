package at.enfilo.def.transfer.util;

/**
 * Created by mase on 13.09.16.
 */
public class MapperException extends RuntimeException {

    public MapperException(String message) {
        super(message);
    }

    public MapperException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapperException(Throwable cause) {
        super(cause);
    }

    public MapperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
