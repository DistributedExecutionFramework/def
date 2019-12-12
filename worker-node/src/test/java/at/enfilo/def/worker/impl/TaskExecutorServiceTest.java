package at.enfilo.def.worker.impl;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.demo.DefaultMapper;
import at.enfilo.def.demo.MemoryStorer;
import at.enfilo.def.demo.PICalc;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.util.BaseRoutineRegistry;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.queue.QueuePriorityWrapper;
import at.enfilo.def.worker.util.WorkerConfiguration;
import org.apache.thrift.TSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaskExecutorServiceTest {

	private TaskExecutorService taskExecutorService;
	private ILibraryServiceClient libraryServiceClient;
	private ITaskStateChangeListener taskStateChangeListener;
	private String storeRoutineId;

	@Before
	public void setUp() throws Exception {
		libraryServiceClient = Mockito.mock(ILibraryServiceClient.class);
		taskStateChangeListener = Mockito.mock(ITaskStateChangeListener.class);
		storeRoutineId = UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString();

		RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
				libraryServiceClient,
				WorkerConfiguration.getDefault()
		);


		taskExecutorService = new TaskExecutorService(
				Mockito.mock(QueuePriorityWrapper.class),
				routineProcessBuilderFactory,
				storeRoutineId,
				taskStateChangeListener
		);
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
		BaseRoutineRegistry baseRoutineRegistry = BaseRoutineRegistry.getInstance();
		Future<RoutineDTO> futurePiCalc = Mockito.mock(Future.class);
		when(futurePiCalc.get()).thenReturn(baseRoutineRegistry.get(piCalcId));
		Future<RoutineDTO> futureMapRoutine = Mockito.mock(Future.class);
		when(futureMapRoutine.get()).thenReturn(baseRoutineRegistry.get(mapId));
		Future<RoutineDTO> futureStoreRoutine = Mockito.mock(Future.class);
		when(futureStoreRoutine.get()).thenReturn(baseRoutineRegistry.get(storeRoutineId));
		when(libraryServiceClient.getRoutine(piCalcId)).thenReturn(futurePiCalc);
		when(libraryServiceClient.getRoutine(mapId)).thenReturn(futureMapRoutine);
		when(libraryServiceClient.getRoutine(storeRoutineId)).thenReturn(futureStoreRoutine);

		taskExecutorService.runTask(task);

		verify(taskStateChangeListener).notifyStateChanged(task.getId(), ExecutionState.RUN, ExecutionState.SUCCESS);
		assertEquals(ExecutionState.SUCCESS, task.getState());
		assertEquals(1, task.getOutParameters().size());
	}
}
