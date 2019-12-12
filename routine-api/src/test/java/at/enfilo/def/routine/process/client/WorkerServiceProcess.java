package at.enfilo.def.routine.process.client;

import at.enfilo.def.routine.api.Result;
import at.enfilo.def.routine.process.WorkerServiceMock;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class WorkerServiceProcess {

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException("2 arguments needed: in_pipe, ctrl_pipe");
        }
        String inPipe = args[0];
        String ctrlPipe = args[1];

        HashMap<String, TBase> params = new HashMap<>();
        System.out.println("Put to params: program with id " + ClientRoutineProcessTest.PROGRAM.getId());
        params.put("program", ClientRoutineProcessTest.PROGRAM);
        params.put("serviceEndpoint", ClientRoutineProcessTest.SERVICE_ENDPOINT);
        params.put("parameterServerEndpoint", ClientRoutineProcessTest.PARAMETER_SERVER_ENDPOINT);

        // Start worker service mock
        WorkerServiceMock ws = new WorkerServiceMock(new File(inPipe), new File(ctrlPipe), params);

        try {
            ws.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        try {
            System.out.println("Receiving");
            List<Result> results = ws.getResults();
            if (results.size() == 1) {
                ProgramDTO program = new ProgramDTO();
                new TDeserializer().deserialize(program, results.get(0).getData());
                if (!ClientRoutineProcessTest.PROGRAM.equals(program)) {
                    System.out.println("Not equals");
                    System.out.println("PROGRAM " + ClientRoutineProcessTest.PROGRAM);
                    System.out.println("RECEIVED " + program);
                    System.exit(2);
                }
            } else {
                System.exit(2);
            }
        } catch (TException e) {
            e.printStackTrace();
            System.exit(2);
        }

    }
}
