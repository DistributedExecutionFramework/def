package at.enfilo.def.routine.mock;

import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.util.DataReader;
import org.apache.thrift.TException;

import static org.junit.Assert.fail;

public class RoutinesCommunicatorMock extends Thread {
	private DataReader ctrlReader;

	public RoutinesCommunicatorMock(DataReader ctrlReader) {
		this.ctrlReader = ctrlReader;
	}

	@Override
	public void run() {
		boolean run = true;
		while (run) {
			try {
				Order o = ctrlReader.read(new Order());
				switch (o.getCommand()) {
					case ROUTINE_DONE:
						run = false;
						break;
					case LOG_DEBUG:
						System.out.println("DEBUG " + o.getValue());
						break;
					case LOG_INFO:
						System.out.println("INFO " + o.getValue());
						break;
					case LOG_ERROR:
						System.out.println("ERROR " + o.getValue());
						break;
				}
			} catch (TException e) {
				fail();
				break;
			}
		}
	}
}
