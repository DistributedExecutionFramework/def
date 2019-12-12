package at.enfilo.def.routine.process;

import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WorkerServiceMock implements Runnable {

	private final File outPipe;
	private final File ctrlPipe;
	private final HashMap<String, TBase> params;
	private final List<Result> results;

	public WorkerServiceMock(File outPipe, File ctrlPipe, HashMap<String, TBase> params) {
		this.outPipe = outPipe;
		this.ctrlPipe = ctrlPipe;
		this.params = params;
		this.results = new ArrayList<>();
	}

	@Override
	public void run() {
		try {
			DataWriter out = null;
			if (outPipe != null) {
				out = new DataWriter(outPipe);
			}
			DataReader ctrl = new DataReader(ctrlPipe);

			boolean active = true;
			while (active) {
				Order o = ctrl.read(new Order());

				switch (o.getCommand()) {
					case GET_PARAMETER:
						String name = o.getValue();

						if (!params.containsKey(name)) {
							throw new RuntimeException("param with index not known");
						}
						TBase param = params.get(name);
						if (out != null) {
							out.store(param);
						}
						break;

					case LOG_DEBUG:
						System.out.println("DEBUG " + o.getValue());
						break;
					case LOG_INFO:
						System.out.println("INFO " + o.getValue());
						break;
					case LOG_ERROR:
						System.err.println("ERROR " + o.getValue());
						break;
					case SEND_RESULT:
						int nrOfResults = Integer.parseInt(o.getValue());
						for (int i = 0; i < nrOfResults; i++) {
							Result result = ctrl.read(new Result());
							results.add(result);
						}
						break;
					case ROUTINE_DONE:
						active = false;
						break;

					default:
						throw new RuntimeException("Wrong Command: " + o.getCommand());
				}
			}

			if (out != null) {
				out.close();
			}
			ctrl.close();

		} catch (IOException | TException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public List<Result> getResults() { return results; }

}
