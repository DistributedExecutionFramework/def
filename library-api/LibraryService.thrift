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
    * Request a Routine by id.
    */
    DTOs.TicketId getRoutineRequiredFeatures(1: DTOs.Id rId)


    /**
    * Sets the data endpoint of the library for pulling routines
    **/
    DTOs.TicketId setDataEndpoint(1: CommunicationDTOs.ServiceEndpointDTO serviceEndPoint)
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
    * Returns RoutineDTO for the given ticket.
    **/
    list<DTOs.FeatureDTO> getRoutineRequiredFeatures(1: DTOs.TicketId ticketId)

    /**
    * Returns RoutineDTO for the given ticket.
    **/
    DTOs.RoutineBinaryDTO getRoutineBinary(1: DTOs.TicketId ticketId)
}
