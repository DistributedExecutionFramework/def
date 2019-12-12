include "ReduceDTOs.thrift"
include "../node-api/NodeService.thrift"
include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.reducer.api.thrift

/**
* Reducer service to reduce routine execution results into a compact representation.
*/
service ReducerService extends NodeService.NodeService {

    /**
    * Create a new ReduceJob with given jId (reduce job id).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createReduceJob(1: DTOs.Id jId, 2: DTOs.Id routineId);

    /**
    * Delete and abort a ReduceJob with given jId (reduce job id).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId deleteReduceJob(1: DTOs.Id jId);

    /**
    * Add resources (to reduce) to a reduce job.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId add(1: DTOs.Id jId, 2: list<DTOs.ResourceDTO> resources);

    /**
    * Reduce the given job: all added tasks will be reduced.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId reduce(1: DTOs.Id jId);

    /**
    * Fetch reduced result.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId fetchResult(1: DTOs.Id jId);
}

/**
* Reducer Service Response Interface.
* This interfaces responses to the existing tickets.
*/
service ReducerResponseService extends NodeService.NodeResponseService {

    /**
    * Fetch/Returns reduced result as a ResourceDTO.
    **/
    list<DTOs.ResourceDTO> fetchResult(1: DTOs.TicketId ticketId);
}
