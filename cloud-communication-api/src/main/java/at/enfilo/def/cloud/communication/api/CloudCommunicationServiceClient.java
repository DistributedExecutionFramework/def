package at.enfilo.def.cloud.communication.api;

import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationResponseService;
import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationService;
import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.InstanceTypeDTO;
import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.api.ticket.service.TicketServiceClientFactory;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;

import java.util.List;
import java.util.concurrent.Future;

public class CloudCommunicationServiceClient<T extends CloudCommunicationService.Iface, R extends CloudCommunicationResponseService.Iface> implements ICloudCommunicationServiceClient {

    protected IClient<T> requestClient;
    protected IClient<R> responseClient;
    protected ITicketServiceClient ticketClient;


    protected CloudCommunicationServiceClient(
            IClient<T> requestClient,
            IClient<R> responseClient)
        throws ClientCreationException {

        this(requestClient, responseClient, TicketServiceClientFactory.create(requestClient.getServiceEndpoint()));
    }

    protected CloudCommunicationServiceClient(
            IClient<T> requestClient,
            IClient<R> responseClient,
            ITicketServiceClient ticketClient
    ) {
        this.requestClient = requestClient;
        this.responseClient = responseClient;
        this.ticketClient = ticketClient;
    }

    @Override
    public Future<String> createAWSCluster(AWSSpecificationDTO specification) throws ClientCommunicationException {
        String ticketId = this.requestClient.execute(t -> t.createAWSCluster(specification));

        // Assembling Future <T> object for received TicketId
        return new TicketFutureBuilder<R, String>()
                .dataTicket(ticketId, this.ticketClient)
                .request(R::createAWSCluster)
                .via(this.responseClient);
    }

    @Override
    public Future<String> bootClusterInstance(String cloudClusterId) throws ClientCommunicationException {
        String ticketId = this.requestClient.execute(t -> t.bootClusterInstance(cloudClusterId));

        // Assembling Future <T> object for received TickedId
        return new TicketFutureBuilder<R, String>()
                .dataTicket(ticketId, this.ticketClient)
                .request(R::bootClusterInstance)
                .via(this.responseClient);
    }

    @Override
    public Future<List<String>> bootNodes(String cloudClusterId, InstanceTypeDTO type, int nrOfNodes) throws ClientCommunicationException {
        String ticketId = this.requestClient.execute(t -> t.bootNodes(cloudClusterId, type, nrOfNodes));

        // Assembling Future <T> object for received TicketId
        return new TicketFutureBuilder<R, List<String>>()
                .dataTicket(ticketId, this.ticketClient)
                .request(R::bootNodes)
                .via(this.responseClient);
    }

    @Override
    public Future<Void> terminateNodes(String cloudClusterId, List<String> cloudInstanceIds) throws ClientCommunicationException {
        String ticketId = this.requestClient.execute(t -> t.terminateNodes(cloudClusterId, cloudInstanceIds));

        // Assembling Future <T> object for received TicketId
        return new TicketFutureBuilder<>().voidTicket(ticketId, this.ticketClient);
    }

    @Override
    public Future<String> getPublicIPAddressOfCloudInstance(String cloudClusterId, String cloudInstanceId) throws ClientCommunicationException {
        String ticketId = this.requestClient.execute(t -> t.getPublicIPAddressOfCloudInstance(cloudClusterId, cloudInstanceId));

        // Assembling Future <T> object for received TicketId
        return new TicketFutureBuilder<R, String>()
                .dataTicket(ticketId, this.ticketClient)
                .request(R::getPublicIPAddressOfCloudInstance)
                .via(this.responseClient);
    }

    @Override
    public Future<String> getPrivateIPAddressOfCloudInstance(String cloudClusterId, String cloudInstanceId) throws ClientCommunicationException {
        String ticketId = this.requestClient.execute(t -> t.getPrivateIPAddressOfCloudInstance(cloudClusterId, cloudInstanceId));

        // Assembling Future <T> object for received TicketId
        return new TicketFutureBuilder<R, String>()
                .dataTicket(ticketId, this.ticketClient)
                .request(R::getPrivateIPAddressOfCloudInstance)
                .via(this.responseClient);
    }

    @Override
    public Future<Void> shutdownCloudCluster(String cloudClusterId) throws ClientCommunicationException {
        String ticketId = this.requestClient.execute(t -> t.shutdownCloudCluster(cloudClusterId));

        // Assembling Future <T> object for received TicketId
        return new TicketFutureBuilder<>().voidTicket(ticketId, this.ticketClient);
    }

    @Override
    public Future<Void> mapDEFIdToCloudInstanceId(String cloudClusterId, String defId, String cloudInstanceId) throws ClientCommunicationException {
        String ticketId = this.requestClient.execute(t -> t.mapDEFIdToCloudInstanceId(cloudClusterId, defId, cloudInstanceId));

        // Assembling Future <T> object for received TicketId
        return new TicketFutureBuilder<>().voidTicket(ticketId, this.ticketClient);
    }

    @Override
    public ServiceEndpointDTO getServiceEndpoint() {
        return this.requestClient.getServiceEndpoint();
    }

    @Override
    public void close() {
        this.requestClient.close();
        this.responseClient.close();
        this.ticketClient.close();
    }
}
