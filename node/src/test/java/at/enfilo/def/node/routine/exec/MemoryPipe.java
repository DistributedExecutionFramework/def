package at.enfilo.def.node.routine.exec;


import at.enfilo.def.routine.util.Pipe;
import at.enfilo.def.routine.util.ThreadSafePipedIOStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

class MemoryPipe extends Pipe {

	private final static String NAME = "MemoryPipe";

	private final ThreadSafePipedIOStream threadSafePipedIOStream;

	/**
	 * Create pipe with a name.
	 */
	public MemoryPipe(ThreadSafePipedIOStream threadSafePipedIOStream) {
		super(new File(NAME));
		this.threadSafePipedIOStream = threadSafePipedIOStream;
	}

	@Override
	public InputStream getInputStream() throws FileNotFoundException {
		return threadSafePipedIOStream.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws FileNotFoundException {
		return threadSafePipedIOStream.getOutputStream();
	}
}
