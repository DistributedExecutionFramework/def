package at.enfilo.def.reducer.server;

import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.reducer.util.ReducerConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

public class ReducerStartShutdownTest {

    @Before
    public void setUp() throws Exception {
        Reducer.main(null);
    }

    @Test
    public void running() throws Exception {

        ReducerConfiguration configuration = Reducer.getInstance().getConfiguration();

        // TicketRegistry must be initialized, otherwise getInstance() throws an exception
        TicketRegistry.getInstance();

        // Check if services are registered
        if (configuration.getServerHolderConfiguration().getRESTConfiguration().isEnabled()) {
            assertTrue(ServiceRegistry.getInstance().hasServiceInstanceOf(RESTServer.class));
            RESTServer restServer = ServiceRegistry.getInstance().getServiceInstanceOf(RESTServer.class);
            await().atMost(30, TimeUnit.SECONDS).until(restServer::isRunning);
        }
        if (configuration.getServerHolderConfiguration().getThriftTCPConfiguration().isEnabled()) {
            assertTrue(ServiceRegistry.getInstance().hasServiceInstanceOf(ThriftTCPServer.class));
            ThriftTCPServer thriftTCPServer = ServiceRegistry.getInstance().getServiceInstanceOf(ThriftTCPServer.class);
            await().atMost(30, TimeUnit.SECONDS).until(thriftTCPServer::isRunning);
        }
        if (configuration.getServerHolderConfiguration().getThriftHTTPConfiguration().isEnabled()) {
            assertTrue(ServiceRegistry.getInstance().hasServiceInstanceOf(ThriftHTTPServer.class));
            ThriftHTTPServer thriftHTTPServer = ServiceRegistry.getInstance().getServiceInstanceOf(ThriftHTTPServer.class);
            await().atMost(30, TimeUnit.SECONDS).until(thriftHTTPServer::isRunning);
        }
    }
}
