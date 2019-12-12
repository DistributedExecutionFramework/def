include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.library.api.thrift

/**
* Library Service to store and fetch routines.
*/
service LibraryService {
    /**
    * Request Library information.
    */
    DTOs.TicketId getLibraryInfo()

    /**
    * Request a Routine by id.
    */
    DTOs.TicketId getRoutine(1: DTOs.Id rId)

    /**
    * Request a RoutineBinary by id.
    */
    DTOs.TicketId getRoutineBinary(1: DTOs.Id rbId)

    /**
    * Request a RoutineBinary (Chunk) by id.
    */
    DTOs.TicketId getRoutineBinaryChunk(1: DTOs.Id rbId, 2: i16 chunk, 3: i32 chunkSize)

    /**
    * Request a Routine by id.
    */
    DTOs.TicketId getRoutineRequiredFeatures(1: DTOs.Id rId)
}

/**
* Library Service Response interface.
**/
service LibraryResponseService {

    /**
    * Returns LibraryInfoDTO for the given ticket.
    **/
    DTOs.LibraryInfoDTO getLibraryInfo(1: DTOs.TicketId ticketId)

    /**
    * Returns RoutineDTO for the given ticket.
    **/
    DTOs.RoutineDTO getRoutine(1: DTOs.TicketId ticketId)

    /**
    * Returns RoutineBinaryDTO for the given ticket.
    **/
    DTOs.RoutineBinaryDTO getRoutineBinary(1: DTOs.TicketId ticketId)

    /**
    * Returns RoutineBinaryChunkDTO for the given ticket.
    **/
    DTOs.RoutineBinaryChunkDTO getRoutineBinaryChunk(1: DTOs.TicketId ticketId)

    /**
    * Returns FeatureDTO for the given ticket.
    **/
    list<DTOs.FeatureDTO> getRoutineRequiredFeatures(1: DTOs.TicketId ticketId)
}
