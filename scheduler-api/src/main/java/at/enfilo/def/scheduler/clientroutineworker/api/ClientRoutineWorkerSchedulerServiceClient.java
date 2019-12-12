package at.enfilo.def.scheduler.clientroutineworker.api;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.scheduler.clientroutineworker.api.thrift.ClientRoutineWorkerSchedulerResponseService;
import at.enfilo.def.scheduler.clientroutineworker.api.thrift.ClientRoutineWorkerSchedulerService;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.concurrent.Future;

public class ClientRoutineWorkerSchedulerServiceClient<T extends ClientRoutineWorkerSchedulerService.Iface, R extends ClientRoutineWorkerSchedulerResponseService.Iface>
implements IClientRoutineWorkerSchedulerServiceClient {

    protected final IClient<T> requestClient;
    protected final IClient<R> responseClient;
    protected final ITicketServiceClient ticketClient;

    public ClientRoutineWorkerSchedulerServiceClient(
            IClient<T> requestClient,
            IClient<R> responseClient,
            ITicketServiceClient ticketClient
    ) {
        this.requestClient = requestClient;
        this.responseClient = responseClient;
        this.ticketClient = ticketClient;
    }

    @Override
    public Future<Void> addClientRoutineWorker(String wId, ServiceEndpointDTO endpoint) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.addClientRoutineWorker(wId, endpoint));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> removeClientRoutineWorker(String wId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.removeClientRoutineWorker(wId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> addUser(String uId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.addUser(uId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> removeUser(String uId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.removeUser(uId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> abortProgram(String wId, String pId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.abortProgram(wId, pId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> scheduleProgram(String uId, ProgramDTO program) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.scheduleProgram(uId, program));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public ServiceEndpointDTO getServiceEndpoint() {
        return requestClient.getServiceEndpoint();
    }

    @Override
    public void close() {
        requestClient.close();
        responseClient.close();
        ticketClient.close();
    }
}
