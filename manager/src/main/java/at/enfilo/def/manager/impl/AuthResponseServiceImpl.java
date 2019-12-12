package at.enfilo.def.manager.impl;

import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.manager.api.rest.IAuthResponseService;
import at.enfilo.def.manager.api.thrift.AuthResponseService;
import at.enfilo.def.transfer.dto.AuthDTO;

public class AuthResponseServiceImpl extends ResponseService implements IAuthResponseService, AuthResponseService.Iface {

	private final static IDEFLogger LOGGER = DEFLoggerFactory.getLogger(AuthResponseServiceImpl.class);

	public AuthResponseServiceImpl() {
		this(TicketRegistry.getInstance());
	}

	public AuthResponseServiceImpl(ITicketRegistry ticketRegistry) {
		super(LOGGER, ticketRegistry);
	}

	@Override
	public AuthDTO getToken(String ticketId) {
		return getResult(ticketId, AuthDTO.class);
	}
}
