package at.enfilo.def;

import at.enfilo.def.client.api.DEFClientFactory;
import at.enfilo.def.client.api.IDEFClient;
import at.enfilo.def.client.api.RoutineInstanceBuilder;
import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.demo.DefaultMapper;
import at.enfilo.def.demo.DoubleSumReducer;
import at.enfilo.def.demo.MemoryStorer;
import at.enfilo.def.demo.PICalc;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class LocalIntegrationTest extends IntegrationTest {

	@Before
	public void setUp() throws Exception {
		services = new LinkedList<>();
		services.add(Manager.getInstance());
		services.add(Cluster.getInstance());
		services.add(Scheduler.getInstance());
		services.add(Library.getInstance()); // TODO: a second slave library
		services.add(Worker.getInstance());
		services.add(Reducer.getInstance());
		thriftServices = new HashMap<>();
		restServices = new HashMap<>();

		// Adapt configuration
		Library.getInstance().getConfiguration().setLibraryType(LibraryType.MASTER);
		Worker.getInstance().getConfiguration().setWorkingDir("/tmp/def/worker");
		Worker.getInstance().getConfiguration().setClusterRegistration(false);
		Worker.getInstance().getConfiguration().setExecutionThreads(2);
		Worker.getInstance().getConfiguration().setLibraryEndpoint(new ServiceEndpointDTO("localhost", 40042, Protocol.THRIFT_TCP));

		startServices();
	}

	@Test
	public void integrationTest() throws Exception {
		// Create Clients
		long timeMillis = System.currentTimeMillis();
		ServiceEndpointDTO managerEndpoint = thriftServices.get(Manager.class).getServiceEndpoint();
		managerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		IManagerServiceClient managerClient = new ManagerServiceClientFactory().createClient(managerEndpoint);
		IDEFClient defClient = DEFClientFactory.createClient(managerEndpoint);

		ServiceEndpointDTO clusterEndpoint = thriftServices.get(Cluster.class).getServiceEndpoint();
		clusterEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		IClusterServiceClient clusterClient = new ClusterServiceClientFactory().createClient(clusterEndpoint);

		ServiceEndpointDTO libraryEndpoint = thriftServices.get(Library.class).getServiceEndpoint();
		libraryEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		ILibraryServiceClient libraryClient = new LibraryServiceClientFactory().createClient(libraryEndpoint);

		ServiceEndpointDTO workerEndpoint = thriftServices.get(Worker.class).getServiceEndpoint();
		workerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		IWorkerServiceClient workerClient = new WorkerServiceClientFactory().createClient(workerEndpoint);

		ServiceEndpointDTO reducerEndpoint = thriftServices.get(Reducer.class).getServiceEndpoint();
		reducerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		IReducerServiceClient reducerClient = new ReducerServiceClientFactory().createClient(reducerEndpoint);

		// Add Cluster to Manager
		Future<Void> future = managerClient.addCluster(clusterClient.getServiceEndpoint());
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
		assertNotNull(managerClient.getClusterIds().get());
		assertEquals(1, managerClient.getClusterIds().get().size());

		// Add Worker to Cluster
		future = clusterClient.addNode(workerClient.getServiceEndpoint(), NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
		assertNotNull(clusterClient.getAllNodes(NodeType.WORKER).get());
		assertEquals(1, clusterClient.getAllNodes(NodeType.WORKER).get().size());

		// Add Reducer to Cluster
		future = clusterClient.addNode(reducerClient.getServiceEndpoint(), NodeType.REDUCER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
		assertNotNull(clusterClient.getAllNodes(NodeType.REDUCER).get());
		assertEquals(1, clusterClient.getAllNodes(NodeType.REDUCER).get().size());

		// Set default routines
		String defaultMapRoutineId = UUID.nameUUIDFromBytes(DefaultMapper.class.getCanonicalName().getBytes()).toString();
		String storeRoutineId = UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString();
		assertNotNull(libraryClient.getRoutine(defaultMapRoutineId).get());
		assertNotNull(libraryClient.getRoutine(storeRoutineId).get());
		clusterClient.setDefaultMapRoutine(defaultMapRoutineId);
		clusterClient.setStoreRoutine(storeRoutineId);

		// Create Program and Job with reduce
		String userId = UUID.randomUUID().toString();
		String clusterId = clusterClient.getClusterInfo().get().getId();
		assertNotNull(clusterId);
		String pId = defClient.createProgram(clusterId, userId).get();
		assertNotNull(pId);
		String jId = defClient.createJob(pId).get();
		assertNotNull(jId);
		future = defClient.attachReduceRoutine(pId, jId, UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString());
		assertNull(future.get());

		// Create 10 Tasks with PiCalc routine
		List<Future<String>> futureTaskIds = new LinkedList<>();
		int nrTasks = 10;
		double steps = Math.pow(10.0, 9.0);
		double stepSize = 1.0 / steps;
		double taskSteps = steps / nrTasks;
		double start = 0;
		double end = taskSteps;
		String piCalcRoutineId = UUID.nameUUIDFromBytes(PICalc.class.getCanonicalName().getBytes()).toString();

		// Create shared resource for stepSize
		Future<String> frId = defClient.createSharedResource(pId, new DEFDouble(stepSize));
		String rId = frId.get();
		assertNotNull(rId);

		for (int i = 0; i < nrTasks; i++) {
			RoutineInstanceDTO piCalc = new RoutineInstanceBuilder(piCalcRoutineId)
					.addParameter("start", new DEFDouble(start))
					.addParameter("end", new DEFDouble(end))
					.addParameter("stepSize", rId)
					.build();

			Future<String> ftId = defClient.createTask(pId, jId, piCalc);
			String tId = ftId.get();
			assertNotNull(tId);

			start = end + 1;
			end += taskSteps;
		}

		future = defClient.markJobAsComplete(pId, jId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		// Wait for job and fetch task result
		JobDTO job = defClient.waitForJob(pId, jId);
		assertEquals(ExecutionState.SUCCESS, job.getState());
		if (job.getState() == ExecutionState.SUCCESS) {
			List<String> tIds = defClient.getAllTasksWithState(pId, jId, ExecutionState.SUCCESS, SortingCriterion.CREATION_DATE_FROM_NEWEST).get();
			assertEquals(nrTasks, tIds.size());
			for (String tId : tIds) {
				TaskDTO task = defClient.getTask(pId, jId, tId).get();
				assertEquals(tId, task.getId());
				assertEquals(jId, task.getJobId());
				assertEquals(pId, task.getProgramId());
				assertEquals(piCalcRoutineId, task.getObjectiveRoutineId());
				assertEquals(defaultMapRoutineId, task.getMapRoutineId());
				assertTrue(task.isSetOutParameters());
				assertEquals(1, task.getOutParametersSize());
				DEFDouble piPart = defClient.extractOutParameter(task, DEFDouble.class);
				assertTrue(piPart.getValue() > 0);
			}
		}

		assertEquals(1, job.getReducedResultsSize());

		DEFDouble pi = defClient.extractReducedResult(job, DEFDouble.class);
		assertEquals(Math.PI, pi.getValue(), 1e4);

		Future<Void> futureDeleteJob = defClient.deleteJob(pId, jId);
		assertNull(futureDeleteJob.get());
		Future<Void> futureDeleteProgram = defClient.deleteProgram(pId);
		assertNull(futureDeleteProgram.get());
		System.out.println("Time elapsed: " + (System.currentTimeMillis() - timeMillis) + "ms");
	}

	@After
	public void tearDown() throws Exception {
		stopServices();
	}
}
