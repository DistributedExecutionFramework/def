package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.api.ticket.service.TicketServiceClientFactory;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public abstract class TicketServiceTest {

    private IServer server;
    private ITicketServiceClient client;
    private Thread serverThread;
    protected ITicketRegistry ticketRegistry;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());
    }

    @Before
    public void setUp() throws Exception {
    	ticketRegistry = Mockito.mock(ITicketRegistry.class);
        server = getServer(ticketRegistry);
        serverThread = new Thread(server);
        serverThread.start();
        await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
        client = TicketServiceClientFactory.create(server.getServiceEndpoint());
    }

    @After
    public void teardown() throws Exception {
        client.close();
        server.close();
        serverThread.join();
    }

    protected abstract IServer getServer(ITicketRegistry ticketRegistry) throws Exception;

    @Test
    public void getTicketStatus() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketStatusDTO status = TicketStatusDTO.FAILED;

        when(ticketRegistry.getTicketStatus(ticketId)).thenReturn(status);

        assertEquals(status, client.getTicketStatus(ticketId.toString()));
    }


    @Test
    public void cancelTicket() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        boolean mayInterrupt = true;

        when(ticketRegistry.cancelTicketExecution(UUID.fromString(ticketId), true)).thenReturn(TicketStatusDTO.CANCELED);

        assertEquals(TicketStatusDTO.CANCELED, client.cancelTicket(ticketId, mayInterrupt));
    }

    @Test
    public void getFailedMessage() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String failedMessage = UUID.randomUUID().toString();

        when(ticketRegistry.getFailedMessage(UUID.fromString(ticketId))).thenReturn(failedMessage);

        assertEquals(failedMessage, client.getFailedMessage(ticketId));
    }
}
