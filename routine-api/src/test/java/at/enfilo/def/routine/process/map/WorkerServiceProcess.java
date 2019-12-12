package at.enfilo.def.routine.process.map;

import at.enfilo.def.routine.process.WorkerServiceMock;

import java.io.File;

public class WorkerServiceProcess {

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new RuntimeException("1 arguments needed: ctrl_pipe");
		}
		String ctrlPipe = args[0];

		// Start worker service mock
		WorkerServiceMock ws = new WorkerServiceMock(null, new File(ctrlPipe), null);

		try {
			ws.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
