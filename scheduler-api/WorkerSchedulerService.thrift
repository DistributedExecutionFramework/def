include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.scheduler.worker.api.thrift

/**
* WorkerScheduler service API
**/
service WorkerSchedulerService {

    /**
    * Adds a worker with the given id to Scheduling-strategy.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.Id addWorker(1: DTOs.Id wId, 2: CommunicationDTOs.ServiceEndpointDTO serviceEndpoint);

    /**
    * Removes worker with the given id from Scheduling-strategy.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.Id removeWorker(1: DTOs.Id wId);

    /**
    * Adds a new job to scheduling strategy.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId addJob(1: DTOs.Id jId);

    /**
    * Remove job with id from scheduling strategy. If job is running, it will be aborted, including all tasks on workers.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId removeJob(1: DTOs.Id jId);

    /**
    * Mark the job with the given id as complete. This means all tasks are created and scheduled.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId markJobAsComplete(1: DTOs.Id jId);

    /**
    * Schedule a given task.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId scheduleTask(1: DTOs.Id jId, 2: DTOs.TaskDTO task);

    /**
    * Abort a given task on a given worker.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId abortTask(1: DTOs.Id wId, 2: DTOs.Id tId);
}

/**
* WorkerScheduler response service API
**/
service WorkerSchedulerResponseService {

}