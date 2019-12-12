package at.enfilo.def.routine;

import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.mock.ReduceRoutineMock;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import at.enfilo.def.routine.util.ThreadSafePipedIOStream;
import org.apache.thrift.TException;
import org.junit.Test;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class ReduceRoutineTest {

	@Test
	public void routineWorkFlow() throws Exception {
		// Setup streams and IO
		ThreadSafePipedIOStream inPipe = new ThreadSafePipedIOStream();
		DataReader inReader = new DataReader(inPipe.getInputStream());
		DataWriter inWriter = new DataWriter(inPipe.getOutputStream());

		ThreadSafePipedIOStream outPipe = new ThreadSafePipedIOStream();
		DataReader outReader = new DataReader(outPipe.getInputStream());
		DataWriter outWriter = new DataWriter(outPipe.getOutputStream());

		ThreadSafePipedIOStream ctrlPipe = new ThreadSafePipedIOStream();
		DataReader ctrlReader = new DataReader(ctrlPipe.getInputStream());
		DataWriter ctrlWriter = new DataWriter(ctrlPipe.getOutputStream());

		ReduceTestRoutine routine = new ReduceTestRoutine();
		routine.in = inReader;
		routine.ctrl = ctrlWriter;
		routine.out = outWriter;
		Thread t = new Thread(routine);
		t.start();
		await().atMost(10, SECONDS).until(t::isAlive);

		handleLog(ctrlReader);
		handleLog(ctrlReader);

		int expectedResult = 0;

		// Receiver values for reduce
		int i;
		for (i = 0; i < 10; i++) {
			// key
			handleLog(ctrlReader);
			Order o = ctrlReader.read(new Order());
			assertEquals(Command.GET_PARAMETER_KEY, o.getCommand());
			assertEquals(String.format("%d", i), o.getValue());
			inWriter.store("DEFAULT");
			handleLog(ctrlReader);

			// value
			handleLog(ctrlReader);
			o = ctrlReader.read(new Order());
			assertEquals(Command.GET_PARAMETER, o.getCommand());
			assertEquals(String.format("%d", i), o.getValue());
			inWriter.store(new DEFInteger(i));
			handleLog(ctrlReader);

			expectedResult += i;
		}

		handleLog(ctrlReader);
		Order o = ctrlReader.read(new Order());
		assertEquals(Command.GET_PARAMETER_KEY, o.getCommand());
		assertEquals(String.format("%d", i), o.getValue());
		inWriter.store("REDUCE");
		handleLog(ctrlReader);

		// Receive result
		int tuples = outReader.readInt();
		assertEquals(1, tuples);
		String reducedKey = outReader.readString();
		assertEquals("DEFAULT", reducedKey);
		int size = outReader.readInt();
		assertTrue(size > 0);
		DEFInteger reducedResult = outReader.read(new DEFInteger());
		assertEquals(expectedResult, reducedResult.getValue());

		handleLog(ctrlReader);
		handleLog(ctrlReader);
		handleLog(ctrlReader);
		handleLog(ctrlReader);
		o = ctrlReader.read(new Order());
		assertEquals(Command.ROUTINE_DONE, o.getCommand());

		// wait for thread
		t.join();

		// Close all streams
		inReader.close();
		inWriter.close();
		outReader.close();
		outWriter.close();
		ctrlReader.close();
		ctrlWriter.close();
	}

	private void handleLog(DataReader ctrlReader) throws TException {
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
		String outPipe = UUID.randomUUID().toString();
		String ctrlPipe = UUID.randomUUID().toString();
		ReduceRoutine.main(new String[]{ReduceRoutineMock.class.getCanonicalName(), inPipe, outPipe, ctrlPipe});
		assertNotNull(ReduceRoutineMock.getLastInstance());
		assertTrue(ReduceRoutineMock.getLastInstance().isRun());
	}

	@Test(expected = RoutineException.class)
	public void mainWrongArgs() throws Exception {
		ReduceRoutineMock.main(new String[]{"", ""});
	}
}
