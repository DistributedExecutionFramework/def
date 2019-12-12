package at.enfilo.def.routine.util;

import java.io.*;
import java.util.concurrent.CountDownLatch;


/**
 * Thread safe PipedInputStream and PipedOutputStream wrapper.
 *
 */
public class ThreadSafePipedIOStream {
	private class InStream extends InputStream {
		@Override
		public int read() throws IOException {
			try {
				latch.await();
				return pipedInputStream.read();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException(e);
			}
		}
	}

	private class OutStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			pipedOutputStream.write(b);
			latch.countDown();
		}
	}

	private final PipedInputStream pipedInputStream;
	private final PipedOutputStream pipedOutputStream;
	private final CountDownLatch latch;
	private final InputStream inputStream;
	private final OutputStream outputStream;

	public ThreadSafePipedIOStream() throws IOException {
		this.pipedInputStream = new PipedInputStream();
		this.pipedOutputStream = new PipedOutputStream();
		this.pipedOutputStream.connect(this.pipedInputStream);
		this.latch = new CountDownLatch(1);
		this.inputStream = new InStream();
		this.outputStream = new OutStream();
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}
}
