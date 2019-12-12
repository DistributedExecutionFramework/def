include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.cluster.api.thrift

/**
* Cluster Service Facade for Manager.
*/
service ClusterService {

    /**
    * Manager (DEF Module) take control over this cluster.
    * This method can only be called one time.
    **/
    DTOs.TicketId takeControl(1: DTOs.Id managerId);

    /**
    * Request assembly of ClusterDTO (cluster information).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getClusterInfo();

    /**
    * Destroys (removes / deletes) this Cluster and every assigned Worker.
    */
    void destroyCluster();

    /**
    * Add a new already running node (worker or reducer) instance to this cluster.
    * Worker cannot be under control from another cluster instance.
    **/
    DTOs.TicketId addNode(1: CommunicationDTOs.ServiceEndpointDTO serviceEndpoint, 2: DTOs.NodeType type)

    /**
    * Requests a list of Nodes (nId) managed by this cluster.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getAllNodes(1: DTOs.NodeType type);

    /**
    * Remove the given node from this cluster.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId removeNode(1: DTOs.Id nId);

    /**
    * Request the info about a specific Node (nId). A node could be a Worker or Reducer.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getNodeInfo(1: DTOs.Id wId);

    /**
    * Request the ServiceEndpoint from a specific node (nId). A node could be a Worker or Reducer.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getNodeServiceEndpoint(1: DTOs.Id nId);

    /**
    * Returns ServiceEndpoint of SchedulerService.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getSchedulerServiceEndpoint(1: DTOs.NodeType nodeType);

    /**
    * Set ServiceEndpoint for (new) SchedulerService
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId setSchedulerServiceEndpoint(
        1: DTOs.NodeType nodeType,
        2: CommunicationDTOs.ServiceEndpointDTO schedulerServiceEndpoint
    );

    /**
    * Request the current active StoreRoutine for this Cluster.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getStoreRoutine();

    /**
    * Set new StoreRoutine for this cluster (every worker)
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId setStoreRoutine(1: DTOs.Id routineId);

    /**
    * Request the current default MapRoutine for this Cluster.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getDefaultMapRoutine();

    /**
    * Set default MapRoutine for this cluster (every worker)
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId setDefaultMapRoutine(1: DTOs.Id routineId);

    /**
    *  Returns a ServiceEndpointDTO with the corresponding port and protocol set for the given NodeType.
    *  Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getNodeServiceEndpointConfiguration(1: DTOs.NodeType nodeType);

    /**
    * Returns a ServiceEndpointDTO with the port and protocol set for accessing the library service on the cluster and node instances.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getLibraryEndpointConfiguration();

    /**
    * Finds the given number of nodes of the given NodeType that have the least number of scheduled tasks.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId findNodesForShutdown(1: DTOs.NodeType nodeType, 2: i32 nrOfNodesToShutdown);

    /**
    * Request the info about a specific Node environment (nId). A node could be a Worker or Reducer.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getNodeEnvironment(1: DTOs.Id nId);

    /**
    * Request the info about the cluster environment.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getEnvironment();
}

/**
* Response Interface for ClusterService
**/
service ClusterResponseService {
    /**
    * Returns info about this Cluster.
    * Takes one argument: ticket id.
    */
    DTOs.ClusterInfoDTO getClusterInfo(1: DTOs.TicketId ticketId);

    /**
    * Returns list of Workers (wId) managed by holder cluster.
    * Takes one argument: ticket id.
    */
    list<DTOs.Id> getAllNodes(1: DTOs.TicketId ticketId);

    /**
    * Returns info about Node (nId).
    * Takes one argument: ticket id.
    */
    DTOs.NodeInfoDTO getNodeInfo(1: DTOs.TicketId ticketId);

    /**
    * Returns ServiceEndpoint of requested Worker.
    * Takes one argument: ticket id.
    **/
    CommunicationDTOs.ServiceEndpointDTO getNodeServiceEndpoint(1: DTOs.TicketId ticketId);

    /**
    * Returns ServiceEndpoint of SchedulerService.
    * Takes one argument: ticket id.
    **/
    CommunicationDTOs.ServiceEndpointDTO getSchedulerServiceEndpoint(1: DTOs.TicketId ticketId);

    /**
    * Returns current active StoreRoutine Id.
    **/
    DTOs.Id getStoreRoutine(1: DTOs.TicketId ticketId);

    /**
    * Returns current default MapRoutine Id.
    **/
    DTOs.Id getDefaultMapRoutine(1: DTOs.TicketId ticketId);

    /**
    * Returns the requested node ServiceEndpoint
    **/
    CommunicationDTOs.ServiceEndpointDTO getNodeServiceEndpointConfiguration(1: DTOs.TicketId ticketId);

    /**
    * Returns the requested library ServiceEndpoint
    **/
    CommunicationDTOs.ServiceEndpointDTO getLibraryEndpointConfiguration(1: DTOs.TicketId ticketId);

    /**
    * Returns a list with the ids of the requested number of nodes to shut down.
    **/
    list<DTOs.Id> findNodesForShutdown(1: DTOs.TicketId ticketId);

    /**
    * Returns the info about a specific Node environment (nId). A node could be a Worker or Reducer.
    */
    list<DTOs.FeatureDTO> getNodeEnvironment(1: DTOs.TicketId ticketId);

    /**
    * Returns the info about the cluster environment.
    */
    list<DTOs.FeatureDTO> getEnvironment(1: DTOs.TicketId ticketId);

}
