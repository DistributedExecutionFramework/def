package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.NodeCreationException;
import at.enfilo.def.cluster.api.NodeExecutionException;
import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.configuration.ClusterConfiguration;
import at.enfilo.def.cluster.util.configuration.ReducersConfiguration;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.TicketFailedException;
import at.enfilo.def.node.observer.api.util.NodeNotificationConfiguration;
import at.enfilo.def.reducer.api.IReducerServiceClient;
import at.enfilo.def.reducer.api.ReducerServiceClientFactory;
import at.enfilo.def.scheduler.reducer.api.IReducerSchedulerServiceClient;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ReducerControllerTest {

    private ReducerController reducerController;
    private ClusterConfiguration configuration = Cluster.getInstance().getConfiguration();
    private ReducerServiceClientFactory reducerServiceClientFactoryMock;
    private IReducerSchedulerServiceClient schedulerServiceClientMock;
    private IReducerServiceClient reducerServiceClientMock;
    private List<String> reducers;
    private Map<String, String> reducerInstanceMap;
    private Map<String, IReducerServiceClient> reducerConnectionMap;
    private Map<String, NodeInfoDTO> reducerInfoMap;
    private Map<String, List<FeatureDTO>> reducerFeatureMap;
    private Map<String, Set<String>> reducerJobAssignment;
    private Map<String, Set<String>> jobKeyAssignment;

    @Before
    public void setUp() throws Exception {
        // Creating general mocks
        reducerServiceClientFactoryMock = Mockito.mock(ReducerServiceClientFactory.class);
        reducerServiceClientMock = Mockito.mock(IReducerServiceClient.class);
        schedulerServiceClientMock = Mockito.mock(IReducerSchedulerServiceClient.class);

        ClusterResource.getInstance().setReducerSchedulerServiceClient(schedulerServiceClientMock);

        // Create lists and maps for controller
        reducers = new LinkedList<>();
        reducerInstanceMap = new HashMap<>();
        reducerConnectionMap = new HashMap<>();
        reducerInfoMap = new HashMap<>();
        reducerFeatureMap = new HashMap<>();
        reducerJobAssignment = new HashMap<>();
        jobKeyAssignment = new HashMap<>();

        // Create controller with special constructor
        Constructor<ReducerController> constructor = ReducerController.class.getDeclaredConstructor(
                ReducerServiceClientFactory.class,
                List.class,
                Map.class,
                Map.class,
                Map.class,
                Map.class,
                ReducersConfiguration.class,
                Map.class,
                Map.class
        );
        constructor.setAccessible(true);
        reducerController = constructor.newInstance(
                reducerServiceClientFactoryMock,
                reducers,
                reducerInstanceMap,
                reducerConnectionMap,
                reducerInfoMap,
                reducerFeatureMap,
                Cluster.getInstance().getConfiguration().getReducersConfiguration(),
                reducerJobAssignment,
                jobKeyAssignment
        );
    }

    @Test
    public void getReducerInfo() throws Exception {
        String reducerId = UUID.randomUUID().toString();
        NodeInfoDTO info = new NodeInfoDTO(
                reducerId,
                ClusterResource.getInstance().getId(),
                NodeType.REDUCER,
                -1,
                -1,
                -1,
                new HashMap<>(),
                "localhost"
        );
        reducers.add(reducerId);
        reducerInfoMap.put(reducerId, info);

        NodeInfoDTO receivedInfo = reducerController.getNodeInfo(reducerId);

        assertEquals(info, receivedInfo);
    }

    @Test (expected = UnknownNodeException.class)
    public void getReducerInfo_failed() throws Exception {
        reducerController.getNodeInfo(UUID.randomUUID().toString());
    }

    @Test
    public void getServiceClient() throws Exception {
        String reducerId = UUID.randomUUID().toString();

        reducers.add(reducerId);
        reducerConnectionMap.put(reducerId, reducerServiceClientMock);

        IReducerServiceClient client = reducerController.getServiceClient(reducerId);
        assertEquals(reducerServiceClientMock, client);
    }

    @Test (expected = UnknownNodeException.class)
    public void getServiceClient_failed() throws Exception {
        reducerController.getServiceClient(UUID.randomUUID().toString());
    }

    @Test
    public void addReducer() throws Exception {
        NodeInfoDTO reducerInfo = new NodeInfoDTO();
        reducerInfo.setId(UUID.randomUUID().toString());
        reducerInfo.setClusterId(ClusterResource.getInstance().getId());

        FeatureDTO feature = new FeatureDTO();
        feature.setName("java");
        feature.setVersion("1.8");
        feature.setGroup("language");
        List<FeatureDTO> featureList = Collections.singletonList(feature);

        ServiceEndpointDTO serviceEndpoint = new ServiceEndpointDTO();
        Future<Void> futureStatus = Mockito.mock(Future.class);
        Future<NodeInfoDTO> futureInfo = Mockito.mock(Future.class);
        Future<Void> futureSetReducers = Mockito.mock(Future.class);
        Future<Void> futureRegisterObserver = Mockito.mock(Future.class);
        Future<List<FeatureDTO>> futureEnvironment = Mockito.mock(Future.class);

        // Setup observer config / service endpoint
        NodeNotificationConfiguration notificationConfig = configuration.getReducersConfiguration().getNotificationFromNode();
        ServiceEndpointDTO observerEndpoint = notificationConfig.getEndpoint();
        observerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());

        // Mock actions
        when(reducerServiceClientFactoryMock.createClient(anyObject())).thenReturn(reducerServiceClientMock);
        when(reducerServiceClientMock.takeControl(anyString())).thenReturn(futureStatus);
        when(futureStatus.get()).thenReturn(null);
        when(reducerServiceClientMock.getInfo()).thenReturn(futureInfo);
        when(futureInfo.get()).thenReturn(reducerInfo);
        when(reducerServiceClientMock.getFeatures()).thenReturn(futureEnvironment);
        when(futureEnvironment.get()).thenReturn(featureList);
        when(schedulerServiceClientMock.addReducer(reducerInfo.getId(), serviceEndpoint)).thenReturn(futureSetReducers);
        when(futureSetReducers.get()).thenReturn(null);
        when(reducerServiceClientMock.registerObserver(
                observerEndpoint,
                notificationConfig.isPeriodically(),
                notificationConfig.getPeriodDuration(),
                notificationConfig.getPeriodUnit()
        )).thenReturn(futureRegisterObserver);
        when(futureRegisterObserver.get()).thenReturn(null);
        when(reducerServiceClientMock.setStoreRoutine(anyString())).thenReturn(futureStatus);

        reducerController.addReducer(serviceEndpoint);

        assertEquals(1, reducers.size());
        assertTrue(reducerInfoMap.containsKey(reducerInfo.getId()));
        assertTrue(reducerFeatureMap.containsKey(reducerInfo.getId()));
        verify(schedulerServiceClientMock).addReducer(reducerInfo.getId(), serviceEndpoint);
    }

    @Test (expected = NodeCreationException.class)
    public void addReducer_failed() throws Exception {
        NodeInfoDTO reducerInfo = new NodeInfoDTO();
        reducerInfo.setId(UUID.randomUUID().toString());

        ServiceEndpointDTO serviceEndpoint = new ServiceEndpointDTO();
        Future<Void> futureStatus = Mockito.mock(Future.class);

        when(reducerServiceClientFactoryMock.createClient(anyObject())).thenReturn(reducerServiceClientMock);
        when(reducerServiceClientMock.takeControl(anyString())).thenReturn(futureStatus);
        when(futureStatus.get()).thenThrow(new TicketFailedException("failed"));

        reducerController.addReducer(serviceEndpoint);
    }

    @Test
    public void notifyNodeInfo() throws Exception {
        // Prepare a reducer
        String nId = UUID.randomUUID().toString();
        NodeInfoDTO nodeInfo = new NodeInfoDTO();
        nodeInfo.setParameters(new HashMap<>());
        nodeInfo.setId(nId);
        nodeInfo.getParameters().put("numberOfQueuedElements", "1");
        nodeInfo.setLoad(1.1);

        reducers.add(nId);
        reducerInfoMap.put(nId, nodeInfo);

        assertEquals(nodeInfo, reducerController.getNodeInfo(nId));

        // New nodeInfo + notification
        NodeInfoDTO newNodeInfo = new NodeInfoDTO();
        newNodeInfo.setId(nId);
        nodeInfo.getParameters().put("numberOfQueuedElements", "4");
        newNodeInfo.setLoad(2.1);
        reducerController.notifyNodeInfo(nId, newNodeInfo);
        assertEquals(newNodeInfo, reducerController.getNodeInfo(nId));
    }

    @Test (expected = UnknownNodeException.class)
    public void notfiyNodeInfo_failed() throws Exception {
        NodeInfoDTO nodeInfo = new NodeInfoDTO();
        reducerController.notifyNodeInfo(UUID.randomUUID().toString(), nodeInfo);
    }

    @Test
    public void notifyReduceKeysReceived() throws Exception {
        // Prepare Reducer
        String nId = UUID.randomUUID().toString();
        NodeInfoDTO nInfo = new NodeInfoDTO();
        nInfo.setId(nId);

        reducerInfoMap.put(nId, nInfo);
        reducers.add(nId);

        assertTrue(reducerJobAssignment.isEmpty());
        assertTrue(jobKeyAssignment.isEmpty());

        String jId = UUID.randomUUID().toString();
        List<String> reduceKeys = new LinkedList<>();
        String rKey1 = "firstKey";
        String rKey2 = "secondKey";
        reduceKeys.add(rKey1);
        reduceKeys.add(rKey2);

        // Notify and proof
        reducerController.notifyReduceKeysReceived(nId, jId, reduceKeys);
        assertFalse(reducerJobAssignment.isEmpty());
        assertFalse(jobKeyAssignment.isEmpty());
        assertNotNull(reducerJobAssignment.get(nId));
        assertNotNull(jobKeyAssignment.get(jId));
        assertTrue(reducerJobAssignment.get(nId).contains(jId));
        assertTrue(jobKeyAssignment.get(jId).contains(rKey1));
        assertTrue(jobKeyAssignment.get(jId).contains(rKey2));
    }

    @Test
    public void notifyJobsSuccess() throws Exception {
        // Prepare reducer and assignments
        String nId = UUID.randomUUID().toString();
        NodeInfoDTO nInfo = new NodeInfoDTO();
        nInfo.setId(nId);

        reducerInfoMap.put(nId, nInfo);
        reducers.add(nId);

        reducerJobAssignment.put(nId, new HashSet<>());
        String j1Id = UUID.randomUUID().toString();
        String j2Id = UUID.randomUUID().toString();
        reducerJobAssignment.get(nId).add(j1Id);
        reducerJobAssignment.get(nId).add(j2Id);
        jobKeyAssignment.put(j1Id, new HashSet<>());
        jobKeyAssignment.put(j2Id, new HashSet<>());
        String r11Key = "key11";
        String r12Key = "key12";
        String r21Key = "key21";
        String r22Key = "key22";
        jobKeyAssignment.get(j1Id).add(r11Key);
        jobKeyAssignment.get(j1Id).add(r12Key);
        jobKeyAssignment.get(j2Id).add(r21Key);
        jobKeyAssignment.get(j2Id).add(r22Key);

        // Notify and proof
        reducerController.notifyJobsNewState(nId, Collections.singletonList(j1Id), ExecutionState.SUCCESS);
        assertFalse(reducerJobAssignment.isEmpty());
        assertFalse(jobKeyAssignment.isEmpty());
        assertFalse(reducerJobAssignment.get(nId).contains(j1Id));
        assertFalse(jobKeyAssignment.containsKey(j1Id));
        assertTrue(reducerJobAssignment.get(nId).contains(j2Id));
        assertTrue(jobKeyAssignment.get(j2Id).contains(r21Key));
        assertTrue(jobKeyAssignment.get(j2Id).contains(r22Key));
    }

    @Test
    public void removeReducer() throws Exception {
        // Prepare reducers
        String n1Id = UUID.randomUUID().toString();
        NodeInfoDTO n1Info = new NodeInfoDTO();
        n1Info.setId(n1Id);
        String n2Id = UUID.randomUUID().toString();
        NodeInfoDTO n2Info = new NodeInfoDTO();
        n2Info.setId(n2Id);
        String n3Id = UUID.randomUUID().toString();
        NodeInfoDTO n3Info = new NodeInfoDTO();
        n3Info.setId(n3Id);

        reducers.add(n1Id);
        reducers.add(n2Id);
        reducers.add(n3Id);
        reducerInfoMap.put(n1Id, n1Info);
        reducerInfoMap.put(n2Id, n2Info);
        reducerInfoMap.put(n3Id, n3Info);
        reducerInstanceMap.put(n1Id, null);
        reducerInstanceMap.put(n2Id, null);
        reducerInstanceMap.put(n3Id, null);
        reducerConnectionMap.put(n1Id, reducerServiceClientMock);
        reducerConnectionMap.put(n2Id, reducerServiceClientMock);
        reducerConnectionMap.put(n3Id, reducerServiceClientMock);

        // Mocking
        Future<Void> future = Mockito.mock(Future.class);
        when(reducerServiceClientMock.deregisterObserver(anyObject())).thenReturn(future);
        when(future.get()).thenReturn(null);
        when(schedulerServiceClientMock.removeReducer(anyString())).thenReturn(future);

        reducerController.removeNode(n2Id, true);

        assertFalse(reducers.contains(n2Id));
        assertFalse(reducerInstanceMap.containsKey(n2Id));
        assertFalse(reducerInfoMap.containsKey(n2Id));
        assertFalse(reducerConnectionMap.containsKey(n2Id));
        assertEquals(2, reducerConnectionMap.size());
        assertEquals(2, reducerInfoMap.size());
        assertEquals(2, reducerInstanceMap.size());
        assertEquals(2, reducers.size());
    }

    @Test (expected = UnknownNodeException.class)
    public void removeReducer_failed() throws Exception {
        String rId = UUID.randomUUID().toString();
        reducerController.removeNode(rId, true);
    }

    @Test
    public void getReducerServiceEndpoint() throws Exception {
        Map<String, ServiceEndpointDTO> endpoints = new HashMap<>();
        int nr = new Random().nextInt(5) + 5;
        ReducerServiceClientFactory factory = new ReducerServiceClientFactory();
        for (int i  = 0; i < nr; i++) {
            String rId = UUID.randomUUID().toString();
            ServiceEndpointDTO endpoint = new ServiceEndpointDTO(UUID.randomUUID().toString(), i, Protocol.REST);
            endpoints.put(rId, endpoint);
            reducers.add(rId);
            reducerConnectionMap.put(rId, factory.createClient(endpoint));
            reducerInfoMap.put(rId, null);
        }

        for (Map.Entry<String, ServiceEndpointDTO> e: endpoints.entrySet()) {
            ServiceEndpointDTO endpoint = reducerController.getNodeServiceEndpoint(e.getKey());
            assertEquals(e.getValue(), endpoint);
        }
    }

    @Test (expected = UnknownNodeException.class)
    public void getReducerServiceEndpoint_failed() throws Exception {
        String rId = UUID.randomUUID().toString();
        reducerController.getNodeServiceEndpoint(rId);
    }

    @Test
    public void getAndSetRoutines() throws Exception {
        assertNotNull(reducerController.getStoreRoutineId());

        String storeRoutineId = UUID.randomUUID().toString();

        // Add 3 reducers
        String r1Id = UUID.randomUUID().toString();
        String r2Id = UUID.randomUUID().toString();
        String r3Id = UUID.randomUUID().toString();
        reducerConnectionMap.put(r1Id, reducerServiceClientMock);
        reducerConnectionMap.put(r2Id, reducerServiceClientMock);
        reducerConnectionMap.put(r3Id, reducerServiceClientMock);

        // Mocking actions
        Future<Void> future = Mockito.mock(Future.class);
        when(reducerServiceClientMock.setStoreRoutine(storeRoutineId)).thenReturn(future);
        when(future.get()).thenReturn(null);

        reducerController.setStoreRoutineId(storeRoutineId);
        assertEquals(storeRoutineId, reducerController.getStoreRoutineId());

        verify(reducerServiceClientMock, times(3)).setStoreRoutine(storeRoutineId);
    }

    @Test
    public void getInstance() throws Exception {
        ReducerController rc = ReducerController.getInstance();
        assertEquals(0, rc.getAllNodeIds().size());

        ReducerController rc2 = ReducerController.getInstance();
        assertSame(rc, rc2);
    }

    @Test
    public void findNodesForShutdown() {
        ReducerController rc = ReducerController.getInstance();

        String n1Id = UUID.randomUUID().toString();
        NodeInfoDTO n1Info = new NodeInfoDTO();
        n1Info.setId(n1Id);
        n1Info.putToParameters("numberOfQueuedElements", "1");

        String n2Id = UUID.randomUUID().toString();
        NodeInfoDTO n2Info = new NodeInfoDTO();
        n2Info.setId(n2Id);
        n2Info.putToParameters("numberOfQueuedElements", "3");

        String n3Id = UUID.randomUUID().toString();
        NodeInfoDTO n3Info = new NodeInfoDTO();
        n3Info.setId(n3Id);
        n3Info.putToParameters("numberOfQueuedElements", "2");

        String n4Id = UUID.randomUUID().toString();
        NodeInfoDTO n4Info = new NodeInfoDTO();
        n4Info.setId(n4Id);
        n4Info.putToParameters("numberOfQueuedElements", "4");

        int nrOfNodesToShutdown = 2;
        Map<String, NodeInfoDTO> nodesMap = new HashMap<>();
        nodesMap.put(n1Id, n1Info);
        nodesMap.put(n2Id, n2Info);
        nodesMap.put(n3Id, n3Info);
        nodesMap.put(n4Id, n4Info);
        rc.nodeInfoMap.clear();
        rc.nodeInfoMap.putAll(nodesMap);

        List<String> nodeIds = rc.findNodesForShutdown(nrOfNodesToShutdown);

        assertEquals(nrOfNodesToShutdown, nodeIds.size());
        assertTrue(nodeIds.contains(n1Id));
        assertFalse(nodeIds.contains(n2Id));
        assertTrue(nodeIds.contains(n3Id));
        assertFalse(nodeIds.contains(n4Id));
    }

    @Test (expected = IllegalArgumentException.class)
    public void findNodesForShutdown_failed() {
        ReducerController rc = ReducerController.getInstance();

        rc.findNodesForShutdown(10);
    }

    @Test
    public void removeReduceJob() throws Exception {
        String rId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();
        Set<String> jIds = new HashSet<>();
        jIds.add(jId);

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenReturn(null);
        when(schedulerServiceClientMock.removeReduceJob(jId)).thenReturn(future);

        reducerJobAssignment.put(rId, jIds);

        reducerController.deleteReduceJob(jId);

        verify(schedulerServiceClientMock).removeReduceJob(jId);
    }

    @Test
    public void removeReduceJob_missingReducerJobAssignment() throws Exception {
        String jId = UUID.randomUUID().toString();

        reducerController.deleteReduceJob(jId);

        verify(schedulerServiceClientMock, times(0)).removeReduceJob(jId);
    }

    @Test (expected = NodeExecutionException.class)
    public void removeReduceJob_failed() throws Exception {
        String rId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();
        Set<String> jIds = new HashSet<>();
        jIds.add(jId);

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenThrow(new TicketFailedException("failed"));
        when(schedulerServiceClientMock.removeReduceJob(jId)).thenReturn(future);

        reducerJobAssignment.put(rId, jIds);

        reducerController.deleteReduceJob(jId);
    }

    @Test
    public void scheduleResourcesToReduce() throws Exception {
        String jId = UUID.randomUUID().toString();
        List<ResourceDTO> resources = new LinkedList<>();
        resources.add(new ResourceDTO());
        resources.add(new ResourceDTO());

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenReturn(null);
        when(schedulerServiceClientMock.scheduleResourcesToReduce(jId, resources)).thenReturn(future);

        reducerController.scheduleResourcesToReduce(jId, resources);

        verify(schedulerServiceClientMock).scheduleResourcesToReduce(jId, resources);
    }

    @Test (expected = NodeExecutionException.class)
    public void scheduleResourcesToReduce_failed() throws Exception {
        String jId = UUID.randomUUID().toString();
        List<ResourceDTO> resources = new LinkedList<>();
        resources.add(new ResourceDTO());
        resources.add(new ResourceDTO());

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenThrow(new TicketFailedException("failed"));
        when(schedulerServiceClientMock.scheduleResourcesToReduce(jId, resources)).thenReturn(future);

        reducerController.scheduleResourcesToReduce(jId, resources);
    }

    @Test
    public void finalizeReduce() throws Exception {
        String jId = UUID.randomUUID().toString();
        JobDTO job = new JobDTO();
        job.setId(jId);

        Future<JobDTO> future = Mockito.mock(Future.class);
        when(future.get()).thenReturn(job);
        when(schedulerServiceClientMock.finalizeReduce(jId)).thenReturn(future);

        JobDTO fetchedJob = reducerController.finalizeReduce(jId);

        assertEquals(job, fetchedJob);
        verify(schedulerServiceClientMock).finalizeReduce(jId);
    }

    @Test
    public void addReduceJob() throws Exception {
        JobDTO job = new JobDTO();

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenReturn(null);
        when(schedulerServiceClientMock.addReduceJob(job)).thenReturn(future);

        reducerController.addReduceJob(job);

        verify(schedulerServiceClientMock).addReduceJob(job);
    }

    @Test (expected = NodeExecutionException.class)
    public void addReduceJob_failed() throws Exception {
        JobDTO job = new JobDTO();

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenThrow(new TicketFailedException("failed"));
        when(schedulerServiceClientMock.addReduceJob(job)).thenReturn(future);

        reducerController.addReduceJob(job);
    }
}
