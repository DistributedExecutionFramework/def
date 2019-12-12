include "../transfer/DTOs.thrift"
include "../node-api/NodeService.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.clientroutine.worker.api.thrift

service ClientRoutineWorkerService extends NodeService.NodeService {
    /*************************************/
    /** CLIENT ROUTINE SPECIFIC METHODS **/
    /*************************************/

    /**
    * Requests all queued programs with client routines.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getQueuedPrograms(1: DTOs.Id qId);

    /**
    * Queues a program with a client routine.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId queueProgram(1: DTOs.Id qId, 2: DTOs.ProgramDTO program);

    /**
    * Queues a list of programs with a client routine.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId queuePrograms(1: DTOs.Id qId, 2: list<DTOs.ProgramDTO> programs);

    /**
    * Moves a list of programs to another node.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId movePrograms(
        1: DTOs.Id qId,
        2: list<DTOs.Id> programIds,
        3: CommunicationDTOs.ServiceEndpointDTO targetNodeEndpoint
    );

    /**
    * Moves all programs to another node.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId moveAllPrograms(1: CommunicationDTOs.ServiceEndpointDTO targetNodeEnpoint);

    /**
    * Fetches and removes a finished program with a client routine from this node.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId fetchFinishedProgram(1: DTOs.Id pId);

    /**
    * Aborts the program with the given id.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId abortProgram(1: DTOs.Id pId);
}

/**
* ClientRoutine Response Service Interface.
* This interface responses to the existing tickets.
**/
service ClientRoutineWorkerResponseService extends NodeService.NodeResponseService {
    /*************************************/
    /** CLIENT ROUTINE SPECIFIC METHODS **/
    /*************************************/

    /**
    * Returns all queued client routine IDs.
    **/
    list<DTOs.Id> getQueuedPrograms(1: DTOs.TicketId ticketId);

    /**
    * Fetches and removes the requested client routine from this node.
    **/
    DTOs.ProgramDTO fetchFinishedProgram(1: DTOs.TicketId ticketId);
}