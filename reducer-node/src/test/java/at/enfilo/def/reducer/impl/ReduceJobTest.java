package at.enfilo.def.reducer.impl;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.demo.DoubleSumReducer;
import at.enfilo.def.demo.MemoryStorer;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.util.BaseRoutineRegistry;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.reducer.util.ReducerConfiguration;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ReduceJobTest {
	private ReduceJob reduceJob;
	private Thread reduceJobThread;
	private ILibraryServiceClient libraryServiceClient;

	@Before
	public void setUp() throws Exception {
		BaseRoutineRegistry registry = BaseRoutineRegistry.getInstance();
		RoutineDTO doubleSumReducer = registry.get(UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString());
		RoutineDTO memoryStorer = registry.get(UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString());
		libraryServiceClient = Mockito.mock(ILibraryServiceClient.class);
		RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
				libraryServiceClient,
				ReducerConfiguration.getDefault()
		);
		reduceJob = new ReduceJob(
				UUID.randomUUID().toString(),
				doubleSumReducer.getId(),
				memoryStorer.getId(),
				routineProcessBuilderFactory
		);
		Future<RoutineDTO> doubleSumReducerFuture = Mockito.mock(Future.class);
		when(doubleSumReducerFuture.isDone()).thenReturn(true);
		when(doubleSumReducerFuture.get()).thenReturn(doubleSumReducer);
		when(libraryServiceClient.getRoutine(doubleSumReducer.getId())).thenReturn(doubleSumReducerFuture);
		Future<RoutineDTO> memoryStorerFuture = Mockito.mock(Future.class);
		when(memoryStorerFuture.isDone()).thenReturn(true);
		when(memoryStorerFuture.get()).thenReturn(memoryStorer);
		when(libraryServiceClient.getRoutine(memoryStorer.getId())).thenReturn(memoryStorerFuture);
		reduceJobThread = new Thread(reduceJob);
		reduceJobThread.start();
		await().atMost(10, TimeUnit.SECONDS).until(reduceJob::isRunning);
	}

	@Test//(timeout = 60L * 1000L)
	public void reduceProcess() throws Exception {
		assertTrue(reduceJob.isRunning());

		TSerializer serializer = new TSerializer();
		TDeserializer deserializer = new TDeserializer();

		List<ResourceDTO> resources = new LinkedList<>();
		DEFDouble value1 = new DEFDouble(10);
		DEFDouble value2 = new DEFDouble(5.5);
		ResourceDTO resource1 = new ResourceDTO();
		resource1.setKey("key1");
		resource1.setData(serializer.serialize(value1));
		ResourceDTO resource2 = new ResourceDTO();
		resource2.setKey("key2");
		resource2.setData(serializer.serialize(value2));
		for (int i = 0; i < 10; i++) {
			resources.add(resource1);
			resources.add(resource2);
		}
		reduceJob.addResources(resources);

		reduceJob.reduceAndWait();
		assertFalse(reduceJob.isRunning());
		assertTrue(reduceJob.isSuccessful());

		List<Result> results = reduceJob.getResults();
		assertEquals(2, results.size());
		for (Result r : results) {
			DEFDouble d = new DEFDouble();
			deserializer.deserialize(d, r.getData());
			if (r.getKey().equals("key1")) {
				assertEquals(10 * 10, d.getValue(), 0.0);
			} else if (r.getKey().equals("key2")) {
				assertEquals(5.5 * 10, d.getValue(), 0.0);
			}
		}
	}
}
