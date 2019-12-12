package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.node.api.NodeServiceClientFactory;
import at.enfilo.def.reducer.util.ReducerConfiguration;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReducerServiceControllerTest {
	private ReducerServiceController reducerController;
	private ReducerConfiguration configuration;
	private Map<String, ReduceJob> reduceJobs;
	private ExecutorService executorService;

	@Before
	public void setUp() throws Exception {
		configuration = new ReducerConfiguration();
		reduceJobs = new HashMap<>();
		executorService = Mockito.mock(ExecutorService.class);

		Constructor<ReducerServiceController> constructor = ReducerServiceController.class.getDeclaredConstructor(
				List.class,
				UnifiedClientFactory.class,
				ReducerConfiguration.class,
				Map.class,
				Map.class,
				ExecutorService.class
		);
		constructor.setAccessible(true);
		reducerController = constructor.newInstance(
				new LinkedList<>(),
				new NodeServiceClientFactory(),
				configuration,
				reduceJobs,
				new HashMap<>(),
				executorService
		);
	}

	@Test
	public void getAndSetStoreRoutine() {
		assertEquals(configuration.getStoreRoutineId(), reducerController.getStoreRoutineId());

		String storeRoutineId = UUID.randomUUID().toString();
		reducerController.setStoreRoutineId(storeRoutineId);
		assertEquals(storeRoutineId, reducerController.getStoreRoutineId());
	}

	@Test
	public void createReduceJob() throws ClientCreationException {
		String jId = UUID.randomUUID().toString();
		String reduceRoutineId = UUID.randomUUID().toString();

		assertFalse(reduceJobs.containsKey(jId));

		reducerController.createReduceJob(jId, reduceRoutineId);
		assertTrue(reduceJobs.containsKey(jId));
		ReduceJob reduceJob = reduceJobs.get(jId);
		assertNotNull(reduceJob);

		verify(executorService).submit(reduceJob);
	}


	@Test
	public void addResources() throws UnknownJobException {
		String jId = UUID.randomUUID().toString();
		ReduceJob reduceJob = Mockito.mock(ReduceJob.class);
		reduceJobs.put(jId, reduceJob);

		List<ResourceDTO> resources = new LinkedList<>();
		reducerController.addResources(jId, resources);

		verify(reduceJob).addResources(resources);
	}

	@Test(expected = UnknownJobException.class)
	public void addResourcesToUnknownJob() throws UnknownJobException {
		reducerController.addResources(UUID.randomUUID().toString(), new LinkedList<>());
	}

	@Test
	public void reduceAndFetchResults() throws UnknownJobException, ReduceJobException {
		String jId = UUID.randomUUID().toString();
		ReduceJob reduceJob = Mockito.mock(ReduceJob.class);
		reduceJobs.put(jId, reduceJob);
		List<Result> results = new LinkedList<>();
		results.add(new Result(1, "key", "url", null));

		when(reduceJob.isRunning()).thenReturn(false);
		when(reduceJob.isSuccessful()).thenReturn(true);
		when(reduceJob.getResults()).thenReturn(results);

		reducerController.reduce(jId);

		List<ResourceDTO> resources = reducerController.fetchResult(jId);
		assertNotNull(resources);
		assertEquals(results.size(), resources.size());
		assertEquals(results.get(0).getKey(), resources.get(0).getKey());
	}

	@Test(expected = UnknownJobException.class)
	public void reduceUnknownJob() throws UnknownJobException, ReduceJobException {
		reducerController.reduce(UUID.randomUUID().toString());
	}

	@Test(expected = UnknownJobException.class)
	public void fetchUnknownResults() throws UnknownJobException {
		reducerController.fetchResult(UUID.randomUUID().toString());
	}
}
