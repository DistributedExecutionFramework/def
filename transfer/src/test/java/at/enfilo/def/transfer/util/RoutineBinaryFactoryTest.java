package at.enfilo.def.transfer.util;

import at.enfilo.def.transfer.dto.RoutineBinaryChunkDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import org.junit.Test;

import java.io.File;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

public class RoutineBinaryFactoryTest {
	private static final String FILE1_NAME = "file1";
	private static final String FILE2_NAME = "file2";
	private static final File FILE1 = new File(RoutineBinaryFactoryTest.class.getClassLoader().getResource(FILE1_NAME).getFile());
	private static final File FILE2 = new File(RoutineBinaryFactoryTest.class.getClassLoader().getResource(FILE2_NAME).getFile());
	private static final String FILE1_MD5 = "00249ff70cf546ef00dabddfb914ba7a";
	private static final String FILE2_MD5 = "2b621ccc7e555d45b4e5f6c5308bb5a7";
	private static final int FILE1_SIZE = 5120;
	private static final int FILE2_SIZE = 51200;

	@Test
	public void md5() throws Exception {
		assertEquals(FILE1_MD5, RoutineBinaryFactory.md5(FILE1));
		assertEquals(FILE2_MD5, RoutineBinaryFactory.md5(FILE2));
	}

	@Test
	public void toRoutineBinary() throws Exception {
		String rb2Id = UUID.randomUUID().toString();
		RoutineBinaryDTO rb1 = RoutineBinaryFactory.createFromFile(FILE1, true, FILE1_NAME);
		RoutineBinaryDTO rb2 = RoutineBinaryFactory.createFromFile(FILE2, false, FILE2_NAME, rb2Id);

		assertEquals(FILE1_MD5, rb1.getMd5());
		assertEquals(FILE1_SIZE, rb1.getSizeInBytes());
		assertEquals(FILE1.toURI().toURL().toString(), rb1.getUrl());
		assertTrue(rb1.isPrimary());

		assertEquals(rb2Id, rb2.getId());
		assertFalse(rb2.isPrimary());
		assertEquals(FILE2_MD5, rb2.getMd5());
		assertEquals(FILE2_SIZE, rb2.getSizeInBytes());
		assertEquals(FILE2.toURI().toURL().toString(), rb2.getUrl());
	}

	@Test
	public void calculateChunks() {
		int chunkSize = 1024;

		assertEquals(FILE1_SIZE / chunkSize, RoutineBinaryFactory.calculateChunks(FILE1, chunkSize));
		assertEquals(FILE2_SIZE / chunkSize, RoutineBinaryFactory.calculateChunks(FILE2, chunkSize));

		chunkSize = FILE1_SIZE;
		assertEquals(1, RoutineBinaryFactory.calculateChunks(FILE1, chunkSize));
		chunkSize++;
		assertEquals(1, RoutineBinaryFactory.calculateChunks(FILE1, chunkSize));
		chunkSize--;
		chunkSize--;
		assertEquals(2, RoutineBinaryFactory.calculateChunks(FILE1, chunkSize));
	}

	@Test
	public void readAndStoreChunks() throws Exception {
		Random rnd = new Random();
		File newBinary = new File(UUID.randomUUID().toString());
		newBinary.deleteOnExit();

		int chunkSize = rnd.nextInt(1024);
		chunkSize = chunkSize == 0 ? 16 : chunkSize;

		short nrOfChunks = RoutineBinaryFactory.calculateChunks(FILE1, chunkSize);
		for (short i = 0; i < nrOfChunks; i++) {
			RoutineBinaryChunkDTO chunk = RoutineBinaryFactory.readChunk(FILE1, i, chunkSize);
			RoutineBinaryFactory.storeChunk(newBinary, chunk);
		}

		assertEquals(FILE1_MD5, RoutineBinaryFactory.md5(newBinary));
	}
}
