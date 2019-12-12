include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"
include "../cloud-communication-api/CloudCommunicationDTOs.thrift"

namespace java at.enfilo.def.manager.api.thrift

/**
* Manager specific service interface.
* DTOs logic for creating programs, jobs and tasks will be found in ExecLogicService interface.
*/
service ManagerService {

    /**
    * Requests a list with Cluster ids that were bound to registry.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getClusterIds();

    /**
    * Reqeusts information about the given cluster.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getClusterInfo(1: DTOs.Id cId);

    /**
    * Requests ServiceEnpoint description for the given cluster id.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getClusterEndpoint(1: DTOs.Id cId);

    /**
    * Create a new cluster in the AWS environment with an initial worker and reducer pools size with the given AWS specification.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId createAWSCluster(1: i32 numberOfWorkers, 2: i32 numberOfReducers, 3: CloudCommunicationDTOs.AWSSpecificationDTO awsSpecification);

    /**
    * Adds an existing cluster to this manager.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId addCluster(1: CommunicationDTOs.ServiceEndpointDTO endpoint);

    /**
    * Destroy the given cluster.
    * This means all machines (cluster controller and workers) will be turned off.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId destroyCluster(1: DTOs.Id cId);

    /**
    * Adjusts the pool size of the given NodeType in the cluster with the given id.
    * Returns a ticket id, state of ticket is availabe over TicketService interface.
    **/
    DTOs.TicketId adjustNodePoolSize(1: DTOs.Id cId, 2: i32 newNodePoolSize, 3: DTOs.NodeType nodeType);

    /**
    * Create a new ClientRoutine
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface. 
    **/
    DTOs.TicketId createClientRoutine(1: DTOs.RoutineDTO routine);

    /**
    * Create binary/executable for a ClientRoutine
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId createClientRoutineBinary(
        1: DTOs.Id rId,
        2: string binaryName,
        3: string md5,
        4: i64 sizeInBytes,
        5: bool isPrimary
    );

    /**
    * Uploads a RoutineBinaryChunk to the given RoutineBinary (rbId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId uploadClientRoutineBinaryChunk(1: DTOs.Id rbId, 2: DTOs.RoutineBinaryChunkDTO chunk);

    /**
    * Delete a ClientRoutine.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId removeClientRoutine(1: DTOs.Id rcId);

    /**
    * Fetches a feature by name and version.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getFeatureByNameAndVersion(1: string name, 2: string version);
}

/**
* Manager service response interface.
*/
service ManagerResponseService {

    /**
    * Returns id of created AWS cluster.
    **/
    DTOs.Id createAWSCluster(1: DTOs.TicketId ticketId);

    /**
    * Returns information about the requested cluster.
    */
    DTOs.ClusterInfoDTO getClusterInfo(1: DTOs.TicketId ticketId);

    /**
    * Returns ServiceEndpoint of the requested cluster.
    */
    CommunicationDTOs.ServiceEndpointDTO getClusterEndpoint(1: DTOs.TicketId ticketId);

    /**
    * Returns a list of all registered clusters.
    */
    list<DTOs.Id> getClusterIds(1: DTOs.TicketId ticketId);

    /**
    * Returns Id of the new created ClientRoutine
    **/
    DTOs.Id createClientRoutine(1: DTOs.TicketId ticketId);

    /**
    * Returns Id of the uploaded ClientRoutineBinary
    **/
    DTOs.Id createClientRoutineBinary(1: DTOs.TicketId ticketId);

    /**
    * Returns a feature with a given name and version.
    **/
    DTOs.FeatureDTO getFeatureByNameAndVersion(1: DTOs.TicketId ticketId);
}
