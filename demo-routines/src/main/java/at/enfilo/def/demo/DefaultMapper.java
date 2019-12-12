package at.enfilo.def.demo;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.routine.LogLevel;
import at.enfilo.def.routine.MapRoutine;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.List;

public class DefaultMapper extends MapRoutine {

	public static final String DEFAULT_KEY = "DEFAULT";

	public DefaultMapper() {
		super(TBase.class);
	}

	@Override
	protected List<ITuple> map(TBase toMap) {
		return null;
	}

	@Override
	public void runRoutine() {
		try {
			log(LogLevel.DEBUG, "Try to receive result from ObjectiveRoutine");
			int bytes = in.readInt();
			byte[] data = in.readBytes(bytes);
			log(LogLevel.DEBUG, String.format("Received %d bytes from ObjectiveRoutine", bytes));

			out.store(1); // 1 Tuple
			out.store(DEFAULT_KEY); // Key
			out.store(bytes); // Number of data bytes
			out.store(data);
			log(LogLevel.DEBUG, String.format("Wrote 1 tuple with key %s and %d bytes of value data", DEFAULT_KEY, bytes));

		} catch (TException e) {
			log(LogLevel.ERROR, String.format("Error while read or write mapping %s", e.getMessage()));
		}
	}
}
