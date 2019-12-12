package at.enfilo.def.routine.process.map;

import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.routine.MapTestRoutine;
import at.enfilo.def.routine.ObjectSize;
import at.enfilo.def.routine.process.ReceiverRoutineMock;
import org.apache.thrift.TException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ReceiverRoutineProcess {
	public static void main(String[] args) throws TException {
		if (args.length != 1) {
			throw new RuntimeException("1 argument needed: out_pipe");
		}
		String outPipe = args[0];

		List<Class<?>> toReceive = new LinkedList<>();
		toReceive.add(Integer.class);
		toReceive.add(String.class);
		toReceive.add(Integer.class);
		toReceive.add(DEFInteger.class);
		ReceiverRoutineMock rr = new ReceiverRoutineMock(new File(outPipe), toReceive);

		try {
			rr.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}

		List<Object> received = rr.getReceived();
		if (received.size() == 4) {
			if (!(new Integer(1)).equals(received.get(0))) {
				System.exit(2);
			}
			if (!MapTestRoutine.DEFAULT_KEY.equals(received.get(1))) {
				System.exit(2);
			}
			int size = Integer.class.cast(received.get(2));
			DEFInteger age = new DEFInteger(MapRoutineProcessTest.AGE);
			if (!age.equals(received.get(3))) {
				System.exit(2);
			}
			if (!ObjectSize.proofSize(age, size)) {
				System.exit(2);
			}
		} else {
			System.exit(2);
		}
	}
}
