package at.enfilo.def.transfer.util;

/**
 * Created by mase on 26.08.2016.
 */
public class UnsupportedMappingException extends RuntimeException {

    public UnsupportedMappingException(String message) {
        super(message);
    }

    public UnsupportedMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedMappingException(Throwable cause) {
        super(cause);
    }

    public UnsupportedMappingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
