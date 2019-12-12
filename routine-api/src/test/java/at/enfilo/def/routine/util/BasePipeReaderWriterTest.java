package at.enfilo.def.routine.util;

import at.enfilo.def.routine.factory.NamedPipeFactory;

import java.io.File;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class BasePipeReaderWriterTest extends DataReaderWriterTest {
	private File pipe;
	private BlockingIO<DataReader> reader;
	private BlockingIO<DataWriter> writer;

	@Override
	protected void init() throws Exception {
		pipe = new File(UUID.randomUUID().toString());
		NamedPipeFactory.createPipe(pipe);

		reader = new BlockingIO<>(pipe, DataReader.class);
		reader.start();
		writer = new BlockingIO<>(pipe, DataWriter.class);
		writer.start();

		await().atMost(10, SECONDS).until(reader::isRunning);
		await().atMost(10, SECONDS).until(writer::isRunning);
	}

	@Override
	protected DataReader getDataReader() {
		return reader.getIO();
	}

	@Override
	protected DataWriter getDataWriter() {
		return writer.getIO();
	}

	@Override
	protected void shutdown() throws Exception {
		writer.shutdown();
		reader.shutdown();
		NamedPipeFactory.deletePipe(pipe);
	}
}
