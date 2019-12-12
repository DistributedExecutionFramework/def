package at.enfilo.def.cluster.impl;

import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import at.enfilo.def.clientroutine.worker.api.IClientRoutineWorkerServiceClient;
import at.enfilo.def.cluster.api.NodeCreationException;
import at.enfilo.def.cluster.api.NodeExecutionException;
import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.configuration.ClientRoutineWorkersConfiguration;
import at.enfilo.def.cluster.util.configuration.ClusterConfiguration;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.TicketFailedException;
import at.enfilo.def.node.observer.api.util.NodeNotificationConfiguration;
import at.enfilo.def.scheduler.clientroutineworker.api.IClientRoutineWorkerSchedulerServiceClient;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class ClientRoutineWorkerControllerTest {

    private ClientRoutineWorkerController clientRoutineWorkerController;
    private ClusterConfiguration clusterConfiguration = Cluster.getInstance().getConfiguration();
    private ClientRoutineWorkerServiceClientFactory factoryMock;
    private IClientRoutineWorkerSchedulerServiceClient schedulerServiceClientMock;
    private IClientRoutineWorkerServiceClient serviceClientMock;
    private List<String> clientRoutineWorkers;
    private Map<String, String> clientRoutineWorkerInstanceMap;
    private Map<String, IClientRoutineWorkerServiceClient> connectionMap;
    private Map<String, NodeInfoDTO> infoMap;
    private Map<String, List<FeatureDTO>> featureMap;
    private Map<String, Set<String>> workerProgramAssignment;

    @Before
    public void setUp() throws Exception {
        // Creating general mocks
        factoryMock = Mockito.mock(ClientRoutineWorkerServiceClientFactory.class);
        serviceClientMock = Mockito.mock(IClientRoutineWorkerServiceClient.class);
        schedulerServiceClientMock = Mockito.mock(IClientRoutineWorkerSchedulerServiceClient.class);

        ClusterResource.getInstance().setClientRoutineWorkerSchedulerSerivceClient(schedulerServiceClientMock);

        // Create lists and maps of NodeController
        clientRoutineWorkers = new LinkedList<>();
        clientRoutineWorkerInstanceMap = new HashMap<>();
        connectionMap = new HashMap<>();
        infoMap = new HashMap<>();
        featureMap = new HashMap<>();
        workerProgramAssignment = new HashMap<>();

        // Create controller with special constructor
        Constructor<ClientRoutineWorkerController> constructor = ClientRoutineWorkerController.class.getDeclaredConstructor(
                ClientRoutineWorkerServiceClientFactory.class,
                List.class,
                Map.class,
                Map.class,
                Map.class,
                Map.class,
                ClientRoutineWorkersConfiguration.class,
                Map.class
        );
        constructor.setAccessible(true);
        clientRoutineWorkerController = constructor.newInstance(
                factoryMock,
                clientRoutineWorkers,
                clientRoutineWorkerInstanceMap,
                connectionMap,
                infoMap,
                featureMap,
                Cluster.getInstance().getConfiguration().getClientRoutineWorkersConfiguration(),
                workerProgramAssignment
        );
    }

    @Test
    public void getClientRoutineWorkerInfo() throws Exception {
        String wId = UUID.randomUUID().toString();
        NodeInfoDTO info = new NodeInfoDTO(
                wId,
                ClusterResource.getInstance().getId(),
                NodeType.CLIENT,
                -1,
                -1,
                -1,
                new HashMap<>(),
                "localhost"
        );
        clientRoutineWorkers.add(wId);
        infoMap.put(wId, info);

        NodeInfoDTO receivedInfo = clientRoutineWorkerController.getNodeInfo(wId);

        assertEquals(info, receivedInfo);
    }

    @Test (expected = UnknownNodeException.class)
    public void getClientRoutineWorkerInfo_failed() throws Exception {
        clientRoutineWorkerController.getNodeInfo(UUID.randomUUID().toString());
    }

    @Test
    public void getServiceClient() throws Exception {
        String wId = UUID.randomUUID().toString();

        clientRoutineWorkers.add(wId);
        connectionMap.put(wId, serviceClientMock);

        IClientRoutineWorkerServiceClient client = clientRoutineWorkerController.getServiceClient(wId);
        assertEquals(serviceClientMock, client);
    }

    @Test (expected = UnknownNodeException.class)
    public void getServiceClient_failed() throws Exception {
        String wId = UUID.randomUUID().toString();
        String unknownWId = UUID.randomUUID().toString();

        clientRoutineWorkers.add(wId);
        connectionMap.put(wId, serviceClientMock);

        IClientRoutineWorkerServiceClient client = clientRoutineWorkerController.getServiceClient(unknownWId);
    }

    @Test
    public void addClientRoutineWorker() throws Exception {
        NodeInfoDTO workerInfo = new NodeInfoDTO();
        workerInfo.setId(UUID.randomUUID().toString());
        workerInfo.setClusterId(ClusterResource.getInstance().getId());

        FeatureDTO feature = new FeatureDTO();
        feature.setName("java");
        feature.setVersion("1.8");
        feature.setGroup("language");
        List<FeatureDTO> featureList = Collections.singletonList(feature);

        ServiceEndpointDTO serviceEndpoint = new ServiceEndpointDTO();
        Future<Void> futureStatus = Mockito.mock(Future.class);
        Future<NodeInfoDTO> futureInfo = Mockito.mock(Future.class);
        Future<List<FeatureDTO>> futureEnvironment = Mockito.mock(Future.class);

        // Setup observer config / service endpoint
        NodeNotificationConfiguration notificationConfig = clusterConfiguration.getClientRoutineWorkersConfiguration().getNotificationFromNode();
        ServiceEndpointDTO observerEndpoint = notificationConfig.getEndpoint();
        observerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());

        // Mock actions
        when(factoryMock.createClient(anyObject())).thenReturn(serviceClientMock);
        when(serviceClientMock.takeControl(anyString())).thenReturn(futureStatus);
        when(futureStatus.get()).thenReturn(null);
        when(serviceClientMock.getInfo()).thenReturn(futureInfo);
        when(futureInfo.get()).thenReturn(workerInfo);
        when(serviceClientMock.getFeatures()).thenReturn(futureEnvironment);
        when(futureEnvironment.get()).thenReturn(featureList);
        Future<Void> futureSetClientRoutineWorkers = Mockito.mock(Future.class);
        when(schedulerServiceClientMock.addClientRoutineWorker(workerInfo.getId(), serviceEndpoint)).thenReturn(futureSetClientRoutineWorkers);
        when(futureSetClientRoutineWorkers.get()).thenReturn(null);
        Future<Void> futureRegisterObserver = Mockito.mock(Future.class);
        when(serviceClientMock.registerObserver(
                anyObject(),
                anyBoolean(),
                anyInt(),
                any()
        )).thenReturn(futureRegisterObserver);
        when(futureRegisterObserver.get()).thenReturn(null);
        when(serviceClientMock.setStoreRoutine(anyString())).thenReturn(futureStatus);

        clientRoutineWorkerController.addClientRoutineWorker(serviceEndpoint);

        assertEquals(1, clientRoutineWorkers.size());
        assertTrue(infoMap.containsKey(workerInfo.getId()));
        assertTrue(featureMap.containsKey(workerInfo.getId()));
        verify(schedulerServiceClientMock).addClientRoutineWorker(workerInfo.getId(), serviceEndpoint);
    }

    @Test (expected = NodeCreationException.class)
    public void addClientRoutineWorker_failed() throws Exception {
        NodeInfoDTO workerInfo = new NodeInfoDTO();
        workerInfo.setId(UUID.randomUUID().toString());

        ServiceEndpointDTO serviceEndpoint = new ServiceEndpointDTO();
        Future<Void> futureStatus = Mockito.mock(Future.class);

        when(factoryMock.createClient(anyObject())).thenReturn(serviceClientMock);
        when(serviceClientMock.takeControl(anyString())).thenReturn(futureStatus);
        when(futureStatus.get()).thenThrow(new TicketFailedException("failed"));

        clientRoutineWorkerController.addClientRoutineWorker(serviceEndpoint);
    }

    @Test
    public void removeClientRoutineWorker() throws Exception {
        // Prepare client routine workers
        String n1Id = UUID.randomUUID().toString();
        NodeInfoDTO n1Info = new NodeInfoDTO();
        n1Info.setId(n1Id);

        String n2Id = UUID.randomUUID().toString();
        NodeInfoDTO n2Info = new NodeInfoDTO();
        n2Info.setId(n2Id);

        String n3Id = UUID.randomUUID().toString();
        NodeInfoDTO n3Info = new NodeInfoDTO();
        n3Info.setId(n3Id);

        clientRoutineWorkers.add(n1Id);
        clientRoutineWorkers.add(n2Id);
        clientRoutineWorkers.add(n3Id);
        infoMap.put(n1Id, n1Info);
        infoMap.put(n2Id, n2Info);
        infoMap.put(n3Id, n3Info);
        clientRoutineWorkerInstanceMap.put(n1Id, null);
        clientRoutineWorkerInstanceMap.put(n2Id, null);
        clientRoutineWorkerInstanceMap.put(n3Id, null);
        connectionMap.put(n1Id, serviceClientMock);
        connectionMap.put(n2Id, serviceClientMock);
        connectionMap.put(n3Id, serviceClientMock);

        // Mocking
        Future<Void> future = Mockito.mock(Future.class);
        when(serviceClientMock.deregisterObserver(anyObject())).thenReturn(future);
        when(future.get()).thenReturn(null);
        when(schedulerServiceClientMock.removeClientRoutineWorker(anyString())).thenReturn(future);

        clientRoutineWorkerController.removeNode(n2Id, true);

        assertFalse(clientRoutineWorkers.contains(n2Id));
        assertFalse(clientRoutineWorkerInstanceMap.containsKey(n2Id));
        assertFalse(infoMap.containsKey(n2Id));
        assertFalse(connectionMap.containsKey(n2Id));
        assertEquals(2, connectionMap.size());
        assertEquals(2, infoMap.size());
        assertEquals(2, clientRoutineWorkerInstanceMap.size());
        assertEquals(2, clientRoutineWorkers.size());
    }

    @Test (expected = UnknownNodeException.class)
    public void removeClientRoutineWorker_failed() throws Exception {
        clientRoutineWorkerController.removeNode(UUID.randomUUID().toString(), true);
    }

    @Test
    public void notifyNodeInfo() throws Exception {
        // Prepare a client routine worker
        String nId = UUID.randomUUID().toString();
        NodeInfoDTO nodeInfo = new NodeInfoDTO();
        nodeInfo.setParameters(new HashMap<>());
        nodeInfo.setId(nId);
        nodeInfo.getParameters().put("numberOfQueuedElements", "1");
        nodeInfo.setLoad(1.1);

        clientRoutineWorkers.add(nId);
        infoMap.put(nId, nodeInfo);

        assertEquals(nodeInfo, clientRoutineWorkerController.getNodeInfo(nId));

        // New nodeInfo + notification
        NodeInfoDTO newNodeInfo = new NodeInfoDTO();
        newNodeInfo.setId(nId);
        newNodeInfo.setParameters(new HashMap<>());
        newNodeInfo.getParameters().put("numberOfQueuedElements", "4");
        newNodeInfo.setLoad(2.1);
        clientRoutineWorkerController.notifyNodeInfo(nId, newNodeInfo);
        assertEquals(newNodeInfo, clientRoutineWorkerController.getNodeInfo(nId));
    }

    @Test(expected = UnknownNodeException.class)
    public void notifyNodeInfo_failed() throws Exception {
        NodeInfoDTO nodeInfo = new NodeInfoDTO();
        clientRoutineWorkerController.notifyNodeInfo(UUID.randomUUID().toString(), nodeInfo);
    }

    @Test
    public void notifyProgramsReceived() throws Exception {
        // Prepare a client routine worker
        String nId = UUID.randomUUID().toString();
        NodeInfoDTO nInfo = new NodeInfoDTO();
        nInfo.setId(nId);

        infoMap.put(nId, nInfo);
        clientRoutineWorkers.add(nId);

        assertTrue(workerProgramAssignment.isEmpty());

        List<String> pIds = new LinkedList<>();
        String p1Id = UUID.randomUUID().toString();
        pIds.add(p1Id);
        String p2Id = UUID.randomUUID().toString();
        pIds.add(p2Id);

        // Notify and proof
        clientRoutineWorkerController.notifyProgramsReceived(nId, pIds);
        assertFalse(workerProgramAssignment.isEmpty());
        assertNotNull(workerProgramAssignment.get(nId));
        assertTrue(workerProgramAssignment.get(nId).contains(p1Id));
        assertTrue(workerProgramAssignment.get(nId).contains(p2Id));
    }

    @Test
    public void notifyProgramsSuccess() throws Exception {
        // Prepare client routine worker and assignments
        String nId = UUID.randomUUID().toString();
        NodeInfoDTO nInfo = new NodeInfoDTO();
        nInfo.setId(nId);

        infoMap.put(nId, nInfo);
        clientRoutineWorkers.add(nId);

        workerProgramAssignment.put(nId, new HashSet<>());
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();
        String p3Id = UUID.randomUUID().toString();
        workerProgramAssignment.get(nId).add(p1Id);
        workerProgramAssignment.get(nId).add(p2Id);
        workerProgramAssignment.get(nId).add(p3Id);

        List<String> pIds = new LinkedList<>();
        pIds.add(p1Id);
        pIds.add(p3Id);

        // Notify and proof
        clientRoutineWorkerController.notifyProgramsNewState(nId, pIds, ExecutionState.SUCCESS);
        assertFalse(workerProgramAssignment.isEmpty());
        assertFalse(workerProgramAssignment.get(nId).contains(p1Id));
        assertTrue(workerProgramAssignment.get(nId).contains(p2Id));
        assertFalse(workerProgramAssignment.get(nId).contains(p3Id));
    }

    @Test
    public void notifyProgramsFailed() throws Exception {
        // Prepare client routine worker and assignments
        String nId = UUID.randomUUID().toString();
        NodeInfoDTO nInfo = new NodeInfoDTO();
        nInfo.setId(nId);

        infoMap.put(nId, nInfo);
        clientRoutineWorkers.add(nId);

        workerProgramAssignment.put(nId, new HashSet<>());
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();
        String p3Id = UUID.randomUUID().toString();
        workerProgramAssignment.get(nId).add(p1Id);
        workerProgramAssignment.get(nId).add(p2Id);
        workerProgramAssignment.get(nId).add(p3Id);

        List<String> pIds = new LinkedList<>();
        pIds.add(p1Id);
        pIds.add(p3Id);

        // Notify and proof
        clientRoutineWorkerController.notifyProgramsNewState(nId, pIds, ExecutionState.FAILED);
        assertFalse(workerProgramAssignment.isEmpty());
        assertFalse(workerProgramAssignment.get(nId).contains(p1Id));
        assertTrue(workerProgramAssignment.get(nId).contains(p2Id));
        assertFalse(workerProgramAssignment.get(nId).contains(p3Id));
    }

    @Test
    public void notifyProgramsScheduled() throws Exception {
        // Prepare client routine worker and assignments
        String nId = UUID.randomUUID().toString();
        NodeInfoDTO nInfo = new NodeInfoDTO();
        nInfo.setId(nId);

        infoMap.put(nId, nInfo);
        clientRoutineWorkers.add(nId);

        workerProgramAssignment.put(nId, new HashSet<>());
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();
        String p3Id = UUID.randomUUID().toString();
        workerProgramAssignment.get(nId).add(p1Id);
        workerProgramAssignment.get(nId).add(p2Id);
        workerProgramAssignment.get(nId).add(p3Id);

        List<String> pIds = new LinkedList<>();
        pIds.add(p1Id);
        pIds.add(p3Id);

        // Notify and proof
        clientRoutineWorkerController.notifyProgramsNewState(nId, pIds, ExecutionState.SCHEDULED);
        assertFalse(workerProgramAssignment.isEmpty());
        assertTrue(workerProgramAssignment.get(nId).contains(p1Id));
        assertTrue(workerProgramAssignment.get(nId).contains(p2Id));
        assertTrue(workerProgramAssignment.get(nId).contains(p3Id));
    }

    @Test
    public void notifyProgramsRun() throws Exception {
        // Prepare client routine worker and assignments
        String nId = UUID.randomUUID().toString();
        NodeInfoDTO nInfo = new NodeInfoDTO();
        nInfo.setId(nId);

        infoMap.put(nId, nInfo);
        clientRoutineWorkers.add(nId);

        workerProgramAssignment.put(nId, new HashSet<>());
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();
        String p3Id = UUID.randomUUID().toString();
        workerProgramAssignment.get(nId).add(p1Id);
        workerProgramAssignment.get(nId).add(p2Id);
        workerProgramAssignment.get(nId).add(p3Id);

        List<String> pIds = new LinkedList<>();
        pIds.add(p1Id);
        pIds.add(p3Id);

        // Notify and proof
        clientRoutineWorkerController.notifyProgramsNewState(nId, pIds, ExecutionState.RUN);
        assertFalse(workerProgramAssignment.isEmpty());
        assertTrue(workerProgramAssignment.get(nId).contains(p1Id));
        assertTrue(workerProgramAssignment.get(nId).contains(p2Id));
        assertTrue(workerProgramAssignment.get(nId).contains(p3Id));
    }

    @Test
    public void getClientRoutineWorkerServiceEndpoint() throws Exception {
        Map<String, ServiceEndpointDTO> endpoints = new HashMap<>();
        int nr = new Random().nextInt(5) + 5;
        ClientRoutineWorkerServiceClientFactory factory = new ClientRoutineWorkerServiceClientFactory();
        for (int i = 0; i < nr; i++) {
            String wId = UUID.randomUUID().toString();
            ServiceEndpointDTO endpoint = new ServiceEndpointDTO(UUID.randomUUID().toString(), i, Protocol.REST);
            endpoints.put(wId, endpoint);
            clientRoutineWorkers.add(wId);
            connectionMap.put(wId, factory.createClient(endpoint));
            infoMap.put(wId, null);
        }

        for (Map.Entry<String, ServiceEndpointDTO> e : endpoints.entrySet()) {
            ServiceEndpointDTO endpoint = clientRoutineWorkerController.getNodeServiceEndpoint(e.getKey());
            assertEquals(e.getValue(), endpoint);
        }
    }

    @Test (expected = UnknownNodeException.class)
    public void getClientRoutineWorkerServiceEndpoint_failed() throws Exception {
        clientRoutineWorkerController.getNodeServiceEndpoint(UUID.randomUUID().toString());
    }

    @Test
    public void getAndSetRoutines() throws Exception {
        assertNotNull(clientRoutineWorkerController.getStoreRoutineId());

        String storeRoutineId = UUID.randomUUID().toString();

        // Add 3 client routine workers
        String w1Id = UUID.randomUUID().toString();
        String w2Id = UUID.randomUUID().toString();
        String w3Id = UUID.randomUUID().toString();
        connectionMap.put(w1Id, serviceClientMock);
        connectionMap.put(w2Id, serviceClientMock);
        connectionMap.put(w3Id, serviceClientMock);

        // Mocking actions
        Future<Void> futureState = Mockito.mock(Future.class);
        when(serviceClientMock.setStoreRoutine(storeRoutineId)).thenReturn(futureState);
        when(futureState.get()).thenReturn(null);

        clientRoutineWorkerController.setStoreRoutineId(storeRoutineId);
        assertEquals(storeRoutineId, clientRoutineWorkerController.getStoreRoutineId());

        verify(serviceClientMock, times(3)).setStoreRoutine(storeRoutineId);
    }

    @Test
    public void getInstance() throws Exception {
        ClientRoutineWorkerController controller = ClientRoutineWorkerController.getInstance();
        assertEquals(0, controller.getAllNodeIds().size());

        ClientRoutineWorkerController controller2 = ClientRoutineWorkerController.getInstance();
        assertSame(controller, controller2);
    }

    @Test
    public void findNodesForShutdown() {
        ClientRoutineWorkerController controller = ClientRoutineWorkerController.getInstance();

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
        controller.nodeInfoMap.clear();
        controller.nodeInfoMap.put(n1Id, n1Info);
        controller.nodeInfoMap.put(n2Id, n2Info);
        controller.nodeInfoMap.put(n3Id, n3Info);
        controller.nodeInfoMap.put(n4Id, n4Info);

        List<String> nodeIds = controller.findNodesForShutdown(nrOfNodesToShutdown);

        assertEquals(nrOfNodesToShutdown, nodeIds.size());
        assertTrue(nodeIds.contains(n1Id));
        assertFalse(nodeIds.contains(n2Id));
        assertTrue(nodeIds.contains(n3Id));
        assertFalse(nodeIds.contains(n4Id));
    }

    @Test (expected = IllegalArgumentException.class)
    public void findNodesForShutdown_failed() {
        ClientRoutineWorkerController controller = ClientRoutineWorkerController.getInstance();

        controller.findNodesForShutdown(10);
    }

    @Test
    public void abortProgram() throws Exception {
        // Add client routine worker - program assignments
        String w1Id = UUID.randomUUID().toString();
        String w2Id = UUID.randomUUID().toString();
        String w3Id = UUID.randomUUID().toString();

        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();
        String p3Id = UUID.randomUUID().toString();
        String p4Id = UUID.randomUUID().toString();
        String p5Id = UUID.randomUUID().toString();
        String p6Id = UUID.randomUUID().toString();

        Set<String> worker1Programs = new LinkedHashSet<>();
        worker1Programs.add(p1Id);
        worker1Programs.add(p2Id);

        Set<String> worker2Programs = new LinkedHashSet<>();
        worker2Programs.add(p3Id);

        Set<String> worker3Programs = new LinkedHashSet<>();
        worker3Programs.add(p4Id);
        worker3Programs.add(p5Id);
        worker3Programs.add(p6Id);

        workerProgramAssignment.put(w1Id, worker1Programs);
        workerProgramAssignment.put(w2Id, worker2Programs);
        workerProgramAssignment.put(w3Id, worker3Programs);

        // Mocking actions
        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenReturn(null);
        when(schedulerServiceClientMock.abortProgram(w1Id, p1Id)).thenReturn(future);

        clientRoutineWorkerController.abortProgram(p1Id);

        assertTrue(workerProgramAssignment.get(w1Id).contains(p1Id));
        verify(schedulerServiceClientMock).abortProgram(w1Id, p1Id);
    }

    @Test
    public void abortProgram_missingMapping() throws Exception {
        clientRoutineWorkerController.abortProgram(UUID.randomUUID().toString());

        verify(schedulerServiceClientMock, times(0)).abortProgram(anyString(), anyString());
    }

    @Test (expected = NodeExecutionException.class)
    public void abortProgram_failed() throws Exception {
        String wId = UUID.randomUUID().toString();
        String pId = UUID.randomUUID().toString();
        Set<String> programIds = new HashSet<>();
        programIds.add(pId);

        workerProgramAssignment.put(wId, programIds);

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenThrow(new TicketFailedException("failed"));
        when(schedulerServiceClientMock.abortProgram(wId, pId)).thenReturn(future);

        clientRoutineWorkerController.abortProgram(pId);
    }

    @Test
    public void runProgram() throws Exception {
        String uId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();
        program.setUserId(uId);

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenReturn(null);
        when(schedulerServiceClientMock.scheduleProgram(uId, program)).thenReturn(future);

        clientRoutineWorkerController.runProgram(program);

        verify(schedulerServiceClientMock).scheduleProgram(uId, program);
    }

    @Test (expected = NodeExecutionException.class)
    public void runProgram_failed() throws Exception {
        String uId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();
        program.setUserId(uId);

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenThrow(new TicketFailedException("failed"));
        when(schedulerServiceClientMock.scheduleProgram(uId, program)).thenReturn(future);

        clientRoutineWorkerController.runProgram(program);
    }

    @Test
    public void addUser() throws Exception {
        String uId = UUID.randomUUID().toString();

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenReturn(null);
        when(schedulerServiceClientMock.addUser(uId)).thenReturn(future);

        clientRoutineWorkerController.addUser(uId);

        verify(schedulerServiceClientMock).addUser(uId);
    }

    @Test (expected = NodeExecutionException.class)
    public void addUser_failed() throws Exception {
        String uId = UUID.randomUUID().toString();

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenThrow(new TicketFailedException("failed"));
        when(schedulerServiceClientMock.addUser(uId)).thenReturn(future);

        clientRoutineWorkerController.addUser(uId);
    }

    @Test
    public void removeUser() throws Exception {
        String wId = UUID.randomUUID().toString();
        String uId = UUID.randomUUID().toString();
        String p1Id = UUID.randomUUID().toString();
        String p2Id = UUID.randomUUID().toString();
        Set<String> programIds = new HashSet<>();
        programIds.add(p1Id);
        programIds.add(p2Id);

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenReturn(null);
        when(schedulerServiceClientMock.removeUser(uId)).thenReturn(future);
        when(schedulerServiceClientMock.abortProgram(eq(wId), anyString())).thenReturn(future);

        workerProgramAssignment.put(wId, programIds);

        clientRoutineWorkerController.removeUser(uId, programIds);

        verify(schedulerServiceClientMock).removeUser(uId);
        verify(schedulerServiceClientMock, times(programIds.size())).abortProgram(eq(wId), anyString());
    }

    @Test (expected =  NodeExecutionException.class)
    public void removeUser_failed() throws Exception {
        String uId = UUID.randomUUID().toString();

        Future<Void> future = Mockito.mock(Future.class);
        when(future.get()).thenThrow(new TicketFailedException("failed"));
        when(schedulerServiceClientMock.removeUser(uId)).thenReturn(future);

        clientRoutineWorkerController.removeUser(uId, Collections.emptyList());
    }

    @Test
    public void fetchFinishedProgram() throws Exception {
        String wId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();
        program.setId(UUID.randomUUID().toString());

        Future<ProgramDTO> future = Mockito.mock(Future.class);
        when(future.get()).thenReturn(program);
        when(serviceClientMock.fetchFinishedProgram(program.getId())).thenReturn(future);

        connectionMap.put(wId, serviceClientMock);

        ProgramDTO fetchedProgram = clientRoutineWorkerController.fetchFinishedProgram(wId, program.getId());

        assertEquals(program, fetchedProgram);
        verify(serviceClientMock).fetchFinishedProgram(program.getId());
    }

}
