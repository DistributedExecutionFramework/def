package at.enfilo.def.scheduler.strategy;

import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.scheduler.util.SchedulerConfiguration;
import at.enfilo.def.transfer.dto.NodeEnvironmentDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class SchedulingStrategyTest {
	private TestSchedulingStrategy testSchedulingStrategy;
	private Map<String, IWorkerServiceClient> nodes;
	private Map<String, Environment> nodeEnvironments;
	private IWorkerServiceClient workerServiceClient;
	private ILibraryServiceClient libraryServiceClient;

	@Before
	public void setUp() throws Exception {
		WorkerServiceClientFactory workerServiceClientFactory = Mockito.mock(WorkerServiceClientFactory.class);
		workerServiceClient = Mockito.mock(IWorkerServiceClient.class);
		when(workerServiceClientFactory.createClient(any())).thenReturn(workerServiceClient);
		libraryServiceClient = Mockito.mock(ILibraryServiceClient.class);
		nodes = new HashMap<>();
		nodeEnvironments = new HashMap<>();

		testSchedulingStrategy = new TestSchedulingStrategy(
				nodes, nodeEnvironments,
				workerServiceClientFactory,
				null,
				new SchedulerConfiguration()
		);
	}

	@Test
	public void addNode() throws Exception {
		String nId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		Future<Void> ticketStatusFuture = Mockito.mock(Future.class);
		when(workerServiceClient.registerObserver(any(), anyBoolean(), anyInt(), any())).thenReturn(ticketStatusFuture);
		when(ticketStatusFuture.get()).thenReturn(null);
		when(ticketStatusFuture.isDone()).thenReturn(true);

		Future<NodeEnvironmentDTO> envFuture = Mockito.mock(Future.class);
		when(workerServiceClient.getEnvironment()).thenReturn(envFuture);
		when(envFuture.get()).thenReturn(new NodeEnvironmentDTO());

		assertTrue(testSchedulingStrategy.getNodes().isEmpty());
		testSchedulingStrategy.addNode(nId, endpoint);
		assertTrue(testSchedulingStrategy.getNodes().contains(nId));

		// Add once more the same.
		testSchedulingStrategy.addNode(nId, endpoint);
		assertEquals(1, testSchedulingStrategy.getNodes().size());
	}

	@Test
	public void getNode() throws UnknownNodeException {
		String nId = UUID.randomUUID().toString();
		nodes.put(nId, workerServiceClient);

		IWorkerServiceClient client = testSchedulingStrategy.getNodeClient(nId);
		assertEquals(workerServiceClient, client);
	}

	@Test(expected = UnknownNodeException.class)
	public void getUnknownNode() throws UnknownNodeException {
		testSchedulingStrategy.getNodeClient(UUID.randomUUID().toString());
	}


	@Test
	public void resolveIP() {
		String ip = testSchedulingStrategy.resolveIP();
		assertFalse(ip.isEmpty());
		assertNotEquals("127.0.0.1", ip);
	}
}
