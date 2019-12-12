package at.enfilo.def.scheduler.reducer.strategy;

import at.enfilo.def.scheduler.reducer.api.strategy.IReduceSchedulingStrategy;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class DefaultReduceSchedulingStrategyTest extends ReduceSchedulingStrategyTest {
	private DefaultReduceSchedulingStrategy defaultReduceSchedulingStrategy;

	@Override
	protected IReduceSchedulingStrategy createStrategy() {
		defaultReduceSchedulingStrategy = new DefaultReduceSchedulingStrategy(schedulerConfiguration);
		return defaultReduceSchedulingStrategy;
	}

	@Test
	public void mapToOneReducer() {
		List<String> nodes = new ArrayList<>(1);
		String nId = UUID.randomUUID().toString();
		nodes.add(nId);

		for (int i = 0; i < 1000; i++) {
			String key = UUID.randomUUID().toString();
			String toNode = defaultReduceSchedulingStrategy.map(key, nodes);
			assertEquals(nId, toNode);
		}
	}

	@Test
	public void mapToManyReducers() {
		List<String> nodes = new ArrayList<>();
		nodes.add(UUID.randomUUID().toString());
		nodes.add(UUID.randomUUID().toString());
		nodes.add(UUID.randomUUID().toString());
		nodes.add(UUID.randomUUID().toString());
		nodes.add(UUID.randomUUID().toString());

		for (int j = 0; j < 5; j++) {
			// Multiple maps with the same key MUST result the same node
			String key = UUID.randomUUID().toString();
			String toNode1 = defaultReduceSchedulingStrategy.map(key, nodes);
			for (int i = 0; i < 10000; i++) {
				String toNodeN = defaultReduceSchedulingStrategy.map(key, nodes);
				assertEquals(toNode1, toNodeN);
			}
		}
	}
}
