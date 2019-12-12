package at.enfilo.def.routine;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.routine.mock.StoreRoutineMock;
import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import at.enfilo.def.routine.util.ThreadSafePipedIOStream;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.junit.Test;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class StoreRoutineTest {
	static final String KEY_PREFIX = "KEY_";
	static final String VALUE_PREFIX = "VALUE_";

	@Test
	public void routineWorkFlow() throws Exception {
		// Setup streams
		ThreadSafePipedIOStream inPipe = new ThreadSafePipedIOStream();
		DataReader inReader = new DataReader(inPipe.getInputStream());
		DataWriter inWriter = new DataWriter(inPipe.getOutputStream());

		ThreadSafePipedIOStream ctrlPipe = new ThreadSafePipedIOStream();
		DataReader ctrlReader = new DataReader(ctrlPipe.getInputStream());
		DataWriter ctrlWriter = new DataWriter(ctrlPipe.getOutputStream());

		// write nr of tuples
		Random rnd = new Random();
		int tuples = rnd.nextInt(12) + 1;

		// Start routine
		StoreTestRoutine routine = new StoreTestRoutine();
		routine.in = inReader;
		routine.ctrl = ctrlWriter;
		Thread t = new Thread(routine);
		t.start();
		await().atMost(10, SECONDS).until(t::isAlive);

		List<String> keys = new LinkedList<>();
		List<TBase> values = new LinkedList<>();
		List<TBase> emptyInstances = new LinkedList<>();

		// Initial logs
		handleLog(ctrlReader);
		handleLog(ctrlReader);

		// Write nr of tuples
		handleLog(ctrlReader);
		inWriter.store(tuples);
		handleLog(ctrlReader);

		// Generate tuples
		for (int i = 0; i < tuples; i++) {
			// Store key
			String key = UUID.randomUUID().toString();
			inWriter.store(key);
			keys.add(key);

			// Store value
			TBase value;
			if (rnd.nextBoolean()) {
				value = new DEFDouble(rnd.nextDouble());
				emptyInstances.add(new DEFDouble());
			} else {
				value = new Person(UUID.randomUUID().toString(), UUID.randomUUID().toString(), (short)rnd.nextInt());
				emptyInstances.add(new Person());
			}
			values.add(value);
			inWriter.store(ObjectSize.getSize(value));
			inWriter.store(value);

			handleLog(ctrlReader);
		}

		// Receive ResultInfos
		handleLog(ctrlReader);
		Order o = new Order();
		ctrlReader.read(o);
		assertEquals(Command.SEND_RESULT, o.getCommand());
		assertEquals(tuples, Integer.parseInt(o.getValue()));
		for (int i = 0; i < tuples; i++) {
			Result result = new Result();
			ctrlReader.read(result);
			assertEquals(i, result.getSeq());
			assertEquals(keys.get(i), result.getKey());
		}

		// Close streams
		inReader.close();
		inWriter.close();
		ctrlReader.close();
		ctrlWriter.close();

		// Verify key files and keys & value files and values
		for (int i = 0; i < tuples; i++) {
			// Keys
			File keyFile = new File(KEY_PREFIX + i);
			assertTrue(keyFile.exists());
			try (BufferedReader reader = new BufferedReader(new FileReader(keyFile))) {
				String key = reader.readLine();
				assertEquals(keys.get(i), key);
			}
			keyFile.deleteOnExit();

			// Values
			File valueFile = new File(VALUE_PREFIX + i);
			assertTrue(valueFile.exists());
			try (InputStream in = new FileInputStream(valueFile)) {
				TProtocol inProto = new TBinaryProtocol(new TIOStreamTransport(in));
				TBase instance = emptyInstances.get(i);
				instance.read(inProto);
				assertEquals(values.get(i), instance);
			}
			valueFile.deleteOnExit();
		}
	}

	private void handleLog(DataReader ctrlReader) throws IOException, TException {
		Order o = ctrlReader.read(new Order());
		switch (o.getCommand()) {
			case LOG_DEBUG:
				System.out.println("DEBUG " + o.getValue());
				break;
			case LOG_INFO:
				System.out.println("INFO " + o.getValue());
				break;
			case LOG_ERROR:
				System.out.println("ERROR " + o.getValue());
				break;
			default:
				fail();
		}
	}

	@Test
	public void mainSuccess() throws Exception {
		String inPipe = UUID.randomUUID().toString();
		String ctrlPipe = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		String configFile = UUID.randomUUID().toString();

		StoreRoutine.main(new String[]{StoreRoutineMock.class.getCanonicalName(), inPipe, ctrlPipe, configFile});
		assertNotNull(StoreRoutineMock.getLastInstance());
		assertTrue(pId, StoreRoutineMock.getLastInstance().isSetupStorage());
		assertEquals(configFile, StoreRoutineMock.getLastInstance().getConfigFile());
		assertTrue(StoreRoutineMock.getLastInstance().isRun());
		assertTrue(pId, StoreRoutineMock.getLastInstance().isShutdownStorage());
	}

	@Test(expected = RoutineException.class)
	public void mainWrongArgs() throws Exception {
		StoreRoutine.main(new String[]{"", ""});
	}

}
