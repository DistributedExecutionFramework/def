package at.enfilo.def.parameterserver.api.client;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.parameterserver.api.IParameterServerServiceClient;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerResponseService;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerService;
import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ParameterType;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.concurrent.Future;

class ParameterServerServiceClient<T extends ParameterServerService.Iface, R extends ParameterServerResponseService.Iface>
        implements IParameterServerServiceClient {

    private final IClient<T> requestClient;
    private final IClient<R> responseClient;
    private final ITicketServiceClient ticketClient;

    /**
     * Constructor used by ClusterServiceClientFactory.
     *
     * @param requestClient
     * @param responseClient
     * @param ticketClient
     */
    ParameterServerServiceClient(
            IClient<T> requestClient,
            IClient<R> responseClient,
            ITicketServiceClient ticketClient
    ) {
        this.requestClient = requestClient;
        this.responseClient = responseClient;
        this.ticketClient = ticketClient;
    }

    @Override
    public Future<String> setParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.setParameter(programId, parameterId, parameter, protocol));
        return new TicketFutureBuilder<R, String>()
                .dataTicket(ticketId, ticketClient)
                .request(R::setParameter)
                .via(responseClient);
    }

    @Override
    public Future<String> createParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol, ParameterType type) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.createParameter(programId, parameterId, parameter, protocol, type));
        return new TicketFutureBuilder<R, String>()
                .dataTicket(ticketId, ticketClient)
                .request(R::createParameter)
                .via(responseClient);
    }

    @Override
    public Future<ResourceDTO> getParameter(String programId, String parameterId, ParameterProtocol protocol) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.getParameter(programId, parameterId, protocol));
        return new TicketFutureBuilder<R, ResourceDTO>()
                .dataTicket(ticketId, ticketClient)
                .request(R::getParameter)
                .via(responseClient);
    }

    @Override
    public Future<String> addToParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.addToParameter(programId, parameterId, parameter, protocol));
        return new TicketFutureBuilder<R, String>()
                .dataTicket(ticketId, ticketClient)
                .request(R::addToParameter)
                .via(responseClient);
    }

    @Override
    public Future<String> deleteParameter(String programId, String parameterId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.deleteParameter(programId, parameterId));
        return new TicketFutureBuilder<R, String>()
                .dataTicket(ticketId, ticketClient)
                .request(R::deleteParameter)
                .via(responseClient);
    }

    @Override
    public Future<String> deleteAllParameters(String programId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.deleteAllParameters(programId));
        return new TicketFutureBuilder<R, String>()
                .dataTicket(ticketId, ticketClient)
                .request(R::deleteAllParameters)
                .via(responseClient);
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
