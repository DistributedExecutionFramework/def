package at.enfilo.def.scheduler.reducer.api;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerResponseService;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerService;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;
import java.util.concurrent.Future;

public class ReducerSchedulerServiceClient<T extends ReducerSchedulerService.Iface, R extends ReducerSchedulerResponseService.Iface>
        implements IReducerSchedulerServiceClient {

    protected final IClient<T> requestClient;
    protected final IClient<R> responseClient;
    protected final ITicketServiceClient ticketClient;

    /**
     * Constructor used by ReducerSchedulerServiceClientFactory.
     *
     * @param requestClient - request client
     * @param responseClient - response client
     * @param ticketClient - ticket client
     */
    public ReducerSchedulerServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient) {
        this.requestClient = requestClient;
        this.responseClient = responseClient;
        this.ticketClient = ticketClient;
    }

    @Override
    public Future<Void> addReducer(String rId, ServiceEndpointDTO endpoint) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.addReducer(rId, endpoint));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> removeReducer(String rId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.removeReducer(rId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> addReduceJob(JobDTO job) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.addReduceJob(job));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> removeReduceJob(String jId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.removeReduceJob(jId));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<Void> scheduleResourcesToReduce(String jId, List<ResourceDTO> resources) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.scheduleResourcesToReduce(jId, resources));
        return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
    }

    @Override
    public Future<JobDTO> finalizeReduce(String jId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.finalizeReduce(jId));
        return new TicketFutureBuilder<R, JobDTO>()
                .dataTicket(ticketId, ticketClient)
                .request(R::finalizeReduce)
                .via(responseClient);
    }

    @Override
    public ServiceEndpointDTO getServiceEndpoint() { return requestClient.getServiceEndpoint(); }

    @Override
    public void close() {
        requestClient.close();
        responseClient.close();
        ticketClient.close();
    }
}
