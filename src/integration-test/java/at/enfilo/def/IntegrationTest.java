package at.enfilo.def;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.misc.ServerStartup;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public abstract class IntegrationTest {

    protected List<ServerStartup> services;
    protected Map<Class<? extends ServerStartup>, IServer> thriftServices;
    protected Map<Class<? extends ServerStartup>, IServer> restServices;

    protected void startServices() throws Exception {
        for (ServerStartup st : services) {
            st.startServices();
            if (st.getConfiguration().getServerHolderConfiguration().getRESTConfiguration().isEnabled()) {
                IServer restService = st.getRESTServer(st.getConfiguration().getServerHolderConfiguration().getRESTConfiguration());
                await().atMost(30, TimeUnit.SECONDS).until(restService::isRunning);
                restServices.put(st.getClass(), restService);
            }
            if (st.getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration().isEnabled()) {
                IServer thriftService = st.getThriftTCPServer(st.getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration());
                await().atMost(30, TimeUnit.SECONDS).until(thriftService::isRunning);
                thriftServices.put(st.getClass(), thriftService);
            }
        }
    }

    protected void stopServices() throws Exception {
        for (IServer server : thriftServices.values()) {
            server.close();
        }
        for (IServer server : restServices.values()) {
            server.close();
        }

        // TODO kill scenario library JVM.
    }
}
