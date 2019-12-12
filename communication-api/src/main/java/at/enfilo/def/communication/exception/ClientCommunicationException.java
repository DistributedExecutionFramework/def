package at.enfilo.def.communication.exception;

/**
 * Created by mase on 31.08.2016.
 */
public class ClientCommunicationException extends Exception {

    public ClientCommunicationException(String message) {
        super(message);
    }

    public ClientCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientCommunicationException(Throwable cause) {
        super(cause);
    }

    public ClientCommunicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
