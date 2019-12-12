package at.enfilo.def.node.routine.exec;

import at.enfilo.def.routine.util.Pipe;
import at.enfilo.def.transfer.dto.RoutineType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class SequenceProcessWatcherTest {
	private final static int EXIT_SUCCESS = 0;
	private final static int EXIT_ERROR = 1;

	private List<RoutineProcess> processes;
	private Process p1;
	private Process p2;
	private Process p3;

	@Before
	public void setUp() throws Exception {
		processes = new LinkedList<>();
		p1 = Mockito.mock(Process.class);
		p2 = Mockito.mock(Process.class);
		p3 = Mockito.mock(Process.class);
		Pipe p = Mockito.mock(Pipe.class);
		processes.add(new RoutineProcess(p1, new ProcessBuilder(), new SequenceStep("rId", RoutineType.OBJECTIVE, p, p)));
		processes.add(new RoutineProcess(p2, new ProcessBuilder(), new SequenceStep("rId", RoutineType.OBJECTIVE, p, p)));
		processes.add(new RoutineProcess(p3, new ProcessBuilder(), new SequenceStep("rId", RoutineType.OBJECTIVE, p, p)));
	}

	@Test
	public void waitForSuccess() throws Exception {
		when(p1.waitFor()).thenAnswer(invocationOnMock -> {
			Thread.sleep(200);
			return EXIT_SUCCESS;
		});
		when(p2.waitFor()).thenAnswer(invocationOnMock -> {
			Thread.sleep(200);
			return EXIT_SUCCESS;
		});
		when(p3.waitFor()).thenAnswer(invocationOnMock -> {
			Thread.sleep(400);
			return EXIT_SUCCESS;
		});

		SequenceProcessWatcher spw = new SequenceProcessWatcher(processes);

		await()
			.between(200, TimeUnit.MILLISECONDS, 500, TimeUnit.MILLISECONDS)
			.pollDelay(20, TimeUnit.MILLISECONDS)
			.until(() -> {
				boolean success = spw.waitForAllProcesses();
				assertTrue(success);
				return true;
			});
	}

	@Test
	public void waitForFailed() throws Exception {
		CountDownLatch p3Lock = new CountDownLatch(1);

		when(p1.waitFor()).thenAnswer(invocationOnMock -> {
			Thread.sleep(200);
			return EXIT_SUCCESS;
		});
		when(p1.exitValue()).thenReturn(EXIT_SUCCESS);
		when(p2.waitFor()).thenAnswer(invocationOnMock -> {
			Thread.sleep(200);
			return EXIT_ERROR;
		});
		when(p2.exitValue()).thenReturn(EXIT_ERROR);
		when(p3.waitFor()).thenAnswer(invocationOnMock -> {
			p3Lock.await();
			return EXIT_ERROR;
		});
		when(p3.exitValue()).thenReturn(EXIT_ERROR);
		when(p3.isAlive()).thenReturn(true);
		doAnswer((Answer<Void>) invocation -> {
					p3Lock.countDown();
					return null;
				}
		).when(p3).destroy();

		SequenceProcessWatcher spw = new SequenceProcessWatcher(processes);
		await().atMost(400, TimeUnit.MILLISECONDS).ignoreNoExceptions().until(() -> {
			boolean success = spw.waitForAllProcesses();
			assertFalse(success);
			return true;
		});
	}
}
