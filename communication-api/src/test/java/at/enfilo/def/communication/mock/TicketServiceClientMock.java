package at.enfilo.def.communication.mock;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.rest.ITicketService;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by mase on 29.09.2016.
 */
public class TicketServiceClientMock implements IClient<ITicketService> {

    public static final String UNKNOWN_TICKET = UUID.randomUUID().toString();
    public static final String CANCELED_TICKET = UUID.randomUUID().toString();
    public static final String DONE_TICKET = UUID.randomUUID().toString();
    public static final String IN_PROGRESS_TICKET = UUID.randomUUID().toString();
    public static final String LONG_WAITING_TICKET = UUID.randomUUID().toString();
    public static final String FAILED_TICKET = UUID.randomUUID().toString();
    public static final int UNKNOWN_TICKET_SLEEP = 5000; // 1min
    public static final int LONG_WAITING = 5;
    public static final int CANCEL_SLEEP = 1000;
    public static final int IN_PROGRESS_SLEEP = 1000;

    private ITicketService mockTicketStatusResource;
    private int changeStateCounter;

    public TicketServiceClientMock() {
        this.mockTicketStatusResource = Mockito.mock(ITicketService.class);

        when(mockTicketStatusResource.getTicketStatus(UNKNOWN_TICKET)).thenReturn(TicketStatusDTO.UNKNOWN);
        when(mockTicketStatusResource.waitForTicket(UNKNOWN_TICKET)).thenAnswer(invocation -> {
           Thread.sleep(UNKNOWN_TICKET_SLEEP);
           return TicketStatusDTO.UNKNOWN;
        });

        when(mockTicketStatusResource.getTicketStatus(DONE_TICKET)).thenReturn(TicketStatusDTO.DONE);
        when(mockTicketStatusResource.waitForTicket(DONE_TICKET)).thenReturn(TicketStatusDTO.DONE);

        when(mockTicketStatusResource.getTicketStatus(CANCELED_TICKET)).thenAnswer(invocation -> {
        	Thread.sleep(CANCEL_SLEEP);
        	return TicketStatusDTO.CANCELED;
		});
        when(mockTicketStatusResource.waitForTicket(CANCELED_TICKET)).thenAnswer(invocation -> {
            Thread.sleep(CANCEL_SLEEP);
            return TicketStatusDTO.CANCELED;
        });

        when(mockTicketStatusResource.getTicketStatus(FAILED_TICKET)).thenReturn(TicketStatusDTO.FAILED);
        when(mockTicketStatusResource.waitForTicket(FAILED_TICKET)).thenReturn(TicketStatusDTO.FAILED);

        when(mockTicketStatusResource.getTicketStatus(IN_PROGRESS_TICKET)).thenReturn(TicketStatusDTO.IN_PROGRESS);
        when(mockTicketStatusResource.waitForTicket(IN_PROGRESS_TICKET)).thenAnswer(invocation -> {
            Thread.sleep(IN_PROGRESS_SLEEP);
            return TicketStatusDTO.DONE;
        });

        when(mockTicketStatusResource.getTicketStatus(LONG_WAITING_TICKET)).thenAnswer(invocation -> {
			Thread.sleep(LONG_WAITING * 1000);
			return TicketStatusDTO.DONE;
		});
        when(mockTicketStatusResource.waitForTicket(LONG_WAITING_TICKET)).thenAnswer(invocation -> {
            Thread.sleep(LONG_WAITING * 1000);
            return TicketStatusDTO.DONE;
        });

        when(mockTicketStatusResource.cancelTicketExecution(anyString(), anyBoolean())).thenReturn(TicketStatusDTO.CANCELED);

        changeStateCounter = 0;
    }

	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
    	return null;
	}

	@Override
    public void executeVoid(IThrowingConsumer<ITicketService> proxy)
    throws ClientCommunicationException {
        try {
            proxy.accept(mockTicketStatusResource);
        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    @Override
    public <R> R execute(IThrowingFunction<ITicketService, R> proxy)
    throws ClientCommunicationException {
        try {
            return proxy.apply(mockTicketStatusResource);
        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    @Override
    public void close() {

    }
}
