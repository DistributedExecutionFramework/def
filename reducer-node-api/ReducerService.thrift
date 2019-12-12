include "../node-api/NodeService.thrift"
include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.reducer.api.thrift

/**
* Reducer service to reduce routine execution results into a compact representation.
*/
service ReducerService extends NodeService.NodeService {
    /******************************/
    /** REDUCER SPECIFIC METHODS **/
    /******************************/

    /**
    * Request all queued JobIDs.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getQueuedJobs(1: DTOs.Id pId);

    /**
    * Create a new ReduceJob with given job.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    */
    DTOs.TicketId createReduceJob(1: DTOs.JobDTO job);

    /**
    * Delete and abort a ReduceJob with given jId (reduce reducejob id).
    * Returns a ticket id, state of ticket is available over TicketService interface.
    */
    DTOs.TicketId abortReduceJob(1: DTOs.Id jId);

    /**
    * Add resources (to reduce) to a reduce reducejob.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    */
    DTOs.TicketId addResourcesToReduce(1: DTOs.Id jId, 2: list<DTOs.ResourceDTO> resources);

    /**
    * Reduce the given reducejob: all added tasks will be reduced.
    * Returns a ticket id, state of ticket is available over TicketService interface.
    **/
    DTOs.TicketId reduceJob(1: DTOs.Id jId);

    /**
    * Fetch reduced results.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId fetchResults(1: DTOs.Id jId);
}

/**
* Reducer Service Response Interface.
* This interfaces responses to the existing tickets.
*/
service ReducerResponseService extends NodeService.NodeResponseService {
    /******************************/
    /** REDUCER SPECIFIC METHODS **/
    /******************************/

    /**
    * Returns all queued JobIDs.
    **/
    list<DTOs.Id> getQueuedJobs(1: DTOs.TicketId ticketId);

    /**
    * Fetch (and remove) the requested reducejob from this node.
    **/
    list<DTOs.ResourceDTO> fetchResults(1: DTOs.TicketId ticketId);
}
