package at.enfilo.def.communication.api.common.util;

import java.util.Collection;

/**
 * Created by mase on 24.10.2016.
 */
public interface IHandlerDaemon {

    /**
     * Attempts to shutdown {@code this} handler.
     * Returns list of all not finished runnable tasks.
     *
     * @return list of tasks that were never executed.
     */
    Collection<Runnable> shutdownNow();
}
