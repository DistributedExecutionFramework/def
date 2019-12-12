package at.enfilo.def.scheduler.clientroutineworker.api;

public class ProgramOperationException extends Exception {
    public ProgramOperationException(String message) {
        super(message);
    }

    public ProgramOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProgramOperationException(Exception e) {
        super(e);
    }
}
