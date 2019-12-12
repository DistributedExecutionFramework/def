include "../transfer/DTOs.thrift"
include "CloudCommunicationDTOs.thrift"

namespace java at.enfilo.def.cloud.communication.api.thrift
namespace py def_api.thrift.cloud.communication

service CloudCommunicationService {

    /**
    * Creates a new cloud cluster object that communicates with the AWS environment including some AWS specific actions
    * for setting up the specific AWS environment.
    * Returns a ticket id, state of ticket is available over TicketSerivce interface, real result over Response interface.
    **/
    DTOs.TicketId createAWSCluster(1: CloudCommunicationDTOs.AWSSpecificationDTO specification);

    /**
    * Boots a DEF cluster instance in the specific cloud environment.
    * Returns a ticket id, state of ticket is available over TicketSerivce interface, real result over Response interface.
    **/
    DTOs.TicketId bootClusterInstance(1: DTOs.Id cloudClusterId);

    /**
    * Boots a given number of DEF nodes (worker or reducers) in the specific cloud environment.
    * Returns a ticket id, state of ticket is available over TicketSerivce interface, real result over Response interface.
    **/
    DTOs.TicketId bootNodes(1: DTOs.Id cloudClusterId, 2: CloudCommunicationDTOs.InstanceTypeDTO instanceType, 3: i32 nrOfNodes);

    /**
    * Terminates the DEF nodes with the given IDs in the specific cloud environment.
    * Returns a ticket id, state of ticket is available over TicketSerivce interface, real result over Response interface.
    **/
    DTOs.TicketId terminateNodes(1: DTOs.Id cloudClusterId, 2: list<DTOs.Id> cloudInstanceIds);

    /**
    * Returns the public IP address of a DEF instance in the specific cloud environment.
    * Returns a ticket id, state of ticket is available over TicketSerivce interface, real result over Response interface.
    **/
    DTOs.TicketId getPublicIPAddressOfCloudInstance(1: DTOs.Id cloudClusterId, 2: DTOs.Id cloudInstanceId);

    /**
    * Returns the private IP address of a DEF instance in the specific cloud environment.
    * Returns a ticket id, state of ticket is available over TicketSerivce interface, real result over Response interface.
    **/
    DTOs.TicketId getPrivateIPAddressOfCloudInstance(1: DTOs.Id cloudClusterId, 2: DTOs.Id cloudInstanceId);

    /**
    * Shuts down the cloud cluster with the given ID by terminating all DEF nodes and cluster instances in the specific
    * cloud environment. Executes also some cloud specific actions for clearing the specific cloud environment.
    * Returns a ticket id, state of ticket is available over TicketSerivce interface.
    **/
    DTOs.TicketId shutdownCloudCluster(1: DTOs.Id cloudClusterId);

    /**
    * Maps an ID of a cluster or node instance to an ID of a cloud instance.
    * Returns a ticket id, state of the ticket is availabe over TicketService interface.
    **/
    DTOs.TicketId mapDEFIdToCloudInstanceId(1: DTOs.Id cloudClusterId, 2: DTOs.Id defId, 3: DTOs.Id cloudInstanceId);
}

service CloudCommunicationResponseService {

    /**
    * Returns the id of the created AWS cluster object by the given ticket.
    **/
    DTOs.Id createAWSCluster(1: DTOs.TicketId ticketId);

    /**
    * Returns the id of the booted DEF cluster instance by the given ticket.
    **/
    DTOs.Id bootClusterInstance(1: DTOs.TicketId ticketId);

    /**
    * Returns the ids of the booted DEF node instances by the given ticket.
    **/
    list<DTOs.Id> bootNodes(1: DTOs.TicketId ticketId);

    /**
    * Returns the public IP address requested by the given ticket.
    **/
    string getPublicIPAddressOfCloudInstance(1: DTOs.TicketId ticketId);

    /**
    * Returns the private IP address requested by the given ticket.
    **/
    string getPrivateIPAddressOfCloudInstance(2: DTOs.TicketId ticketId);
}