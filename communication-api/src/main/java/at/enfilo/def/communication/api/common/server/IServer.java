package at.enfilo.def.communication.api.common.server;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;

import java.io.Closeable;

/**
 * Created by mase on 31.08.2016.
 */
public interface IServer extends Runnable, Closeable, AutoCloseable {

    /**
     * Get ServiceEndpoint of this server.
     *
     * @return Service endpoint information.
     */
    ServiceEndpointDTO getServiceEndpoint();

    /**
     * Provides information about server state.
     *
     * @return true if server is running, false if not.
     */
    boolean isRunning();
}
