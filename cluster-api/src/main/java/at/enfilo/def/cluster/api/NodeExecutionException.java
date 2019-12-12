package at.enfilo.def.cluster.api;

public class NodeExecutionException extends Exception {
    public NodeExecutionException(String message) {
        super(message);
    }

    public NodeExecutionException(Throwable cause) {
        super(cause);
    }
}
