package at.enfilo.def.clientroutine.worker.api;

import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerResponseService;
import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerService;
import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.node.api.NodeServiceClient;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.List;
import java.util.concurrent.Future;

public class ClientRoutineWorkerServiceClient<T extends ClientRoutineWorkerService.Iface, R extends ClientRoutineWorkerResponseService.Iface> extends NodeServiceClient<T, R> implements IClientRoutineWorkerServiceClient {

    private final IClient<T> requestClient;
    private final IClient<R> responseClient;
    private final ITicketServiceClient ticketClient;

    public ClientRoutineWorkerServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient) throws ClientCreationException {
        super(requestClient, responseClient, ticketClient);

        this.requestClient = requestClient;
        this.responseClient = responseClient;
        this.ticketClient = ticketClient;
    }

    @Override
    public Future<List<String>> getQueuedPrograms(String qId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(c -> c.getQueuedPrograms(qId));
        return new TicketFutureBuilder<R, List<String>>()
                .dataTicket(ticketId, ticketClient)
                .request(R::getQueuedPrograms)
                .via(responseClient);
    }

    @Override
    public Future<Void> queueProgram(String qId, ProgramDTO program) throws ClientCommunicationException {
        String ticketId = requestClient.execute(c -> c.queueProgram(qId, program));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> queuePrograms(String qId, List<ProgramDTO> programs) throws ClientCommunicationException {
        String ticketId = requestClient.execute(c -> c.queuePrograms(qId, programs));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> movePrograms(String qId, List<String> programIds, ServiceEndpointDTO targetNodeEndpoint) throws ClientCommunicationException {
        String ticketId = requestClient.execute(c -> c.movePrograms(qId, programIds, targetNodeEndpoint));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> moveAllPrograms(ServiceEndpointDTO targetNodeEndpoint) throws ClientCommunicationException {
        String ticketId = requestClient.execute(c -> c.moveAllPrograms(targetNodeEndpoint));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<ProgramDTO> fetchFinishedProgram(String pId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(c -> c.fetchFinishedProgram(pId));
        return new TicketFutureBuilder<R, ProgramDTO>()
                .dataTicket(ticketId, ticketClient)
                .request(R::fetchFinishedProgram)
                .via(responseClient);
    }

    @Override
    public Future<Void> abortProgram(String pId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(c -> c.abortProgram(pId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }
}
