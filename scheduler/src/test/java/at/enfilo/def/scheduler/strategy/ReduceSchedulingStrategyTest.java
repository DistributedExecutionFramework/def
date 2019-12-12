package at.enfilo.def.scheduler.strategy;

import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.reducer.api.IReducerServiceClient;
import at.enfilo.def.reducer.api.ReducerServiceClientFactory;
import at.enfilo.def.scheduler.api.strategy.IReduceSchedulingStrategy;
import at.enfilo.def.scheduler.util.SchedulerConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public abstract class ReduceSchedulingStrategyTest {
	protected Map<String, IReducerServiceClient> nodes;
	protected SchedulerConfiguration schedulerConfiguration;
	protected IReduceSchedulingStrategy strategy;
	protected IReducerServiceClient reducerServiceClient;

	@Before
	public void setUp() throws Exception {
		nodes = new HashMap<>();
		ReducerServiceClientFactory reducerServiceClientFactory = Mockito.mock(ReducerServiceClientFactory.class);
		reducerServiceClient = Mockito.mock(IReducerServiceClient.class);
		when(reducerServiceClientFactory.createClient(any())).thenReturn(reducerServiceClient);
		schedulerConfiguration = SchedulerConfiguration.getDefault();
		strategy = createStrategy();
	}

	protected abstract IReduceSchedulingStrategy createStrategy();

	@Test
	public void addJobGetJobRemoveJob() throws Exception {
		// Create mocks and two workers
		nodes.put(UUID.randomUUID().toString(), reducerServiceClient);
		nodes.put(UUID.randomUUID().toString(), reducerServiceClient);
		String jId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();

		Future<Void> futureStatus = Mockito.mock(Future.class);
		when(reducerServiceClient.createReduceJob(jId, rId)).thenReturn(futureStatus);
		when(reducerServiceClient.deleteReduceJob(jId)).thenReturn(futureStatus);
		when(futureStatus.isDone()).thenReturn(true);
		when(futureStatus.get()).thenReturn(null);
	}
}
