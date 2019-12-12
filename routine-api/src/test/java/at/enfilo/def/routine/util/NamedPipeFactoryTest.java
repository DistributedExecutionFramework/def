package at.enfilo.def.routine.util;


import at.enfilo.def.routine.exception.PipeCreationException;
import at.enfilo.def.routine.factory.NamedPipeFactory;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NamedPipeFactoryTest {

	@Test
	public void createAndDeleteByString() throws Exception {
		String pipeName = UUID.randomUUID().toString();
		String fullName = NamedPipeFactory.createPipe(pipeName);
		File pipe = new File(fullName);
		assertTrue(pipe.exists());
		assertTrue(pipe.canRead());
		assertTrue(pipe.canWrite());

		NamedPipeFactory.deletePipe(pipeName);
		assertFalse(pipe.exists());
	}

	@Test
	public void createAndDeleteByFile() throws Exception {
		File pipe = new File(UUID.randomUUID().toString());
		NamedPipeFactory.createPipe(pipe);

		assertTrue(pipe.exists());
		assertTrue(pipe.canRead());
		assertTrue(pipe.canWrite());

		NamedPipeFactory.deletePipe(pipe);
		assertFalse(pipe.exists());
	}

	@Test
	public void multipleCreate() throws Exception {
		File pipe = new File(UUID.randomUUID().toString());
		assertFalse(pipe.exists());

		NamedPipeFactory.createPipe(pipe);
		NamedPipeFactory.createPipe(pipe);
		NamedPipeFactory.createPipe(pipe);
		NamedPipeFactory.createPipe(pipe);

		assertTrue(pipe.exists());
		assertTrue(pipe.canRead());
		assertTrue(pipe.canWrite());

		NamedPipeFactory.deletePipe(pipe);
		assertFalse(pipe.exists());
	}


	@Test(expected = PipeCreationException.class)
	public void cannotCreatePipe() throws Exception {
		File pipe = new File("/dev/" + UUID.randomUUID().toString());
		assertFalse(pipe.exists());
		NamedPipeFactory.createPipe(pipe);
	}
}
