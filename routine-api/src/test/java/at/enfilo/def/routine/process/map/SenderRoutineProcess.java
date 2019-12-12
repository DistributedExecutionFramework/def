package at.enfilo.def.routine.process.map;

import at.enfilo.def.routine.ObjectSize;
import at.enfilo.def.routine.Person;
import at.enfilo.def.routine.process.SenderRoutineMock;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class SenderRoutineProcess {
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new RuntimeException("1 argument needed: in_pipe");
		}
		String inPipe = args[0];

		try {
			Person person = new Person(MapRoutineProcessTest.FIRST_NAME, MapRoutineProcessTest.LAST_NAME, MapRoutineProcessTest.AGE);

			// Start Sender Mock
			List<Object> toSend = new LinkedList<>();
			toSend.add(ObjectSize.getSize(person)); // size of object
			toSend.add(person); // object self

			SenderRoutineMock sr = new SenderRoutineMock(new File(inPipe), toSend);
			sr.run();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
}
