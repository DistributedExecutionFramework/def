package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.rest.IClusterResponseService;
import at.enfilo.def.cluster.api.thrift.ClusterResponseService;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import java.util.List;

public class ClusterResponseServiceImpl extends ResponseService
implements IClusterResponseService, ClusterResponseService.Iface {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ClusterResponseServiceImpl.class);

	public ClusterResponseServiceImpl() {
		super(LOGGER);
	}

	public ClusterResponseServiceImpl(ITicketRegistry ticketRegistry) {
		super(LOGGER, ticketRegistry);
	}

	@Override
	public ClusterInfoDTO getClusterInfo(String ticketId) {
		return getResult(ticketId, ClusterInfoDTO.class);
	}

	@Override
	public List<String> getAllNodes(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public NodeInfoDTO getNodeInfo(String ticketId) {
		return getResult(ticketId, NodeInfoDTO.class);
	}

	@Override
	public ServiceEndpointDTO getNodeServiceEndpoint(String ticketId) {
		return getResult(ticketId, ServiceEndpointDTO.class);
	}

	@Override
	public ServiceEndpointDTO getSchedulerServiceEndpoint(String ticketId) {
		return getResult(ticketId, ServiceEndpointDTO.class);
	}

	@Override
	public String getStoreRoutine(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public String getDefaultMapRoutine(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public ServiceEndpointDTO getNodeServiceEndpointConfiguration(String ticketId) {
		return getResult(ticketId, ServiceEndpointDTO.class);
	}

	@Override
	public ServiceEndpointDTO getLibraryEndpointConfiguration(String ticketId) {
		return getResult(ticketId, ServiceEndpointDTO.class);
	}

	@Override
	public List<String> findNodesForShutdown(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public List<FeatureDTO> getEnvironment(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public List<FeatureDTO> getNodeEnvironment(String ticketId) {
		return getResult(ticketId, List.class);
	}
}
