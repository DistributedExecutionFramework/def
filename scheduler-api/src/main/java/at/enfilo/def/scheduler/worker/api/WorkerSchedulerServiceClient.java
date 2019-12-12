package at.enfilo.def.scheduler.worker.api;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerResponseService;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerService;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.util.concurrent.Future;

public class WorkerSchedulerServiceClient<T extends WorkerSchedulerService.Iface, R extends WorkerSchedulerResponseService.Iface>
    implements IWorkerSchedulerServiceClient {

    protected final IClient<T> requestClient;
    protected final IClient<R> responseClient;
    protected final ITicketServiceClient ticketClient;

    /**
     * Constructor used by factory.
     *
     * @param requestClient - request client
     * @param responseClient - response client
     * @param ticketClient - ticket client
     */
    public WorkerSchedulerServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient) {
        this.requestClient = requestClient;
        this.responseClient = responseClient;
        this.ticketClient = ticketClient;
    }

    @Override
    public Future<Void> addWorker(String wId, ServiceEndpointDTO endpoint) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.addWorker(wId, endpoint));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> removeWorker(String wId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.removeWorker(wId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> addJob(String jId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.addJob(jId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> removeJob(String jId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.removeJob(jId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> markJobAsComplete(String jId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.markJobAsComplete(jId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> scheduleTask(String jId, TaskDTO task) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.scheduleTask(jId, task));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> abortTask(String wId, String tId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.abortTask(wId, tId));
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
