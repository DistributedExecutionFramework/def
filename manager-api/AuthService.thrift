include "../transfer/DTOs.thrift"

namespace java at.enfilo.def.manager.api.thrift

/**
* Auth Service interface
*/
service AuthService {

    /**
    * Authenticates user by name and password.
    * Returns user id and generated token as a response.
    */
    DTOs.TicketId getToken(1: string name, 2: string password);
}

/**
* Auth response service interface
*/
service AuthResponseService {
    DTOs.AuthDTO getToken(1: DTOs.TicketId ticketId);
}
