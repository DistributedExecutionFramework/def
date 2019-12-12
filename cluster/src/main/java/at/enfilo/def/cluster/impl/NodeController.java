package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.NodeCreationException;
import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.NodesConfiguration;
import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.INodeServiceClient;
import at.enfilo.def.node.api.NodeCommunicationException;
import at.enfilo.def.node.observer.api.util.NodeNotificationConfiguration;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @param <C> - Node service client interface
 * @param <F> - Node service client factory
 */
abstract class NodeController<C extends INodeServiceClient, F extends UnifiedClientFactory<C>> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(NodeController.class);

	protected static final String UNKNOWN_NODE = "Node with id %s is not known by this cluster.";

	private final NodeType nodeType;
	protected final Object nodeLock;
	private final List<String> nodes;
	private final Map<String, String> nodeInstanceMap;
	protected final Map<String, NodeInfoDTO> nodeInfoMap;
	protected final Map<String, List<FeatureDTO>> nodeFeatureMap;
	protected final Map<String, C> nodeConnectionMap;
	private final F nodeServiceClientFactory;
	private final ServiceEndpointDTO observerEndpoint;

	/**
	 * Constructor for unit tests.
	 */
	protected NodeController(
			NodeType nodeType,
			F nodeServiceClientFactory,
			List<String> nodes,
			Map<String, String> nodeInstanceMap,
			Map<String, C> nodeConnectionMap,
			Map<String, NodeInfoDTO> nodeInfoMap,
			Map<String, List<FeatureDTO>> nodeFeatureMap,
			NodesConfiguration nodesConfiguration
	) {
		this.nodeType = nodeType;
		this.nodeLock = new Object();
		this.nodes = nodes;
		this.nodeInstanceMap = nodeInstanceMap;
		this.nodeConnectionMap = nodeConnectionMap;
		// Create a TimeoutMap for node if periodically notification is configured
		if (nodeInfoMap == null) {
			if (nodesConfiguration.getNotificationFromNode().isPeriodically()) {
				long timeout = nodesConfiguration.getTimeout();
				TimeUnit timeoutUnit = TimeUnit.valueOf(nodesConfiguration.getTimeoutUnit().name());
				this.nodeInfoMap = new TimeoutMap<>(
						timeout,
						timeoutUnit,
						timeout * 2,
						timeoutUnit,
						this::notifyNodeDown
				);
			} else {
				// Otherwise use a normal HashMap
				this.nodeInfoMap = new HashMap<>();
			}
		} else {
			this.nodeInfoMap = nodeInfoMap;
		}
		this.nodeFeatureMap = nodeFeatureMap == null ? new HashMap<>() : nodeFeatureMap;
		this.nodeServiceClientFactory = nodeServiceClientFactory;

		this.observerEndpoint = nodesConfiguration.getNotificationFromNode().getEndpoint();
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("Error while locate local ip address, use loopback address", e);
		} finally {
			if (ip == null) {
				ip = InetAddress.getLoopbackAddress().getHostAddress();
			}
			this.observerEndpoint.setHost(ip);
		}
	}

	/**
	 * Notification that a node is "down".
	 * This notification is triggered by timeout map, if a node does not send an update periodically.
	 *
	 * @param nId - Node id
	 */
	protected abstract void notifyNodeDown(String nId, NodeInfoDTO nodeInfo);

	/**
	 * Returns all registered Node Ids.
	 *
	 * @return
	 */
	List<String> getAllNodeIds() {
		synchronized (nodeLock) {
			return Collections.unmodifiableList(nodes);
		}
	}


	/**
	 * Find and returns a Id from Node which can be shutdown (most preferred one).
	 * @return
	 */
	List<String> findNodesForShutdown(int nrOfNodesToShutdown) {
		synchronized (nodeLock) {
			if (nrOfNodesToShutdown > nodeInfoMap.size()) {
				throw new IllegalArgumentException("The number of workers that shall be shutdown is greater than the actual number of workers this cluster contains.");
			}
			return nodeInfoMap
				.entrySet()
				.stream()
				.sorted(Comparator.comparingInt(o -> Integer.parseInt(o.getValue().getParameters().get("numberOfQueuedTasks"))))
				.limit(nrOfNodesToShutdown)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		}
	}


	/**
	 * Returns last known node info object of requested node
	 * @param nId - node id
	 * @return cached node info
	 * @throws UnknownNodeException
	 */
	NodeInfoDTO getNodeInfo(String nId) throws UnknownNodeException {
		synchronized (nodeLock) {
			if (nodeInfoMap.containsKey(nId)) {
				return nodeInfoMap.get(nId);
			} else {
				LOGGER.error(String.format(UNKNOWN_NODE, nId));
				throw new UnknownNodeException(String.format(UNKNOWN_NODE, nId));
			}
		}
	}

	/**
	 * Returns last known node environment of requested node
	 * @param nId - node id
	 * @return cached node environment
	 * @throws UnknownNodeException
	 */
	List<FeatureDTO> getNodeEnvironment(String nId) throws UnknownNodeException {
		if (nodeFeatureMap.containsKey(nId)) {
			return nodeFeatureMap.get(nId);
		} else {
			LOGGER.error(String.format(UNKNOWN_NODE, nId));
			throw new UnknownNodeException(String.format(UNKNOWN_NODE, nId));
		}
	}

	/**
	 * Returns Node service client connection for given Node id.
	 *
	 * @param nId
	 * @return
	 * @throws UnknownNodeException
	 */
	C getServiceClient(String nId) throws UnknownNodeException {
		synchronized (nodeLock) {
			if (nodeConnectionMap.containsKey(nId)) {
				return nodeConnectionMap.get(nId);
			}
			LOGGER.error(String.format(UNKNOWN_NODE, nId));
			throw new UnknownNodeException(String.format(UNKNOWN_NODE, nId));
		}
	}

	/**
	 * Returns size of worker pool.
	 *
	 * @return current worker pool size
	 */
	int getNodePoolSize() {
		synchronized (nodeLock) {
			return nodes.size();
		}
	}

	/**
	 * Adds a new already running Node instance to this cluster.
	 * Attention: Node instance can not be under control of another cluster.
	 *
	 * @param serviceEndpoint - service endpoint of client
	 * @return Node id
	 */
	public String addNode(ServiceEndpointDTO serviceEndpoint)
	throws NodeCreationException {

		LOGGER.debug("Trying to add given Node instance with endpoint at {}", serviceEndpoint);
		try {
			C nodeServiceClient = nodeServiceClientFactory.createClient(serviceEndpoint);

			LOGGER.debug("Trying to take control over Node {}", serviceEndpoint);
			Future<Void> futureTakeControl = nodeServiceClient.takeControl(ClusterResource.getInstance().getId());
			try {
				futureTakeControl.get(); // wait for ticket state done
			} catch (ExecutionException e) {
				String msg = String.format("Cannot take control over Node on endpoint %s.", serviceEndpoint);
				LOGGER.error(msg, e);
				throw new NodeCreationException(msg, e);
			}
			LOGGER.info("Take control over Node {} done.", serviceEndpoint);

			// Get NodeInfo and proof Cluster id
			Future<NodeInfoDTO> futureNodeInfo = nodeServiceClient.getInfo();
			NodeInfoDTO nodeInfo = futureNodeInfo.get();
			if (!nodeInfo.getClusterId().equals(ClusterResource.getInstance().getId())) {
				String msg = String.format("Node already under control of another Cluster with id %s.", nodeInfo.getClusterId());
				LOGGER.error(msg);
				throw new NodeCreationException(msg);
			}

			// Register as observer
			LOGGER.debug("Trying to register as observer on Node {}", serviceEndpoint);
			NodeNotificationConfiguration configuration = Cluster.getInstance().getConfiguration().getWorkersConfiguration().getNotificationFromNode();
			Future<Void> futureRegisterObserver = nodeServiceClient.registerObserver(
				observerEndpoint,
				configuration.isPeriodically(),
				configuration.getPeriodDuration(),
				configuration.getPeriodUnit()
			);
			try {
				futureRegisterObserver.get(); // wait for ticket done
			} catch (ExecutionException e) {
				String msg = String.format("Error while registering as observer on Node %s. Ticket-Status: %s",
						serviceEndpoint,
						futureRegisterObserver.get()
				);
				LOGGER.error(msg, e);
				throw new NodeCreationException(msg, e);
			}
			LOGGER.info("Registered as observer on Node instance {}.", serviceEndpoint);

			// Get node environment
			List<FeatureDTO> nodeFeatures = nodeServiceClient.getFeatures().get();

			// Register new worker
			synchronized (nodeLock) {
				nodes.add(nodeInfo.getId());
				nodeConnectionMap.put(nodeInfo.getId(), nodeServiceClient);
				nodeInfoMap.put(nodeInfo.getId(), nodeInfo);
				nodeFeatureMap.put(nodeInfo.getId(), nodeFeatures);
			}
			addNodeToScheduler(nodeInfo.getId(), serviceEndpoint);

			LOGGER.info("Added Node {} at {} successfully to this cluster.", nodeInfo.getId(), serviceEndpoint);
			return nodeInfo.getId();

		} catch (NodeCommunicationException | InterruptedException | ExecutionException | ClientCreationException | ClientCommunicationException e) {
			String msg = "Error while adding new Node instance";
			LOGGER.error(msg, e);
			throw new NodeCreationException(msg, e);
		}
	}


	/**
	 * Notification from a worker with Worker-Information.
	 * Updates internal information about this worker.
	 *
	 * @param nId - node id
	 * @param nodeInfo - info object
	 */
	void notifyNodeInfo(String nId, NodeInfoDTO nodeInfo) throws UnknownNodeException {
		synchronized (nodeLock) {
			if (nodeInfoMap.containsKey(nId)) {
				nodeInfoMap.put(nId, nodeInfo);
			} else {
				String msg = String.format("Received a NodeInfo notification from an unknown Node with id %s, try to add Node.", nId);
				LOGGER.info(msg);
				throw new UnknownNodeException(msg);
			}
		}
	}

	/**
	 * Remove a Node from this cluster. Notifies Scheduler to remove the specified Node.
	 *
	 * @param nId - Node to remove
	 * @throws UnknownNodeException - if Node id not known
	 */
	protected void removeNode(String nId) throws UnknownNodeException {
		removeNode(nId, false);
	}

	/**
	 * Remove a Node from this cluster. Notifies Scheduler to remove the specified Node.
	 *
	 * @param nId - Node to remove
	 * @param deregisterObserver - De-register Cluster as observer on given node.
	 * @throws UnknownNodeException - if Node id not known
	 */
	void removeNode(String nId, boolean deregisterObserver) throws UnknownNodeException {
		LOGGER.debug("Try to remove Node {} from Cluster.", nId);

		C nodeServiceClient;

		// First remove Node from any list and map
		synchronized (nodeLock) {
			if (!nodeInfoMap.containsKey(nId)) {
				LOGGER.error(String.format(UNKNOWN_NODE, nId));
				throw new UnknownNodeException(String.format(UNKNOWN_NODE, nId));
			}
			nodeServiceClient = nodeConnectionMap.remove(nId);
			nodeInstanceMap.remove(nId);
			nodeInfoMap.remove(nId);
			nodeFeatureMap.remove(nId);
			nodes.remove(nId);
		}

		// Second, update scheduler service and remove the requested worker from scheduling
		try {
			removeNodeFromScheduler(nId);
		} catch (NodeCommunicationException e) {
			LOGGER.error("Error while remove Mpde {} from Scheduler", nId, e);
		}

		// Third, remove Cluster as observer from Node
		if (deregisterObserver && (nodeServiceClient != null)) {
			LOGGER.debug("Trying to de-register Cluster as observer on Node {}.", nId);
			try {
				Future<Void> futureDeregister = nodeServiceClient.deregisterObserver(observerEndpoint);
				futureDeregister.get();
				LOGGER.info("Deregistered Cluster as observer on Node {} done.", nId);
			} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
				LOGGER.warn("Deregistering observer on Node {} failed, ignoring.", nId, e);
			}
		}

		LOGGER.info("Node {} successful removed from Cluster.", nId);
	}

	/**
	 * Adds a Node to the right Scheduler implementation/instance.
	 * @param nId
	 * @param endpoint
	 * @throws NodeCommunicationException
	 */
	private void addNodeToScheduler(String nId, ServiceEndpointDTO endpoint) throws NodeCommunicationException {
		LOGGER.debug("Adding Node {} ({}) to Scheduler", nId, endpoint);
		try {
			Future<Void> future = null;
			switch (nodeType) {
				case WORKER:
					future = ClusterResource.getInstance()
							.getSchedulerServiceClient(NodeType.WORKER)
							.addWorker(nId, endpoint);
					break;
				case REDUCER:
					future = ClusterResource.getInstance()
							.getSchedulerServiceClient(NodeType.REDUCER)
							.addReducer(nId, endpoint);
					break;
				default:
					LOGGER.error("Error while add Node {} to Scheduler. NodeType {} not supported.", nId, nodeType);
					return;
			}

			future.get(); // wait for ticket done
			LOGGER.info("Node {} successfully added.", nId);

		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while adding Node {} to Scheduler.", nId, e);
			throw new NodeCommunicationException("Error while adding Node to Scheduler", e);
		}
	}

	/**
	 * Removes a Node from the right Scheduler implementation/instance.
	 * @param nId
	 * @throws NodeCommunicationException
	 */
	private void removeNodeFromScheduler(String nId) throws NodeCommunicationException {
		LOGGER.debug("Try to remove Node {} from Scheduler", nId);
		try {
			Future<Void> future = null;
			switch (nodeType) {
				case WORKER:
					future = ClusterResource.getInstance()
							.getSchedulerServiceClient(NodeType.WORKER)
							.removeWorker(nId);
					break;
				case REDUCER:
					future = ClusterResource.getInstance()
							.getSchedulerServiceClient(NodeType.REDUCER)
							.removeReducer(nId);
					break;
				default:
					LOGGER.error("Error while remove Node {} from Scheduler. NodeType {} not supported.", nId, nodeType);
					return;
			}

			future.get(); // wait for ticket done
			LOGGER.info("Removed Node {} from Scheduler", nId);

		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while remove Node {} to Scheduler.", nId, e);
			throw new NodeCommunicationException("Error while remove Node to Scheduler", e);
		}
	}

	/**
	 * Execute given function on all registered Nodes.
	 * @param exec - given function to execute
	 * @return true if function returns TicketStatusDTO.DONE on every Node
	 */
	boolean execOnAllNodes(Function<C, Future<Void>> exec) {
		boolean rv = true;
		Map<String, Future<Void>> futures = new HashMap<>();
		synchronized (nodeLock) {
			nodeConnectionMap.forEach((key, value) -> {
				Future<Void> voidFuture = exec.apply(value);
				futures.put(key, voidFuture);
			});
		}
		for (Map.Entry<String, Future<Void>> entry : futures.entrySet()) {
			try {
				entry.getValue().get(); // wait for ticket done
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Execution on Node {} failed.", entry.getKey(), e);
				rv = false;
			}
		}
		return rv;
	}

	boolean containsNode(String nId) {
		synchronized (nodeLock) {
			return nodes.contains(nId);
		}
	}

	void addAllNodesToScheduler() throws NodeCommunicationException {
		synchronized (nodeLock) {
			for (Map.Entry<String, C> entry : nodeConnectionMap.entrySet()) {
				addNodeToScheduler(entry.getKey(), entry.getValue().getServiceEndpoint());
			}
		}
	}

	/**
	 * Returns {@link ServiceEndpointDTO} for the given Node id.
	 * @param nId - node id
	 * @return ServiceEndpoint instance
	 * @throws UnknownNodeException if Node not exists
	 */
	ServiceEndpointDTO getNodeServiceEndpoint(String nId) throws UnknownNodeException {
		synchronized (nodeLock) {
			if (nodeConnectionMap.containsKey(nId)) {
				return nodeConnectionMap.get(nId).getServiceEndpoint();
			}
			throw new UnknownNodeException();
		}
	}

	/**
	 * Distribute SharedResource to all workers.
	 * @param sharedResource
	 */
	void distributeSharedResource(ResourceDTO sharedResource) {
		execOnAllNodes(nodeServiceClient -> {
			try {
				return nodeServiceClient.addSharedResource(sharedResource);
			} catch (ClientCommunicationException e) {
				LOGGER.error("Error while add SharedResource {} to Node on {}.", sharedResource.getId(), nodeServiceClient.getServiceEndpoint().getHost());
				return null;
			}
		});
	}

	void removeSharedResources(List<String> sharedResources) {
		execOnAllNodes(nodeServiceClient -> {
			try {
				return nodeServiceClient.removeSharedResources(sharedResources);
			} catch (ClientCommunicationException e) {
				LOGGER.error("Error while remove SharedResources from Node on {}.", nodeServiceClient.getServiceEndpoint().getHost());
				return null;
			}
		});
	}
}
