package at.enfilo.def.scheduler.strategy;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.scheduler.api.strategy.ITaskSchedulingStrategy;
import at.enfilo.def.scheduler.util.SchedulerConfiguration;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public abstract class TaskSchedulingStrategyTest {
	protected Map<String, IWorkerServiceClient> nodes;
	protected Map<String, Environment> nodeEnvironments;
	protected SchedulerConfiguration schedulerConfiguration;
	protected ITaskSchedulingStrategy strategy;
	protected IWorkerServiceClient workerServiceClient;
	protected ILibraryServiceClient libraryServiceClient;

	@Before
	public void setUp() throws Exception {
		nodes = new HashMap<>();
		nodeEnvironments = new HashMap<>();
		WorkerServiceClientFactory workerServiceClientFactory = Mockito.mock(WorkerServiceClientFactory.class);
		workerServiceClient = Mockito.mock(IWorkerServiceClient.class);
		when(workerServiceClientFactory.createClient(any())).thenReturn(workerServiceClient);
		libraryServiceClient = Mockito.mock(ILibraryServiceClient.class);
		schedulerConfiguration = SchedulerConfiguration.getDefault();
		strategy = createStrategy();
	}

	protected abstract ITaskSchedulingStrategy createStrategy();

	@Test
	public void addGetAndRemoveJob() throws Exception {
		// Create mocks and two workers
		String worker1 = UUID.randomUUID().toString();
		String worker2 = UUID.randomUUID().toString();
		nodes.put(worker1, workerServiceClient);
		nodes.put(worker2, workerServiceClient);

		Environment workerEnvironment = Environment.buildFromString(Collections.singletonList("java(1.8)"));

		nodeEnvironments.put(worker1, workerEnvironment);
		nodeEnvironments.put(worker2, workerEnvironment);

		String jId = UUID.randomUUID().toString();

		Future<Void> futureStatus = Mockito.mock(Future.class);
		when(workerServiceClient.createQueue(jId)).thenReturn(futureStatus);
		when(workerServiceClient.deleteQueue(jId)).thenReturn(futureStatus);
		when(futureStatus.isDone()).thenReturn(true);
		when(futureStatus.get()).thenReturn(null);

		// Add a job
		assertTrue(strategy.getJobs().isEmpty());
		strategy.addJob(jId);
		assertTrue(strategy.getJobs().contains(jId));
		verify(workerServiceClient, times(2)).createQueue(jId);

		// Remove job
		strategy.removeJob(jId);
		assertTrue(strategy.getJobs().isEmpty());
		verify(workerServiceClient, times(2)).deleteQueue(jId);
	}

	@Test
	public void scheduleTasks() throws Exception {
		// Create mocks and two workers
		String worker1 = UUID.randomUUID().toString();
		String worker2 = UUID.randomUUID().toString();
		nodes.put(worker1, workerServiceClient);
		nodes.put(worker2, workerServiceClient);

		Environment workerEnvironment = Environment.buildFromString(Collections.singletonList("java(1.8)"));

		nodeEnvironments.put(worker1, workerEnvironment);
		nodeEnvironments.put(worker2, workerEnvironment);

		String jId = UUID.randomUUID().toString();

		Future<Void> futureStatus = Mockito.mock(Future.class);
		when(workerServiceClient.createQueue(jId)).thenReturn(futureStatus);
		when(workerServiceClient.queueTasks(eq(jId), any())).thenReturn(futureStatus);
		when(futureStatus.isDone()).thenReturn(true);
		when(futureStatus.get()).thenReturn(null);

		// Add job, schedule tasks and mark Job as complete.
		strategy.addJob(jId);
		Random rnd = new Random();
		int tasks = rnd.nextInt(20);
		for (int i = 0; i < tasks; i++) {
			FeatureDTO featureDTO = new FeatureDTO();
			featureDTO.setName("java");
			featureDTO.setGroup("language");
			featureDTO.setVersion(">1.8");
			featureDTO.setId(UUID.randomUUID().toString());
			List<FeatureDTO> requiredFeatures = Collections.singletonList(featureDTO);
			TaskDTO task = new TaskDTO();
			task.setId(UUID.randomUUID().toString());
			String routineId = UUID.randomUUID().toString();
			task.setObjectiveRoutineId(routineId);
			Future<List<FeatureDTO>> requiredFeaturesFuture = Mockito.mock(Future.class);
			when(libraryServiceClient.getRoutineRequiredFeatures(routineId)).thenReturn(requiredFeaturesFuture);
			when(requiredFeaturesFuture.get()).thenReturn(requiredFeatures);
			strategy.schedule(jId, Collections.singletonList(task));
		}
		strategy.markJobAsComplete(jId);

		verify(workerServiceClient, times(tasks)).queueTasks(eq(jId), anyList());
	}
}
