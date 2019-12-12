package at.enfilo.def.routine.process.objective;

import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.datatype.DEFString;
import at.enfilo.def.routine.process.WorkerServiceMock;
import org.apache.thrift.TBase;

import java.io.File;
import java.util.HashMap;

public class WorkerServiceProcess {

	public static void main(String[] args) {
		if (args.length != 2) {
			throw new RuntimeException("2 arguments needed: in_pipe, ctrl_pipe");
		}
		String inPipe = args[0];
		String ctrlPipe = args[1];

		HashMap<String, TBase> params = new HashMap<>();
		params.put("firstName", new DEFString(ObjectiveRoutineProcessTest.FIRST_NAME));
		params.put("lastName", new DEFString(ObjectiveRoutineProcessTest.LAST_NAME));
		params.put("age", new DEFInteger(ObjectiveRoutineProcessTest.AGE));

		// Start workservice mock
		WorkerServiceMock ws = new WorkerServiceMock(new File(inPipe), new File(ctrlPipe), params);

		try {
			ws.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
}
