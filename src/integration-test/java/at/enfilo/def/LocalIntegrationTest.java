package at.enfilo.def;

import at.enfilo.def.client.api.DEFClientFactory;
import at.enfilo.def.client.api.IDEFClient;
import at.enfilo.def.client.api.RoutineInstanceBuilder;
import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import at.enfilo.def.clientroutine.worker.api.IClientRoutineWorkerServiceClient;
import at.enfilo.def.clientroutine.worker.server.ClientRoutineWorker;
import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.demo.*;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.library.Library;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.manager.api.ManagerServiceClientFactory;
import at.enfilo.def.manager.server.Manager;
import at.enfilo.def.reducer.api.IReducerServiceClient;
import at.enfilo.def.reducer.api.ReducerServiceClientFactory;
import at.enfilo.def.reducer.server.Reducer;
import at.enfilo.def.scheduler.server.Scheduler;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import at.enfilo.def.worker.server.Worker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class LocalIntegrationTest extends IntegrationTest {

	private static IClusterServiceClient clusterClient;
	private static IDEFClient defClient;
	private static String defaultMapRoutineId;

	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println("Starting services");
		long timeMillis = System.currentTimeMillis();

		services = new LinkedList<>();
		services.add(Manager.getInstance());
		services.add(Cluster.getInstance());
		services.add(Scheduler.getInstance());
		services.add(Library.getInstance()); // TODO: a second slave library
		services.add(Worker.getInstance());
		services.add(Reducer.getInstance());
		services.add(ClientRoutineWorker.getInstance());
		thriftServices = new HashMap<>();
		restServices = new HashMap<>();

		// Adapt configuration
		Library.getInstance().getConfiguration().setLibraryType(LibraryType.MASTER);

		Worker.getInstance().getConfiguration().setWorkingDir("/tmp/def/worker");
		Worker.getInstance().getConfiguration().setClusterRegistration(false);
		Worker.getInstance().getConfiguration().setExecutionThreads(2);
		Worker.getInstance().getConfiguration().setLibraryEndpoint(new ServiceEndpointDTO("127.0.0.1", 40042, Protocol.THRIFT_TCP));

		Reducer.getInstance().getConfiguration().setWorkingDir("/tmp/def/reducer");
		Reducer.getInstance().getConfiguration().setClusterRegistration(false);
		Reducer.getInstance().getConfiguration().setExecutionThreads(2);
		Reducer.getInstance().getConfiguration().setLibraryEndpoint(new ServiceEndpointDTO("127.0.0.1", 40042, Protocol.THRIFT_TCP));

		ClientRoutineWorker.getInstance().getConfiguration().setWorkingDir("/tmp/def/client-routine-worker");
		ClientRoutineWorker.getInstance().getConfiguration().setClusterRegistration(false);
		ClientRoutineWorker.getInstance().getConfiguration().setExecutionThreads(1);
		ClientRoutineWorker.getInstance().getConfiguration().setLibraryEndpoint(new ServiceEndpointDTO("127.0.0.1", 40042, Protocol.THRIFT_TCP));
		ClientRoutineWorker.getInstance().getConfiguration().setClusterEndpoint(new ServiceEndpointDTO("127.0.0.1", 40012, Protocol.THRIFT_TCP));

		startServices();

		// Create Clients
		System.out.println("Create manager client");
		ServiceEndpointDTO managerEndpoint = thriftServices.get(Manager.class).getServiceEndpoint();
		managerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		IManagerServiceClient managerClient = new ManagerServiceClientFactory().createClient(managerEndpoint);
		System.out.println("Create exec logic client");
		IExecLogicServiceClient execClient = new ExecLogicServiceClientFactory().createClient(managerEndpoint);
		System.out.println("Create DEF client");
		defClient = DEFClientFactory.createClient(managerEndpoint);

		System.out.println("Create cluster client");
		ServiceEndpointDTO clusterEndpoint = thriftServices.get(Cluster.class).getServiceEndpoint();
		clusterEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		clusterClient = new ClusterServiceClientFactory().createClient(clusterEndpoint);

		System.out.println("Create library client");
		ServiceEndpointDTO libraryEndpoint = thriftServices.get(Library.class).getServiceEndpoint();
		libraryEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		ILibraryServiceClient libraryClient = new LibraryServiceClientFactory().createClient(libraryEndpoint);

		System.out.println("Create worker client");
		ServiceEndpointDTO workerEndpoint = thriftServices.get(Worker.class).getServiceEndpoint();
		workerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		IWorkerServiceClient workerClient = new WorkerServiceClientFactory().createClient(workerEndpoint);

		System.out.println("Create reducer client");
		ServiceEndpointDTO reducerEndpoint = thriftServices.get(Reducer.class).getServiceEndpoint();
		reducerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		IReducerServiceClient reducerClient = new ReducerServiceClientFactory().createClient(reducerEndpoint);

		System.out.println("Create client routine worker client");
		ServiceEndpointDTO clientRoutineWorkerEndpoint = thriftServices.get(ClientRoutineWorker.class).getServiceEndpoint();
		clientRoutineWorkerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		IClientRoutineWorkerServiceClient clientRoutineWorkerClient = new ClientRoutineWorkerServiceClientFactory().createClient(clientRoutineWorkerEndpoint);

		// Add Cluster to Manager
		System.out.println("Add cluster to manager");
		Future<Void> future = managerClient.addCluster(clusterClient.getServiceEndpoint());
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
		assertNotNull(managerClient.getClusterIds().get());
		assertEquals(1, managerClient.getClusterIds().get().size());

		// Add Worker to Cluster
		System.out.println("Add worker to cluster");
		future = clusterClient.addNode(workerClient.getServiceEndpoint(), NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
		assertNotNull(clusterClient.getAllNodes(NodeType.WORKER).get());
		assertEquals(1, clusterClient.getAllNodes(NodeType.WORKER).get().size());

		// Add Reducer to Cluster
		System.out.println("Add reducer to cluster");
		future = clusterClient.addNode(reducerClient.getServiceEndpoint(), NodeType.REDUCER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
		assertNotNull(clusterClient.getAllNodes(NodeType.REDUCER).get());
		assertEquals(1, clusterClient.getAllNodes(NodeType.REDUCER).get().size());

		// Add Client routine worker to Cluster
		System.out.println("Add client routine worker to cluster");
		future = clusterClient.addNode(clientRoutineWorkerClient.getServiceEndpoint(), NodeType.CLIENT);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
		assertNotNull(clusterClient.getAllNodes(NodeType.CLIENT).get());
		assertEquals(1, clusterClient.getAllNodes(NodeType.CLIENT).get().size());

		// Set default routines
		System.out.println("Set default routines");
		defaultMapRoutineId = UUID.nameUUIDFromBytes(DefaultMapper.class.getCanonicalName().getBytes()).toString();
		String storeRoutineId = UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString();
		assertNotNull(libraryClient.getRoutine(defaultMapRoutineId).get());
		assertNotNull(libraryClient.getRoutine(storeRoutineId).get());
		future = clusterClient.setDefaultMapRoutine(defaultMapRoutineId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
		future = clusterClient.setStoreRoutine(storeRoutineId, NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
		future = clusterClient.setStoreRoutine(storeRoutineId, NodeType.REDUCER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
		future = clusterClient.setStoreRoutine(storeRoutineId, NodeType.CLIENT);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		System.out.println("Started services in " + (System.currentTimeMillis() - timeMillis) + "ms");
	}

	@Test
	public void integrationTestWithoutClientRoutine() throws Exception {
		System.out.println("Start integration test without client routine");
		long timeMillis = System.currentTimeMillis();

		// Create Program and two jobs with reduce
		System.out.println("Create program");
		String userId = UUID.randomUUID().toString();
		String clusterId = clusterClient.getClusterInfo().get().getId();
		assertNotNull(clusterId);
		String pId = defClient.createProgram(clusterId, userId).get();
		assertNotNull(pId);

		System.out.println("Create jobs");
		String j1Id = defClient.createJob(pId).get();
		assertNotNull(j1Id);
		Future<Void> future = defClient.attachReduceRoutine(pId, j1Id, UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString());
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		String j2Id = defClient.createJob(pId).get();
		assertNotNull(j2Id);
		future = defClient.attachReduceRoutine(pId, j2Id, UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString());
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		// Create 10 Tasks with PiCalc routine
		int nrTasks = 10;
		double steps = Math.pow(10.0, 7.0);
		double stepSize = 1.0 / steps;
		double taskSteps = steps / nrTasks;
		String piCalcRoutineId = UUID.nameUUIDFromBytes(PICalc.class.getCanonicalName().getBytes()).toString();

		// Create shared resource for stepSize
		System.out.println("Create shared resource");
		Future<String> frId = defClient.createSharedResource(pId, new DEFDouble(stepSize));
		String rId = frId.get();
		assertNotNull(rId);

		for (String jId : new String[] {j1Id, j2Id}) {
			double start = 0;
			double end = taskSteps;
			for (int i = 0; i < nrTasks; i++) {
				System.out.println("Create task in Job " + jId);
				RoutineInstanceDTO piCalc = new RoutineInstanceBuilder(piCalcRoutineId)
					.addParameter("start", new DEFDouble(start))
					.addParameter("end", new DEFDouble(end))
					.addParameter("stepSize", rId)
					.build();

				Future<String> ftId = defClient.createTask(pId, jId, piCalc);
				ftId.get();

				start = end + 1;
				end += taskSteps;
			}
		}

		System.out.println("Mark jobs as complete");
		future = defClient.markJobAsComplete(pId, j1Id);
        await().atMost(60, TimeUnit.SECONDS).until(future::isDone);
		future = defClient.markJobAsComplete(pId, j2Id);
		await().atMost(60, TimeUnit.SECONDS).until(future::isDone);

        // Wait for job and fetch task result
		System.out.println("Wait for jobs");
		JobDTO job1 = defClient.waitForJob(pId, j1Id);
		assertEquals(ExecutionState.SUCCESS, job1.getState());
		JobDTO job2 = defClient.waitForJob(pId, j2Id);
		assertEquals(ExecutionState.SUCCESS, job2.getState());
		System.out.println("Jobs fetched");
		if (job1.getState() == ExecutionState.SUCCESS) {
			System.out.println("Process results of job1");
			List<String> tIds = defClient.getAllTasksWithState(pId, j1Id, ExecutionState.SUCCESS, SortingCriterion.CREATION_DATE_FROM_NEWEST).get();
			assertEquals(nrTasks, tIds.size());
			for (String tId : tIds) {
				TaskDTO task = defClient.getTask(pId, j1Id, tId).get();
				assertEquals(tId, task.getId());
				assertEquals(j1Id, task.getJobId());
				assertEquals(pId, task.getProgramId());
				assertEquals(piCalcRoutineId, task.getObjectiveRoutineId());
				assertEquals(defaultMapRoutineId, task.getMapRoutineId());
				assertTrue(task.isSetOutParameters());
				assertEquals(1, task.getOutParametersSize());
				DEFDouble piPart = defClient.extractOutParameter(task, DEFDouble.class);
				assertTrue(piPart.getValue() > 0);
			}
		}

		if (job2.getState() == ExecutionState.SUCCESS) {
			System.out.println("Process results of job2");
			List<String> tIds = defClient.getAllTasksWithState(pId, j2Id, ExecutionState.SUCCESS, SortingCriterion.CREATION_DATE_FROM_NEWEST).get();
			assertEquals(nrTasks, tIds.size());
			for (String tId: tIds) {
				TaskDTO task = defClient.getTask(pId, j2Id, tId).get();
				assertEquals(tId, task.getId());
				assertEquals(j2Id, task.getJobId());
				assertEquals(pId, task.getProgramId());
				assertEquals(piCalcRoutineId, task.getObjectiveRoutineId());
				assertEquals(defaultMapRoutineId, task.getMapRoutineId());
				assertTrue(task.isSetOutParameters());
				assertEquals(1, task.getOutParametersSize());
				DEFDouble piPart = defClient.extractOutParameter(task, DEFDouble.class);
				assertTrue(piPart.getValue() > 0);
			}
		}

		assertEquals(1, job1.getReducedResultsSize());
		assertEquals(1, job2.getReducedResultsSize());

		DEFDouble pi = defClient.extractReducedResult(job1, DEFDouble.class);
		assertEquals(Math.PI, pi.getValue(), 0.1);
		pi = defClient.extractReducedResult(job2, DEFDouble.class);
		assertEquals(Math.PI, pi.getValue(), 0.1);

		System.out.println("Delete jobs");
		Future<Void> futureDeleteJob = defClient.deleteJob(pId, j1Id);
		await().atMost(30, TimeUnit.SECONDS).until(futureDeleteJob::isDone);
		System.out.println("Delete program");
		Future<Void> futureDeleteProgram = defClient.deleteProgram(pId);
		await().atMost(30, TimeUnit.SECONDS).until(futureDeleteProgram::isDone);

		System.out.println("Finished integration test without client routine in " + (System.currentTimeMillis() - timeMillis) + "ms");
	}

	@Test
	public void integrationTestWithClientRoutine() throws Exception {
		System.out.println("Start integration test with client routine");
		long timeMillis = System.currentTimeMillis();

		// Create Program and two jobs with reduce
		System.out.println("Create program");
		String userId = UUID.randomUUID().toString();
		String clusterId = clusterClient.getClusterInfo().get().getId();
		assertNotNull(clusterId);
		String pId = defClient.createProgram(clusterId, userId).get();
		assertNotNull(pId);

		System.out.println("Attach client routine");
		Future<Void> future = defClient.startClientRoutine(pId, UUID.nameUUIDFromBytes(PiCalcClientRoutine.class.getCanonicalName().getBytes()).toString());
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		System.out.println("Wait for program to finish");
		ProgramDTO program = defClient.waitForProgram(pId);
		assertNotNull(program);

		Map<String, DEFDouble> results = defClient.extractResults(program, DEFDouble.class);
		assertTrue(results.size() > 0);
		for (Map.Entry<String, DEFDouble> entry: results.entrySet()) {
			assertEquals(Math.PI, entry.getValue().getValue(), 1e-4);
		}

		System.out.println("Finished integration test with client routine in " + (System.currentTimeMillis() - timeMillis) + "ms");
	}

	@Test
	public void integrationTestWithClientRoutineAndReduce() throws Exception {
		System.out.println("Start integration test with client routine and reduce");
		long timeMillis = System.currentTimeMillis();

		// Create Program and two jobs with reduce
		System.out.println("Create program");
		String userId = UUID.randomUUID().toString();
		String clusterId = clusterClient.getClusterInfo().get().getId();
		assertNotNull(clusterId);
		String pId = defClient.createProgram(clusterId, userId).get();
		assertNotNull(pId);

		System.out.println("Attach client routine");
		Future<Void> future = defClient.startClientRoutine(pId, UUID.nameUUIDFromBytes(PiCalcWithReduceClientRoutine.class.getCanonicalName().getBytes()).toString());
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		System.out.println("Wait for program to finish");
		ProgramDTO program = defClient.waitForProgram(pId);
		assertNotNull(program);

		Map<String, DEFDouble> results = defClient.extractResults(program, DEFDouble.class);
		assertTrue(results.size() > 0);
		for (Map.Entry<String, DEFDouble> entry: results.entrySet()) {
			assertEquals(Math.PI, entry.getValue().getValue(), 1);
		}

		System.out.println("Finished integration test with client routine and reduce in " + (System.currentTimeMillis() - timeMillis) + "ms");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		System.out.println("Stopping services");
		long timeMillis = System.currentTimeMillis();
		stopServices();
		System.out.println("Stopped services in " + (System.currentTimeMillis() - timeMillis) + "ms");
	}
}
