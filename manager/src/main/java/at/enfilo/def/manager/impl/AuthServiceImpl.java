package at.enfilo.def.manager.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.manager.api.rest.IAuthService;
import at.enfilo.def.manager.api.thrift.AuthService;
import at.enfilo.def.transfer.dto.AuthDTO;

public class AuthServiceImpl implements IAuthService, AuthService.Iface {

	private final AuthController controller;
	private final ITicketRegistry ticketRegistry;

	public AuthServiceImpl() {
		this(AuthController.getInstance(), TicketRegistry.getInstance());
	}

    AuthServiceImpl(AuthController controller, ITicketRegistry ticketRegistry) {
        this.controller = controller;
        this.ticketRegistry = ticketRegistry;
    }

	@Override
	public String getToken(String name, String password) {
		ITicket ticket = ticketRegistry.createTicket(AuthDTO.class, () -> controller.getToken(name, password));
		return ticket.getId().toString();
	}
}
