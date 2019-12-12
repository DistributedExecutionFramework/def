package at.enfilo.def.routine.util;

import at.enfilo.def.routine.api.IPipeReader;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.*;

public class DataReader implements Closeable, IPipeReader {
	private final TTransport inPipe;
	private final TBinaryProtocol inProto;

	public DataReader(File inPipe) throws IOException {
		this(new FileInputStream(inPipe));
	}

	public DataReader(InputStream is) {
		this.inPipe = new TIOStreamTransport(is);
		this.inProto = new TBinaryProtocol(this.inPipe);
	}

	@Override
	public void close() {
		inPipe.close();
	}

	/**
	 * Reset reader to a blank state
	 */
	public void reset() {
		int bufLen = inPipe.getBytesRemainingInBuffer();
		inPipe.consumeBuffer(bufLen);
		inProto.reset();
	}

	/**
	 * Read/Fetch data from pipe.
	 *
	 * @param <T> - type to read
	 * @return retrieved object
	 * @throws IOException
	 */
	public <T extends TBase> T read(T instance) throws TException {
		instance.read(inProto);
		return instance;
	}


	/**
	 * Read the given size of bytes (data).
	 *
	 * @param length
	 * @return data as byte array
	 * @throws TTransportException
	 */
	public byte[] readBytes(int length) throws TTransportException {
		byte[] buf = new byte[length];
		inPipe.read(buf, 0, length);
		return buf;
	}

	/**
	 * Read/fetch int value from pipe.
	 *
	 * @return - int value
	 * @throws TException
	 */
	public int readInt() throws TException {
		return inProto.readI32();
	}


	/**
	 * Read/fetch a String from pipe.
	 *
	 * @return - string value
	 * @throws TException
	 */
	public String readString() throws TException {
		return inProto.readString();
	}
}
