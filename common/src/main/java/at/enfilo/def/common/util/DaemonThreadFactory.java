package at.enfilo.def.common.util;

/**
 * Created by mase on 27.10.16.
 */
public class DaemonThreadFactory {

    private DaemonThreadFactory() {
        // Hiding public constructor
    }

	/**
	 * Creates new daemon thread (Factory method).
	 *
	 * @param target target the runnable object whose {@code run} method is invoked when this thread is started.
	 * @param name name of thread.
	 * @return new daemon thread.
	 */
    public static Thread newDaemonThread(Runnable target, String name) {
        Thread daemonThread = new Thread(target, name);
        daemonThread.setDaemon(true);

        return daemonThread;
    }
}
