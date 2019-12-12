package at.enfilo.def.routine.process.objective;

import at.enfilo.def.routine.Person;
import at.enfilo.def.routine.process.ReceiverRoutineMock;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ReceiverRoutineProcess {
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new RuntimeException("1 argument needed: out_pipe");
		}
		String outPipe = args[0];

		List<Class<?>> toReceive = new LinkedList<>();
		toReceive.add(Integer.class);
		toReceive.add(Person.class);
		ReceiverRoutineMock rr = new ReceiverRoutineMock(new File(outPipe), toReceive);

		try {
			rr.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}

		List<Object> received = rr.getReceived();
		if (received.size() == 2) {
			Person p = new Person(ObjectiveRoutineProcessTest.FIRST_NAME, ObjectiveRoutineProcessTest.LAST_NAME, ObjectiveRoutineProcessTest.AGE);
			if (!p.equals(received.get(1))) {
				System.exit(2);
			}
		} else {
			System.exit(2);
		}
	}
}
