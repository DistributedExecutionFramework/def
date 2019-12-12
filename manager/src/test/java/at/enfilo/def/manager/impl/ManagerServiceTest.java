package at.enfilo.def.manager.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.manager.api.ManagerServiceClientFactory;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryChunkDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ManagerServiceTest {

	private IServer server;
	private Thread serverThread;
	private IManagerServiceClient client;

	protected ManagerController managerController;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());
		managerController = Mockito.mock(ManagerController.class);

		this.server = getServer();
		serverThread = new Thread(server);
		serverThread.start();

		await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
		client = new ManagerServiceClientFactory().createClient(server.getServiceEndpoint());
	}

	protected abstract IServer getServer() throws ServerCreationException;


	@After
	public void tearDown() throws Exception {
		client.close();
		server.close();
		serverThread.join();
		ServiceRegistry.getInstance().closeAll();
	}

	@Test
	public void getClusterIds() throws Exception {
		List<String> clusterIds = new LinkedList<>();
		clusterIds.add(UUID.randomUUID().toString());
		clusterIds.add(UUID.randomUUID().toString());

		when(managerController.getClusterIds()).thenReturn(clusterIds);

		Future<List<String>> futureClusterIds = client.getClusterIds();
		await().atMost(10, TimeUnit.SECONDS).until(futureClusterIds::isDone);

		assertEquals(clusterIds, futureClusterIds.get());
	}

	@Test
	public void getClusterInfo() throws Exception {
		String cId = UUID.randomUUID().toString();
		ClusterInfoDTO info = new ClusterInfoDTO();
		info.setId(cId);

		when(managerController.getClusterInfo(cId)).thenReturn(info);

		Future<ClusterInfoDTO> futureClusterInfo = client.getClusterInfo(cId);
		await().atMost(10, TimeUnit.SECONDS).until(futureClusterInfo::isDone);

		assertEquals(info, futureClusterInfo.get());
	}

	@Test
	public void getClusterEndpoint() throws Exception {
		String cId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		endpoint.setHost(cId);

		when(managerController.getClusterEndpoint(cId)).thenReturn(endpoint);

		Future<ServiceEndpointDTO> futureClusterInfo = client.getClusterEndpoint(cId);
		await().atMost(10, TimeUnit.SECONDS).until(futureClusterInfo::isDone);

		assertEquals(endpoint, futureClusterInfo.get());
	}

	@Test
	public void createCluster() throws Exception {
		// TODO rosa
		//int numberOfWorkers = 5;
		//int numberOfReducers = 1;
		//String cId = UUID.randomUUID().toString();

		//when(managerController.createAWSCluster(anyInt(), anyInt(), (AWSSpecificationDTO)notNull())).thenReturn(cId);

		//Future<String> futureCreateCluster = client.createAWSCluster(numberOfWorkers, numberOfReducers, new AWSSpecificationDTO());
		//await().atMost(10, TimeUnit.SECONDS).until(futureCreateCluster::isDone);

		//assertEquals(cId, futureCreateCluster.get());
	}

	@Test
	public void deleteCluster() throws Exception {
		String cId = UUID.randomUUID().toString();

		Future<Void> futureDeleteCluster = client.deleteCluster(cId);
		await().atMost(10, TimeUnit.SECONDS).until(futureDeleteCluster::isDone);

		verify(managerController).destroyCluster(cId);
	}

	@Test
	public void addCluster() throws Exception {
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		Future<Void> futureAddCluster = client.addCluster(endpoint);
		await().atMost(10, TimeUnit.SECONDS).until(futureAddCluster::isDone);

		verify(managerController).addCluster(endpoint);
	}

	@Test
	public void createClientRoutine() throws Exception {
		String name = UUID.randomUUID().toString();
		List<FeatureDTO> requiredFeatures = Arrays.asList(new FeatureDTO());
		List<String> arguments = Arrays.asList("argument");
		RoutineDTO routine = new RoutineDTO();
		routine.setName(name);
		routine.setRequiredFeatures(requiredFeatures);
		routine.setArguments(arguments);
		String rId = UUID.randomUUID().toString();

		when(managerController.createClientRoutine(routine)).thenReturn(rId);

		Future<String> futureRoutineId = client.createClientRoutine(routine);
		await().atMost(10, TimeUnit.SECONDS).until(futureRoutineId::isDone);

		assertEquals(rId, futureRoutineId.get());
	}

	@Test
	public void createClientRoutineBinary() throws Exception {
		String rId = UUID.randomUUID().toString();
		String rbName = "name";
		Random rnd = new Random();
		boolean isPrimary = rnd.nextBoolean();
		String rbId = UUID.randomUUID().toString();
		String md5 = UUID.randomUUID().toString();
		long sizeInBytes = rnd.nextInt();

		when(managerController.createClientRoutineBinary(rId, rbName, md5, sizeInBytes, isPrimary)).thenReturn(rbId);

		Future<String> futureRoutineBinaryId = client.createClientRoutineBinary(rId, rbName, md5, sizeInBytes, isPrimary);
		await().atMost(10, TimeUnit.SECONDS).until(futureRoutineBinaryId::isDone);

		assertEquals(rbId, futureRoutineBinaryId.get());
	}

	@Test
	public void uploadClientRoutineBinaryChunk() throws Exception {
		String rbId = UUID.randomUUID().toString();
		Random rnd = new Random();
		RoutineBinaryChunkDTO chunk = new RoutineBinaryChunkDTO();
		chunk.setChunkSize(rnd.nextInt());
		chunk.setChunk((short)rnd.nextInt());
		chunk.setTotalChunks((short)rnd.nextInt());
		byte[] data = new byte[16];
		rnd.nextBytes(data);
		chunk.setData(data);


		Future<Void> future = client.uploadClientRoutineBinaryChunk(rbId, chunk);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(managerController).uploadClientRoutineBinaryChunk(rbId, chunk);
	}


	@Test
	public void removeClientRoutine() throws Exception {
		String rId = UUID.randomUUID().toString();

		Future<Void> futureRemoveClientRoutine = client.removeClientRoutine(rId);
		await().atMost(10, TimeUnit.SECONDS).until(futureRemoveClientRoutine::isDone);

		verify(managerController).removeClientRoutine(rId);
	}
}
