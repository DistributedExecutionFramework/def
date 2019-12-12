package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import at.enfilo.def.clientroutine.worker.api.IClientRoutineWorkerServiceClient;
import at.enfilo.def.clientroutine.worker.queue.ProgramQueue;
import at.enfilo.def.clientroutine.worker.server.ClientRoutineWorker;
import at.enfilo.def.clientroutine.worker.util.ClientRoutineWorkerConfiguration;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class ClientRoutineWorkerServiceControllerTest {

    private ClientRoutineWorkerServiceController controller;
    private QueuePriorityWrapper<ProgramDTO> queuePriorityWrapper;
    private List<INodeObserverServiceClient> observers;
    private ClientRoutineWorkerServiceClientFactory factory;
    private Set<String> finishedPrograms;
    private NodeObserverServiceClientFactory nodeObserverServiceClientFactory;

    @Before
    public void setUp() throws Exception {
        queuePriorityWrapper = new QueuePriorityWrapper<>(NodeConfiguration.getDefault());
        finishedPrograms = new HashSet<>();
        observers = new LinkedList<>();
        factory = Mockito.mock(ClientRoutineWorkerServiceClientFactory.class);
        nodeObserverServiceClientFactory = Mockito.mock(NodeObserverServiceClientFactory.class);

        Constructor<ClientRoutineWorkerServiceController> constructor = ClientRoutineWorkerServiceController.class.getDeclaredConstructor(
                QueuePriorityWrapper.class,
                Set.class,
                List.class,
                ClientRoutineWorkerServiceClientFactory.class,
                ClientRoutineWorkerConfiguration.class,
                NodeObserverServiceClientFactory.class,
                IDEFLogger.class
        );
        constructor.setAccessible(true);
        controller = constructor.newInstance(
                queuePriorityWrapper,
                finishedPrograms,
                observers,
                factory,
                ClientRoutineWorker.getInstance().getConfiguration(),
                nodeObserverServiceClientFactory,
                DEFLoggerFactory.getLogger(this.getClass())
        );
    }

    @Test
    public void queuePrograms() throws Exception {
        // Prepare observers
        INodeObserverServiceClient observerClient = Mockito.mock(INodeObserverServiceClient.class);
        observers.add(observerClient);

        // Prepare queues and programs
        String q1Id = UUID.randomUUID().toString();
        ProgramQueue pq1 = new ProgramQueue(q1Id);
        queuePriorityWrapper.addQueue(pq1);

        String q2Id = UUID.randomUUID().toString();
        ProgramQueue pq2 = new ProgramQueue(q2Id);
        queuePriorityWrapper.addQueue(pq2);

        List<ProgramDTO> programs1 = new LinkedList<>();
        String p11Id = UUID.randomUUID().toString();
        ProgramDTO p11 = new ProgramDTO();
        p11.setId(p11Id);
        String p12Id = UUID.randomUUID().toString();
        ProgramDTO p12 = new ProgramDTO();
        p12.setId(p12Id);
        programs1.add(p11);
        programs1.add(p12);

        List<ProgramDTO> programs2 = new LinkedList<>();
        String p2Id = UUID.randomUUID().toString();
        ProgramDTO p2 = new ProgramDTO();
        p2.setId(p2Id);
        programs2.add(p2);

        controller.queueElements(q1Id, programs1);
        controller.queueElements(q2Id, programs2);

        assertEquals(2, controller.getQueuedElements(q1Id).size());
        assertEquals(2, controller.getQueueInfo(q1Id).getNumberOfTasks());
        assertTrue(controller.getQueuedElements(q1Id).contains(p11Id));
        assertTrue(controller.getQueuedElements(q1Id).contains(p12Id));
        assertTrue(controller.getQueuedElements(q2Id).contains(p2Id));
        assertFalse(controller.getQueuedElements(q1Id).contains(p2Id));

        verify(observerClient).notifyProgramsReceived(anyString(), eq(programs1.stream().map(ProgramDTO::getId).collect(Collectors.toList())));
        verify(observerClient).notifyProgramsReceived(anyString(), eq(programs2.stream().map(ProgramDTO::getId).collect(Collectors.toList())));
        verify(observerClient, times(2)).notifyProgramsReceived(anyString(), anyObject());
    }

    @Test
    public void pauseAndReleaseQueue() throws Exception {
        String q1Id = UUID.randomUUID().toString();
        ProgramQueue pq1 = new ProgramQueue(q1Id);
        queuePriorityWrapper.addQueue(pq1);
        String q2Id = UUID.randomUUID().toString();
        ProgramQueue pq2 = new ProgramQueue(q2Id);
        queuePriorityWrapper.addQueue(pq2);

        assertFalse(controller.getQueueInfo(q1Id).isReleased());
        assertFalse(controller.getQueueInfo(q2Id).isReleased());

        controller.releaseQueue(q1Id);
        assertTrue(controller.getQueueInfo(q1Id).isReleased());
        assertFalse(controller.getQueueInfo(q2Id).isReleased());

        controller.pauseQueue(q1Id);
        controller.pauseQueue(q2Id);
        assertFalse(controller.getQueueInfo(q1Id).isReleased());
        assertFalse(controller.getQueueInfo(q2Id).isReleased());
    }

    @Test
    public void movePrograms() throws Exception {
        // Prepare observers
        INodeObserverServiceClient observerClient = Mockito.mock(INodeObserverServiceClient.class);
        observers.add(observerClient);

        // Prepare a queue with 3 programs
        String q1Id = UUID.randomUUID().toString();

        String p1Id = UUID.randomUUID().toString();
        ProgramDTO p1 = new ProgramDTO();
        p1.setId(p1Id);
        String p2Id = UUID.randomUUID().toString();
        ProgramDTO p2 = new ProgramDTO();
        p2.setId(p2Id);
        String p3Id = UUID.randomUUID().toString();
        ProgramDTO p3 = new ProgramDTO();
        p3.setId(p3Id);

        ProgramQueue pq = new ProgramQueue(q1Id);
        queuePriorityWrapper.addQueue(pq);
        pq.queue(p1);
        pq.queue(p2);
        pq.queue(p3);

        assertTrue(controller.getQueuedElements(q1Id).contains(p1Id));
        assertTrue(controller.getQueuedElements(q1Id).contains(p2Id));
        assertTrue(controller.getQueuedElements(q1Id).contains(p3Id));

        // Prepare destination endpoints and mocks
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
        IClientRoutineWorkerServiceClient clientMock = Mockito.mock(IClientRoutineWorkerServiceClient.class);
        when(factory.createClient(endpoint)).thenReturn(clientMock);
        Future<Void> ticketMock = Mockito.mock(Future.class);

        List<ProgramDTO> programsToMove = new LinkedList<>();
        programsToMove.add(p1);
        programsToMove.add(p3);
        List<String> pIdsToMove = new LinkedList<>();
        pIdsToMove.add(p1Id);
        pIdsToMove.add(p3Id);

        when(clientMock.queuePrograms(q1Id, programsToMove)).thenReturn(ticketMock);
        when(ticketMock.get()).thenReturn(null);

        // Verify number of programs
        assertEquals(3, controller.getQueuePriorityWrapper().getNumberOfQueuedElements());

        // Move programs
        controller.moveElements(q1Id, pIdsToMove, endpoint);
        assertFalse(controller.getQueuedElements(q1Id).contains(p1Id));
        assertTrue(controller.getQueuedElements(q1Id).contains(p2Id));
        assertFalse(controller.getQueuedElements(q1Id).contains(p3Id));

        verify(clientMock).queuePrograms(q1Id, programsToMove);

        // Verify observer
        assertEquals(1, controller.getQueuePriorityWrapper().getNumberOfQueuedElements());
        verify(observerClient).notifyNodeInfo(anyString(), anyObject());
    }

    @Test
    public void moveAllPrograms() throws Exception {
        // Prepare observers
        INodeObserverServiceClient observerClient = Mockito.mock(INodeObserverServiceClient.class);
        observers.add(observerClient);

        // Prepare two queues with each two programs
        String q1Id = UUID.randomUUID().toString();
        ProgramQueue pq1 = new ProgramQueue(q1Id);
        queuePriorityWrapper.addQueue(pq1);
        String q2Id = UUID.randomUUID().toString();
        ProgramQueue pq2 = new ProgramQueue(q2Id);
        queuePriorityWrapper.addQueue(pq2);

        String p11Id = UUID.randomUUID().toString();
        ProgramDTO p11 = new ProgramDTO();
        p11.setId(p11Id);
        String p12Id = UUID.randomUUID().toString();
        ProgramDTO p12 = new ProgramDTO();
        p12.setId(p12Id);
        String p21Id = UUID.randomUUID().toString();
        ProgramDTO p21 = new ProgramDTO();
        p21.setId(p21Id);
        String p22Id = UUID.randomUUID().toString();
        ProgramDTO p22 = new ProgramDTO();
        p22.setId(p22Id);

        pq1.queue(p11);
        pq1.queue(p12);
        pq2.queue(p21);
        pq2.queue(p22);

        assertTrue(controller.getQueuedElements(q1Id).contains(p11Id));
        assertTrue(controller.getQueuedElements(q1Id).contains(p12Id));
        assertTrue(controller.getQueuedElements(q2Id).contains(p21Id));
        assertTrue(controller.getQueuedElements(q2Id).contains(p22Id));

        // Prepare destination endpoints and mock
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
        IClientRoutineWorkerServiceClient clientMock = Mockito.mock(IClientRoutineWorkerServiceClient.class);
        when(factory.createClient(endpoint)).thenReturn(clientMock);
        Future<TicketStatusDTO> ticketMock = Mockito.mock(Future.class);
        when(clientMock.queuePrograms(anyString(), anyList())).thenReturn(ticketMock);
        when(ticketMock.get()).thenReturn(TicketStatusDTO.DONE);

        controller.moveAllElements(endpoint);

        verify(clientMock, times(1)).queuePrograms(eq(q1Id), anyList());
        verify(clientMock, times(1)).queuePrograms(eq(q2Id), anyList());

        // Verify observer
        assertEquals(0, controller.getQueuePriorityWrapper().getNumberOfQueuedElements());
        verify(observerClient).notifyNodeInfo(anyString(), anyObject());
    }

    @Test (expected = UnknownProgramException.class)
    public void fetchFinishedUnknownProgram() throws Exception {
        controller.fetchFinishedElement(UUID.randomUUID().toString());
    }

    @Test
    public void getQueueIds() throws Exception {
        String qId = UUID.randomUUID().toString();
        ProgramQueue queue = new ProgramQueue(qId);
        queuePriorityWrapper.addQueue(queue);

        List<String> qIds = controller.getQueueIds();

        assertEquals(1, qIds.size());
        assertTrue(qIds.contains(qId));
    }

    @Test
    public void abortProgram() throws Exception {
        ProgramDTO program = new ProgramDTO();
        String pId = UUID.randomUUID().toString();
        program.setId(pId);
        program.setState(ExecutionState.SCHEDULED);
        controller.getProgramCache().cache(pId, program);

        controller.abortProgram(pId);

        assertTrue(program.getMessages().size() > 0);
    }
}
