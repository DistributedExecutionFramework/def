package at.enfilo.def.manager.webservice;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public abstract class ServiceTest {

    private static IServer webserviceServer;
    private static Thread webserviceServerThread;


    @BeforeClass
    public static void setUp() throws Exception {

        // start ticket service
        TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

        // start webservice server for testing methods
        webserviceServer = ServiceRESTTest.getWebserviceServer();
        webserviceServerThread = new Thread(webserviceServer);
        webserviceServerThread.start();

        await().atMost(30, TimeUnit.SECONDS).until(webserviceServer::isRunning);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        webserviceServer.close();
        webserviceServerThread.join();
        ServiceRegistry.getInstance().closeAll();
    }

}
