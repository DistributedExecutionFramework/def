package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.api.rest.IClusterService;
import at.enfilo.def.cluster.api.thrift.ClusterService;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClusterServiceImpl implements IClusterService, ClusterService.Iface {

	private final ITicketRegistry ticketRegistry;
	private final WorkerController workerController;
	private final ReducerController reducerController;
	private final ClusterResource clusterResource;
	private final ClusterExecLogicController execLogicController;

	/**
	 * Default Constructor
	 */
	public ClusterServiceImpl() {
		this(
			WorkerController.getInstance(),
			ReducerController.getInstance(),
			ClusterResource.getInstance(),
			ClusterExecLogicController.getInstance()
		);
	}

	/**
	 * Constructor for Unittests to mock internal services.
	 *
	 * @param workerController
	 * @param reducerController
	 * @param clusterResource
	 * @param execLogicController
	 */
	public ClusterServiceImpl(
		WorkerController workerController,
		ReducerController reducerController,
		ClusterResource clusterResource,
		ClusterExecLogicController execLogicController
	) {
		this.ticketRegistry = TicketRegistry.getInstance();
		this.workerController = workerController;
		this.reducerController = reducerController;
		this.clusterResource = clusterResource;
		this.execLogicController = execLogicController;
	}


	/******
	 *
	 * General methods for cluster control
	 *
	 *****/

	@Override
	public String takeControl(String managerId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> clusterResource.takeControl(managerId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getClusterInfo() {
		ITicket ticket = ticketRegistry.createTicket(
				ClusterInfoDTO.class,
				this::getClusterInfoImpl,
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	private ClusterInfoDTO getClusterInfoImpl() {
		ClusterInfoDTO info = new ClusterInfoDTO();
		info.setId(clusterResource.getId());
		info.setManagerId(clusterResource.getManagerId());
		info.setName(clusterResource.getName());
		info.setStartTime(clusterResource.getStartTime().toEpochMilli());
		info.setActivePrograms(
			execLogicController.getAllPrograms()
					.stream()
					.map(Program::getId)
					.collect(Collectors.toList())
		);
		info.setCloudType(clusterResource.getCloudType());
		info.setNumberOfWorkers(workerController.getNodePoolSize());
		info.setNumberOfReducers(reducerController.getNodePoolSize());
		info.setDefaultMapRoutineId(clusterResource.getDefaultMapRoutineId());
		info.setStoreRoutineId(workerController.getStoreRoutineId());
		return info;
	}

	@Override
	public void destroyCluster() {
		ticketRegistry.createTicket(
				ServiceRegistry.getInstance()::closeAll,
				ITicket.SERVICE_PRIORITY
		);
	}


	/*******
	 *
	 * Methods for worker control
	 *
	 *******/

	@Override
	public String getAllNodes(NodeType type) {
		ITicket ticket;
		switch (type) {
			case WORKER:
				ticket = ticketRegistry.createTicket(
						List.class,
						workerController::getAllNodeIds,
						ITicket.SERVICE_PRIORITY
				);
				break;
			case REDUCER:
				ticket = ticketRegistry.createTicket(
						List.class,
						reducerController::getAllNodeIds,
						ITicket.SERVICE_PRIORITY
				);
				break;
			default:
				String msg = String.format("NodeType %s not supported.", type);
				ticket = ticketRegistry.createFailedTicket(msg);
		}
		return ticket.getId().toString();
	}

	@Override
	public String getNodeInfo(String nId) {
		ITicket ticket = ticketRegistry.createTicket(
				NodeInfoDTO.class,
				() -> fetchNodeInfo(nId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	private NodeInfoDTO fetchNodeInfo(String nId) throws UnknownNodeException {
		NodeInfoDTO nodeInfo;
		if (workerController.containsNode(nId)) {
			nodeInfo = workerController.getNodeInfo(nId);
		} else if (reducerController.containsNode(nId)) {
			nodeInfo = reducerController.getNodeInfo(nId);
		} else {
			throw new UnknownNodeException();
		}
		return nodeInfo;
	}

	@Override
	public String addNode(ServiceEndpointDTO serviceEndpoint, NodeType type) {
		ITicket ticket;
		switch (type) {
			case WORKER:
				ticket = ticketRegistry.createTicket(
						() -> workerController.addNode(serviceEndpoint),
						ITicket.SERVICE_PRIORITY
				);
				break;
			case REDUCER:
				ticket = ticketRegistry.createTicket(
						() -> reducerController.addNode(serviceEndpoint),
						ITicket.SERVICE_PRIORITY
				);
				break;
			default:
				String msg = String.format("NodeType %s not supported.", type);
				ticket = ticketRegistry.createFailedTicket(msg);
		}
		return ticket.getId().toString();
	}

	@Override
	public String removeNode(String nId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> removeWorkerOrReducer(nId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	private void removeWorkerOrReducer(String nId) throws UnknownNodeException {
		if (workerController.containsNode(nId)) {
			workerController.removeNode(nId, true);
		} else if (reducerController.containsNode(nId)) {
			reducerController.removeNode(nId, true);
		} else {
			throw new UnknownNodeException();
		}
	}

	@Override
	public String getSchedulerServiceEndpoint(NodeType nodeType) {
		ITicket ticket = ticketRegistry.createTicket(
				ServiceEndpointDTO.class,
				() -> clusterResource.getSchedulerServiceClient(nodeType).getServiceEndpoint(),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String setSchedulerServiceEndpoint(
		NodeType nodeType,
		ServiceEndpointDTO schedulerServiceEndpoint
	) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> {
					clusterResource.setSchedulerService(nodeType, schedulerServiceEndpoint);
					switch (nodeType) {
						case WORKER:
							workerController.addAllNodesToScheduler();
							break;
						case REDUCER:
							reducerController.addAllNodesToScheduler();
							break;
					}
				},
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getStoreRoutine() {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				workerController::getStoreRoutineId,
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String setStoreRoutine(String routineId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> {
					workerController.setStoreRoutineId(routineId);
					// TODO: reducerController
				},
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getDefaultMapRoutine() {
		ITicket ticket = ticketRegistry.createTicket(String.class, clusterResource::getDefaultMapRoutineId);
		return ticket.getId().toString();
	}

	@Override
	public String setDefaultMapRoutine(String routineId) {
		ITicket ticket = ticketRegistry.createTicket(() -> clusterResource.setDefaultMapRoutineId(routineId));
		return ticket.getId().toString();
	}

	@Override
	public String findNodesForShutdown(NodeType nodeType, int nrOfNodesToShutdown) {
		ITicket ticket;
		switch (nodeType) {
			case WORKER:
					ticket = ticketRegistry.createTicket(
							List.class,
							() -> workerController.findNodesForShutdown(nrOfNodesToShutdown),
							ITicket.SERVICE_PRIORITY
					);
				break;
			case REDUCER:
					ticket = ticketRegistry.createTicket(
							List.class,
							() -> reducerController.findNodesForShutdown(nrOfNodesToShutdown),
							ITicket.SERVICE_PRIORITY
					);
				break;
			default:
				String msg = String.format("NodeType %s not supported.", nodeType);
				ticket = ticketRegistry.createFailedTicket(msg);
		}
		return ticket.getId().toString();
	}

	@Override
	public String getNodeServiceEndpointConfiguration(NodeType nodeType) {
		ITicket ticket = ticketRegistry.createTicket(ServiceEndpointDTO.class, () -> fetchNodeServiceEndpointConfiguration(nodeType));
		return ticket.getId().toString();
	}

	@Override
	public String getLibraryEndpointConfiguration() {
		ITicket ticket = ticketRegistry.createTicket(ServiceEndpointDTO.class, this::fetchLibraryEndpointConfiguration);
		return ticket.getId().toString();
	}

	@Override
	public String getEnvironment() {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				this::fetchEnvironment,
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getNodeEnvironment(String nId) {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				() -> fetchNodeEnvironment(nId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	private List<FeatureDTO> fetchEnvironment() {
		List<String> nIds = new ArrayList<>();
		if (workerController != null) {
			nIds.addAll(workerController.getAllNodeIds());
		}
		if (reducerController != null) {
			nIds.addAll(reducerController.getAllNodeIds());
		}

		List<FeatureDTO> features = new ArrayList<>();
		for(String nId : nIds) {
			try {
				features.addAll(fetchNodeEnvironment(nId));
			} catch (UnknownNodeException e) {
				//Only occurs if there has been a problem removing old nodes or there was a
				//concurrency issue when nodes are removed in exactly the same moment as the
				//cluster environment is requested, in which case we can ignore the exception.
			}
		}
		return features;
	}

	private List<FeatureDTO> fetchNodeEnvironment(String nId) throws UnknownNodeException {
		List<FeatureDTO> environment;
		if (workerController.containsNode(nId)) {
			environment = workerController.getNodeEnvironment(nId);
		} else if (reducerController.containsNode(nId)) {
			environment = reducerController.getNodeEnvironment(nId);
		} else {
			throw new UnknownNodeException();
		}
		return environment;
	}

	private ServiceEndpointDTO fetchNodeServiceEndpointConfiguration(NodeType nodeType) {
		switch (nodeType) {
			case WORKER:
				return Cluster.getInstance().getConfiguration().getWorkersConfiguration().getNodeServiceEndpoint();
			case REDUCER:
				return Cluster.getInstance().getConfiguration().getReducersConfiguration().getNodeServiceEndpoint();
			default:
				return null;
		}
	}

	private ServiceEndpointDTO fetchLibraryEndpointConfiguration() {
		return Cluster.getInstance().getConfiguration().getLibraryEndpoint();
	}

	@Override
	public String getNodeServiceEndpoint(String nId) {
		ITicket ticket = ticketRegistry.createTicket(ServiceEndpointDTO.class, () -> fetchNodeServiceEndpoint(nId));
		return ticket.getId().toString();
	}

	private ServiceEndpointDTO fetchNodeServiceEndpoint(String nId) throws UnknownNodeException {
		ServiceEndpointDTO serviceEndpoint;
		if (workerController.containsNode(nId)) {
			serviceEndpoint = workerController.getNodeServiceEndpoint(nId);
		} else if (reducerController.containsNode(nId)) {
			serviceEndpoint = reducerController.getNodeServiceEndpoint(nId);
		} else {
			throw new UnknownNodeException();
		}
		return serviceEndpoint;

	}
}
