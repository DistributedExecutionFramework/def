package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.demo.DefaultMapper;
import at.enfilo.def.demo.MemoryStorer;
import at.enfilo.def.demo.PICalc;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.util.BaseRoutineRegistry;
import at.enfilo.def.node.impl.IStateChangeListener;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.routine.exec.RoutinesCommunicator;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.worker.util.WorkerConfiguration;
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
import static org.mockito.Mockito.*;

public class TaskExecutorServiceTest {

	private TaskExecutorService taskExecutorService;
	private ILibraryServiceClient libraryServiceClient;
	private IStateChangeListener taskStateChangeListener;
	private String storeRoutineId;
	private TDeserializer deserializer;
	private DTOCache cache;

	@Before
	public void setUp() throws Exception {
		libraryServiceClient = Mockito.mock(ILibraryServiceClient.class);
		taskStateChangeListener = Mockito.mock(IStateChangeListener.class);
		storeRoutineId = UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString();
		cache = Mockito.mock(DTOCache.class);

		RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
				libraryServiceClient,
				WorkerConfiguration.getDefault()
		);

		WorkerConfiguration configuration = Mockito.mock(WorkerConfiguration.class);
		when(configuration.getWorkingDir()).thenReturn("/tmp/def/");
		ServiceEndpointDTO parameterServerEndpoint = new ServiceEndpointDTO();
		parameterServerEndpoint.setProtocol(Protocol.THRIFT_TCP);
		parameterServerEndpoint.setPort(40092);
		parameterServerEndpoint.setHost("localhost");
		when(configuration.getParameterServerEndpoint()).thenReturn(parameterServerEndpoint);

		deserializer = Mockito.mock(TDeserializer.class);
		when(deserializer.partialDeserializeString(any(), any())).thenReturn(UUID.randomUUID().toString());

		taskExecutorService = new TaskExecutorService(
				Mockito.mock(QueuePriorityWrapper.class),
				routineProcessBuilderFactory,
				storeRoutineId,
				taskStateChangeListener,
				configuration,
				cache,
				deserializer
		);
	}

	@Test
	public void getElementId() {
		String tId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();
		task.setId(tId);

		assertEquals(tId, taskExecutorService.getElementId(task));
	}

	@Test
	public void getElementState() {
		ExecutionState state = ExecutionState.SUCCESS;
		TaskDTO task = new TaskDTO();
		task.setState(state);

		assertEquals(state, taskExecutorService.getElementState(task));
	}

	@Test
	public void buildSequenceStepsExecutor() {
		TaskDTO task = Mockito.mock(TaskDTO.class);
		when(task.getId()).thenReturn(UUID.randomUUID().toString());

		SequenceStepsExecutor executor = taskExecutorService.buildSequenceStepsExecutor(task);

		assertNotNull(executor);

		verify(task, atLeast(1)).getId();
		verify(task, atLeast(1)).getObjectiveRoutineId();
		verify(task, atLeast(1)).getMapRoutineId();
		assertEquals(3, executor.getNumberOfSequenceSteps());
	}

	@Test
	public void prepareElementForExecution() {
		TaskDTO task = Mockito.mock(TaskDTO.class);

		taskExecutorService.prepareElementForExecution(task);

		verify(task).setState(ExecutionState.RUN);
		verify(task).setStartTime(anyLong());
	}

	@Test
	public void executeElement() throws Exception {
		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());
		SequenceStepsExecutor executor = Mockito.mock(SequenceStepsExecutor.class);
		RoutinesCommunicator communicator = Mockito.mock(RoutinesCommunicator.class);
		when(executor.getCommunicator()).thenReturn(communicator);

		taskExecutorService.executeElement(task, executor);

		verify(executor, times(1)).run(any(), any());
		verify(communicator, times(2)).addParameter(anyString(), any());
	}

	@Test
	public void handleSuccessfulExecutionOfElement() throws Exception {
		TaskDTO task = Mockito.mock(TaskDTO.class);
		Result result1 = Mockito.mock(Result.class);
		Result result2 = Mockito.mock(Result.class);

		taskExecutorService.handleSuccessfulExecutionOfElement(task, Arrays.asList(result1, result2));

		verify(task).setOutParameters(any());
		verify(task).setState(ExecutionState.SUCCESS);
	}

	@Test
	public void handleFailedExecutionOfElement() {
		TaskDTO task = Mockito.mock(TaskDTO.class);
		Exception exception = Mockito.mock(Exception.class);

		taskExecutorService.handleFailedExecutionOfElement(task, exception);

		verify(task).setState(ExecutionState.FAILED);
		verify(task).addToMessages(any());
		verify(exception).getMessage();
	}

	@Test
	public void extractResults() throws Exception {
		Result result1 = Mockito.mock(Result.class);
		Result result2 = Mockito.mock(Result.class);
		List<Result> results = Arrays.asList(result1, result2);

		List<ResourceDTO> resources = taskExecutorService.extractResults(results);

		verify(deserializer, atLeast(2)).partialDeserializeString(any(), any());
		assertNotNull(resources);
		assertEquals(results.size(), resources.size());
	}

	@Test
	public void runTask() throws Exception {
		TSerializer tSerializer = new TSerializer();
		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());
		task.setJobId(UUID.randomUUID().toString());
		task.setProgramId(UUID.randomUUID().toString());
		task.setState(ExecutionState.SCHEDULED);
		String piCalcId = UUID.nameUUIDFromBytes(PICalc.class.getCanonicalName().getBytes()).toString();
		String mapId = UUID.nameUUIDFromBytes(DefaultMapper.class.getCanonicalName().getBytes()).toString();
		task.setObjectiveRoutineId(piCalcId);
		task.setMapRoutineId(mapId);
		// Start param
		ResourceDTO startParam = new ResourceDTO();
		startParam.setId(UUID.randomUUID().toString());
		DEFDouble start = new DEFDouble(0);
		startParam.setDataTypeId(start.get_id());
		startParam.setData(tSerializer.serialize(start));
		task.putToInParameters("start", startParam);
		// StepSize param
		ResourceDTO stepSizeParam = new ResourceDTO();
		stepSizeParam.setId(UUID.randomUUID().toString());
		DEFDouble stepSize = new DEFDouble(1e-7);
		stepSizeParam.setDataTypeId(stepSize.get_id());
		stepSizeParam.setData(tSerializer.serialize(stepSize));
		task.putToInParameters("stepSize", stepSizeParam);
		// End param
		ResourceDTO endParam = new ResourceDTO();
		endParam.setId(UUID.randomUUID().toString());
		DEFDouble end = new DEFDouble(1e7);
		endParam.setDataTypeId(end.get_id());
		endParam.setData(tSerializer.serialize(end));
		task.putToInParameters("end", endParam);

		// Mock library
		BaseRoutineRegistry registry = BaseRoutineRegistry.getInstance();
		RoutineDTO piCalc = registry.get(piCalcId);
		for (RoutineBinaryDTO binary : piCalc.getRoutineBinaries()) {
			binary.setExecutionUrl(binary.getUrl());
		}
		RoutineDTO defaultMapper = registry.get(mapId);
		for (RoutineBinaryDTO binary : defaultMapper.getRoutineBinaries()) {
			binary.setExecutionUrl(binary.getUrl());
		}
		RoutineDTO memoryStorer = registry.get(storeRoutineId);
		for (RoutineBinaryDTO binary : memoryStorer.getRoutineBinaries()) {
			binary.setExecutionUrl(binary.getUrl());
		}

		Future<RoutineDTO> futurePiCalc = Mockito.mock(Future.class);
		when(futurePiCalc.get()).thenReturn(piCalc);
		Future<RoutineDTO> futureMapRoutine = Mockito.mock(Future.class);
		when(futureMapRoutine.get()).thenReturn(defaultMapper);
		Future<RoutineDTO> futureStoreRoutine = Mockito.mock(Future.class);
		when(futureStoreRoutine.get()).thenReturn(memoryStorer);
		when(libraryServiceClient.getRoutine(piCalcId)).thenReturn(futurePiCalc);
		when(libraryServiceClient.getRoutine(mapId)).thenReturn(futureMapRoutine);
		when(libraryServiceClient.getRoutine(storeRoutineId)).thenReturn(futureStoreRoutine);

		taskExecutorService.runTask(task);

		verify(taskStateChangeListener).notifyStateChanged(task.getId(), ExecutionState.RUN, ExecutionState.SUCCESS);
		assertEquals(ExecutionState.SUCCESS, task.getState());
		assertEquals(1, task.getOutParameters().size());
	}
}
