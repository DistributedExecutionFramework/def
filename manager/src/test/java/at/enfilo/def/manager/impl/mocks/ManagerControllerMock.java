package at.enfilo.def.manager.impl.mocks;

import at.enfilo.def.cloud.communication.api.ICloudCommunicationServiceClient;
import at.enfilo.def.cloud.communication.dto.InstanceTypeDTO;
import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.communication.exception.TakeControlException;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.manager.impl.ManagerController;
import at.enfilo.def.manager.impl.UnknownClusterException;
import at.enfilo.def.manager.util.ProgramClusterRegistry;
import at.enfilo.def.node.api.NodeServiceClientFactory;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ManagerControllerMock extends ManagerController {

    private boolean callBootNodeInstancesInCluster = true;
    private boolean callAddCluster = true;
    private boolean callCreateCluster = true;
    private boolean callGetCloudClusterId = true;
    private boolean callGetClusterInfo = true;
    private boolean callGetClusterEndpoint = true;
    private ClusterInfoDTO clusterInfoDTO;

    public int counterBootNodeInstancesInClusterCalls = 0;

    public ManagerControllerMock (
            ProgramClusterRegistry registry,
            ClusterServiceClientFactory clusterServiceClientFactory,
            ICloudCommunicationServiceClient cloudCommunicationServiceClient
    ) {
        super(registry, clusterServiceClientFactory, cloudCommunicationServiceClient);

        clusterInfoDTO = new ClusterInfoDTO();
        clusterInfoDTO.setHost("127.0.0.1");
    }

    public void setCallBootNodeInstancesInCluster(boolean callBootNodeInstancesInCluster) {
        this.callBootNodeInstancesInCluster = callBootNodeInstancesInCluster;
    }

    public void setCallAddCluster(boolean callAddCluster) {
        this.callAddCluster = callAddCluster;
    }

    public void setCallCreateCluster(boolean callCreateCluster) {
        this.callCreateCluster = callCreateCluster;
    }

    public void setCallGetCloudClusterId(boolean callGetCloudClusterId) {
        this.callGetCloudClusterId = callGetCloudClusterId;
    }

    public void setCallGetClusterInfo(boolean callGetClusterInfo) {
        this.callGetClusterInfo = callGetClusterInfo;
    }

    public void setNumberOfWorkersInClusterInfoDTO(int numberOfWorkers) {
        this.clusterInfoDTO.setNumberOfWorkers(numberOfWorkers);
    }

    public void setNumberOfReducersInClusterInfoDTO(int numberOfReducers) {
        this.clusterInfoDTO.setNumberOfReducers(numberOfReducers);
    }

    public void setCallGetClusterEndpoint(boolean callGetClusterEndpoint) {
        this.callGetClusterEndpoint = callGetClusterEndpoint;
    }

    @Override
    public void bootNodeInstancesInCluster(String cloudClusterId, IClusterServiceClient clusterServiceClient, ServiceEndpointDTO clusterLibraryEndpoint, LibraryServiceClientFactory libraryServiceClientFactory, InstanceTypeDTO instanceType, int nrOfNodes) {
        this.counterBootNodeInstancesInClusterCalls++;
        if (callBootNodeInstancesInCluster) {
            super.bootNodeInstancesInCluster(cloudClusterId, clusterServiceClient, clusterLibraryEndpoint, libraryServiceClientFactory, instanceType, nrOfNodes);
        }
    }

    @Override
    public String addCluster(ServiceEndpointDTO serviceEndpoint) throws InterruptedException, ExecutionException, ClientCreationException, TakeControlException, ClientCommunicationException {
        if (callAddCluster) {
            return super.addCluster(serviceEndpoint);
        }
        return UUID.randomUUID().toString();
    }

    @Override
    public String addCluster(IClusterServiceClient serviceClient) throws InterruptedException, ExecutionException, TakeControlException, ClientCommunicationException {
        if (callAddCluster) {
            return super.addCluster(serviceClient);
        }
        return UUID.randomUUID().toString();
    }

    @Override
    public String createCluster(String cloudClusterId, int numberOfWorkers, int numberOfReducers, LibraryServiceClientFactory libraryServiceClientFactory) throws InterruptedException, ExecutionException, ClientCreationException, TakeControlException, ClientCommunicationException {
        if (callCreateCluster) {
            return super.createCluster(cloudClusterId, numberOfWorkers, numberOfReducers, libraryServiceClientFactory);
        }
        return UUID.randomUUID().toString();
    }

    @Override
    public String getCloudClusterId(String cId) {
        if (callGetCloudClusterId) {
            return super.getCloudClusterId(cId);
        }
        return UUID.randomUUID().toString();
    }

    @Override
    public ClusterInfoDTO getClusterInfo(String cId) throws UnknownClusterException, InterruptedException, ExecutionException, ClientCommunicationException, ClientCreationException {
        if (callGetClusterInfo) {
            return super.getClusterInfo(cId);
        }
        return this.clusterInfoDTO;
    }

    @Override
    public void adjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType, ClusterServiceClientFactory clusterServiceClientFactory) throws UnknownClusterException, InterruptedException, ExecutionException, ClientCommunicationException, ClientCreationException {
        super.adjustNodePoolSize(cId, newNodePoolSize, nodeType, clusterServiceClientFactory);
    }

    @Override
    public ServiceEndpointDTO getClusterEndpoint(String cId) throws UnknownClusterException {
        if (callGetClusterEndpoint) {
            return super.getClusterEndpoint(cId);
        }
        return new ServiceEndpointDTO();
    }
}
