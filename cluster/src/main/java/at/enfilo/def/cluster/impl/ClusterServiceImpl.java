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
import at.enfilo.def.routine.WrongTypeException;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClusterServiceImpl implements IClusterService, ClusterService.Iface {

	private final ITicketRegistry ticketRegistry;
	private final WorkerController workerController;
	private final ReducerController reducerController;
	private final ClientRoutineWorkerController clientRoutineWorkerController;
	private final ClusterResource clusterResource;
	private final ClusterExecLogicController execLogicController;

	/**
	 * Default Constructor
	 */
	public ClusterServiceImpl() {
		this(
			WorkerController.getInstance(),
			ReducerController.getInstance(),
			ClientRoutineWorkerController.getInstance(),
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
		ClientRoutineWorkerController clientRoutineWorkerController,
		ClusterResource clusterResource,
		ClusterExecLogicController execLogicController
	) {
		this.ticketRegistry = TicketRegistry.getInstance();
		this.workerController = workerController;
		this.reducerController = reducerController;
		this.clientRoutineWorkerController = clientRoutineWorkerController;
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
					.map(ProgramDTO::getId)
					.collect(Collectors.toList())
		);
		info.setCloudType(clusterResource.getCloudType());
		info.setNumberOfWorkers(workerController.getNodePoolSize());
		info.setNumberOfReducers(reducerController.getNodePoolSize());
		info.setNumberOfClientRoutineWorkers(clientRoutineWorkerController.getNodePoolSize());
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
		ITicket ticket = ticketRegistry.createTicket(
			List.class,
			() -> {
				switch (type) {
					case WORKER:
						return workerController.getAllNodeIds();
					case REDUCER:
						return reducerController.getAllNodeIds();
					case CLIENT:
						return clientRoutineWorkerController.getAllNodeIds();
					default:
						String msg = String.format("NodeType %s not supported.", type);
						throw new WrongTypeException(msg);
				}
			},
			ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getNodeInfo(String nId) {
		ITicket ticket = ticketRegistry.createTicket(
			NodeInfoDTO.class,
			() -> {
				if (workerController.containsNode(nId)) {
					return workerController.getNodeInfo(nId);
				} else if (reducerController.containsNode(nId)) {
					return reducerController.getNodeInfo(nId);
				} else if (clientRoutineWorkerController.containsNode(nId)) {
					return clientRoutineWorkerController.getNodeInfo(nId);
				}
				String msg = String.format("Node with id %s is not known.", nId);
				throw new UnknownNodeException(msg);
			},
			ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String addNode(ServiceEndpointDTO serviceEndpoint, NodeType type) {
		ITicket ticket = ticketRegistry.createTicket(
			String.class,
			() -> {
				switch (type) {
					case WORKER:
						return workerController.addWorker(serviceEndpoint);
					case REDUCER:
						return reducerController.addReducer(serviceEndpoint);
					case CLIENT:
						return clientRoutineWorkerController.addClientRoutineWorker(serviceEndpoint);
					default:
						String msg = String.format("NodeType %s is not supported.", type);
						throw new WrongTypeException(msg);
				}
			},
			ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String removeNode(String nId) {
		ITicket ticket = ticketRegistry.createTicket(
			() -> {
				if (workerController.containsNode(nId)) {
					workerController.removeNode(nId);
				} else if (reducerController.containsNode(nId)) {
					reducerController.removeNode(nId);
				} else if (clientRoutineWorkerController.containsNode(nId)) {
					clientRoutineWorkerController.removeNode(nId);
				} else {
					String msg = String.format("Node with id %s is not known.", nId);
					throw new UnknownNodeException(msg);
				}
			},
			ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getSchedulerServiceEndpoint(NodeType nodeType) {
		ITicket ticket = ticketRegistry.createTicket(
			ServiceEndpointDTO.class,
			() -> {
				switch (nodeType) {
					case WORKER:
						return clusterResource.getWorkerSchedulerServiceClient().getServiceEndpoint();
					case REDUCER:
						return clusterResource.getReducerSchedulerServiceClient().getServiceEndpoint();
					case CLIENT:
						return clusterResource.getClientRoutineWorkerSchedulerSerivceClient().getServiceEndpoint();
					default:
						String msg = String.format("NodeType %s is not known.", nodeType);
						throw new WrongTypeException(msg);
				}
			},
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
					case CLIENT:
						clientRoutineWorkerController.addAllNodesToScheduler();
						break;
				}
			},
			ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getStoreRoutine(NodeType nodeType) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> {
					switch (nodeType) {
						case WORKER:
							return workerController.getStoreRoutineId();
						case REDUCER:
							return reducerController.getStoreRoutineId();
						case CLIENT:
							return clientRoutineWorkerController.getStoreRoutineId();
						default:
							String msg = String.format("NodeType %s is not supported.", nodeType);
							throw new WrongTypeException(msg);
					}
				},
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String setStoreRoutine(String routineId, NodeType nodeType) {
		ITicket ticket = ticketRegistry.createTicket(
			() -> {
				switch (nodeType) {
					case WORKER:
						workerController.setStoreRoutineId(routineId);
						break;
					case REDUCER:
						reducerController.setStoreRoutineId(routineId);
						break;
					case CLIENT:
						clientRoutineWorkerController.setStoreRoutineId(routineId);
						break;
					default:
						String msg = String.format("NodeType %s is not supported.", nodeType);
						throw new WrongTypeException(msg);
				}
			},
			ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getDefaultMapRoutine() {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				clusterResource::getDefaultMapRoutineId,
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String setDefaultMapRoutine(String routineId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> clusterResource.setDefaultMapRoutineId(routineId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String findNodesForShutdown(NodeType nodeType, int nrOfNodesToShutdown) {
		ITicket ticket = ticketRegistry.createTicket(
			List.class,
			() -> {
				switch (nodeType) {
					case WORKER:
						return workerController.findNodesForShutdown(nrOfNodesToShutdown);
					case REDUCER:
						return reducerController.findNodesForShutdown(nrOfNodesToShutdown);
					case CLIENT:
						return clientRoutineWorkerController.findNodesForShutdown(nrOfNodesToShutdown);
					default:
						String msg = String.format("NodeType %s is not supported.", nodeType);
						throw new WrongTypeException(msg);
				}
			},
			ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getNodeServiceEndpointConfiguration(NodeType nodeType) {
		ITicket ticket = ticketRegistry.createTicket(
				ServiceEndpointDTO.class,
				() -> {
					switch (nodeType) {
						case WORKER:
							return Cluster.getInstance().getConfiguration().getWorkersConfiguration().getNodeServiceEndpoint();
						case REDUCER:
							return Cluster.getInstance().getConfiguration().getReducersConfiguration().getNodeServiceEndpoint();
						case CLIENT:
							return Cluster.getInstance().getConfiguration().getClientRoutineWorkersConfiguration().getNodeServiceEndpoint();
						default:
							String msg = String.format("NodeType %s not supported.", nodeType);
							throw new WrongTypeException(msg);
					}
				}
		);
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
		if (clientRoutineWorkerController != null) {
			nIds.addAll(clientRoutineWorkerController.getAllNodeIds());
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
		} else if (clientRoutineWorkerController.containsNode(nId)) {
			environment = clientRoutineWorkerController.getNodeEnvironment(nId);
		} else {
			throw new UnknownNodeException();
		}
		return environment;
	}

	@Override
	public String getLibraryEndpointConfiguration() {
		ITicket ticket = ticketRegistry.createTicket(
				ServiceEndpointDTO.class,
				() -> Cluster.getInstance().getConfiguration().getLibraryEndpoint()
		);
		return ticket.getId().toString();
	}

	@Override
	public String getNodeServiceEndpoint(String nId) {
		ITicket ticket = ticketRegistry.createTicket(
			ServiceEndpointDTO.class,
			() -> {
				if (workerController.containsNode(nId)) {
					return workerController.getNodeServiceEndpoint(nId);
				} else if (reducerController.containsNode(nId)) {
					return reducerController.getNodeServiceEndpoint(nId);
				} else if (clientRoutineWorkerController.containsNode(nId)) {
					return clientRoutineWorkerController.getNodeServiceEndpoint(nId);
				} else {
					String msg = String.format("Node with id %s is not known.", nId);
					throw new UnknownNodeException(msg);
				}
			}
		);
		return ticket.getId().toString();
	}
}
