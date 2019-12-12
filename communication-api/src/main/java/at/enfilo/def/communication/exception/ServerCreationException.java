package at.enfilo.def.communication.exception;

/**
 * Created by mase on 31.08.2016.
 */
public class ServerCreationException extends Exception {

    public ServerCreationException(String message) {
        super(message);
    }

    public ServerCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerCreationException(Throwable cause) {
        super(cause);
    }

    public ServerCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
