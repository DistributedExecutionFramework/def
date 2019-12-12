include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.scheduler.api.thrift

/**
* Scheduler Service API.
*/
service SchedulerService {

    /**
     * Adds a new job to Scheduling-strategy.
     * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
     **/
    DTOs.Id addJob(1: DTOs.Id jId);

    /**
    * Extends a job to a reduce job.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.Id extendToReduceJob(1: DTOs.Id jId, 2: DTOs.Id reduceRoutineId);

    /**
     * Schedule a given task.
     * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
     **/
    DTOs.TicketId scheduleTask(1: DTOs.Id jId, 2: DTOs.TaskDTO task);

    /**
     * Mark the given Job (jId) as complete. This means all Tasks are created (scheduled).
     * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
     **/
    DTOs.TicketId markJobAsComplete(1: DTOs.Id jId);

    /**
     * Remove Job (jId) from Scheduling-strategy. If Job is running, Job will be aborted, including all Tasks on Workers.
     * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
     **/
    DTOs.TicketId removeJob(1: DTOs.Id jId);

    /**
     * Adds a new worker node to Scheduling-strategy.
     * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
     **/
    DTOs.TicketId addWorker(1: DTOs.Id nId, 2: CommunicationDTOs.ServiceEndpointDTO serviceEndpoint);

    /**
     * Remove given worker node from Scheduling-strategy.
     * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
     **/
    DTOs.TicketId removeWorker(1: DTOs.Id nId);

    /**
     * Adds a new worker node to Scheduling-strategy.
     * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
     **/
    DTOs.TicketId addReducer(1: DTOs.Id nId, 2: CommunicationDTOs.ServiceEndpointDTO serviceEndpoint);

    /**
     * Remove given worker node from Scheduling-strategy.
     * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
     **/
    DTOs.TicketId removeReducer(1: DTOs.Id nId);

    /**
    * Schedule a given resource to a reducer.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId scheduleReduce(1: DTOs.Id jId, 2: list<DTOs.ResourceDTO> resources);

    /**
    * Finalize reduce for given job and collect all reduced results.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId finalizeReduce(1: DTOs.Id jId);
}

/**
* Scheduler Response Service API.
*/
service SchedulerResponseService {

    /**
     * Returns next node as ServiceEndpointDTO.
     **/
    list<DTOs.ResourceDTO> finalizeReduce(1: DTOs.TicketId ticketId);
}