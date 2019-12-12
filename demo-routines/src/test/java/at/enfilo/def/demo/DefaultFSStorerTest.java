package at.enfilo.def.demo;

import at.enfilo.def.routine.api.Result;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

public class DefaultFSStorerTest {

	private FSStorer storer;

	@Before
	public void setUp() throws Exception {
		storer = new FSStorer();
		storer.configure(FSStorer.class.getClassLoader().getResource("fs-storer.properties").getFile());
	}

	@Test
	public void createDest() throws Exception {
		File f = storer.createDestination();
		assertTrue(f.exists());
	}

	@Test
	public void store() throws Exception {
		Random rnd = new Random();
		int seq = rnd.nextInt();
		byte[] data = new byte[8];
		rnd.nextBytes(data);
		String key = UUID.randomUUID().toString();
		Result result = storer.store(key, data, seq);

		assertEquals(key, result.getKey());
		assertEquals(seq, result.getSeq());

		URL url = new URL(result.getUrl());
		File file = Paths.get(url.toURI()).toFile();
		assertTrue(file.exists());
		assertEquals(file.toURI().toURL().toString(), result.getUrl());

		try (FileInputStream fin = new FileInputStream(file)) {
			byte[] storedData = new byte[8];
			fin.read(storedData, 0, 8);
			assertArrayEquals(data, storedData);
		}

		file.delete();
	}

}
