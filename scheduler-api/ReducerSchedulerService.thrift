include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.scheduler.reducer.api.thrift

/**
* ReducerScheduler Service API
**/
service ReducerSchedulerService {

    /**
    * Adds a reducer with the given id to Scheduling-strategy.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.Id addReducer(1: DTOs.Id rId, 2: CommunicationDTOs.ServiceEndpointDTO serviceEndpoint);

    /**
    * Removes reducer with the given id from Scheduling-strategy.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.Id removeReducer(1: DTOs.Id rId);

    /**
    * Extends a job to a reduce job.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId addReduceJob(1: DTOs.JobDTO job);

    /**
    * Remove job with id from scheduling strategy. If job is running, it will be aborted, including all tasks on workers.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId removeReduceJob(1: DTOs.Id jId);

    /**
    * Schedule a given resource to a reducer.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId scheduleResourcesToReduce(1: DTOs.Id jId, 2: list<DTOs.ResourceDTO> resources);

    /**
    * Finalize reduce for given job and collect all reduced results.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId finalizeReduce(1: DTOs.Id jId);
}

/**
* ReducerScheduler Response Service API.
**/
service ReducerSchedulerResponseService {

    /**
    * Returns next node as ServiceEndpointDTO.
    **/
    DTOs.JobDTO finalizeReduce(1: DTOs.TicketId ticketId);
}