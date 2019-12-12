package at.enfilo.def.manager.impl;

import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.manager.api.rest.IManagerResponseService;
import at.enfilo.def.manager.api.thrift.ManagerResponseService;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;

import java.util.List;

public class ManagerResponseServiceImpl extends ResponseService
implements IManagerResponseService, ManagerResponseService.Iface {

	private final static IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ManagerResponseServiceImpl.class);

	public ManagerResponseServiceImpl() {
		this(TicketRegistry.getInstance());
	}

	public ManagerResponseServiceImpl(ITicketRegistry ticketRegistry) {
		super(LOGGER, ticketRegistry);
	}

	@Override
	public String createAWSCluster(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public ClusterInfoDTO getClusterInfo(String ticketId) {
		return getResult(ticketId, ClusterInfoDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getClusterIds(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public ServiceEndpointDTO getClusterEndpoint(String ticketId) {
		return getResult(ticketId, ServiceEndpointDTO.class);
	}
}
