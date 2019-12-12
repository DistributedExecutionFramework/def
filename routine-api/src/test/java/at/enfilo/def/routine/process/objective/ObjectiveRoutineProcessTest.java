package at.enfilo.def.routine.process.objective;

import at.enfilo.def.routine.ObjectiveTestRoutine;
import at.enfilo.def.routine.factory.NamedPipeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class ObjectiveRoutineProcessTest {
	private static final String OS = System.getProperty("os.name").toLowerCase();

	public static final String IN_PIPE = "ObjectiveRoutineTest_in";
	public static final String OUT_PIPE = "ObjectiveRoutineTest_out";
	public static final String CTRL_PIPE = "ObjectiveRoutineTest_ctrl";

	public static final String FIRST_NAME = "FirstName1234";
	public static final String LAST_NAME = "LastName9876";
	public static final Short AGE = 33;

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
	public void testObjectiveRoutine() throws IOException, InterruptedException {

		if (OS.contains("windows")) {
			System.out.println(this.getClass().getName() + " Test is disabled under windows. Named Pipes communication not working yet.");
			return;
		}

		ProcessBuilder workerServicePb = new ProcessBuilder(
				"java",
				"-cp",
				"build/classes/java/test:build/libs/*",
				WorkerServiceProcess.class.getCanonicalName(),
				inPipe,
				ctrlPipe
		);
		Process workerServiceProcess = workerServicePb.start();

		ProcessBuilder receiverRoutinePb = new ProcessBuilder(
				"java",
				"-cp",
				"build/classes/java/test:build/libs/*",
				ReceiverRoutineProcess.class.getCanonicalName(),
				outPipe
		);
		Process receiverRoutineProcess = receiverRoutinePb.start();

		ProcessBuilder routinePb = new ProcessBuilder(
				"java",
				"-cp",
				"build/classes/java/test:build/libs/*",
				ObjectiveTestRoutine.class.getCanonicalName(),
				ObjectiveTestRoutine.class.getCanonicalName(),
				inPipe,
				outPipe,
				ctrlPipe
		);
		Process routineProcess = routinePb.start();

		if (!routineProcess.waitFor(30, TimeUnit.SECONDS)) {
			fail();
		}
		int exitCode = routineProcess.exitValue();
		printProcessOutputs(routineProcess);
		assertEquals(0, exitCode);

		if (!workerServiceProcess.waitFor(30, TimeUnit.SECONDS)) {
			fail();
		}
		exitCode = workerServiceProcess.exitValue();
		printProcessOutputs(workerServiceProcess);
		assertEquals(0, exitCode);

		if (!receiverRoutineProcess.waitFor(30, TimeUnit.SECONDS)) {
			fail();
		}
		exitCode = receiverRoutineProcess.exitValue();
		printProcessOutputs(receiverRoutineProcess);
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
