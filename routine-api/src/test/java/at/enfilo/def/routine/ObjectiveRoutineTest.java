package at.enfilo.def.routine;

import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.datatype.DEFString;
import at.enfilo.def.routine.mock.ObjectiveRoutineMock;
import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import at.enfilo.def.routine.util.ThreadSafePipedIOStream;
import org.apache.thrift.TException;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class ObjectiveRoutineTest {

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

		ObjectiveTestRoutine routine = new ObjectiveTestRoutine();
		routine.in = inReader;
		routine.ctrl = ctrlWriter;
		routine.out = outWriter;
		Thread t = new Thread(routine);
		t.start();
		await().atMost(10, SECONDS).until(t::isAlive);

		// Receiver 3 orders: request input parameters
		// 1. param: firstname
		handleLog(ctrlReader);
		handleLog(ctrlReader);
		handleLog(ctrlReader);
		Order o = ctrlReader.read(new Order());
		assertEquals(Command.GET_PARAMETER, o.getCommand());
		assertEquals("firstName", o.getValue());
		DEFString firstName = new DEFString(UUID.randomUUID().toString());
		inWriter.store(firstName);

		// 2. param: lastname
		handleLog(ctrlReader);
		handleLog(ctrlReader);
		o = ctrlReader.read(new Order());
		assertEquals(Command.GET_PARAMETER, o.getCommand());
		assertEquals("lastName", o.getValue());
		DEFString lastName = new DEFString(UUID.randomUUID().toString());
		inWriter.store(lastName);

		// 3. param: age
		handleLog(ctrlReader);
		handleLog(ctrlReader);
		o = ctrlReader.read(new Order());
		assertEquals(Command.GET_PARAMETER, o.getCommand());
		assertEquals("age", o.getValue());
		Random rnd = new Random();
		DEFInteger age = new DEFInteger(rnd.nextInt(100));
		inWriter.store(age);

		// Receive result
		int size = outReader.readInt();
		assertTrue(size > 0);
		Person assertedResult = new Person();
		assertedResult.setAge((short)age.getValue());
		assertedResult.setFirstName(firstName.getValue());
		assertedResult.setLastName(lastName.getValue());
		Person receivedResult = outReader.read(new Person());
		assertEquals(assertedResult, receivedResult);

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
		String outPipe = UUID.randomUUID().toString();
		String ctrlPipe = UUID.randomUUID().toString();
		ObjectiveRoutine.main(new String[]{ObjectiveRoutineMock.class.getCanonicalName(), inPipe, outPipe, ctrlPipe});
		assertNotNull(ObjectiveRoutineMock.getLastInstance());
		assertTrue(ObjectiveRoutineMock.getLastInstance().isRun());
	}

	@Test(expected = RoutineException.class)
	public void mainWrongArgs() throws Exception {
		ObjectiveRoutine.main(new String[]{"", ""});
	}
}
