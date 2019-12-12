include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.node.api.thrift

/**
* Node Base Service interface, which provides base functions for every node (worker or reducer)
*/
service NodeService {

    /**
     * Cluster (DEF ClusterModule) take control over this node.
     * This method can only be called one time.
     *
     * @param cId - id of cluster
     */
    DTOs.TicketId takeControl(1: DTOs.Id clusterId);

    /**
     * Request info about node.
     */
    DTOs.TicketId getInfo();

    /**
    * Request info about node environment.
    */
    DTOs.TicketId getEnvironment();

    /**
    * Request info about node environment.
    */
    DTOs.TicketId getFeatures();

    /**
     * Shutdown node service.
     */
    DTOs.TicketId shutdown();

    /**
     * Register an Observer (node-observer-api / service) on this node.
     * Notification on every state change: received tasks, finished tasks,
     * Also possible periodically notifications (load information).
     */
    DTOs.TicketId registerObserver(
        1: CommunicationDTOs.ServiceEndpointDTO endpoint,
        2: bool checkPeriodically,
        3: i64 periodDuration,
        4: DTOs.PeriodUnit periodUnit
    );

    /**
     * De-Register observer with given endpoint (observer endpoint).
     */
    DTOs.TicketId deregisterObserver(1: CommunicationDTOs.ServiceEndpointDTO endpoint);

    /**
    * Adds a new SharedResorce to this node.
    */
    DTOs.TicketId addSharedResource(1: DTOs.ResourceDTO sharedResource);

    /**
    * Remove SharedResources from this node.
    */
    DTOs.TicketId removeSharedResources(1: list<DTOs.Id> rIds);
}

/**
* Node Observer Service interface.
* A node notifies all registered observers, which must implement this interface.
*/
service NodeResponseService {

    /**
     * Request info about node.
     */
    DTOs.NodeInfoDTO getInfo(1: DTOs.TicketId ticketId);

    /**
     * Request info about node environment.
     */
    DTOs.NodeEnvironmentDTO getEnvironment(1: DTOs.TicketId ticketId);

    /**
    * Request info about node environment.
    */
    list<DTOs.FeatureDTO> getFeatures(1: DTOs.TicketId ticketId);
}