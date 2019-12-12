package at.enfilo.def.demo;

import at.enfilo.def.routine.StoreRoutine;
import at.enfilo.def.routine.api.Result;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MemoryStorer extends StoreRoutine {

	@Override
	protected Result store(String key, byte[] data, int tupleSeq) throws IOException {
		return new Result(tupleSeq, key, null, ByteBuffer.wrap(data));
	}

	@Override
	protected void configure(String configFile) {

	}

	@Override
	protected void shutdownStorage() throws IOException {

	}

	@Override
	protected void setupStorage() throws IOException {

	}
}
