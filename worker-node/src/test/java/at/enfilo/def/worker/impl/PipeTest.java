package at.enfilo.def.worker.impl;

import at.enfilo.def.routine.api.IPipeReader;
import at.enfilo.def.routine.api.IPipeWriter;
import at.enfilo.def.routine.util.Pipe;
import at.enfilo.def.worker.server.Worker;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;

public class PipeTest {

	private Pipe pipe;
	private String name;

	@Before
	public void setUp() throws Exception {
		name = UUID.randomUUID().toString();
		pipe = new Pipe(Worker.getInstance().getConfiguration().getWorkingDir(), name);
	}

	@Test
	public void fullyConnected() throws Exception {
		assertFalse(pipe.isFullyConnected());

		pipe.setWriter(new IPipeWriter() {});
		assertFalse(pipe.isFullyConnected());

		pipe.setReader(new IPipeReader() {});
		assertTrue(pipe.isFullyConnected());
	}

	@Test
	public void name() throws Exception {
		assertEquals(this.name, pipe.getName());
	}

	@Test
	public void getAndSetConnectors() throws Exception {
		IPipeWriter inConnector = new IPipeWriter() {};
		IPipeReader outConnector = new IPipeReader() {};

		pipe.setWriter(inConnector);
		assertSame(inConnector, pipe.getWriter());

		pipe.setReader(outConnector);
		assertSame(outConnector, pipe.getReader());
	}

	@Test
	public void pipePath() throws Exception {
		Path expected = Paths.get(Worker.getInstance().getConfiguration().getWorkingDir(), pipe.getName()).toAbsolutePath();
		File pipePath = pipe.resolve();
		assertEquals(expected.toFile(), pipePath);
	}
}
