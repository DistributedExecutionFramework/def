include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.scheduler.clientroutineworker.api.thrift

/**
* ClientRoutineWorkerScheduler service API
**/
service ClientRoutineWorkerSchedulerService {

    /**
     * Adds a client routine worker with the given id to scheduling strategy.
     * Returns a ticket id, state of ticket is available over TicketService interface.
     **/
    DTOs.TicketId addClientRoutineWorker(1: DTOs.Id wId, 2: CommunicationDTOs.ServiceEndpointDTO serviceEndpoint);

    /**
    * Removes a client routine worker with the given id from scheduling strategy.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId removeClientRoutineWorker(1: DTOs.Id wId);

    /**
    * Add a new user to scheduling strategy.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId addUser(1: DTOs.Id uId);

    /**
    * Remove a user with given id from scheduling strategy.
    * Returns a ticket id, state of ticket is available over TicketSerivce interface.
    **/
    DTOs.TicketId removeUser(1: DTOs.Id uId);

    /**
    * Abort a given program, including all reduce jobs on reducers and tasks on workers.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId abortProgram(1: DTOs.Id wId, 2: DTOs.Id pId);

    /**
    * Schedule a given program.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId scheduleProgram(1: DTOs.Id uId, 2: DTOs.ProgramDTO program);
}

service ClientRoutineWorkerSchedulerResponseService {

}