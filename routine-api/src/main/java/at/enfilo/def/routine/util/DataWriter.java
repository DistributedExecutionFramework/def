package at.enfilo.def.routine.util;

import at.enfilo.def.routine.api.IPipeWriter;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.*;

public class DataWriter implements Closeable, IPipeWriter {
	private final TIOStreamTransport outPipe;
	private final TProtocol outProto;

	public DataWriter(File outPipe) throws IOException {
		this(new FileOutputStream(outPipe));
	}

	public DataWriter(OutputStream os) {
		this.outPipe = new TIOStreamTransport(os);
		this.outProto = new TBinaryProtocol(this.outPipe);
	}

	@Override
	public void close() {
		outPipe.close();
	}

	/**
	 * Store data to pipe.
	 * @param t - data to store
	 * @param <T> - type of data
	 * @throws IOException
	 */
	public <T extends TBase> void store(T t) throws TException {
		t.write(outProto);
		outPipe.flush();
	}


	/**
	 * Write/store bytes to pipe.
	 *
	 * @param data - bytes to store
	 * @throws IOException
	 * @throws TTransportException
	 */
	public void store(byte[] data) throws TTransportException {
		outPipe.write(data, 0, data.length);
		outPipe.flush();
	}


	/**
	 * Write/store an objective string to pipe.
	 *
	 * @param str - string to store
	 * @throws TException
	 */
	public void store(String str) throws TException {
		outProto.writeString(str);
		outPipe.flush();
	}


	/**
	 * Write store an objective integer to pipe.
	 *
	 * @param i
	 * @throws TException
	 */
	public void store(int i) throws TException {
		outProto.writeI32(i);
		outPipe.flush();
	}
}
