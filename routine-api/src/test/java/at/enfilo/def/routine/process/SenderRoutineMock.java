package at.enfilo.def.routine.process;

import at.enfilo.def.routine.util.DataWriter;
import org.apache.thrift.TBase;

import java.io.File;
import java.util.List;

public class SenderRoutineMock implements Runnable {
	private final File outPipe;
	private final List<?> toSend;

	public SenderRoutineMock(File outPipe, List<?> toSend) {
		this.outPipe = outPipe;
		this.toSend = toSend;
	}

	@Override
	public void run() {
		try {
			DataWriter out = new DataWriter(outPipe);
			for (Object o : toSend) {
				if (o instanceof Integer) {
					out.store(Integer.class.cast(o).intValue());
				} else if (o instanceof String) {
					out.store(String.class.cast(o));
				} else if (o instanceof TBase) {
					out.store(TBase.class.cast(o));
				} else {
					throw new RuntimeException("Object/Type " + o.getClass() + " not known");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
