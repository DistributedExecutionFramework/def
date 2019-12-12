package at.enfilo.def.manager.webservice.mocks;

import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.manager.webservice.impl.ClusterServiceImpl;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClusterServiceImplMock extends ClusterServiceImpl {

    public Future<List<String>> clusterIdFuture;
    public Future<ClusterInfoDTO> clusterFuture;
    public List<String> clusterIds;
    public Future<ServiceEndpointDTO> endpointFuture;
    public IClusterServiceClient clusterServiceClient;
    public List<String> workerIds;
    public Future<List<String>> workerIdsFuture;
    public Future<NodeInfoDTO> workerFuture;
    public Future<List<FeatureDTO>> featureFuture;
    public ServiceEndpointDTO endpoint;

    public ClusterServiceImplMock() {
        super();
        initMockingComponents();
    }

    private void initMockingComponents() {
        managerServiceClient = Mockito.mock(IManagerServiceClient.class);
        clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
        clusterClientMap = Mockito.mock(TimeoutMap.class);

        clusterIds = new LinkedList<>();
        clusterIds.add(UUID.randomUUID().toString());
        clusterIds.add(UUID.randomUUID().toString());
        clusterIds.add(UUID.randomUUID().toString());

        workerIds = new LinkedList<>();
        workerIds.add(UUID.randomUUID().toString());
        workerIds.add(UUID.randomUUID().toString());
        workerIds.add(UUID.randomUUID().toString());

        endpoint = new ServiceEndpointDTO();
        endpoint.setHost("localhost");

        endpointFuture = new Future<ServiceEndpointDTO>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public ServiceEndpointDTO get() throws InterruptedException, ExecutionException {
                return endpoint;
            }

            @Override
            public ServiceEndpointDTO get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        clusterIdFuture = new Future<List<String>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public List<String> get() throws InterruptedException, ExecutionException {
                return clusterIds;
            }

            @Override
            public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        clusterFuture = new Future<ClusterInfoDTO>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public ClusterInfoDTO get() throws InterruptedException, ExecutionException {
                return new ClusterInfoDTO();
            }

            @Override
            public ClusterInfoDTO get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        workerIdsFuture = new Future<List<String>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public List<String> get() throws InterruptedException, ExecutionException {
                return workerIds;
            }

            @Override
            public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        workerFuture = new Future<NodeInfoDTO>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public NodeInfoDTO get() throws InterruptedException, ExecutionException {
                return new NodeInfoDTO();
            }

            @Override
            public NodeInfoDTO get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        featureFuture = new Future<List<FeatureDTO>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public List<FeatureDTO> get() throws InterruptedException, ExecutionException {
                List<FeatureDTO> featureDTOS = new ArrayList<>();
                featureDTOS.add(new FeatureDTO());
                return featureDTOS;
            }

            @Override
            public List<FeatureDTO> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
    }
}
