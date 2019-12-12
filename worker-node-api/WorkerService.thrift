include "../transfer/DTOs.thrift"
include "../node-api/NodeService.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.worker.api.thrift

service WorkerService extends NodeService.NodeService {
    /*****************************/
    /** WORKER SPECIFIC METHODS **/
    /*****************************/

    /**
    * Request all queued TaskIDs.
    * Returns a ticketId id, state of ticketId is avail over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getQueuedTasks(1: DTOs.Id qId);

    /**
    * Queue a list of tasks.
    * Returns a ticketId id, state of ticketId is avail over TicketService interface.
    */
    DTOs.TicketId queueTasks(1: DTOs.Id qId, 2: list<DTOs.TaskDTO> taskList);

    /**
    * Move a list of tasks to another node.
    * Returns a ticketId id, state of ticketId is avail over TicketService interface.
    */
    DTOs.TicketId moveTasks(
        1: DTOs.Id qId,
        2: list<DTOs.Id> taskIds,
        3: CommunicationDTOs.ServiceEndpointDTO targetNodeEndpoint
    );

    /**
    * Move all tasks to another node.
    * Returns a ticketId id, state of ticketId is avail over TicketService interface.
    **/
    DTOs.TicketId moveAllTasks(1: CommunicationDTOs.ServiceEndpointDTO targetNodeEndpoint);

    /**
    * Fetch (and removes) a finished task from this node.
    * Returns a ticketId id, state of ticketId is avail over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId fetchFinishedTask(1: DTOs.Id tId);

    /**
    * Abort the given task.
    */
    DTOs.TicketId abortTask(1: DTOs.Id tId);
}

/**
* Worker Service Response Interface.
* This interfaces responses to the existing tickets.
*/
service WorkerResponseService extends NodeService.NodeResponseService {
    /*****************************/
    /** WORKER SPECIFIC METHODS **/
    /*****************************/

    /**
    * Returns all queued TaskIDs.
    */
    list<DTOs.Id> getQueuedTasks(1: DTOs.TicketId ticketId);

    /**
    * Fetch (and remove) the requested task from this node.
    **/
    DTOs.TaskDTO fetchFinishedTask(1: DTOs.TicketId ticketId);
}
