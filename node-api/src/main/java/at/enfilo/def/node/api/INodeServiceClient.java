package at.enfilo.def.node.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.*;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Basic node client interface.
 */
public interface INodeServiceClient extends IServiceClient {

    /**
     * Cluster (DEF ClusterModule) take control over this node.
     * This method can only be called one time.
     *
     * @param clusterId - cluster id
     */
    Future<Void> takeControl(String clusterId) throws ClientCommunicationException;

    /**
     * Request info about node.
     */
    Future<NodeInfoDTO> getInfo() throws ClientCommunicationException;

    /**
     * Request node environment.
     */
    Future<NodeEnvironmentDTO> getEnvironment() throws ClientCommunicationException;

    /**
     * Request node environment.
     */
    Future<List<FeatureDTO>> getFeatures() throws ClientCommunicationException;

    /**
     * Register an Observer (node-observer-api / service) on this node.
     * Notification on every state change: received tasks, finished tasks,
     * Also possible periodically notifications (load information)
     *
     * @param endpoint - endpoint of observer
     * @param checkPeriodically - true or false
     * @param periodDuration - duration
     * @param periodUnit - unit of duration
     */
    Future<Void> registerObserver(
        ServiceEndpointDTO endpoint,
        boolean checkPeriodically,
        int periodDuration,
        PeriodUnit periodUnit
    ) throws ClientCommunicationException;

    /**
     * De-Register observer with given endpoint.
     * @param endpoint - observer endpoint
     * @return ticket state as future
     */
    Future<Void> deregisterObserver(ServiceEndpointDTO endpoint) throws ClientCommunicationException;

	/**
	 * Add a new shared resource to this node.
	 * @param sharedResource - shared resource to add.
	 * @return ticket state as future
	 */
    Future<Void> addSharedResource(ResourceDTO sharedResource) throws ClientCommunicationException;

	/**
	 * Removes a given shared resource from this node.
	 * @param rIds - shared resource ids to remove.
	 * @return ticket state as future
	 */
    Future<Void> removeSharedResources(List<String> rIds) throws ClientCommunicationException;

    /**
     * Shutdown node service.
     */
    void shutdown() throws ClientCommunicationException;

}
