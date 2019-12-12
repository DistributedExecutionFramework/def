package at.enfilo.def.cluster.impl;

import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.domain.entity.*;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.scheduler.api.ISchedulerServiceClient;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ClusterExecLogicControllerTest {

	private ClusterExecLogicController execLogicController;
	private Map<String, Program> programMap;
	private ILibraryServiceClient libraryServiceClientMock;
	private ISchedulerServiceClient schedulerServiceClient;
	private WorkerController nodeControllerMock;
	private Random rnd;
	private DTOCache<TaskDTO> taskCache;

	@Before
	public void setUp() throws Exception {
		programMap = new TimeoutMap<>(
			1, TimeUnit.HOURS,
			1, TimeUnit.HOURS
		);
		rnd = new Random();
		taskCache = DTOCache.getInstance(ClusterExecLogicController.DTO_TASK_CACHE_CONTEXT, TaskDTO.class);
		libraryServiceClientMock = Mockito.mock(ILibraryServiceClient.class);
		schedulerServiceClient = Mockito.mock(ISchedulerServiceClient.class);
		nodeControllerMock = Mockito.mock(WorkerController.class);

		ClusterResource.getInstance().setWorkerSchedulerServiceClient(
				schedulerServiceClient
		);
		ClusterResource.getInstance().setReducerSchedulerServiceClient(
				schedulerServiceClient
		);

		// Create controller with special constructor
		Constructor<ClusterExecLogicController> constructor = ClusterExecLogicController.class.getDeclaredConstructor(
				WorkerController.class,
				ILibraryServiceClient.class,
				Map.class
		);
		constructor.setAccessible(true);
		execLogicController = constructor.newInstance(
                nodeControllerMock,
				libraryServiceClientMock,
				programMap
		);

		when(libraryServiceClientMock.getRoutine((String)notNull())).then(invocation -> {
			RoutineDTO routineDTO = new RoutineDTO();
			routineDTO.setType(RoutineType.MAP);
			routineDTO.setInParameters(new LinkedList<>());
			routineDTO.setOutParameter(new FormalParameterDTO());
			Future<RoutineDTO> future = new Future<RoutineDTO>() {
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
				public RoutineDTO get() throws InterruptedException, ExecutionException {
					return routineDTO;
				}

				@Override
				public RoutineDTO get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
					return null;
				}
			};
			return future;
		});

		when(schedulerServiceClient.addJob((String)notNull())).then(invocation -> new Future<Void>() {
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
			public Void get() throws InterruptedException, ExecutionException {
				return null;
			}

			@Override
			public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				return null;
			}
		});

		when(schedulerServiceClient.scheduleTask((String)notNull(), (TaskDTO)notNull())).then(invocation -> new Future<Void>() {
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
			public Void get() throws InterruptedException, ExecutionException {
				return null;
			}

			@Override
			public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				return null;
			}
		});
	}

	@Test
	public void getAllPrograms() throws Exception {
		// Create test users
		User u1 = new User();
		String u1Id = UUID.randomUUID().toString();
		u1.setId(u1Id);
		User u2 = new User();
		String u2Id = UUID.randomUUID().toString();
		u2.setId(u2Id);

		// Create test programs
		Program p1 = new Program();
		String p1Id = UUID.randomUUID().toString();
		p1.setId(p1Id);
		p1.setOwner(u1);
		Program p2 = new Program();
		String p2Id = UUID.randomUUID().toString();
		p2.setId(p2Id);
		p2.setOwner(u1);
		Program p3 = new Program();
		String p3Id = UUID.randomUUID().toString();
		p3.setId(p3Id);
		p3.setOwner(u2);
		programMap.put(p1Id, p1);
		programMap.put(p2Id, p2);
		programMap.put(p3Id, p3);

		assertEquals(2, execLogicController.getAllPrograms(u1Id).size());
		assertEquals(1, execLogicController.getAllPrograms(u2Id).size());
		assertTrue(execLogicController.getAllPrograms(u1Id).contains(p1Id));
		assertTrue(execLogicController.getAllPrograms(u1Id).contains(p2Id));
		assertTrue(execLogicController.getAllPrograms(u2Id).contains(p3Id));
	}

	@Test
	public void createAndGetProgram() throws Exception {
		String cId = ClusterResource.getInstance().getId();
		String uId = UUID.randomUUID().toString();

		assertEquals(0, programMap.size());

		List<String> pIds = new LinkedList<>();

		// Create Programs
		int programs = rnd.nextInt(10) + 1;
		for (int i = 0; i < programs; i++) {
			String pId = execLogicController.createProgram(cId, uId);
			pIds.add(pId);
			assertNotNull(pId);
			assertTrue(programMap.containsKey(pId));
		}

		// Fetch Programs
		for (String pId : pIds) {
			ProgramDTO program = execLogicController.getProgram(pId);
			assertEquals(pId, program.getId());
		}
	}

	@Test(expected = UnknownProgramException.class)
	public void unknownProgram() throws Exception {
		execLogicController.getProgram(UUID.randomUUID().toString());
	}

	@Test
	public void deleteProgram() throws Exception {
		// Prepare a Program with Jobs and Tasks
		Program p = new Program();
		ResourceDTO sr1 = new ResourceDTO(UUID.randomUUID().toString(), "dataTypeId");
		p.addSharedResource(sr1);
		ResourceDTO sr2 = new ResourceDTO(UUID.randomUUID().toString(), "dataTypeId");
		p.addSharedResource(sr2);
		Job j1 = new Job();
		p.addJob(j1);
		Job j2 = new Job();
		p.addJob(j2);
		Task t11 = new Task();
		j1.addTask(t11.getId());
		Task t12 = new Task();
		j1.addTask(t12.getId());
		Task t21 = new Task();
		j2.addTask(t21.getId());
		Task t22 = new Task();
		j2.addTask(t22.getId());
		programMap.put(p.getId(), p);

		Future<Void> future = Mockito.mock(Future.class);
		when(schedulerServiceClient.removeJob(anyString())).thenReturn(future);
		when(future.get()).thenReturn(null);

		// Proof domain
		assertEquals(2, p.getJobs().size());
		assertEquals(2, p.getSharedResources().size());
		assertEquals(2, j1.getAllTasks().size());
		assertEquals(2, j2.getAllTasks().size());

		// Delete Program (with all other)
		execLogicController.deleteProgram(p.getId());

		assertFalse(programMap.containsKey(p.getId()));
		assertTrue(p.getSharedResources().isEmpty());
		assertTrue(p.getJobs().isEmpty());
		assertTrue(j1.getAllTasks().isEmpty());
		assertTrue(j2.getAllTasks().isEmpty());
	}

	@Test
	public void deleteAndAbortProgram() throws Exception {
		// Prepare a Program with Jobs and Tasks
		Program p = new Program();
		p.setState(ExecutionState.RUN);
		Job j1 = new Job();
		j1.setStartTime(Instant.now());
		j1.setState(ExecutionState.RUN);
		p.addJob(j1);
		TaskDTO t11 = new TaskDTO();
		t11.setId(UUID.randomUUID().toString());
		j1.addTask(t11.getId());
		DTOCache.getInstance(ClusterExecLogicController.DTO_TASK_CACHE_CONTEXT, TaskDTO.class).cache(t11.getId(), t11);
		TaskDTO t12 = new TaskDTO();
		t12.setId(UUID.randomUUID().toString());
		DTOCache.getInstance(ClusterExecLogicController.DTO_TASK_CACHE_CONTEXT, TaskDTO.class).cache(t12.getId(), t12);
		j1.addTask(t12.getId());

		programMap.put(p.getId(), p);

		Future<Void> future = Mockito.mock(Future.class);
		when(schedulerServiceClient.removeJob(anyString())).thenReturn(future);
		when(future.get()).thenReturn(null);

		// Proof domain
		assertEquals(ExecutionState.RUN, p.getState());
		assertEquals(ExecutionState.RUN, p.getJobById(j1.getId()).getState());
		assertEquals(1, p.getJobs().size());
		assertEquals(2, j1.getAllTasks().size());

		// Delete Program (with all other)
		execLogicController.deleteProgram(p.getId());

		assertFalse(programMap.containsKey(p.getId()));
		assertTrue(p.getSharedResources().isEmpty());
		assertTrue(p.getJobs().isEmpty());
		assertTrue(j1.getAllTasks().isEmpty());
	}

	@Test
	public void abortProgram() throws Exception {
		// Prepare a Program with Jobs and Tasks
		String pId = execLogicController.createProgram(ClusterResource.getInstance().getId(), "");
		String j1Id = execLogicController.createJob(pId);
		String t11Id = execLogicController.createTask(pId, j1Id, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));
		String t12Id = execLogicController.createTask(pId, j1Id, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));
		String j2Id = execLogicController.createJob(pId);
		String t21Id = execLogicController.createTask(pId, j2Id, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));
		String t22Id = execLogicController.createTask(pId, j2Id, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));

		ProgramDTO program = execLogicController.getProgram(pId);
		JobDTO job1 = execLogicController.getJob(pId, j1Id);
		TaskDTO task11 = execLogicController.getTask(pId, j1Id, t11Id);
		TaskDTO task12 = execLogicController.getTask(pId, j1Id, t12Id);
		JobDTO job2 = execLogicController.getJob(pId, j2Id);
		TaskDTO task21 = execLogicController.getTask(pId, j2Id, t21Id);
		TaskDTO task22 = execLogicController.getTask(pId, j2Id, t22Id);

		assertEquals(ExecutionState.SCHEDULED, program.getState());
		assertEquals(ExecutionState.SCHEDULED, job1.getState());
		assertEquals(ExecutionState.SCHEDULED, task11.getState());
		assertEquals(ExecutionState.SCHEDULED, task12.getState());
		assertEquals(ExecutionState.SCHEDULED, job2.getState());
		assertEquals(ExecutionState.SCHEDULED, task21.getState());
		assertEquals(ExecutionState.SCHEDULED, task22.getState());

		// Mock scheduler
		Future<Void> futureAbortJob = Mockito.mock(Future.class);
		when(schedulerServiceClient.removeJob((String)notNull())).thenReturn(futureAbortJob);
		when(futureAbortJob.get()).thenReturn(null);

		// Delete Program (with all other)
		execLogicController.abortProgram(pId);

		program = execLogicController.getProgram(pId);
		job1 = execLogicController.getJob(pId, j1Id);
		task11 = execLogicController.getTask(pId, j1Id, t11Id);
		task12 = execLogicController.getTask(pId, j1Id, t12Id);
		job2 = execLogicController.getJob(pId, j2Id);
		task21 = execLogicController.getTask(pId, j2Id, t21Id);
		task22 = execLogicController.getTask(pId, j2Id, t22Id);

		assertEquals(ExecutionState.FAILED, program.getState());
		assertEquals(ExecutionState.FAILED, job1.getState());
		assertEquals(ExecutionState.FAILED, job2.getState());
		assertEquals(ExecutionState.FAILED, task11.getState());
		assertEquals(ExecutionState.FAILED, task12.getState());
		assertEquals(ExecutionState.FAILED, task21.getState());
		assertEquals(ExecutionState.FAILED, task22.getState());
	}

	@Test
	public void updateProgramName() throws Exception {
		String oldName = "name";
		Program p = new Program();
		p.setName(oldName);
		programMap.put(p.getId(), p);
		assertEquals(oldName, p.getName());

		String newName = "new name";
		execLogicController.updateProgramName(p.getId(), newName);
		assertEquals(newName, p.getName());
	}

	@Test
	public void updateProgramDescription() throws Exception {
		String oldDescription = "description";
		Program p = new Program();
		p.setDescription(oldDescription);
		programMap.put(p.getId(), p);
		assertEquals(oldDescription, p.getDescription());

		String newDescription = "new description";
		execLogicController.updateProgramDescription(p.getId(), newDescription);
		assertEquals(newDescription, p.getDescription());
	}

	@Test
	public void markProgramAsFinished() throws Exception {
		Program p = new Program();
		programMap.put(p.getId(), p);

		assertFalse(p.isFinished());

		// Mark as finished
		execLogicController.markProgramAsFinished(p.getId());

		assertTrue(p.isFinished());
	}

	@Test
	public void getAllJobs() throws Exception {
		// Prepare Jobs
		int nrJobs = rnd.nextInt(100) + 5;
		Program p = new Program();
		List<String> sortedJobs = new LinkedList<>();
		for (int i = 0; i < nrJobs; i++) {
			Job j = new Job();
			p.addJob(j);
			sortedJobs.add(j.getId());
			// Wait before create a new job, because there will be sorted by creation time
			// Creation time stamp has millisecond resolution
			Thread.sleep(1);
		}
		programMap.put(p.getId(), p);

		// Request Jobs
		List<String> jobs = execLogicController.getAllJobs(p.getId());
		assertEquals(nrJobs, jobs.size());
		assertEquals(sortedJobs, jobs);
	}


	@Test
	public void createAndGetJob() throws Exception {
		// Prepare Program
		Program p = new Program();
		programMap.put(p.getId(), p);

		// Mock scheduler
		Future<Void> futureAddJob = Mockito.mock(Future.class);
		when(schedulerServiceClient.addJob(anyObject())).thenReturn(futureAddJob);
		when(futureAddJob.get()).thenReturn(null);

		// Mock library
		RoutineDTO mapRoutine = new RoutineDTO();
		mapRoutine.setType(RoutineType.MAP);
		Future<RoutineDTO> futureRoutine = Mockito.mock(Future.class);
		when(libraryServiceClientMock.getRoutine(anyString())).thenReturn(futureRoutine);
		when(futureRoutine.get()).thenReturn(mapRoutine);

		// Create Job
		String jId = execLogicController.createJob(p.getId());
		assertNotNull(jId);
		assertEquals(1, p.getJobs().size());

		// Get JobInfo
		JobDTO job = execLogicController.getJob(p.getId(), jId);
		assertNotNull(job);
		assertEquals(jId, job.getId());

		verify(schedulerServiceClient).addJob(job.getId());
	}


	@Test
	public void deleteJob() throws Exception {
		// Prepare a Program with Jobs and Tasks
		Program p = new Program();
		Job j1 = new Job();
		p.addJob(j1);
		Job j2 = new Job();
		p.addJob(j2);
		Task t11 = new Task();
		j1.addTask(t11.getId());
		Task t12 = new Task();
		j1.addTask(t12.getId());
		programMap.put(p.getId(), p);

		// Mock scheduler service client
		Future<Void> future = Mockito.mock(Future.class);
		when(future.get()).thenReturn(null);
		when(schedulerServiceClient.removeJob(j1.getId())).thenReturn(future);

		// Proof domain
		assertEquals(2, p.getJobs().size());
		assertEquals(2, j1.getAllTasks().size());

		// Delete Job
		execLogicController.deleteJob(p.getId(), j1.getId());
		assertEquals(1, p.getJobs().size());
		assertEquals(0, j1.getAllTasks().size());
		assertFalse(execLogicController.getAllJobs(p.getId()).contains(j1.getId()));
	}


	@Test
	public void attachMapRoutine() throws Exception {
		// Prepare a Program with a Job
		Program p = new Program();
		Job j = new Job();
		p.addJob(j);

		// Prepare Routine
		String mapRoutineId = UUID.randomUUID().toString();
		RoutineDTO routineDTO = new RoutineDTO();
		routineDTO.setId(mapRoutineId);
		routineDTO.setType(RoutineType.MAP);


		programMap.put(p.getId(), p);

		// No MapRoutine Attached --> should be default map routine id
		String rId = execLogicController.getAttachedMapRoutine(p.getId(), j.getId());
		assertNull(rId);

		// Mocking library service
		Future<RoutineDTO> futureRoutine = Mockito.mock(Future.class);
		when(libraryServiceClientMock.getRoutine(mapRoutineId)).thenReturn(futureRoutine);
		when(futureRoutine.get()).thenReturn(routineDTO);

		execLogicController.attachMapRoutine(p.getId(), j.getId(), mapRoutineId);

		// Get new attachedMapRoutine
		rId = execLogicController.getAttachedMapRoutine(p.getId(), j.getId());
		assertEquals(mapRoutineId, rId);
	}


	@Test
	public void attachReduceRoutine() throws Exception {
		// Prepare a Program with a Job
		Program p = new Program();
		Job j = new Job();
		j.setId(UUID.randomUUID().toString());
		p.addJob(j);

		programMap.put(p.getId(), p);

		// Prepare Routine
		String reduceRoutineId = UUID.randomUUID().toString();
		RoutineDTO routineDTO = new RoutineDTO();
		routineDTO.setId(reduceRoutineId);
		routineDTO.setType(RoutineType.REDUCE);

		// No MapRoutine Attached --> should be default map routine id
		String rId = execLogicController.getAttachedReduceRoutine(p.getId(), j.getId());
		assertNull(rId);

		// Mocking library service
		Future<RoutineDTO> futureRoutine = Mockito.mock(Future.class);
		when(libraryServiceClientMock.getRoutine(reduceRoutineId)).thenReturn(futureRoutine);
		when(futureRoutine.get()).thenReturn(routineDTO);

		// Mocking reduce scheduler
		Future<Void> futureExtend = Mockito.mock(Future.class);
		when(futureExtend.isDone()).thenReturn(true);
		when(futureExtend.get()).thenReturn(null);
		when(schedulerServiceClient.extendToReduceJob(j.getId(), reduceRoutineId)).thenReturn(futureExtend);

		execLogicController.attachReduceRoutine(p.getId(), j.getId(), reduceRoutineId);

		// Get attached reduce routine
		rId = execLogicController.getAttachedReduceRoutine(p.getId(), j.getId());
		assertEquals(reduceRoutineId, rId);
	}


	@Test
	public void getAllTasksWithState_unsorted() throws Exception {
		// Prepare a Program with a Job and a many tasks
		String pId = execLogicController.createProgram(ClusterResource.getInstance().getId(), "");
		String jId = execLogicController.createJob(pId);

		int nrTasks = rnd.nextInt(100) + 5;
		List<String> taskIds = new LinkedList<>();
		for (int i = 0; i < nrTasks; i++) {
			String taskId = execLogicController.createTask(pId, jId, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));
			taskIds.add(taskId);
		}

//		programMap.put(pId, program);

		// Get tasks (unsorted)
		List<String> tasks = execLogicController.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING);
		assertEquals(taskIds.size(), tasks.size());
		for (String tId : taskIds) {
			assertTrue(tasks.contains(tId));
		}

		assertEquals(taskIds.size(), execLogicController.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING).size());
		assertEquals(0, execLogicController.getAllTasksWithState(pId, jId, ExecutionState.RUN, SortingCriterion.NO_SORTING).size());
		assertEquals(0, execLogicController.getAllTasksWithState(pId, jId, ExecutionState.SUCCESS, SortingCriterion.NO_SORTING).size());
		assertEquals(0, execLogicController.getAllTasksWithState(pId, jId, ExecutionState.FAILED, SortingCriterion.NO_SORTING).size());

		String wId = UUID.randomUUID().toString();
		List<String> runningTaskIds = new LinkedList<>();
		runningTaskIds.add(taskIds.get(0));
		execLogicController.notifyTasksNewState(wId, runningTaskIds, ExecutionState.RUN);
		taskIds.remove(taskIds.get(0));
		execLogicController.notifyTasksNewState(wId, taskIds, ExecutionState.SCHEDULED);

		assertEquals(1, execLogicController.getAllTasksWithState(pId, jId, ExecutionState.RUN, SortingCriterion.NO_SORTING).size());
	}

	@Test
	public void getAllTasks_unsorted() throws Exception {
		// Prepare a Program with a Job and many tasks
		String pId = execLogicController.createProgram(ClusterResource.getInstance().getId(), "user");
		String jId = execLogicController.createJob(pId);

		int nrTasks = rnd.nextInt(100) + 5;
		List<String> taskIds = new LinkedList<>();
		for (int i = 0; i < nrTasks; i++) {
			String taskId = execLogicController.createTask(pId, jId, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));
			taskIds.add(taskId);
		}

		// Get tasks (unsorted)
		List<String> tasks = execLogicController.getAllTasks(pId, jId, SortingCriterion.NO_SORTING);
		assertEquals(taskIds.size(), tasks.size());
		for (String tId: taskIds) {
			assertTrue(tasks.contains(tId));
		}
	}

	@Test
	public void getAllTasks_sorted() throws Exception {
		// Prepare a Program with a Job and some Tasks
		String pId = execLogicController.createProgram(ClusterResource.getInstance().getId(), "user");
		String jId = execLogicController.createJob(pId);

		int nrOfTasks = 3;
		List<String> taskIds = new LinkedList<>();
		for (int i = 0; i < nrOfTasks; i++) {
			String taskId = execLogicController.createTask(pId, jId, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));
			TimeUnit.SECONDS.sleep(1);
			taskIds.add(taskId);
		}

		// Get tasks by creation date
		List<String> tasks = execLogicController.getAllTasks(pId, jId, SortingCriterion.CREATION_DATE_FROM_OLDEST);
		assertEquals(taskIds.size(), tasks.size());
		for (String tId: taskIds) {
			assertTrue(tasks.contains(tId));
		}

		assertEquals(taskIds.get(0), tasks.get(0));
		assertEquals(taskIds.get(1), tasks.get(1));
		assertEquals(taskIds.get(2), tasks.get(2));

		tasks = execLogicController.getAllTasks(pId, jId, SortingCriterion.CREATION_DATE_FROM_NEWEST);
		assertEquals(taskIds.size(), tasks.size());
		for (String tId: taskIds) {
			assertTrue(tasks.contains(tId));
		}

		assertEquals(taskIds.get(0), tasks.get(2));
		assertEquals(taskIds.get(1), tasks.get(1));
		assertEquals(taskIds.get(2), tasks.get(0));

		// Get tasks by start date
		execLogicController.notifyTasksNewState(UUID.randomUUID().toString(), Arrays.asList(taskIds.get(1)), ExecutionState.RUN);
		TimeUnit.SECONDS.sleep(1);
		execLogicController.notifyTasksNewState(UUID.randomUUID().toString(), Arrays.asList(taskIds.get(0)), ExecutionState.RUN);
		TimeUnit.SECONDS.sleep(1);
		execLogicController.notifyTasksNewState(UUID.randomUUID().toString(), Arrays.asList(taskIds.get(2)), ExecutionState.RUN);
		TimeUnit.SECONDS.sleep(1);

		tasks = execLogicController.getAllTasks(pId, jId, SortingCriterion.START_DATE_FROM_OLDEST);
		assertEquals(taskIds.size(), tasks.size());
		for (String tId: taskIds) {
			assertTrue(tasks.contains(tId));
		}

		assertEquals(tasks.get(0), taskIds.get(1));
		assertEquals(tasks.get(1), taskIds.get(0));
		assertEquals(tasks.get(2), taskIds.get(2));

		tasks = execLogicController.getAllTasks(pId, jId, SortingCriterion.START_DATE_FROM_NEWEST);
		assertEquals(taskIds.size(), tasks.size());
		for (String tId: taskIds) {
			assertTrue(tasks.contains(tId));
		}

		assertEquals(tasks.get(0), taskIds.get(2));
		assertEquals(tasks.get(1), taskIds.get(0));
		assertEquals(tasks.get(2), taskIds.get(1));

		// Get tasks by finish date
//		execLogicController.notifyTasksNewState(UUID.randomUUID().toString(), Arrays.asList(taskIds.get(2)), ExecutionState.SUCCESS);
//		TimeUnit.SECONDS.sleep(1);
//		execLogicController.notifyTasksNewState(UUID.randomUUID().toString(), Arrays.asList(taskIds.get(1)), ExecutionState.SUCCESS);
//		TimeUnit.SECONDS.sleep(1);
//		execLogicController.notifyTasksNewState(UUID.randomUUID().toString(), Arrays.asList(taskIds.get(0)), ExecutionState.FAILED);
//		TimeUnit.SECONDS.sleep(1);
//
//		tasks = execLogicController.getAllTasks(pId, jId, SortingCriterion.FINISH_DATE_FROM_OLDEST);
//		assertEquals(taskIds.size(), tasks.size());
//		for (String tId: taskIds) {
//			assertTrue(tasks.contains(tId));
//		}
//
//		assertEquals(tasks.get(0), taskIds.get(2));
//		assertEquals(tasks.get(1), taskIds.get(1));
//		assertEquals(tasks.get(2), taskIds.get(0));
//
//		tasks = execLogicController.getAllTasks(pId, jId, SortingCriterion.FINISH_DATE_FROM_NEWEST);
//		assertEquals(taskIds.size(), tasks.size());
//		for (String tId: taskIds) {
//			assertTrue(tasks.contains(tId));
//		}
//
//		assertEquals(tasks.get(0), taskIds.get(0));
//		assertEquals(tasks.get(1), taskIds.get(1));
//		assertEquals(tasks.get(2), taskIds.get(2));

		// TODO: runtime??
	}

	@Test
	public void createAndGetTask() throws Exception {
		// Prepare a Program with a Job
		Program p = new Program();
		Job j = new Job();
		p.addJob(j);
		// Prepare a objective routine
		DataTypeDTO dt = new DataTypeDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "schema");
		String paramName = UUID.randomUUID().toString();
		RoutineDTO r = new RoutineDTO();
		r.setId(UUID.randomUUID().toString());
		r.setType(RoutineType.OBJECTIVE);
		r.setName("demo");
		r.setInParameters(new LinkedList<>());
		r.getInParameters().add(new FormalParameterDTO(UUID.randomUUID().toString(), paramName, "", dt));
		// Prepare a map routine
		Routine rMap = new Routine();
		rMap.setId(UUID.randomUUID().toString());
		rMap.setType(RoutineType.MAP);
		j.setMapRoutine(rMap);
		// Prepare Resource
		RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
		routineInstance.setRoutineId(r.getId());
		routineInstance.setInParameters(new HashMap<>());
		routineInstance.getInParameters().put(paramName, new ResourceDTO(
				null,
				dt.getId()
		));
		routineInstance.getInParameters().get(paramName).setData(new byte[0]);

		programMap.put(p.getId(), p);

		// Mocking library service
		Future<RoutineDTO> future = Mockito.mock(Future.class);
		when(libraryServiceClientMock.getRoutine(r.getId())).thenReturn(future);
		when(future.get()).thenReturn(r);
		// Mock scheduler
		Future<Void> futureScheduleTask = Mockito.mock(Future.class);
		when(schedulerServiceClient.scheduleTask(eq(j.getId()), anyObject())).thenReturn(futureScheduleTask);
		when(futureScheduleTask.get()).thenReturn(null);


		String tId = execLogicController.createTask(p.getId(), j.getId(), routineInstance);
		assertEquals(1, j.getAllTasks().size());
		TaskDTO t = taskCache.fetch(tId);
		assertNotNull(t);

		TaskDTO task = execLogicController.getTaskPartial(p.getId(), j.getId(), tId, false, false);
		assertNotNull(task);
		assertEquals(tId, task.getId());
		assertNull(task.getInParameters());
		assertNull(task.getOutParameters());

		task = execLogicController.getTask(p.getId(), j.getId(), tId);
		assertNotNull(task);
		assertEquals(tId, task.getId());
		assertEquals(r.getId(), task.getObjectiveRoutineId());
		assertEquals(1, task.getInParameters().size());
		assertEquals(ExecutionState.SCHEDULED, task.getState());

		// Change state for verify call
		task.setState(null);
		verify(schedulerServiceClient).scheduleTask(j.getId(), task);
	}


	@Test
	public void markJobAsComplete() throws Exception {
		// Prepare a Program with a Job
		Program p = new Program();
		Job j = new Job();
		p.addJob(j);

		programMap.put(p.getId(), p);

		assertFalse(j.isComplete());

		execLogicController.markJobAsComplete(p.getId(), j.getId());
		assertTrue(j.isComplete());
	}

	@Test
	public void abortJob() throws Exception {
		// Prepare a Program with a Job and two Tasks
		String pId = execLogicController.createProgram(ClusterResource.getInstance().getId(), "");
		String jId = execLogicController.createJob(pId);
		String t1Id = execLogicController.createTask(pId, jId, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));
		String t2Id = execLogicController.createTask(pId, jId, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));

		ProgramDTO program = execLogicController.getProgram(pId);
		JobDTO job = execLogicController.getJob(pId, jId);
		TaskDTO task1 = execLogicController.getTask(pId, jId, t1Id);
		TaskDTO task2 = execLogicController.getTask(pId, jId, t2Id);

		assertEquals(ExecutionState.SCHEDULED, program.getState());
		assertEquals(ExecutionState.SCHEDULED, job.getState());
		assertEquals(ExecutionState.SCHEDULED, task1.getState());
		assertEquals(ExecutionState.SCHEDULED, task2.getState());

		// Mock scheduler
		Future<Void> futureAbortJob = Mockito.mock(Future.class);
		when(schedulerServiceClient.removeJob(jId)).thenReturn(futureAbortJob);
		when(futureAbortJob.get()).thenReturn(null);

		execLogicController.abortJob(pId, jId);

		program = execLogicController.getProgram(pId);
		job = execLogicController.getJob(pId, jId);
		task1 = execLogicController.getTask(pId, jId, t1Id);
		task2 = execLogicController.getTask(pId, jId, t2Id);

		assertEquals(ExecutionState.SCHEDULED, program.getState());
		assertEquals(ExecutionState.FAILED, job.getState());
		assertEquals(ExecutionState.FAILED, task1.getState());
		assertEquals(ExecutionState.FAILED, task2.getState());
	}

	@Test
	public void abortTask() throws Exception {
		// Prepare a Program with a Job and two Tasks
		String pId = execLogicController.createProgram(ClusterResource.getInstance().getId(), "");
		String jId = execLogicController.createJob(pId);
		String t1Id = execLogicController.createTask(pId, jId, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));
		String t2Id = execLogicController.createTask(pId, jId, new RoutineInstanceDTO(UUID.randomUUID().toString(), new HashMap<>(), new LinkedList<>()));

		ProgramDTO program = execLogicController.getProgram(pId);
		JobDTO job = execLogicController.getJob(pId, jId);
		TaskDTO task1 = execLogicController.getTask(pId, jId, t1Id);
		TaskDTO task2 = execLogicController.getTask(pId, jId, t2Id);

		assertEquals(ExecutionState.SCHEDULED, program.getState());
		assertEquals(ExecutionState.SCHEDULED, job.getState());
		assertEquals(ExecutionState.SCHEDULED, task1.getState());
		assertEquals(ExecutionState.SCHEDULED, task2.getState());

		execLogicController.abortTask(pId, jId, t1Id);

		program = execLogicController.getProgram(pId);
		job = execLogicController.getJob(pId, jId);
		task1 = execLogicController.getTask(pId, jId, t1Id);
		task2 = execLogicController.getTask(pId, jId, t2Id);

		assertEquals(ExecutionState.SCHEDULED, program.getState());
		assertEquals(ExecutionState.SCHEDULED, job.getState());
		assertEquals(ExecutionState.FAILED, task1.getState());
		assertEquals(ExecutionState.SCHEDULED, task2.getState());
		assertNotEquals(0, task1.getFinishTime());

		verify(nodeControllerMock, times(1)).abortTask(t1Id);
	}

	@Test
	public void createAndGetSharedResources() throws Exception {
		// Prepare a Program
		Program p = new Program();
		DataTypeDTO dataType = new DataTypeDTO(UUID.randomUUID().toString(), "name", "schema");

		programMap.put(p.getId(), p);

		// Create resources
		String r1Id = execLogicController.createSharedResource(p.getId(), dataType.getId(), ByteBuffer.allocate(1));
		String r2Id = execLogicController.createSharedResource(p.getId(), dataType.getId(), ByteBuffer.allocate(2));
		String r3Id = execLogicController.createSharedResource(p.getId(), dataType.getId(), ByteBuffer.allocate(3));

		// Get resources
		ResourceDTO res1 = execLogicController.getSharedResource(p.getId(), r1Id);
		ResourceDTO res2 = execLogicController.getSharedResource(p.getId(), r2Id);
		ResourceDTO res3 = execLogicController.getSharedResource(p.getId(), r3Id);

		assertNotNull(res1);
		assertNotNull(res2);
		assertNotNull(res3);
		assertEquals(r1Id, res1.getId());
		assertEquals(r2Id, res2.getId());
		assertEquals(r3Id, res3.getId());

		// Get all
		List<String> resIds = execLogicController.getAllSharedResources(p.getId());
		assertEquals(3, resIds.size());
		assertTrue(resIds.contains(r1Id));
		assertTrue(resIds.contains(r2Id));
		assertTrue(resIds.contains(r3Id));
	}

	@Test
	public void deleteSharedResource() throws Exception {
		// Prepare a Program with 2 SharedResources
		Program p = new Program();
		ResourceDTO res1 = new ResourceDTO(UUID.randomUUID().toString(), "dataTypeId");
		ResourceDTO res2 = new ResourceDTO(UUID.randomUUID().toString(), "dataTypeId");
		p.addSharedResource(res1);
		p.addSharedResource(res2);

		programMap.put(p.getId(), p);

		List<String> resIds = execLogicController.getAllSharedResources(p.getId());
		assertEquals(2, resIds.size());

		// Delete res1
		execLogicController.deleteSharedResource(p.getId(), res1.getId());
		resIds = execLogicController.getAllSharedResources(p.getId());
		assertEquals(1, resIds.size());
		assertTrue(resIds.contains(res2.getId()));


		// Delete res2
		execLogicController.deleteSharedResource(p.getId(), res2.getId());
		resIds = execLogicController.getAllSharedResources(p.getId());
		assertEquals(0, resIds.size());
	}

	@Test
	public void getInstance() throws Exception {
		ClusterExecLogicController instance1 = ClusterExecLogicController.getInstance();
		ClusterExecLogicController instance2 = ClusterExecLogicController.getInstance();

		assertSame(instance1, instance2);
	}


	@Test
	public void reSchedule() throws Exception {
		Routine objectiveRoutine = new Routine();
		objectiveRoutine.setType(RoutineType.OBJECTIVE);
		objectiveRoutine.setId(UUID.randomUUID().toString());
		Routine mapRoutine = new Routine();
		mapRoutine.setType(RoutineType.MAP);
		mapRoutine.setId(UUID.randomUUID().toString());
		Program program = new Program();
		program.setId(UUID.randomUUID().toString());
		String jId = UUID.randomUUID().toString();
		Job job = new Job();
		job.setId(jId);
		program.addJob(job);
		String t1Id = UUID.randomUUID().toString();
		TaskDTO task1 = new TaskDTO();
		task1.setId(t1Id);
		task1.setObjectiveRoutineId(objectiveRoutine.getId());
		task1.setMapRoutineId(mapRoutine.getId());
		task1.setJobId(jId);
		job.addTask(task1.getId());
		String t2Id = UUID.randomUUID().toString();
		TaskDTO task2 = new TaskDTO();
		task2.setId(t2Id);
		task2.setObjectiveRoutineId(objectiveRoutine.getId());
		task2.setMapRoutineId(mapRoutine.getId());
		task2.setJobId(jId);
		job.addTask(task2.getId());
		taskCache.cache(task1.getId(), task1);
		taskCache.cache(task2.getId(), task2);

		List<String> toReschedule = new LinkedList<>();
		toReschedule.add(t1Id);
		toReschedule.add(t2Id);

		// Mock scheduler
		Future<Void> futureScheduleTask = Mockito.mock(Future.class);
		when(schedulerServiceClient.scheduleTask(eq(jId), anyObject())).thenReturn(futureScheduleTask);
		when(futureScheduleTask.get()).thenReturn(null);

		execLogicController.reSchedule(toReschedule);

		verify(schedulerServiceClient, times(1)).scheduleTask(jId, task1);
		verify(schedulerServiceClient, times(1)).scheduleTask(jId, task2);
		assertEquals(2, job.getScheduledTasks().size());
	}


	@Test
	public void notifyTasksSuccess() throws Exception {
		// Prepare worker
		String wId = UUID.randomUUID().toString();
		IWorkerServiceClient workerServiceClient = Mockito.mock(IWorkerServiceClient.class);
		when(nodeControllerMock.getServiceClient(wId)).thenReturn(workerServiceClient);

		// Prepare tasks
		RoutineDTO objectiveRoutine = new RoutineDTO();
		objectiveRoutine.setType(RoutineType.OBJECTIVE);
		objectiveRoutine.setId(UUID.randomUUID().toString());
		Routine mapRoutine = new Routine();
		mapRoutine.setType(RoutineType.MAP);
		mapRoutine.setId(UUID.randomUUID().toString());
		String pId = UUID.randomUUID().toString();
		Program program = new Program();
		program.setId(UUID.randomUUID().toString());
		String jId = UUID.randomUUID().toString();
		Job job = new Job();
		job.setId(jId);
		job.setMapRoutine(mapRoutine);
		program.addJob(job);
		String t1Id = UUID.randomUUID().toString();
		TaskDTO task1 = new TaskDTO();
		task1.setJobId(jId);
		task1.setProgramId(pId);
		task1.setId(t1Id);
		task1.setObjectiveRoutineId(objectiveRoutine.getId());
		task1.setMapRoutineId(mapRoutine.getId());
		task1.setState(ExecutionState.SUCCESS);
		job.addTask(task1.getId());
		String t2Id = UUID.randomUUID().toString();
		TaskDTO task2 = new TaskDTO();
		task2.setId(t2Id);
		task2.setJobId(jId);
		task2.setProgramId(pId);
		task2.setObjectiveRoutineId(objectiveRoutine.getId());
		task2.setMapRoutineId(mapRoutine.getId());
		task2.setState(ExecutionState.SUCCESS);
		job.addTask(task2.getId());
		programMap.put(pId, program);
		taskCache.cache(task1.getId(), task1);
		taskCache.cache(task2.getId(), task2);

		// Mock worker actions
		Future<TaskDTO> futureTask1 = Mockito.mock(Future.class);
		Future<TaskDTO> futureTask2 = Mockito.mock(Future.class);
		when(workerServiceClient.fetchFinishedTask(t1Id)).thenReturn(futureTask1);
		when(workerServiceClient.fetchFinishedTask(t2Id)).thenReturn(futureTask2);
		when(futureTask1.get()).thenReturn(task1);
		when(futureTask2.get()).thenReturn(task2);

		assertTrue(job.getScheduledTasks().contains(t1Id));
		assertTrue(job.getScheduledTasks().contains(t2Id));

		execLogicController.notifyTasksNewState(wId, Collections.singletonList(t1Id), ExecutionState.SUCCESS);
		assertTrue(job.getSuccessfulTasks().contains(t1Id));
		assertFalse(job.getSuccessfulTasks().contains(t2Id));

		execLogicController.notifyTasksNewState(wId, Collections.singletonList(t2Id), ExecutionState.SUCCESS);
		assertTrue(job.getSuccessfulTasks().contains(t1Id));
		assertTrue(job.getSuccessfulTasks().contains(t2Id));
	}

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
		when(nodeControllerMock.getServiceClient(wId)).thenReturn(workerServiceClient);

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
            Job job = new Job();
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
}
