package at.enfilo.def.routine.util;


import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class JavaPipeReaderWriterTest extends DataReaderWriterTest {
	private PipedInputStream pis;
	private PipedOutputStream pos;

	@Override
	protected DataReader getDataReader() {
		return new DataReader(pis);
	}

	@Override
	protected DataWriter getDataWriter() {
		return new DataWriter(pos);
	}

	@Override
	protected void init() throws Exception {
		pis = new PipedInputStream();
		pos = new PipedOutputStream();
		pis.connect(pos);
	}

	@Override
	protected void shutdown() throws Exception {
		pis.close();
		pos.close();
	}
}
