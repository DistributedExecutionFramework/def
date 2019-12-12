package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.manager.api.ManagerServiceClientFactory;
import at.enfilo.def.manager.webservice.rest.IClusterService;
import at.enfilo.def.manager.webservice.server.ManagerWebservice;
import at.enfilo.def.manager.webservice.util.ManagerWebserviceConfiguration;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ClusterServiceImpl implements IClusterService {

    /**
     * Logger for logging activities of instances of this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterServiceImpl.class);

    private final ManagerWebserviceConfiguration configuration;

    /**
     * IExecLogicServiceClient needed for accessing the execution logic implementations regarding jobs
     */
    protected IManagerServiceClient managerServiceClient;

    /**
     *
     */
    protected TimeoutMap<String, IClusterServiceClient> clusterClientMap;

    /**
     *
     */
    private TimeoutMap<String, IWorkerServiceClient> workerClientMap;

    /**
     * Constructor of ProgramServiceImpl
     */
    public ClusterServiceImpl() {
        configuration = ManagerWebservice.getInstance().getConfiguration();
        init();
    }

    /**
     * Initializing of all needed components
     */
    private void init() {

        // service client for manager services is fetched
        try {
            LOGGER.info("Initialization of all needed components for managing clusters ");
            managerServiceClient = new ManagerServiceClientFactory().createClient(configuration.getManagerEndpoint());

            clusterClientMap = new TimeoutMap<>(
                    10,
                    TimeUnit.MINUTES,
                    5,
                    TimeUnit.MINUTES
            );
            workerClientMap = new TimeoutMap<>(
                    10,
                    TimeUnit.MINUTES,
                    5,
                    TimeUnit.MINUTES
            );

        } catch (ClientCreationException e) {
            LOGGER.error("Error initializing ClusterServiceClient", e);
        }
    }

    @Override
    public List<String> getAllClusterIds() {
        try {
            LOGGER.info("Fetching all cluster ids");
            Future<List<String>> future = managerServiceClient.getClusterIds();
            return future.get();
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching all cluster ids", e);
            return null;
        }
    }

    @Override
    public List<ClusterInfoDTO> getAllClusters() {
        try {
            LOGGER.info("Fetching all clusters");
            Future<List<String>> future = managerServiceClient.getClusterIds();
            List<String> clusterIds = future.get();
            List<ClusterInfoDTO> clusters = new LinkedList<>();

            for (String id : clusterIds) {
                Future<ClusterInfoDTO> clusterInfoDTOFuture = managerServiceClient.getClusterInfo(id);
                ClusterInfoDTO clusterInfoDTO = clusterInfoDTOFuture.get();
                clusterInfoDTO.setHost(managerServiceClient.getClusterEndpoint(id).get().getHost());
                clusters.add(clusterInfoDTOFuture.get());
            }
            return clusters;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching all clusters", e);
            return null;
        }
    }

    @Override
    public ClusterInfoDTO getClusterInfo(String cId) {
        try {
            LOGGER.info("Fetching cluster info of cluster with id '" + cId + "'");
            Future<ClusterInfoDTO> future = managerServiceClient.getClusterInfo(cId);
            ClusterInfoDTO clusterInfoDTO = future.get();
            clusterInfoDTO.setHost(managerServiceClient.getClusterEndpoint(cId).get().getHost());
            return clusterInfoDTO;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching cluster info of cluster with id '" + cId + "'", e);
            return null;
        }
    }

    @Override
    public List<FeatureDTO> getClusterEnvironment(String cId) {
        try {
            IClusterServiceClient clusterServiceClient = getClusterServiceClient(cId);
            return clusterServiceClient.getEnvironment().get();
        } catch (InterruptedException | ExecutionException | ClientCommunicationException | ClientCreationException e) {
            LOGGER.error("Error fetching environment of cluster with id {}", cId);
            return null;
        }
    }

    @Override
    public List<String> getAllWorkerIdsOfCluster(String cId) {
        return getAllNodeIdsOfCluster(cId, NodeType.WORKER);
    }

    @Override
    public List<NodeInfoDTO> getAllWorkersOfCluster(String cId) {
        return getAllNodesOfCluster(cId, NodeType.WORKER);
    }

    @Override
    public List<String> getAllReducerIdsOfCluster(String cId) {
        return getAllNodeIdsOfCluster(cId, NodeType.REDUCER);
    }

    @Override
    public List<NodeInfoDTO> getAllReducersOfCluster(String cId) {
        return getAllNodesOfCluster(cId, NodeType.REDUCER);
    }

    @Override
    public NodeInfoDTO getNodeInfo(String cId, String nId) {
        try {
            IClusterServiceClient clusterServiceClient = getClusterServiceClient(cId);
            NodeInfoDTO node = clusterServiceClient.getNodeInfo(nId).get();
            node.setHost(clusterServiceClient.getNodeServiceEndpoint(nId).get().getHost());
            return node;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException | ClientCreationException e) {
            LOGGER.error("Error fetching worker with id '{}' of cluster with id '{}'", nId, cId, e);
            return null;
        }
    }

    @Override
    public List<FeatureDTO> getNodeEnvironment(String cId, String nId) {
        try {
            IClusterServiceClient clusterServiceClient = getClusterServiceClient(cId);
            return clusterServiceClient.getNodeEnvironment(nId).get();
        } catch (InterruptedException | ExecutionException | ClientCommunicationException | ClientCreationException e) {
            LOGGER.error("Error fetching environment of node with id '{}' of cluster with id '{}'", nId, cId, e);
            return null;
        }
    }

    private List<String> getAllNodeIdsOfCluster(String cId, NodeType nodeType) {
        try {
            IClusterServiceClient clusterServiceClient = getClusterServiceClient(cId);
            return clusterServiceClient.getAllNodes(nodeType).get();
        } catch (InterruptedException | ExecutionException | ClientCommunicationException | ClientCreationException e) {
            LOGGER.error("Error fetching all node ids of type {} of cluster with id {}", nodeType, cId);
            return null;
        }
    }

    private List<NodeInfoDTO> getAllNodesOfCluster(String cId, NodeType nodeType) {
        try {
            IClusterServiceClient clusterServiceClient = getClusterServiceClient(cId);
            List<String> nodeIds = clusterServiceClient.getAllNodes(nodeType).get();
            List<NodeInfoDTO> nodes = new LinkedList<>();
            for (String nodeId: nodeIds) {
                NodeInfoDTO node = clusterServiceClient.getNodeInfo(nodeId).get();
                node.setHost(clusterServiceClient.getNodeServiceEndpoint(nodeId).get().getHost());
                nodes.add(node);
            }
            return nodes;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException | ClientCreationException e) {
            LOGGER.error("Error fetching all nodes of type {} of cluster with id {}", nodeType, cId);
            return null;
        }
    }

    protected IClusterServiceClient getClusterServiceClient(String cId) throws ClientCommunicationException, InterruptedException, ExecutionException, ClientCreationException {
        if (!clusterClientMap.containsKey(cId)) {
            ServiceEndpointDTO serviceEndpoint = managerServiceClient.getClusterEndpoint(cId).get();
            IClusterServiceClient clusterServiceClient = new ClusterServiceClientFactory().createClient(serviceEndpoint);
            clusterClientMap.put(cId, clusterServiceClient);
        }
        return clusterClientMap.get(cId);
    }
}
