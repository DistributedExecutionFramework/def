package at.enfilo.def.logging.api;

/**
 * Created by mase on 22.02.2017.
 */
public enum ContextIndicator {
    PROGRAM_CONTEXT("Program"),
    JOB_CONTEXT("Job"),
    TASK_CONTEXT("Task");

    private final String contextIndicatorPlaceholder;

    ContextIndicator(String contextIndicatorPlaceholder) {
        this.contextIndicatorPlaceholder = contextIndicatorPlaceholder;
    }

    public String getPlaceholder() {
        return contextIndicatorPlaceholder;
    }
}
