package at.enfilo.def.client.util;

/**
 * Created by mase on 22.08.2016.
 */
public enum SessionConstant {
    ACTIVE_SESSION("ACTIVE_SESSION"),
    ACTIVE_PROGRAM_ID("ACTIVE_PROGRAM_ID"),
    ACTIVE_JOB_ID("ACTIVE_JOB_ID"),
    ACTIVE_TASK_ID("ACTIVE_TASK_ID"),
    AUTH_DTO("authDTO");

    private final String constant;

    SessionConstant(String constant) {
        this.constant = constant;
    }

    public String getConstant() {
        return constant;
    }


    @Override
    public String toString() {
        return constant;
    }
}
