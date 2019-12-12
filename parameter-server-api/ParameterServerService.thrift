include "../transfer/DTOs.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.parameterserver.api.thrift

/**
* Parameter Server Service API.
*/
service ParameterServerService {

    /**
    * Set the value for a parameter with the program id and parameter id given.
    **/
    DTOs.TicketId setParameter(1: DTOs.Id programId, 2: DTOs.Id parameterId, 3: DTOs.ResourceDTO parameter, 4: DTOs.ParameterProtocol protocol);

    /**
    * Creates a new parameter with the given program id, parameter id and type.
    **/
    DTOs.TicketId createParameter(1: DTOs.Id programId, 2: DTOs.Id parameterId, 3: DTOs.ResourceDTO parameter, 4: DTOs.ParameterProtocol protocol, 5: DTOs.ParameterType type);

    /**
    * Request a parameter with the given program id and parameter id.
    */
    DTOs.TicketId getParameter(1: DTOs.Id programId, 2: DTOs.Id parameterId, 3: DTOs.ParameterProtocol protocol);

    /**
    * Update a stored parameter with the given program id and parameter id by adding the value in the given parameter.
    */
    DTOs.TicketId addToParameter(1: DTOs.Id programId, 2: DTOs.Id parameterId, 3: DTOs.ResourceDTO parameter, 4: DTOs.ParameterProtocol protocol);

    /**
    * Delete a parameter with the given program id and parameter id
    **/
    DTOs.TicketId deleteParameter(1: DTOs.Id programId, 2: DTOs.Id parameterId);

    /**
    * Delete all stored parameters for the given programId
    **/
    DTOs.TicketId deleteAllParameters(1: DTOs.Id programId);
}

/**
* Response Interface for ParameterServerService
**/
service ParameterServerResponseService {

     /**
     * Set the value for a parameter with the program id and parameter id given.
     **/
     DTOs.Id setParameter(1: DTOs.TicketId ticketId);

     /**
     * Creates a new parameter with the given program id, parameter id and type.
     **/
     DTOs.Id createParameter(1: DTOs.TicketId ticketId);

     /**
     * Request a parameter with the given program id and parameter id.
     */
     DTOs.ResourceDTO getParameter(1: DTOs.TicketId ticketId);

     /**
     * Update a stored parameter with the given program id and parameter id by adding the value in the given parameter.
     */
     DTOs.Id addToParameter(1: DTOs.TicketId ticketId);

     /**
     * Delete a parameter with the given program id and parameter id
     **/
     DTOs.Id deleteParameter(1: DTOs.TicketId ticketId);

     /**
     * Delete all stored parameters for the given programId
     **/
     DTOs.Id deleteAllParameters(1: DTOs.TicketId ticketId);
}