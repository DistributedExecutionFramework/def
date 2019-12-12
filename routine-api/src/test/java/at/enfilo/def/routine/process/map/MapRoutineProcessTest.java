package at.enfilo.def.routine.process.map;

import at.enfilo.def.routine.MapTestRoutine;
import at.enfilo.def.routine.factory.NamedPipeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class MapRoutineProcessTest {
	private static final String OS = System.getProperty("os.name").toLowerCase();

	public static final String IN_PIPE = "MapRoutineTest_in";
	public static final String OUT_PIPE = "MapRoutineTest_out";
	public static final String CTRL_PIPE = "MapRoutineTest_ctrl";

	public static final String FIRST_NAME = "fn1234";
	public static final String LAST_NAME = "ln9876";
	public static final Short AGE = 66;

	private String inPipe;
	private String outPipe;
	private String ctrlPipe;

	@Before
	public void setUp() throws Exception {
		inPipe = NamedPipeFactory.createPipe(IN_PIPE);
		outPipe = NamedPipeFactory.createPipe(OUT_PIPE);
		ctrlPipe = NamedPipeFactory.createPipe(CTRL_PIPE);
	}

	@Test
	public void testMapRoutine() throws IOException, InterruptedException {
		if (OS.contains("windows")) {
			System.out.println(this.getClass().getName() + " Test is disabled under windows. Named Pipes communication not working yet.");
			return;
		}

		ProcessBuilder senderPb = new ProcessBuilder(
				"java",
				"-cp",
				"build/classes/java/test:build/libs/*",
				SenderRoutineProcess.class.getCanonicalName(),
				inPipe
		);
		Process senderProcess = senderPb.start();

		ProcessBuilder workerPb = new ProcessBuilder(
				"java",
				"-cp",
				"build/classes/java/test:build/libs/*",
				WorkerServiceProcess.class.getCanonicalName(),
				ctrlPipe
		);
		Process workerProcess = workerPb.start();

		ProcessBuilder receiverPb = new ProcessBuilder(
				"java",
				"-cp",
				"build/classes/java/test:build/libs/*",
				ReceiverRoutineProcess.class.getCanonicalName(),
				outPipe
		);
		Process receiverProcess = receiverPb.start();

		ProcessBuilder mapRoutinePb = new ProcessBuilder(
				"java",
				"-cp",
				"build/classes/java/test:build/libs/*",
				MapTestRoutine.class.getCanonicalName(),
				MapTestRoutine.class.getCanonicalName(),
				inPipe,
				outPipe,
				ctrlPipe
		);
		Process mapRoutineProcess = mapRoutinePb.start();

		if (!workerProcess.waitFor(50, TimeUnit.SECONDS)) {
			fail();
		}
		printProcessOutputs(workerProcess);
		int exitCode = workerProcess.exitValue();
		assertEquals(0, exitCode);

		if (!senderProcess.waitFor(30, TimeUnit.SECONDS)) {
			fail();
		}
		exitCode = senderProcess.exitValue();
		printProcessOutputs(senderProcess);
		assertEquals(0, exitCode);

		if (!mapRoutineProcess.waitFor(30, TimeUnit.SECONDS)) {
			fail();
		}
		printProcessOutputs(mapRoutineProcess);
		exitCode = mapRoutineProcess.exitValue();
		assertEquals(0, exitCode);

		if (!receiverProcess.waitFor(30, TimeUnit.SECONDS)) {
			fail();
		}
		printProcessOutputs(receiverProcess);
		exitCode = receiverProcess.exitValue();
		assertEquals(0, exitCode);

	}

	@After
	public void tearDown() throws Exception {
		assertTrue(NamedPipeFactory.deletePipe(inPipe));
		assertTrue(NamedPipeFactory.deletePipe(outPipe));
		assertTrue(NamedPipeFactory.deletePipe(ctrlPipe));
	}

	private void printProcessOutputs(Process p) throws IOException {
		String buffer;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			while ((buffer = reader.readLine()) != null) {
				System.out.println(buffer);
			}
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
			while ((buffer = reader.readLine()) != null) {
				System.err.println(buffer);
			}
		}
	}
}
