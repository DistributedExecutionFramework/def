package at.enfilo.def.scheduler.general.strategy;

import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Extension;
import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.INodeServiceClient;
import at.enfilo.def.node.api.exception.NodeCommunicationException;
import at.enfilo.def.node.observer.api.util.NodeNotificationConfiguration;
import at.enfilo.def.scheduler.general.util.SchedulerConfiguration;
import at.enfilo.def.transfer.dto.NodeEnvironmentDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Abstract base class for every scheduler implementation.
 *
 * @param <C> - Node client interface
 * @param <F> - Node client factory
 */
public abstract class SchedulingStrategy<C extends INodeServiceClient, F extends UnifiedClientFactory<C>> {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(SchedulingStrategy.class);

	private final Map<String, C> nodes;
	private final Map<String, Environment> nodeEnvironments;
	private final F factory;
	private final SchedulerConfiguration schedulerConfiguration;
	private final ServiceEndpointDTO notifyEndpoint;
	private ILibraryServiceClient libraryServiceClient;

	/**
	 * Constructor for unit test - concrete Class TestScheduler.
	 *
	 */
	protected SchedulingStrategy(
		Map<String, C> nodes,
		Map<String, Environment> nodeEnvironments,
		F factory,
		ILibraryServiceClient libraryServiceClient,
		SchedulerConfiguration schedulerConfiguration
	) {
		this.nodes = nodes;
		this.nodeEnvironments = nodeEnvironments;
		this.factory = factory;
		this.schedulerConfiguration = schedulerConfiguration;

		this.notifyEndpoint = schedulerConfiguration.getNotificationFromNode().getEndpoint();
		this.notifyEndpoint.setHost(resolveIP());

		if (libraryServiceClient == null) {
			LibraryServiceClientFactory libraryFactory = new LibraryServiceClientFactory();
			try {
				this.libraryServiceClient = libraryFactory.createClient(schedulerConfiguration.getLibraryEndpoint());
			} catch (ClientCreationException e) {
				LOGGER.error("Error while create ILibraryServiceClient.", e);
			}
		} else {
			this.libraryServiceClient = libraryServiceClient;
		}
	}

	/**
	 * Adds a new node.
	 * @param nId - node id
	 * @param endpoint - service endpoint definition
	 * @throws NodeCommunicationException
	 */
	protected void addNode(String nId, ServiceEndpointDTO endpoint)
	throws NodeCommunicationException {

		LOGGER.debug(
			"Trying to add new Node with id \"{}\" and endpoint \"{}\" to scheduler.",
			nId,
			endpoint
		);

		try {
			if (!nodes.containsKey(nId)) {
				C serviceClient = factory.createClient(endpoint);

				// Register as observer on worker
				LOGGER.debug("Trying to register as observer on Node \"{}\".", nId);
				NodeNotificationConfiguration notificationConf = schedulerConfiguration.getNotificationFromNode();
				Future<Void> futureRegisterObserver = serviceClient.registerObserver(
					notifyEndpoint,
					notificationConf.isPeriodically(),
					notificationConf.getPeriodDuration(),
					notificationConf.getPeriodUnit()
				);

				futureRegisterObserver.get();

				LOGGER.debug("Trying to get Node environment of Node {}", endpoint);
				Future<NodeEnvironmentDTO> futureNodeEnvironment = serviceClient.getEnvironment();
				NodeEnvironmentDTO nodeEnvironment = futureNodeEnvironment.get();
				LOGGER.info("Node environment of Node {} successfully received.", endpoint);
				if(nodeEnvironment.getEnvironment() != null && !nodeEnvironment.getEnvironment().isEmpty()) {
					for(String feature:  nodeEnvironment.getEnvironment()) {
						LOGGER.debug("Node {} has feature: {}", endpoint, feature);
					}
				}

				nodes.put(nId, serviceClient);
				nodeEnvironments.put(nId, Environment.buildFromString(nodeEnvironment.getEnvironment()));
				LOGGER.info("Node \"{}\" was added.", nId);

			} else {
				LOGGER.warn("Node with id \"{}\" is already known.", nId);
			}

		} catch (ExecutionException | ClientCreationException | ClientCommunicationException e) {
			LOGGER.error("Error while adding a Node \"{}\" to Scheduler.", nId, e);
			throw new NodeCommunicationException(e);
		} catch (InterruptedException e) {
			LOGGER.error("Error while adding a Node \"{}\" to Scheduler.", nId, e);
			Thread.currentThread().interrupt();
			throw new NodeCommunicationException(e);
		}
	}

	/**
	 * Remove a node.
	 * @param nId - node id to remove
	 */
	protected void removeNode(String nId) {
		if (nodes.containsKey(nId)) {
			final C client = nodes.remove(nId);
            nodeEnvironments.remove(nId);
			LOGGER.info("Removed Node \"{}\" from Scheduler", nId);

			if (client != null) {
				TicketRegistry.getInstance().createTicket(
						() -> {
							try {
								LOGGER.debug("Try to deregister as observer from Node \"{}\".", nId);
								client.deregisterObserver(notifyEndpoint); // Ignoring result, not needed
							} catch (ClientCommunicationException e) {
								LOGGER.warn("Error while deregister as observer from Node \"{}\", ignoring...", nId, e);
							}
						},
						ITicket.SERVICE_PRIORITY
				);
			}

		} else {
			LOGGER.warn("Cannot remove Node \"{}\", not known by Scheduler.", nId);
		}
	}

	/**
	 * Returns client of given node id.
	 * @param nId - node id
	 * @return
	 */
	protected C getNodeClient(String nId) throws UnknownNodeException {
		if (nodes.containsKey(nId)) {
			return nodes.get(nId);
		}
		throw new UnknownNodeException(String.format("Node not known by this scheduler: %s", nId));
	}

	/**
	 * Get list of registered node ids.
	 *
	 * @return list of node ids.
	 */
	protected List<String> getNodes() {
		return new ArrayList<>(nodes.keySet());
	}

	/**
	 * Helper method that provides basic IP Address resolvent functionality.
	 *
	 * @return resolved IP Address.
	 */
	protected String resolveIP() {
		String ip = null;
		try {

			ip = InetAddress.getLocalHost().getHostAddress();
			LOGGER.debug("Resolved local (scheduler) IP address with {}", ip);

		} catch (UnknownHostException e) {
			LOGGER.error("Error while locate local ip address, using loopback address instead.", e);
		} finally {
			if (ip == null) {
				ip = InetAddress.getLoopbackAddress().getHostAddress();
			}
		}

		return ip;
	}

	protected List<Feature> getRoutineRequiredFeatures(String rId) throws ClientCommunicationException{
		if (rId == null || rId.isEmpty()) {
			return new ArrayList<>();
		}
		try {
			List<FeatureDTO> requiredFeatureDTOs = libraryServiceClient.getRoutineRequiredFeatures(rId).get();
			List<Feature> requiredFeatures = new ArrayList<>();
			for (FeatureDTO featureDTO : requiredFeatureDTOs) {
				Feature feature = new Feature(featureDTO.getName(), featureDTO.getVersion(), featureDTO.getGroup(), null);
				if (featureDTO.getExtensions() != null) {
					for (FeatureDTO extensionDTO : featureDTO.getExtensions()) {
						Extension extension = new Extension(extensionDTO.getName(), extensionDTO.getVersion());
						feature.addExtension(extension);
					}
				}
				requiredFeatures.add(feature);
			}
			return requiredFeatures;
		} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
			throw new ClientCommunicationException("Error getting required features", e);
		}
	}

	/**
	 * Get a list of nodes that support the required features of all specified routines.
	 *
	 * @param rIds list of routine IDs that need to be supported
	 * @return returns a list of matching nodes
	 */
	protected List<String> getNodes(List<String> rIds) throws ClientCommunicationException {
		List<Feature> requiredFeatures = new ArrayList<>();
		for (String id : rIds) {
			requiredFeatures.addAll(getRoutineRequiredFeatures(id));
		}

		List<String> mNodes = new ArrayList<>();
		for (String key : nodes.keySet()) {
			if (nodeEnvironments.containsKey(key)  && nodeEnvironments.get(key) != null
					&& nodeEnvironments.get(key).matches(requiredFeatures)) {
				mNodes.add(key);
			}
		}

		return mNodes;
	}

	public abstract void notifyNodeInfo(String nId, NodeInfoDTO nodeInfo);
}
