package at.enfilo.def.routine;

import at.enfilo.def.routine.api.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

public class StoreTestRoutine extends StoreRoutine {
	private boolean storing = false;

	@Override
	protected Result store(String key, byte[] data, int tupleSeq) throws IOException {
		// Ignoring partition

		// Store key
		String keyFileName = StoreRoutineTest.KEY_PREFIX + tupleSeq;
		File keyFile = Paths.get(keyFileName).toFile();
		try (FileOutputStream out = new FileOutputStream(keyFile)) {
			out.write(key.getBytes());
			out.flush();
		}

		// Store value
		String valueFileName = StoreRoutineTest.VALUE_PREFIX + tupleSeq;
		File valueFile = Paths.get(valueFileName).toFile();
		try (FileOutputStream out = new FileOutputStream(valueFile)) {
			out.write(data);
			out.flush();
		}

		return new Result(tupleSeq, key, valueFile.getAbsolutePath(), ByteBuffer.wrap(data));
	}

	@Override
	protected void configure(String configFile) {

	}

	@Override
	protected void shutdownStorage() throws IOException {
		storing = false;
	}

	@Override
	protected void setupStorage() throws IOException {
		storing = true;
	}

}
