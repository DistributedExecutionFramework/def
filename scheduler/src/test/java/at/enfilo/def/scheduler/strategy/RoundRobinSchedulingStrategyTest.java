package at.enfilo.def.scheduler.strategy;

import at.enfilo.def.scheduler.api.ScheduleTaskException;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RoundRobinSchedulingStrategyTest extends TaskSchedulingStrategyTest {
	private RoundRobinSchedulingStrategy rr;


	@Override
	protected TaskSchedulingStrategy createStrategy() {
		rr = new RoundRobinSchedulingStrategy(
				nodes, nodeEnvironments,
				schedulerConfiguration,
				new WorkerServiceClientFactory(),
				libraryServiceClient
		);
		return rr;
	}

	@Test(expected = ScheduleTaskException.class)
	public void zeroNodes() throws Exception {
		nodes.clear();
		rr.nextWorkerId(null);
		rr.nextWorkerId(null);
	}

	@Test
	public void oneNode() throws Exception {
		String w1 = UUID.randomUUID().toString();
		nodes.put(w1, null);

		assertEquals(w1, rr.nextWorkerId(null));
		assertEquals(w1, rr.nextWorkerId(null));
		assertEquals(w1, rr.nextWorkerId(null));
	}

	@Test
	public void moreNodes() throws Exception {
		int nrWorkers = 5;
		for (int i = 0; i < nrWorkers; i++) {
			nodes.put(UUID.randomUUID().toString(), null);
		}

		String[] orderedWorkers = nodes.keySet().toArray(new String[nrWorkers]);

		for (int i = 0; i < 99; i++) {
			assertEquals(orderedWorkers[i % nrWorkers], rr.nextWorkerId(null));
		}
	}
}
