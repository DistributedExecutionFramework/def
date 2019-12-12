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

    /**
    * Request a list of active queues (id).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getQueueIds();

    /**
    * Request information of the queue with the given id.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getQueueInfo(1: DTOs.Id qId);

    /**
    * Create a new queue with the given id.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId createQueue(1: DTOs.Id qId);

    /**
    * Pause operation of elements in the queue with the given id.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId pauseQueue(1: DTOs.Id qId);

    /**
    * Delete the queue with the given id. Aborts the execution of queue elements.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId deleteQueue(1: DTOs.Id qId);

    /**
    * Release (start) a queue. All elements in this queue will be executed.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId releaseQueue(1: DTOs.Id qId);

    /**
    * Request the current active store routine for this node.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getStoreRoutine();

    /**
    * Set new store routine for this node.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId setStoreRoutine(1: DTOs.Id routineId);
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
    * Returns a list of active queues (id).
    **/
    list<DTOs.Id> getQueueIds(1: DTOs.TicketId ticketId);

    /**
    * Returns information of the requested queue.
    **/
    DTOs.QueueInfoDTO getQueueInfo(1: DTOs.TicketId ticketId);

    /**
    * Returns current active store routine id.
    **/
    DTOs.Id getStoreRoutine(1: DTOs.TicketId ticketId);

    /**
     * Request info about node environment.
     */
    DTOs.NodeEnvironmentDTO getEnvironment(1: DTOs.TicketId ticketId);

    /**
    * Request info about node environment.
    */
    list<DTOs.FeatureDTO> getFeatures(1: DTOs.TicketId ticketId);
}