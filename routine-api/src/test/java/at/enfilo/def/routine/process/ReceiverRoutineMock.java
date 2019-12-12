package at.enfilo.def.routine.process;


import at.enfilo.def.routine.util.DataReader;
import org.apache.thrift.TBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ReceiverRoutineMock implements Runnable {
	//private final static IOHelper IO = IOHelper.getInstance();

	private final File inPipe;
	private final List<Object> received;
	private final List<Class<?>> toReceive;

	public ReceiverRoutineMock(File inPipe, List<Class<?>> toReceive) {
		this.inPipe = inPipe;
		this.toReceive = toReceive;
		this.received = new ArrayList<>();
	}

	@Override
	public void run() {

		try {
			DataReader in = new DataReader(inPipe);

			int i = 1;
			// Listen to Pipe and retrieve expected objects
			for (Class<?> cls : toReceive) {
				// Try to receive expected type
				if (cls == String.class) {
					received.add(in.readString());
				} else if (cls == Integer.class) {
					received.add(in.readInt());
				} else {
					Class<? extends TBase> tbaseClass = (Class<? extends TBase>) cls;
					received.add(in.read(tbaseClass.newInstance()));
				}
			}
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public List<Object> getReceived() {
		return received;
	}

	public boolean hasResult() {
		return (received.size() == toReceive.size());
	}

}
