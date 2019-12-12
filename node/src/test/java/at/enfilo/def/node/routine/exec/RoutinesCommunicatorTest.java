package at.enfilo.def.node.routine.exec;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.NodeServiceController;
import at.enfilo.def.node.queue.ResourceQueue;
import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import at.enfilo.def.routine.util.ThreadSafePipedIOStream;
import at.enfilo.def.transfer.dto.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RoutinesCommunicatorTest {

	private static String DTO_CACHE_CONTEXT = "routines-communicator-test-dtos";

	private TaskDTO task;
	private RoutinesCommunicator communicator;
	private Thread t;
	private DataReader ctrlReader;
	private DataWriter ctrlWriter;
	private DataReader outReader;
	private DataWriter outWriter;
	private MemoryPipe ctrlPipe;
	private MemoryPipe outPipe;
	private boolean alreadyDown;
	private TSerializer serializer;

	@Before
	public void setUp() throws Exception {
		serializer = new TSerializer();

		// Setup streams and IO
		ctrlPipe = new MemoryPipe(new ThreadSafePipedIOStream());
		ctrlReader = new DataReader(ctrlPipe.getInputStream());
		ctrlWriter = new DataWriter(ctrlPipe.getOutputStream());

		outPipe = new MemoryPipe(new ThreadSafePipedIOStream());
		outReader = new DataReader(outPipe.getInputStream());
		outWriter = new DataWriter(outPipe.getOutputStream());

		// Setup task and start communicator
		HashMap<String, ResourceDTO> inParameters = new HashMap<>();
		task = new TaskDTO(
				"id",
				"jobId",
				"programId",
				ExecutionState.RUN,
				0L,
				0L,
				0L,
				"objectiveRoutineId",
				"mapRoutineId",
				inParameters,
				null,
				null,
				0L
		);
		communicator = new RoutinesCommunicator(
				task.getInParameters(),
				false,
				outPipe,
				Collections.singletonList(ctrlPipe),
				DEFLoggerFactory.createTaskContext("tId")
		);
		t = new Thread(communicator);
		t.start();
		await().atMost(10, SECONDS).until(communicator::isReady);
	}

	@Test
	public void receiveParametersFromQueue_queueEmptyAtBeginning() throws Exception {
		String jId = UUID.randomUUID().toString();
		ResourceQueue queue = new ResourceQueue(jId, DTO_CACHE_CONTEXT);
		queue.release();

		queue.registerObserver(communicator);

		Random rnd = new Random();
		DEFDouble value1 = new DEFDouble(rnd.nextDouble());
		ResourceDTO r1 = prepareResource(UUID.randomUUID().toString(), value1.get_id(), value1);
		DEFInteger value2 = new DEFInteger(rnd.nextInt());
		ResourceDTO r2 = prepareResource(UUID.randomUUID().toString(), value2.get_id(), value2);
		queue.queue(r1);
		queue.queue(r2);

		Order order = new Order(Command.GET_PARAMETER, "0");
		ctrlWriter.store(order);
		DEFDouble receivedValue1 = new DEFDouble();
		outReader.read(receivedValue1);
		assertEquals(value1, receivedValue1);

		order = new Order(Command.GET_PARAMETER, "1");
		ctrlWriter.store(order);
		DEFInteger receivedValue2 = new DEFInteger();
		outReader.read(receivedValue2);
		assertEquals(value2, receivedValue2);
	}

	@Test
	public void receiveParametersFromQueue_queueNotEmptyAtBeginning() throws Exception {
		String jId = UUID.randomUUID().toString();
		ResourceQueue queue = new ResourceQueue(jId, DTO_CACHE_CONTEXT);
		queue.release();

		Random rnd = new Random();
		DEFDouble value1 = new DEFDouble(rnd.nextDouble());
		ResourceDTO r1 = prepareResource(UUID.randomUUID().toString(), value1.get_id(), value1);
		DEFInteger value2 = new DEFInteger(rnd.nextInt());
		ResourceDTO r2 = prepareResource(UUID.randomUUID().toString(), value2.get_id(), value2);
		queue.queue(r1);
		queue.queue(r2);

		queue.registerObserver(communicator);

		Order order = new Order(Command.GET_PARAMETER, "0");
		ctrlWriter.store(order);
		DEFDouble receivedValue1 = new DEFDouble();
		outReader.read(receivedValue1);
		assertEquals(value1, receivedValue1);

		order = new Order(Command.GET_PARAMETER, "1");
		ctrlWriter.store(order);
		DEFInteger receivedValue2 = new DEFInteger();
		outReader.read(receivedValue2);
		assertEquals(value2, receivedValue2);
	}

	@Test
	public void receiveParameters() throws Exception {
		Random rnd = new Random();

		DEFDouble value0 = new DEFDouble(rnd.nextDouble());
		DEFInteger value1 = new DEFInteger(rnd.nextInt());
		addParameter("value0", null, value0.get_id(), value0);
		addParameter("value1", null, value1.get_id(), value1);

		Order order = new Order(Command.GET_PARAMETER, "value1");
		ctrlWriter.store(order);
		DEFInteger recvValue1 = new DEFInteger();
		outReader.read(recvValue1);
		assertEquals(value1, recvValue1);

		order = new Order(Command.GET_PARAMETER, "value0");
		ctrlWriter.store(order);
		DEFDouble recvValue0 = new DEFDouble();
		outReader.read(recvValue0);
		assertEquals(value0, recvValue0);
	}

	@Test
	public void receiveParametersWithDelay() throws Exception {
		Random rnd = new Random();

		DEFDouble value0 = new DEFDouble(rnd.nextDouble());
		DEFInteger value1 = new DEFInteger(rnd.nextInt());
		DEFDouble value2 = new DEFDouble(rnd.nextDouble());
		addParameter("value0", null, value0.get_id(), value0);
		addParameter("value1", null, value1.get_id(), value1);
		addParameter("value2", null, value2.get_id(), value2);

		Thread.sleep((long)(Math.random() * 10 * 1000));

		Order order = new Order(Command.GET_PARAMETER, "value1");
		ctrlWriter.store(order);
		DEFInteger recvValue1 = new DEFInteger();
		outReader.read(recvValue1);
		assertEquals(value1, recvValue1);

		Thread.sleep((long)(Math.random() * 10 * 1000));

		order = new Order(Command.GET_PARAMETER, "value0");
		ctrlWriter.store(order);
		DEFDouble recvValue0 = new DEFDouble();
		outReader.read(recvValue0);
		assertEquals(value0, recvValue0);

		Thread.sleep((long)(Math.random() * 10 * 1000));

		order = new Order(Command.GET_PARAMETER, "value2");
		ctrlWriter.store(order);
		DEFDouble recvValue2 = new DEFDouble();
		outReader.read(recvValue2);
		assertEquals(value2, recvValue2);
	}

	@Test(timeout = 60*1000L)
	public void receiveParametersWithWait() throws TException {
		communicator.setWaitForParameter(true);

		final DEFDouble waitValue = new DEFDouble(new Random().nextDouble());
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					addParameter("waitValue", null, waitValue.get_id(), waitValue);
				} catch (TException e) {
					e.printStackTrace();
					fail();
				}
			}
		}, 10 * 1000); // 10s

		Order order = new Order(Command.GET_PARAMETER, "waitValue");
		ctrlWriter.store(order);
		DEFDouble recvValue = new DEFDouble();
		outReader.read(recvValue);
		assertEquals(waitValue, recvValue);
	}

	@Test
	public void receiveSharedResourceParameter() throws TException {
		DEFInteger value = new DEFInteger(255);
		String rId = UUID.randomUUID().toString();
		ResourceDTO sharedResource = new ResourceDTO(rId, value.get_id());
		sharedResource.setData(serializer.serialize(value));
		DTOCache<ResourceDTO> sharedResourceCache = DTOCache.getInstance(NodeServiceController.DTO_RESOURCE_CACHE_CONTEXT, ResourceDTO.class);
		sharedResourceCache.cache(rId, sharedResource);

		addParameter("sharedResourceValue", rId, null, null);

		Order order = new Order(Command.GET_PARAMETER, "sharedResourceValue");
		ctrlWriter.store(order);
		DEFInteger recvValue = new DEFInteger();
		outReader.read(recvValue);
		assertEquals(value, recvValue);
	}

	@Test
	public void sendResultInfos() throws Exception {
		Random rnd = new Random();
		List<Result> results = new LinkedList<>();
		int nrOfResults = rnd.nextInt(10) + 1;

		for (int i = 0; i < nrOfResults; i++) {
			results.add(new Result(i, UUID.randomUUID().toString(), UUID.randomUUID().toString(), null));
		}

		// Send Order
		Order order = new Order(Command.SEND_RESULT, Integer.toString(nrOfResults));
		ctrlWriter.store(order);

		// Send ResultInfos
		results.forEach((IThrowingConsumer<Result>) ri -> ctrlWriter.store(ri));

		// Wait for receive and check
		await().atMost(30, SECONDS).pollDelay(1, SECONDS).until(communicator::hasResultInfos);

		assertEquals(nrOfResults, communicator.getResults().size());
		assertEquals(results, communicator.getResults());
	}

	@Test
	public void unknownCommand() throws Exception {
		Order order = new Order(Command.EXEC_ROUTINE, "5");
		ctrlWriter.store(order);
	}

	@Test(timeout = 60000)
	public void sendWrongData() throws Exception {
		int j = 20;
		Random rnd = new Random();
		for (int i = 0; i < j; i++) {
			// Correct order
			ctrlWriter.store(new Order(Command.LOG_DEBUG, String.format("%d/%d", i, j)));
			i++;
			// Wrong data (order)
			for (int k = 0; k < j; k++) {
				byte[] buf = new byte[1];
				rnd.nextBytes(buf);
				if (buf[0] == 0) {
					buf[0]++;
				}
				ctrlWriter.store(buf);
			}
			// Correct order
			ctrlWriter.store(new Order(Command.LOG_DEBUG, String.format("%d/%d", i, j)));
		}
	}

	@Test
	public void testShutdown() throws Exception {
		await().atMost(5, SECONDS).until(communicator::isReady);
		communicator.shutdown();
		await().atMost(5, SECONDS).until(communicator::isDown);
		alreadyDown = true;
	}

	private ResourceDTO prepareResource(String rId, String dataTypeId, TBase value) throws TException {
		ResourceDTO resource = new ResourceDTO(rId, dataTypeId);
		if (value != null) {
			resource.setData(serializer.serialize(value));
		}
		return resource;
	}

	private void addParameter(String key, String rId, String dataTypeId, TBase value) throws TException {
		ResourceDTO resource = prepareResource(rId, dataTypeId, value);
		communicator.addParameter(key, resource);
	}

	private void shutdown() throws Exception{
		if (!alreadyDown) {
			Order done = new Order(Command.ROUTINE_DONE, "");
			ctrlWriter.store(done);
		}
	}

	@After
	public void tearDown() throws Exception {
		shutdown();

		t.join();
		ctrlReader.close();
		ctrlWriter.close();
		outReader.close();
		outWriter.close();
	}
}
