package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.util.ClientRoutineWorkerConfiguration;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.demo.DefaultMapper;
import at.enfilo.def.demo.MemoryStorer;
import at.enfilo.def.demo.PiCalcClientRoutine;
import at.enfilo.def.demo.TestClientRoutine;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.util.BaseRoutineRegistry;
import at.enfilo.def.node.impl.IStateChangeListener;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.routine.exec.RoutinesCommunicator;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.*;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ProgramExecutorServiceTest {

    private ProgramExecutorService programExecutorService;
    private ILibraryServiceClient libraryServiceClient;
    private IStateChangeListener programStateChangeListener;
    private String storeRoutineId;
    private TDeserializer deserializer;

    @Before
    public void setUp() throws Exception {
        libraryServiceClient = Mockito.mock(ILibraryServiceClient.class);
        programStateChangeListener = Mockito.mock(IStateChangeListener.class);
        storeRoutineId = UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString();

        RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
                libraryServiceClient,
                ClientRoutineWorkerConfiguration.getDefault()
        );

        ClientRoutineWorkerConfiguration configuration = Mockito.mock(ClientRoutineWorkerConfiguration.class);
        when(configuration.getWorkingDir()).thenReturn("/tmp/def");
        ServiceEndpointDTO clusterEndpoint = new ServiceEndpointDTO();
        clusterEndpoint.setProtocol(Protocol.THRIFT_TCP);
        clusterEndpoint.setPort(40012);
        clusterEndpoint.setHost("localhost");
        when(configuration.getClusterEndpoint()).thenReturn(clusterEndpoint);
        ServiceEndpointDTO parameterServerEndpoint = new ServiceEndpointDTO();
        parameterServerEndpoint.setProtocol(Protocol.THRIFT_TCP);
        parameterServerEndpoint.setPort(40092);
        parameterServerEndpoint.setHost("localhost");
        when(configuration.getParameterServerEndpoint()).thenReturn(parameterServerEndpoint);

        deserializer = new TDeserializer();

        programExecutorService = new ProgramExecutorService(
                Mockito.mock(QueuePriorityWrapper.class),
                routineProcessBuilderFactory,
                storeRoutineId,
                programStateChangeListener,
                configuration,
                deserializer
        );
    }

    @Test
    public void getElementId() {
        String pId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();
        program.setId(pId);

        assertEquals(pId, programExecutorService.getElementId(program));
    }

    @Test
    public void getElementState() {
        ExecutionState state = ExecutionState.SCHEDULED;
        ProgramDTO program = new ProgramDTO();
        program.setState(state);

        assertEquals(state, programExecutorService.getElementState(program));
    }

    @Test
    public void buildSequenceStepsExecutor() {
        ProgramDTO program = Mockito.mock(ProgramDTO.class);
        when(program.getId()).thenReturn(UUID.randomUUID().toString());

        SequenceStepsExecutor executor = programExecutorService.buildSequenceStepsExecutor(program);

        assertNotNull(executor);

        verify(program, atLeast(1)).getId();
        verify(program, atLeast(1)).getClientRoutineId();
        assertEquals(1, executor.getNumberOfSequenceSteps());
    }

    @Test
    public void prepareElementForExecution() {
        ProgramDTO program = Mockito.mock(ProgramDTO.class);

        programExecutorService.prepareElementForExecution(program);

        verify(program).setState(ExecutionState.RUN);
    }

    @Test
    public void executeElement() throws Exception {
        ProgramDTO program = new ProgramDTO();
        program.setId(UUID.randomUUID().toString());
        SequenceStepsExecutor executor = Mockito.mock(SequenceStepsExecutor.class);
        RoutinesCommunicator communicator = Mockito.mock(RoutinesCommunicator.class);
        when(executor.getCommunicator()).thenReturn(communicator);

        programExecutorService.executeElement(program, executor);

        verify(executor, times(1)).run(any(), any());
    }

    @Test
    public void handleSuccessfulExecutionOfElement() throws Exception {
        ProgramDTO program = new ProgramDTO();
        program.setId(UUID.randomUUID().toString());
        program.setState(ExecutionState.SCHEDULED);
        program.setUserId("user");
        program.setDescription("description");

        Result result1 = new Result();
        result1.setData(new TSerializer().serialize(program));
        List<Result> results = Arrays.asList(result1);

        ProgramDTO programToBeFilled = new ProgramDTO();
        programToBeFilled.setId(UUID.randomUUID().toString());
        programToBeFilled.setState(ExecutionState.RUN);
        programToBeFilled.setUserId("u");
        programToBeFilled.setDescription("d");

        programExecutorService.handleSuccessfulExecutionOfElement(programToBeFilled, results);

        assertEquals(program.getId(), programToBeFilled.getId());
        assertEquals(ExecutionState.SUCCESS, programToBeFilled.getState());
        assertEquals(program.getUserId(), programToBeFilled.getUserId());
        assertEquals(program.getDescription(), programToBeFilled.getDescription());
    }

    @Test
    public void handleFailedExecutionOfElement() {
        ProgramDTO program = Mockito.mock(ProgramDTO.class);
        Exception exception = Mockito.mock(Exception.class);

        programExecutorService.handleFailedExecutionOfElement(program, exception);

        verify(program).setState(ExecutionState.FAILED);
        verify(program).addToMessages(any());
        verify(exception).getMessage();
    }

    @Test
    public void extractResults() throws Exception {
        ProgramDTO program = new ProgramDTO();
        program.setId(UUID.randomUUID().toString());
        program.setState(ExecutionState.SUCCESS);
        program.setUserId("user");
        program.setDescription("description");

        Result result1 = new Result();
        result1.setData(new TSerializer().serialize(program));
        List<Result> results = Arrays.asList(result1);

        ProgramDTO programToBeFilled = new ProgramDTO();
        programToBeFilled.setId(UUID.randomUUID().toString());
        programToBeFilled.setState(ExecutionState.RUN);
        programToBeFilled.setUserId("u");
        programToBeFilled.setDescription("d");

        programExecutorService.extractResults(programToBeFilled, results);

        assertEquals(program.getId(), programToBeFilled.getId());
        assertEquals(program.getState(), programToBeFilled.getState());
        assertEquals(program.getUserId(), programToBeFilled.getUserId());
        assertEquals(program.getDescription(), programToBeFilled.getDescription());
    }

    @Test
    public void runProgram() throws Exception {
        BaseRoutineRegistry registry = BaseRoutineRegistry.getInstance();
        RoutineDTO clientRoutine = registry.get(UUID.nameUUIDFromBytes(TestClientRoutine.class.getCanonicalName().getBytes()).toString());
        for (RoutineBinaryDTO binary : clientRoutine.getRoutineBinaries()) {
            binary.setExecutionUrl(binary.getUrl());
        }
        RoutineDTO mapRoutine = registry.get(UUID.nameUUIDFromBytes(DefaultMapper.class.getCanonicalName().getBytes()).toString());
        for (RoutineBinaryDTO binary : mapRoutine.getRoutineBinaries()) {
            binary.setExecutionUrl(binary.getUrl());
        }
        RoutineDTO storeRoutine = registry.get(UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString());
        for (RoutineBinaryDTO binary : storeRoutine.getRoutineBinaries()) {
            binary.setExecutionUrl(binary.getUrl());
        }

        // Mock library
        Future<RoutineDTO> futureClientRoutine = Mockito.mock(Future.class);
        when(futureClientRoutine.isDone()).thenReturn(true);
        when(futureClientRoutine.get()).thenReturn(clientRoutine);
        Future<RoutineDTO> futureMapRoutine = Mockito.mock(Future.class);
        when(futureMapRoutine.get()).thenReturn(mapRoutine);
        Future<RoutineDTO> futureStoreRoutine = Mockito.mock(Future.class);
        when(futureStoreRoutine.get()).thenReturn(storeRoutine);
        when(libraryServiceClient.getRoutine(clientRoutine.getId())).thenReturn(futureClientRoutine);
        when(libraryServiceClient.getRoutine(mapRoutine.getId())).thenReturn(futureMapRoutine);
        when(libraryServiceClient.getRoutine(storeRoutineId)).thenReturn(futureStoreRoutine);

        ProgramDTO program = new ProgramDTO();
        String pId = UUID.randomUUID().toString();
        program.setId(pId);
        program.setState(ExecutionState.SCHEDULED);
        program.setClientRoutineId(clientRoutine.getId());

        programExecutorService.runProgram(program);

        verify(programStateChangeListener).notifyStateChanged(pId, ExecutionState.SCHEDULED, ExecutionState.RUN);
        verify(programStateChangeListener).notifyStateChanged(pId, ExecutionState.RUN, ExecutionState.SUCCESS);
        assertEquals(ExecutionState.SUCCESS, program.getState());
    }
}
