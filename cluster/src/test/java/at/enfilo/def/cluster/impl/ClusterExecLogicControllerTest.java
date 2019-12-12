package at.enfilo.def.cluster.impl;

import at.enfilo.def.execlogic.impl.ExecLogicException;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class ClusterExecLogicControllerTest {

	private ClusterExecLogicController execLogicController;
	private WorkerController workerControllerMock;
	private ReducerController reducerControllerMock;
	private ClientRoutineWorkerController clientRoutineWorkerControllerMock;
	private LibraryController libraryControllerMock;
	private DomainController domainController;

	@Before
	public void setUp() throws Exception {
		workerControllerMock = Mockito.mock(WorkerController.class);
		reducerControllerMock = Mockito.mock(ReducerController.class);
		clientRoutineWorkerControllerMock = Mockito.mock(ClientRoutineWorkerController.class);
		libraryControllerMock = Mockito.mock(LibraryController.class);
		domainController = Mockito.mock(DomainController.class);

		// Create controller with special constructor
		Constructor<ClusterExecLogicController> constructor = ClusterExecLogicController.class.getDeclaredConstructor(
				ClusterResource.class,
				WorkerController.class,
				ReducerController.class,
				ClientRoutineWorkerController.class,
				LibraryController.class,
				DomainController.class
		);
		constructor.setAccessible(true);
		execLogicController = constructor.newInstance(
				ClusterResource.getInstance(),
                workerControllerMock,
				reducerControllerMock,
				clientRoutineWorkerControllerMock,
				libraryControllerMock,
				domainController
		);
	}

	@Test
	public void getAllProgramIds() throws Exception {
		String userId = UUID.randomUUID().toString();
		List<String> programIds = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getAllPrograms(userId)).thenReturn(programIds);

		List<String> fetchedProgramIds = execLogicController.getAllPrograms(userId);

		assertEquals(programIds, fetchedProgramIds);
		verify(domainController).getAllPrograms(userId);
	}

	@Test
	public void getAllPrograms() throws Exception {
		List<ProgramDTO> programs = Arrays.asList(
				new ProgramDTO(),
				new ProgramDTO()
		);

		when(domainController.getAllPrograms()).thenReturn(programs);

		Collection<ProgramDTO> fetchedPrograms = execLogicController.getAllPrograms();

		assertEquals(programs, fetchedPrograms);
		verify(domainController).getAllPrograms();
	}

	@Test
	public void createProgram() throws Exception {
		String cId = ClusterResource.getInstance().getId();
		String userId = UUID.randomUUID().toString();

		execLogicController.createProgram(cId, userId);

		verify(domainController).createProgram(userId);
	}

	@Test (expected = ExecLogicException.class)
	public void createProgram_wrongClusterId() throws Exception {
		String cId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		execLogicController.createProgram(cId, userId);
	}

	@Test
	public void getProgram() throws Exception {
		String pId = UUID.randomUUID().toString();
		ProgramDTO program = new ProgramDTO();

		when(domainController.getProgram(pId)).thenReturn(program);

		ProgramDTO fetchedProgram = execLogicController.getProgram(pId);
		assertEquals(program, fetchedProgram);
		verify(domainController).getProgram(pId);
	}

	@Test
	public void deleteProgram() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		List<String> sharedResources = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);
		Job job = new Job(program);
		String jId = job.getId();

		job.setState(ExecutionState.RUN);
		List<String> jobIds = Arrays.asList(
				jId
		);
		List<String> runningTasksOfJob = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getAllSharedResources(pId)).thenReturn(sharedResources);
		when(domainController.getAllJobs(pId)).thenReturn(jobIds);
		when(domainController.getJobById(pId, jId)).thenReturn(job);
		when(domainController.getRunningTasksOfJob(pId, jId)).thenReturn(runningTasksOfJob);

		execLogicController.deleteProgram(pId);

		verify(domainController).getAllSharedResources(pId);
		verify(domainController).getAllJobs(pId);
		verify(domainController).deleteProgram(pId);
		verify(domainController).getRunningTasksOfJob(pId, jId);
		verify(workerControllerMock).removeSharedResources(sharedResources);
		verify(workerControllerMock).deleteJob(jId);
		verify(workerControllerMock, times(runningTasksOfJob.size())).abortTask(anyString());
		verify(reducerControllerMock).removeSharedResources(sharedResources);
	}

	@Test
	public void abortProgram_withReduceJob() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);

		String jId = job.getId();
		job.setState(ExecutionState.RUN);
		List<String> jobIds = Arrays.asList(
				jId
		);
		List<String> runningTasksOfJob = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getAllJobs(pId)).thenReturn(jobIds);
		when(domainController.getJobById(pId, jId)).thenReturn(job);
		when(domainController.getRunningTasksOfJob(pId, jId)).thenReturn(runningTasksOfJob);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(true);

		execLogicController.abortProgram(pId);

		verify(domainController).abortProgram(pId);
		verify(domainController).getAllJobs(pId);
		verify(domainController).getRunningTasksOfJob(pId, jId);
		verify(workerControllerMock, times(0)).deleteJob(jId);
		verify(workerControllerMock, times(runningTasksOfJob.size())).abortTask(anyString());
		verify(reducerControllerMock, times(0)).deleteReduceJob(jId);
	}

	@Test
	public void abortProgram_withouthReduceJob() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();

		job.setState(ExecutionState.RUN);
		List<String> jobIds = Arrays.asList(jId);
		List<String> runningTasksOfJob = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getAllJobs(pId)).thenReturn(jobIds);
		when(domainController.getJobById(pId, jId)).thenReturn(job);
		when(domainController.getRunningTasksOfJob(pId, jId)).thenReturn(runningTasksOfJob);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(false);

		execLogicController.abortProgram(pId);

		verify(domainController).abortProgram(pId);
		verify(domainController).getAllJobs(pId);
		verify(domainController).getRunningTasksOfJob(pId, jId);
		verify(workerControllerMock, times(0)).deleteJob(jId);
		verify(workerControllerMock, times(runningTasksOfJob.size())).abortTask(anyString());
		verify(reducerControllerMock, times(0)).deleteReduceJob(jId);
	}

	@Test
	public void abortProgram_finishedJob() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();

		job.setState(ExecutionState.SUCCESS);
		List<String> jobIds = Arrays.asList(jId);

		when(domainController.getAllJobs(pId)).thenReturn(jobIds);
		when(domainController.getJobById(pId, jId)).thenReturn(job);

		execLogicController.abortProgram(pId);

		verify(domainController).abortProgram(pId);
		verify(domainController).abortJob(pId, jId);
		verify(workerControllerMock, times(0)).deleteJob(anyString());
		verify(reducerControllerMock, times(0)).deleteReduceJob(anyString());
	}

	@Test
	public void updateProgramName() throws Exception {
		String pId = UUID.randomUUID().toString();
		String name = "name";

		execLogicController.updateProgramName(pId, name);

		verify(domainController).updateProgramName(pId, name);
	}

	@Test
	public void updateProgramDescription() throws Exception {
		String pId = UUID.randomUUID().toString();
		String description = "description";

		execLogicController.updateProgramDescription(pId, description);

		verify(domainController).updateProgramDescription(pId, description);
	}

	@Test
	public void markProgramAsFinished() throws Exception {
		String pId = UUID.randomUUID().toString();

		execLogicController.markProgramAsFinished(pId);

		verify(domainController).markProgramAsFinished(pId);
	}

	@Test
	public void getAllJobs() throws Exception {
		String pId = UUID.randomUUID().toString();
		List<String> jobIds = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getAllJobs(pId)).thenReturn(jobIds);

		List<String> fetchedJobIds = execLogicController.getAllJobs(pId);
		assertSame(jobIds, fetchedJobIds);

		verify(domainController).getAllJobs(pId);
	}

	@Test
	public void createJob() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();

		when(domainController.createJob(pId)).thenReturn(jId);

		String fetchedJobId = execLogicController.createJob(pId);
		assertEquals(jId, fetchedJobId);

		verify(domainController).createJob(pId);
		verify(workerControllerMock).addJob(jId);
	}

	@Test
	public void getJob() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		JobDTO job = new JobDTO();

		when(domainController.getJob(pId, jId)).thenReturn(job);

		JobDTO fetchedJob = execLogicController.getJob(pId, jId);
		assertSame(job, fetchedJob);

		verify(domainController).getJob(pId, jId);
	}

	@Test
	public void deleteJob_withReduceJob() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();

		job.setState(ExecutionState.RUN);

		when(domainController.getJobById(pId, jId)).thenReturn(job);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(true);

		execLogicController.deleteJob(pId, jId);

		verify(domainController).deleteJob(pId, jId);
		verify(domainController).hasJobReduceRoutine(pId, jId);
		verify(workerControllerMock).deleteJob(jId);
		verify(reducerControllerMock).deleteReduceJob(jId);
	}

	@Test
	public void deleteJob_withoutReduceJob() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();

		job.setState(ExecutionState.RUN);

		when(domainController.getJobById(pId, jId)).thenReturn(job);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(false);

		execLogicController.deleteJob(pId, jId);

		verify(domainController).deleteJob(pId, jId);
		verify(domainController).hasJobReduceRoutine(pId, jId);
		verify(workerControllerMock).deleteJob(jId);
		verify(reducerControllerMock, times(0)).deleteReduceJob(jId);
	}

	@Test
	public void deleteJob_finishedJob() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();

		job.setState(ExecutionState.SUCCESS);

		when(domainController.getJobById(pId, jId)).thenReturn(job);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(true);

		execLogicController.deleteJob(pId, jId);

		verify(domainController).deleteJob(pId, jId);
		verify(domainController, times(0)).hasJobReduceRoutine(pId, jId);
		verify(workerControllerMock).deleteJob(jId);
		verify(reducerControllerMock, times(0)).deleteReduceJob(jId);
	}

	@Test
	public void abortJob_withReduceJob() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();

		job.setState(ExecutionState.RUN);

		List<String> runningTasksOfJob = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getJobById(pId, jId)).thenReturn(job);
		when(domainController.getRunningTasksOfJob(pId, jId)).thenReturn(runningTasksOfJob);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(true);

		execLogicController.abortJob(pId, jId);

		verify(domainController).abortJob(pId, jId);
		verify(domainController).getRunningTasksOfJob(pId, jId);
		verify(workerControllerMock, times(0)).deleteJob(jId);
		verify(workerControllerMock, times(runningTasksOfJob.size())).abortTask(anyString());
		verify(reducerControllerMock, times(0)).deleteReduceJob(jId);
	}

	@Test
	public void abortJob_withoutReduceJob() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();

		job.setState(ExecutionState.RUN);

		List<String> runningTasksOfJob = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getJobById(pId, jId)).thenReturn(job);
		when(domainController.getRunningTasksOfJob(pId, jId)).thenReturn(runningTasksOfJob);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(false);

		execLogicController.abortJob(pId, jId);

		verify(domainController).abortJob(pId, jId);
		verify(domainController).getRunningTasksOfJob(pId, jId);
		verify(workerControllerMock, times(0)).deleteJob(jId);
		verify(workerControllerMock, times(runningTasksOfJob.size())).abortTask(anyString());
		verify(reducerControllerMock, times(0)).deleteReduceJob(jId);
	}

	@Test
	public void abortJob_finishedJob() throws Exception {
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();
		job.setState(ExecutionState.SUCCESS);


		when(domainController.getJobById(pId, jId)).thenReturn(job);

		execLogicController.abortJob(pId, jId);

		verify(domainController).abortJob(pId, jId);
		verify(domainController).getRunningTasksOfJob(anyString(), anyString());
		verify(domainController, times(0)).hasJobReduceRoutine(anyString(), anyString());
		verify(workerControllerMock, times(0)).deleteJob(anyString());
		verify(reducerControllerMock, times(0)).deleteReduceJob(anyString());
	}

	@Test
	public void getAttachedMapRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String mapRoutineId = UUID.randomUUID().toString();

		when(domainController.getAttachedMapRoutine(pId, jId)).thenReturn(mapRoutineId);

		String fetchedRoutineId = execLogicController.getAttachedMapRoutine(pId, jId);

		assertEquals(mapRoutineId, fetchedRoutineId);
		verify(domainController).getAttachedMapRoutine(pId, jId);
	}

	@Test
	public void attachMapRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String mapRoutineId = UUID.randomUUID().toString();
		RoutineDTO routine = new RoutineDTO();

		when(libraryControllerMock.fetchRoutine(mapRoutineId)).thenReturn(routine);

		execLogicController.attachMapRoutine(pId, jId, mapRoutineId);

		verify(libraryControllerMock).fetchRoutine(mapRoutineId);
		verify(domainController).attachMapRoutine(pId, jId, routine);
	}

	@Test
	public void getAttachedReduceRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String reduceRoutineId = UUID.randomUUID().toString();

		when(domainController.getAttachedReduceRoutine(pId, jId)).thenReturn(reduceRoutineId);

		String fetchedRoutineId = execLogicController.getAttachedReduceRoutine(pId, jId);

		assertEquals(reduceRoutineId, fetchedRoutineId);
		verify(domainController).getAttachedReduceRoutine(pId, jId);
	}

	@Test
	public void attachReduceRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String reduceRoutineId = UUID.randomUUID().toString();
		RoutineDTO routine = new RoutineDTO();
		JobDTO job = new JobDTO();

		when(libraryControllerMock.fetchRoutine(reduceRoutineId)).thenReturn(routine);
		when(domainController.getJob(pId, jId)).thenReturn(job);

		execLogicController.attachReduceRoutine(pId, jId, reduceRoutineId);

		verify(libraryControllerMock).fetchRoutine(reduceRoutineId);
		verify(domainController).attachReduceRoutine(pId, jId, routine);
		verify(domainController).getJob(pId, jId);
		verify(reducerControllerMock).addReduceJob(job);
	}

	@Test
	public void getAllTasks() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		SortingCriterion criterion = SortingCriterion.CREATION_DATE_FROM_NEWEST;

		List<String> taskIds = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getAllTasks(pId, jId, criterion)).thenReturn(taskIds);

		List<String> fetchedIds = execLogicController.getAllTasks(pId, jId, criterion);

		assertEquals(taskIds, fetchedIds);
		verify(domainController).getAllTasks(pId, jId, criterion);
	}

	@Test
	public void getAllTasksWithState() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		ExecutionState state = ExecutionState.SUCCESS;
		SortingCriterion criterion = SortingCriterion.FINISH_DATE_FROM_NEWEST;

		List<String> taskIds = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getAllTasksWithState(pId, jId, state, criterion)).thenReturn(taskIds);

		List<String> fetchedIds = execLogicController.getAllTasksWithState(pId, jId, state, criterion);

		assertEquals(taskIds, fetchedIds);
		verify(domainController).getAllTasksWithState(pId, jId, state, criterion);
	}

	@Test
	public void createTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
		routineInstance.setRoutineId(routineId);

		RoutineDTO routine = new RoutineDTO();
		TaskDTO task = new TaskDTO();
		task.setId(tId);

		when(domainController.createEmptyTask(pId, jId)).thenReturn(tId);
		when(libraryControllerMock.fetchRoutine(routineId)).thenReturn(routine);
		when(domainController.configureTask(pId, jId, tId, routineInstance, routine)).thenReturn(task);

		String fetchedId = execLogicController.createTask(pId, jId, routineInstance);

		assertEquals(tId, fetchedId);
		verify(domainController).createEmptyTask(pId, jId);
		verify(libraryControllerMock).fetchRoutine(routineId);
		verify(domainController).configureTask(pId, jId, tId, routineInstance, routine);
		verify(workerControllerMock).runTask(task);
	}

	@Test
	public void getTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();

		when(domainController.getTask(pId, jId, tId)).thenReturn(task);

		TaskDTO fetchedTask = execLogicController.getTask(pId, jId, tId);

		assertSame(task, fetchedTask);
		verify(domainController).getTask(pId, jId, tId);
	}

	@Test
	public void getTaskPartial() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		boolean includeInParams = true;
		boolean includeOutParams = false;
		TaskDTO task = new TaskDTO();

		when(domainController.getTaskPartial(pId, jId, tId, includeInParams, includeOutParams)).thenReturn(task);

		TaskDTO fetchedTask = execLogicController.getTaskPartial(pId, jId, tId, includeInParams, includeOutParams);

		assertSame(task, fetchedTask);
		verify(domainController).getTaskPartial(pId, jId, tId, includeInParams, includeOutParams);
	}

	@Test
	public void markJobAsComplete() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();

		execLogicController.markJobAsComplete(pId, jId);

		verify(domainController).markJobAsComplete(pId, jId);
		verify(workerControllerMock).markJobAsComplete(jId);
	}

	@Test
	public void abortTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();

		execLogicController.abortTask(pId, jId, tId);

		verify(domainController).abortTask(pId, jId, tId);
		verify(workerControllerMock).abortTask(tId);
	}

	@Test
	public void reRunTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();

		when(domainController.fetchAndPrepareTaskForReRun(pId, jId, tId)).thenReturn(task);

		execLogicController.reRunTask(pId, jId, tId);

		verify(domainController).fetchAndPrepareTaskForReRun(pId, jId, tId);
		verify(workerControllerMock).runTask(task);
	}

	@Test
	public void getAllSharedResources() throws Exception {
		String pId = UUID.randomUUID().toString();
		List<String> resources = Arrays.asList(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);

		when(domainController.getAllSharedResources(pId)).thenReturn(resources);

		List<String> fetchedResources = execLogicController.getAllSharedResources(pId);

		assertSame(resources, fetchedResources);
		verify(domainController).getAllSharedResources(pId);
	}

	@Test
	public void createSharedResource() throws Exception {
		String pId = UUID.randomUUID().toString();
		String dataTypeId = UUID.randomUUID().toString();
		ByteBuffer data = ByteBuffer.wrap("hello world".getBytes());
		String resourceId = UUID.randomUUID().toString();
		ResourceDTO resource = new ResourceDTO();
		resource.setId(resourceId);

		when(domainController.createSharedResource(pId, dataTypeId, data)).thenReturn(resource);

		String fetchedId = execLogicController.createSharedResource(pId, dataTypeId, data);

		assertSame(resourceId, fetchedId);
		verify(domainController).createSharedResource(pId, dataTypeId, data);
		verify(workerControllerMock).distributeSharedResource(resource);
		verify(reducerControllerMock).distributeSharedResource(resource);
	}

	@Test
	public void getSharedResource() throws Exception {
		String pId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		ResourceDTO resource = new ResourceDTO();

		when(domainController.getSharedResource(pId, rId)).thenReturn(resource);

		ResourceDTO fetchedResource = execLogicController.getSharedResource(pId, rId);

		assertSame(resource, fetchedResource);
		verify(domainController).getSharedResource(pId, rId);
	}

	@Test
	public void deleteSharedResource() throws Exception {
		String pId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();

		execLogicController.deleteSharedResource(pId, rId);

		verify(domainController).deleteSharedResource(pId, rId);
		verify(workerControllerMock).removeSharedResources(Collections.singletonList(rId));
		verify(reducerControllerMock).removeSharedResources(Collections.singletonList(rId));
	}

	@Test
	public void notifyTasksNewState_successWithReduceJob_reduceJobNotFinished() throws Exception {
		String wId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();
		List<String> taskIds = Arrays.asList(
				tId
		);
		ExecutionState newState = ExecutionState.SUCCESS;
		TaskDTO task = new TaskDTO();
		task.setId(tId);
		task.setJobId(jId);
		task.setProgramId(pId);
		task.setState(newState);
		task.setOutParameters(Arrays.asList(new ResourceDTO()));
		JobDTO job = new JobDTO();
		job.setId(jId);


		when(workerControllerMock.fetchFinishedTask(wId, tId)).thenReturn(task);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(true);
		when(domainController.allTasksOfJobSuccessful(pId, jId)).thenReturn(false);

		execLogicController.notifyTasksNewState(wId, taskIds, newState);

		verify(workerControllerMock).fetchFinishedTask(wId, tId);
		verify(domainController).notifyTaskChangedState(task);
		verify(domainController, times(1)).hasJobReduceRoutine(pId, jId);
		verify(reducerControllerMock).scheduleResourcesToReduce(jId, task.getOutParameters());
		verify(domainController).allTasksOfJobSuccessful(pId, jId);
		verify(reducerControllerMock, times(0)).finalizeReduce(anyString());
		verify(domainController, times(0)).setReducedResultsOfJob(anyString(), anyString(), anyList());
	}

	@Test
	public void notifyTasksNewState_successWithReduceJob_reduceJobFinished() throws Exception {
		String wId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();
		List<String> taskIds = Arrays.asList(
				tId
		);
		ExecutionState newState = ExecutionState.SUCCESS;
		TaskDTO task = new TaskDTO();
		task.setId(tId);
		task.setJobId(jId);
		task.setProgramId(pId);
		task.setState(newState);
		task.setOutParameters(Arrays.asList(new ResourceDTO()));
		JobDTO job = new JobDTO();
		job.setId(jId);

		JobDTO reduceJob = new JobDTO();
		reduceJob.setId(jId);
		reduceJob.setProgramId(pId);
		reduceJob.setReducedResults(Arrays.asList(new ResourceDTO()));

		when(workerControllerMock.fetchFinishedTask(wId, tId)).thenReturn(task);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(true);
		when(domainController.allTasksOfJobSuccessful(pId, jId)).thenReturn(true);
		when(reducerControllerMock.finalizeReduce(jId)).thenReturn(reduceJob);

		execLogicController.notifyTasksNewState(wId, taskIds, newState);

		verify(workerControllerMock).fetchFinishedTask(wId, tId);
		verify(domainController).notifyTaskChangedState(task);
		verify(domainController, times(1)).hasJobReduceRoutine(pId, jId);
		verify(reducerControllerMock).scheduleResourcesToReduce(jId, task.getOutParameters());
		verify(domainController).allTasksOfJobSuccessful(pId, jId);
		verify(reducerControllerMock).finalizeReduce(jId);
		verify(domainController).setReducedResultsOfJob(pId, jId, reduceJob.getReducedResults());
	}

	@Test
	public void notifyTasksNewState_successWithoutReduceJob() throws Exception {
		String wId = UUID.randomUUID().toString();
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();
		String tId = UUID.randomUUID().toString();
		List<String> taskIds = Arrays.asList(
				tId
		);
		ExecutionState newState = ExecutionState.SUCCESS;
		TaskDTO task = new TaskDTO();
		task.setId(tId);
		task.setState(newState);
		task.setOutParameters(Arrays.asList(new ResourceDTO()));
		job.addTask(task);

		when(workerControllerMock.fetchFinishedTask(wId, tId)).thenReturn(task);
		when(domainController.getJobById(pId, jId)).thenReturn(job);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(false);

		execLogicController.notifyTasksNewState(wId, taskIds, newState);

		verify(workerControllerMock).fetchFinishedTask(wId, tId);
		verify(domainController).notifyTaskChangedState(task);
		verify(domainController, times(1)).hasJobReduceRoutine(pId, jId);
		verify(reducerControllerMock, times(0)).scheduleResourcesToReduce(anyString(), anyList());
		verify(domainController, times(0)).allTasksOfJobSuccessful(anyString(), anyString());
		verify(reducerControllerMock, times(0)).finalizeReduce(anyString());
		verify(domainController, times(0)).setReducedResultsOfJob(anyString(), anyString(), anyList());
	}

	@Test
	public void notifyTasksNewState_failedWithReduceJob() throws Exception {
		String wId = UUID.randomUUID().toString();
		Program program = new Program("user1");
		String pId = program.getId();
		Job job = new Job(program);
		String jId = job.getId();
		String tId = UUID.randomUUID().toString();
		List<String> taskIds = Arrays.asList(
				tId
		);
		ExecutionState newState = ExecutionState.FAILED;
		TaskDTO task = new TaskDTO();
		task.setId(tId);
		task.setState(newState);
		task.setOutParameters(Arrays.asList(new ResourceDTO()));
		job.addTask(task);

		when(workerControllerMock.fetchFinishedTask(wId, tId)).thenReturn(task);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(true);
		when(domainController.allTasksOfJobSuccessful(pId, jId)).thenReturn(false);

		execLogicController.notifyTasksNewState(wId, taskIds, newState);

		verify(workerControllerMock).fetchFinishedTask(wId, tId);
		verify(domainController).notifyTaskChangedState(task);
		verify(domainController, times(0)).hasJobReduceRoutine(pId, jId);
		verify(reducerControllerMock, times(0)).scheduleResourcesToReduce(anyString(), anyList());
		verify(reducerControllerMock, times(0)).finalizeReduce(anyString());
		verify(domainController, times(0)).setReducedResultsOfJob(anyString(), anyString(), anyList());
	}

	@Test
	public void notifyTasksNewState_failedWithoutReduceJob() throws Exception {
		String wId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();
		List<String> taskIds = Arrays.asList(
				tId
		);
		ExecutionState newState = ExecutionState.FAILED;
		TaskDTO task = new TaskDTO();
		task.setId(tId);
		task.setJobId(jId);
		task.setProgramId(pId);
		task.setState(newState);
		task.setOutParameters(Arrays.asList(new ResourceDTO()));


		when(workerControllerMock.fetchFinishedTask(wId, tId)).thenReturn(task);
		when(domainController.hasJobReduceRoutine(pId, jId)).thenReturn(false);

		execLogicController.notifyTasksNewState(wId, taskIds, newState);

		verify(workerControllerMock).fetchFinishedTask(wId, tId);
		verify(domainController).notifyTaskChangedState(task);
		verify(domainController, times(0)).hasJobReduceRoutine(pId, jId);
		verify(reducerControllerMock, times(0)).scheduleResourcesToReduce(anyString(), anyList());
		verify(domainController, times(0)).allTasksOfJobSuccessful(anyString(), anyString());
		verify(reducerControllerMock, times(0)).finalizeReduce(anyString());
		verify(domainController, times(0)).setReducedResultsOfJob(anyString(), anyString(), anyList());
	}

	@Test
	public void reScheduleTasks() throws Exception {
		String t1Id = UUID.randomUUID().toString();
		TaskDTO task1 = new TaskDTO();
		String t2Id = UUID.randomUUID().toString();
		TaskDTO task2 = new TaskDTO();
		List<String> taskIds = Arrays.asList(
				t1Id, t2Id
		);

		when(domainController.getTask(t1Id)).thenReturn(task1);
		when(domainController.getTask(t2Id)).thenReturn(task2);

		execLogicController.reScheduleTasks(taskIds);

		verify(domainController, times(2)).getTask(anyString());
		verify(workerControllerMock, times(2)).runTask((TaskDTO)any());
	}

	@Test
	public void reScheduleReduceResources() throws Exception {
		String jId = UUID.randomUUID().toString();
		List<String> resourceKeys = Arrays.asList(
				"key1", "key2"
		);
		List<ResourceDTO> resources = Arrays.asList(
				new ResourceDTO()
		);

		when(domainController.getResourcesWithSpecificKeys(jId, resourceKeys)).thenReturn(resources);

		execLogicController.reScheduleReduceResources(jId, resourceKeys);

		verify(domainController).getResourcesWithSpecificKeys(jId, resourceKeys);
		verify(reducerControllerMock).scheduleResourcesToReduce(jId, resources);
	}

	@Test
	public void reSchedulePrograms() throws Exception {
		String p1Id = UUID.randomUUID().toString();
		ProgramDTO program1 = new ProgramDTO();
		String p2Id = UUID.randomUUID().toString();
		ProgramDTO program2 = new ProgramDTO();
		List<String> programIds = Arrays.asList(
				p1Id, p2Id
		);

		when(domainController.getProgram(p1Id)).thenReturn(program1);
		when(domainController.getProgram(p2Id)).thenReturn(program2);

		execLogicController.reSchedulePrograms(programIds);

		verify(domainController, times(2)).getProgram(anyString());
		verify(clientRoutineWorkerControllerMock, times(2)).runProgram(any());
	}

	/*
	//@Test
	public void stressTest() throws Exception {
        int startTime = 1900;
        int stepSize = 10;
        int iterations = 160;
        int nrOfThreads = 10;
        int nrOfTasks = 20;

        ExecutorService pool = Executors.newFixedThreadPool(nrOfThreads);

        String wId = UUID.randomUUID().toString();
		IWorkerServiceClient workerServiceClient = Mockito.mock(IWorkerServiceClient.class);
		when(workerControllerMock.getServiceClient(wId)).thenReturn(workerServiceClient);

		final Map<String, TaskDTO> tasks = new HashMap<>();

        // Mock worker actions
        when(workerServiceClient.fetchFinishedTask(anyString())).thenAnswer(answer -> {
            return new Future<TaskDTO>() {
                String id = (String)(answer.getArguments()[0]);
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
                    return true;
                }

                @Override
                public TaskDTO get() throws InterruptedException, ExecutionException {
                    TaskDTO tmpTask = tasks.get(id).deepCopy();
                    tmpTask.setState(ExecutionState.SUCCESS);
                    return tmpTask;
                }

                @Override
                public TaskDTO get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    TaskDTO tmpTask = tasks.get(id).deepCopy();
                    tmpTask.setState(ExecutionState.SUCCESS);
                    return tmpTask;
                }
            };
        });

		// Prepare objects
		String pId = UUID.randomUUID().toString();
		Program program = new Program();
		program.setId(pId);
        programMap.put(pId, program);

        int sleep = startTime;
        for (int i = 0; i < iterations; i++) {
            String jId = UUID.randomUUID().toString();
            Job job = new Job(program);
            job.setId(jId);
            program.addJob(job);
            tasks.clear();

            for (int j = 0; j < nrOfTasks; j++) {
                String tId = UUID.randomUUID().toString();
                TaskDTO task = new TaskDTO();
                task.setId(tId);
                task.setJobId(jId);
                task.setProgramId(pId);

                Random rnd = new Random();
                byte[] buf = new byte[1000000];
                rnd.nextBytes(buf);
                ResourceDTO resource = new ResourceDTO();
                resource.setData(buf);
                task.addToOutParameters(resource);
                job.addTask(tId);
                tasks.put(tId, task);
            }
            job.setComplete(true);

            List<Future> futureList = new LinkedList<>();
            for (TaskDTO t: tasks.values()) {
                // Execute worker actions
				final int sleepBetween = sleep;
				Future f = pool.submit(() -> startTaskExecution(t, sleepBetween, wId));
				futureList.add(f);
            }

            for (Future future: futureList) {
            	future.get();
			}

            for (String tId: tasks.keySet()) {
                TaskDTO fetchedTask = taskCache.fetch(tId);
                if (fetchedTask.getState() != ExecutionState.SUCCESS) {
                    System.out.println("State of task with id " + fetchedTask.getId() + ": " + fetchedTask.getState());
                    fail();
                }
            }
            sleep += stepSize;
		}
		pool.shutdown();
        pool.awaitTermination(1, TimeUnit.HOURS);
	}



	private void startTaskExecution(TaskDTO task, int sleepBetween, String wId) throws RuntimeException {
		task.setState(ExecutionState.SCHEDULED);
		taskCache.cache(task.getId(), task);

		try {
			execLogicController.notifyTasksRun(Collections.singletonList(task.getId()));
			System.out.println("Sleep " + sleepBetween + " ms");
			Thread.sleep(sleepBetween);
			execLogicController.notifyTasksNewState(wId, Collections.singletonList(task.getId()), ExecutionState.SUCCESS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
*/
}
