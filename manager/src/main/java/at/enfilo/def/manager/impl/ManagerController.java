package at.enfilo.def.manager.impl;

import at.enfilo.def.cloud.communication.api.CloudCommunicationServiceClientFactory;
import at.enfilo.def.cloud.communication.api.ICloudCommunicationServiceClient;
import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.InstanceTypeDTO;
import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.communication.exception.TakeControlException;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.manager.server.Manager;
import at.enfilo.def.manager.util.ProgramClusterRegistry;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Controller for manager
 */
public class ManagerController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ManagerController.class);

	private final String managerId;
	private final ProgramClusterRegistry registry;
	private final ClusterServiceClientFactory clusterServiceClientFactory;
	private ICloudCommunicationServiceClient cloudCommunicationServiceClient;
	private final Map<String, String> clusterIdToCloudClusterIdMap;

	/**
	 * Private class to provide thread safe singleton
	 */
	private static class ThreadSafeLazySingletonWrapper {
		private static final ManagerController INSTANCE = new ManagerController();

		private ThreadSafeLazySingletonWrapper() {}
	}

	/**
	 * Private constructor, use getInstance();
	 */
	protected ManagerController() {
		this(
				ProgramClusterRegistry.getInstance(),
				new ClusterServiceClientFactory(),
				null
		);
	}

	/**
	 * Private constructor for unit tests.
	 */
	protected ManagerController(
		ProgramClusterRegistry registry,
		ClusterServiceClientFactory clusterServiceClientFactory,
		ICloudCommunicationServiceClient cloudCommunicationServiceClient
	) {
		this.managerId = Manager.getInstance().getConfiguration().getId();
		this.registry = registry;
		this.clusterServiceClientFactory = clusterServiceClientFactory;
		this.clusterIdToCloudClusterIdMap = new HashMap<>();
		this.cloudCommunicationServiceClient = cloudCommunicationServiceClient;

		try {
			if (cloudCommunicationServiceClient == null) {
				this.cloudCommunicationServiceClient = new CloudCommunicationServiceClientFactory().createClient(
						Manager.getInstance().getConfiguration().getCloudCommunicationEndpoint()
				);
			}
		} catch (ClientCreationException e) {
			LOGGER.error("Error while creating ICloudCommunicationServiceClient.", e);
		}
	}


	/**
	 * Singleton pattern.
	 * @return a ManagerController instance.
	 */
	static ManagerController getInstance() {
		return ThreadSafeLazySingletonWrapper.INSTANCE;
	}


	/**
	 * Returns cluster info from requested cluster id
	 * @param cId - cluster id
	 * @return cluster info object
	 */
	public ClusterInfoDTO getClusterInfo(String cId)
	throws UnknownClusterException, InterruptedException, ExecutionException, ClientCommunicationException, ClientCreationException {
		LOGGER.debug("Try to fetch info for Cluster {}.", cId);
		try {
			IClusterServiceClient clusterServiceClient = registry.getClusterClient(cId);
			// TODO: See reliable connection ticket
			// Naive assumption: all services up and no network problems
			Future<ClusterInfoDTO> clusterInfo = clusterServiceClient.getClusterInfo();
			return clusterInfo.get();

		} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
			LOGGER.error("Error while fetching ClusterInfo object from Cluster {}.", cId, e);
			throw e;
		} catch (UnknownClusterException e) {
			LOGGER.error("Cluster {} not registered.", cId);
			throw e;
		}
	}


	/**
	 * Returns {@link ServiceEndpointDTO} for the given cluster id.
	 * @param cId
	 * @return
	 */
	public ServiceEndpointDTO getClusterEndpoint(String cId) throws UnknownClusterException {
		try {

			return registry.getClusterEndpoint(cId);
		} catch (UnknownClusterException e) {
			LOGGER.error("Cluster {} not known.", cId);
			throw e;
		}
	}

	/**
	 * Creates a new computing cluster in the AWS environment with a running DEF cluster instance and a given number
	 * of DEF worker and reducers instances
	 *
	 * @param numberOfWorkers		the number of DEF worker instances that shall be running in the AWS computing cluster
	 * @param numberOfReducers		the number of DEF reducer instances that shall be running in the AWS computing cluster
	 * @param awsSpecification		the {@link AWSSpecificationDTO} with all the necessary data for connecting to AWS
	 * @return						the id of the booted DEF cluster instance
	 * @throws UnknownCloudException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws ClientCreationException
	 * @throws TakeControlException
	 * @throws ClientCommunicationException
	 */
	public String createAWSCluster(int numberOfWorkers, int numberOfReducers, AWSSpecificationDTO awsSpecification)
			throws UnknownCloudException, InterruptedException, ExecutionException, ClientCreationException, TakeControlException, ClientCommunicationException {

		String awsClusterId = this.cloudCommunicationServiceClient.createAWSCluster(awsSpecification).get();
		return createCluster(awsClusterId, numberOfWorkers, numberOfReducers, null);
	}

	/**
	 *
	 * @param cId
	 * @param newNodePoolSize
	 * @param nodeType
	 */
	public void adjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType)
			throws UnknownClusterException, InterruptedException, ExecutionException, ClientCommunicationException, ClientCreationException {

		adjustNodePoolSize(cId, newNodePoolSize, nodeType, null);
	}

	protected void adjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType, ClusterServiceClientFactory clusterServiceClientFactory)
			throws UnknownClusterException, InterruptedException, ExecutionException, ClientCommunicationException, ClientCreationException{

		String cloudClusterId = getCloudClusterId(cId);
		ClusterInfoDTO clusterInfoDTO = getClusterInfo(cId);

		ServiceEndpointDTO clusterEndpoint = getClusterEndpoint(cId);

		if (clusterServiceClientFactory == null) {
			clusterServiceClientFactory = new ClusterServiceClientFactory();
		}
		IClusterServiceClient clusterServiceClient = clusterServiceClientFactory.createClient(clusterEndpoint);

		ServiceEndpointDTO clusterLibraryEndpoint = clusterServiceClient.getLibraryEndpointConfiguration().get();
		clusterLibraryEndpoint.setHost(clusterInfoDTO.getHost());

		InstanceTypeDTO instanceTypeDTO = null;
		int nodesToAdjust = 0;
		switch (nodeType) {
			case WORKER:
				instanceTypeDTO = InstanceTypeDTO.WORKER;
				nodesToAdjust = newNodePoolSize - clusterInfoDTO.getNumberOfWorkers();
				break;
			case REDUCER:
				instanceTypeDTO = InstanceTypeDTO.REDUCER;
				nodesToAdjust = newNodePoolSize - clusterInfoDTO.getNumberOfReducers();
				break;
		}

		if (nodesToAdjust > 0) {
			bootNodeInstancesInCluster(cloudClusterId, clusterServiceClient, clusterLibraryEndpoint, null, instanceTypeDTO, nodesToAdjust);
		} else if (nodesToAdjust < 0) {
			List<String> nodeIds = clusterServiceClient.findNodesForShutdown(nodeType, nodesToAdjust*(-1)).get();

			for (String nodeId: nodeIds) {
				clusterServiceClient.removeNode(nodeId);
			}

			this.cloudCommunicationServiceClient.terminateNodes(cloudClusterId, nodeIds);
			TimeUnit.SECONDS.sleep(10);
		}
	}

	protected String createCluster(
			String cloudClusterId,
			int numberOfWorkers,
			int numberOfReducers,
			LibraryServiceClientFactory libraryServiceClientFactory)
	throws InterruptedException, ExecutionException, ClientCreationException, TakeControlException, ClientCommunicationException {

		String clusterInstanceId = this.cloudCommunicationServiceClient.bootClusterInstance(cloudClusterId).get();
		String clusterInstanceIPAddress = this.cloudCommunicationServiceClient.getPrivateIPAddressOfCloudInstance(cloudClusterId, clusterInstanceId).get();

		ServiceEndpointDTO clusterEndpoint = Manager.getInstance().getConfiguration().getClusterEndpoint();
		clusterEndpoint.setHost(clusterInstanceIPAddress);
		IClusterServiceClient clusterServiceClient = this.clusterServiceClientFactory.createClient(clusterEndpoint);
		String defClusterId = addCluster(clusterServiceClient);
		this.cloudCommunicationServiceClient.mapDEFIdToCloudInstanceId(cloudClusterId, defClusterId, clusterInstanceId);
		mapClusterIdToCloudClusterId(defClusterId, cloudClusterId);

		ServiceEndpointDTO managerLibraryEndpoint = Manager.getInstance().getConfiguration().getLibraryEndpoint();
		String vpnIp = Manager.getInstance().getConfiguration().getVpnIp();
		managerLibraryEndpoint.setHost(vpnIp);

		ServiceEndpointDTO clusterLibraryEndpoint = clusterServiceClient.getLibraryEndpointConfiguration().get();
		clusterLibraryEndpoint.setHost(clusterInstanceIPAddress);
		if (libraryServiceClientFactory == null) {
			libraryServiceClientFactory = new LibraryServiceClientFactory();
		}
		ILibraryServiceClient clusterLibraryServiceClient = libraryServiceClientFactory.createClient(clusterLibraryEndpoint);
		clusterLibraryServiceClient.setDataEndpoint(managerLibraryEndpoint);

		bootNodeInstancesInCluster(cloudClusterId, clusterServiceClient, clusterLibraryEndpoint, null, InstanceTypeDTO.WORKER, numberOfWorkers);
		bootNodeInstancesInCluster(cloudClusterId, clusterServiceClient, clusterLibraryEndpoint, null, InstanceTypeDTO.REDUCER, numberOfReducers);

		return defClusterId;
	}

	protected void bootNodeInstancesInCluster(
			String cloudClusterId,
			IClusterServiceClient clusterServiceClient,
			ServiceEndpointDTO clusterLibraryEndpoint,
			LibraryServiceClientFactory libraryServiceClientFactory,
			InstanceTypeDTO instanceType,
			int nrOfNodes) {

		if (nrOfNodes > 0) {
			try {
				List<String> nodeIds = this.cloudCommunicationServiceClient.bootNodes(cloudClusterId, instanceType, nrOfNodes).get();

				NodeType nodeType = null;
				switch (instanceType) {
					case WORKER:
						nodeType = NodeType.WORKER;
						break;
					case REDUCER:
						nodeType = NodeType.REDUCER;
						break;
				}
				ServiceEndpointDTO nodeEndpointConfiguration = clusterServiceClient.getNodeServiceEndpointConfiguration(nodeType).get();
				ServiceEndpointDTO libraryEndpointConfiguration = clusterServiceClient.getLibraryEndpointConfiguration().get();
				if (libraryServiceClientFactory == null) {
					libraryServiceClientFactory = new LibraryServiceClientFactory();
				}

				for (String nodeId: nodeIds) {
					String nodeIPAddress = this.cloudCommunicationServiceClient.getPrivateIPAddressOfCloudInstance(cloudClusterId, nodeId).get();

					ServiceEndpointDTO nodeEndpoint = new ServiceEndpointDTO(nodeEndpointConfiguration);
					nodeEndpoint.setHost(nodeIPAddress);
					List<String> nodesBeforeAdding = clusterServiceClient.getAllNodes(nodeType).get();
					clusterServiceClient.addNode(nodeEndpoint, nodeType);
					TimeUnit.SECONDS.sleep(10); // wait until adding of node to cluster has finished
					List<String> nodesAfterAdding = clusterServiceClient.getAllNodes(nodeType).get();
					nodesAfterAdding.removeAll(nodesBeforeAdding);

					String defInstanceId;
					if (nodesAfterAdding.isEmpty()) {
						throw new IllegalStateException("The list with newly added nodes is empty, no node was added.");
					} else if (nodesAfterAdding.size() > 1) {
						throw new IllegalStateException("The list with newly added nodes has more items than one, only one node should have been added.");
					} else {
						defInstanceId = nodesAfterAdding.get(0);
					}

					this.cloudCommunicationServiceClient.mapDEFIdToCloudInstanceId(cloudClusterId, defInstanceId, nodeId);

					ServiceEndpointDTO nodeLibraryEndpoint = new ServiceEndpointDTO(libraryEndpointConfiguration);
					nodeLibraryEndpoint.setHost(nodeIPAddress);
					libraryServiceClientFactory.createClient(nodeLibraryEndpoint).setDataEndpoint(clusterLibraryEndpoint);
				}
			} catch (ClientCommunicationException | ClientCreationException | InterruptedException | ExecutionException e) {
				LOGGER.error(MessageFormat.format("Error while booting node instances of type {0} in cloud cluster with id {1}.", instanceType, cloudClusterId), e);
			}
		}
	}


	void destroyCluster(String cId) throws UnknownClusterException, ClientCommunicationException, ClientCreationException {
		LOGGER.debug("Try to destroy cluster with id {}.", cId);
		try {
			IClusterServiceClient clusterServiceClient = registry.getClusterClient(cId);
			clusterServiceClient.destroyCluster();
			LOGGER.info("Destroy cluster {} successful initiated, removing from registry.", cId);
			registry.deleteCluster(cId);

			String cloudClusterId = getCloudClusterId(cId);
			if (cloudClusterId != null) {
				this.cloudCommunicationServiceClient.shutdownCloudCluster(cloudClusterId);
				this.clusterIdToCloudClusterIdMap.remove(cId);
				LOGGER.info("Shutting down cloud cluster with id {} successfully initiated.", cloudClusterId);
			}
		} catch (ClientCommunicationException e) {
			LOGGER.error("Error while destroy cluster {}.", cId, e);
			throw e;
		} catch (UnknownClusterException e) {
			LOGGER.error("Can not destroy unknown cluster {}.", cId, e);
			throw e;
		}
	}


	/**
	 * Try to add given Cluster to Manager registry.
	 *
	 * @param serviceEndpoint - service endpoint of cluster service
	 * @return - returns cluster id
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws ClientCreationException
	 * @throws TakeControlException
	 * @throws ClientCommunicationException
	 */
	protected String addCluster(ServiceEndpointDTO serviceEndpoint)
	throws InterruptedException, ExecutionException, ClientCreationException, TakeControlException, ClientCommunicationException {
		return addCluster(clusterServiceClientFactory.createClient(serviceEndpoint));
	}

	/**
	 * Try to add given Cluster to Manager registry.
	 *
	 * @param serviceClient - service client of cluster
	 * @return - returns cluster id
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TakeControlException
	 * @throws ClientCommunicationException
	 */
	protected String addCluster(IClusterServiceClient serviceClient)
	throws InterruptedException, ExecutionException, TakeControlException, ClientCommunicationException {
		LOGGER.debug("Try to add cluster with endpoint {} to cluster registry.", serviceClient.getServiceEndpoint());
		try {
			Future<ClusterInfoDTO> futureInfo = serviceClient.getClusterInfo();
			ClusterInfoDTO info = futureInfo.get();
			String cId = info.getId();
			if (!info.isSetManagerId() || info.getManagerId().isEmpty()) {
				try {
					LOGGER.debug("Try to take control over cluster {}.", info.getId());
					Future<Void> futureTakeControl = serviceClient.takeControl(managerId);
					futureTakeControl.get(); // wait for done
					LOGGER.info("Take control of cluster {} was successful.", info.getId());
					registry.addCluster(info.getId(), serviceClient);
				}
				catch (ExecutionException | ClientCommunicationException e) {
					String msg = String.format("Take control of cluster %s failed.", info.getId());
					throw new TakeControlException(msg);
				}

			} else {
				if (info.getManagerId().equals(managerId)) {
					LOGGER.info("Cluster already under control, ignoring request.");
					registry.addCluster(info.getId(), serviceClient);
				} else {
					String msg = String.format(
							"Cluster %s already under control by another manager: %s.", info.getId(), info.getManagerId()
					);
					throw new TakeControlException(msg);
				}
			}

			return cId;

		} catch (ClientCommunicationException | ExecutionException |
				InterruptedException | TakeControlException e) {

			LOGGER.error("Error while add/register cluster with endpoint: {}.", serviceClient, e);
			throw e;
		}
	}

	String getManagerId() {
		return managerId;
	}

	public List<String> getClusterIds() {
		return registry.getClusterIds();
	}

	protected String getCloudClusterId(String cId) {
		if (this.clusterIdToCloudClusterIdMap.containsKey(cId)) {
			return this.clusterIdToCloudClusterIdMap.get(cId);
		}
		return null;
	}

	protected void mapClusterIdToCloudClusterId(String cId, String cloudClusterId) {
		this.clusterIdToCloudClusterIdMap.put(cId, cloudClusterId);
	}
}
