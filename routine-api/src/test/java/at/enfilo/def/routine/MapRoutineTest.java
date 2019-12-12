package at.enfilo.def.routine;

import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.routine.mock.MapRoutineMock;
import at.enfilo.def.routine.mock.RoutinesCommunicatorMock;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import at.enfilo.def.routine.util.ThreadSafePipedIOStream;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class MapRoutineTest {
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

		RoutinesCommunicatorMock routinesCommunicatorMock = new RoutinesCommunicatorMock(ctrlReader);

		// Start MapTestRoutine
		MapTestRoutine routine = new MapTestRoutine();
		routine.in = inReader;
		routine.out = outWriter;
		routine.ctrl = ctrlWriter;
		Thread t = new Thread(routine);
		t.start();
		await().atMost(10, SECONDS).until(t::isAlive);

		routinesCommunicatorMock.start();

		Random rnd = new Random();
		TSerializer serializer = new TSerializer();
		TDeserializer deserializer = new TDeserializer();


		// Send a person object as a result of ObjectiveRoutine
		Person person = new Person();
		person.setLastName(UUID.randomUUID().toString());
		person.setFirstName(UUID.randomUUID().toString());
		person.setAge((short)rnd.nextInt(100));
		byte[] sendBuf = serializer.serialize(person);
		inWriter.store(sendBuf.length);
		inWriter.store(person);

		// Read number of tuples
		int numberOfTuples = outReader.readInt();
		assertEquals(1, numberOfTuples);

		// Read key
		String key = outReader.readString();
		assertEquals(MapTestRoutine.DEFAULT_KEY, key);

		// Read size of value
		int size = outReader.readInt();

		// Read value
		byte[] recvBuf = outReader.readBytes(size);
		DEFInteger value = new DEFInteger();
		deserializer.deserialize(value, recvBuf);
		assertEquals(person.getAge(), value.getValue());

		// wait for thread
		t.join();
		routinesCommunicatorMock.join();

		// Close all streams
		inReader.close();
		inWriter.close();
		outReader.close();
		outWriter.close();
		ctrlReader.close();
		ctrlWriter.close();
	}

	@Test
	public void mainSuccess() throws Exception {
		String inPipe = UUID.randomUUID().toString();
		String outPipe = UUID.randomUUID().toString();
		String ctrlPipe = UUID.randomUUID().toString();
		MapRoutine.main(new String[]{MapRoutineMock.class.getCanonicalName(), inPipe, outPipe, ctrlPipe});
		assertNotNull(MapRoutineMock.getLastInstance());
		assertTrue(MapRoutineMock.getLastInstance().isRun());
	}

	@Test(expected = RoutineException.class)
	public void mainWrongArgs() throws Exception {
		MapRoutine.main(new String[]{"", ""});
	}

	@Test(expected = RoutineException.class)
	public void mainFailed() throws Exception {
		MapRoutine.main(new String[]{MapRoutineMock.class.getCanonicalName(), null, null});
	}
}
