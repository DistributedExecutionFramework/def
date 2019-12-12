package at.enfilo.def;

import at.enfilo.def.client.api.DEFClientFactory;
import at.enfilo.def.client.api.IDEFClient;
import at.enfilo.def.client.api.RoutineInstanceBuilder;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.demo.PICalc;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.transfer.dto.*;
import org.apache.thrift.TDeserializer;
import org.junit.Assert;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertNotNull;

public class MatlabGPUFieldTest {

    private static final String host = "10.0.50.33";
    private static final int port = 50002;
    private static final Protocol protocol = Protocol.THRIFT_TCP;
    private static final String clusterId = "cluster1";
    private static final int programs = 1;
    private static final int jobs = 2;
    private static final int tasks = 25;

    public static void main(String[] args) throws Exception {
        ServiceEndpointDTO managerEndpoint = new ServiceEndpointDTO(host, port, protocol);
        IDEFClient client = DEFClientFactory.createClient(managerEndpoint);
        RoutineInstanceDTO piCalc = new RoutineInstanceBuilder("3d76859d-d534-4be0-8469-5e5216fd8a5a")
                .addParameter("size", new DEFDouble(5000))
                .addParameter("mode", new DEFInteger(0))
                .build();

        for (int p = 1; p <= programs; p++) {
            String pId = client.createProgram(clusterId, "user").get();
            System.out.println(String.format("Created Program (%d/%d): %s", p, programs, pId));

            for (int j = 1; j <= jobs; j++) {
                String jId = client.createJob(pId).get();
                assertNotNull(jId);
                System.out.println("  Created Job (" + j + "/" + jobs + "): " + jId);
                System.out.println("    exec-logic job show --pId " + pId + " --jId " + jId);
                System.out.println(String.format("    Create %d tasks.", tasks));
                for (int i = 1; i <= tasks; i++) {
                    client.createTask(pId, jId, piCalc);
                }
                client.markJobAsComplete(pId, jId);
                System.out.println(String.format("    Waiting for Job (%d/%d) is done.", j, jobs));
                JobDTO job = client.waitForJob(pId, jId);

                if (job.getState() == ExecutionState.SUCCESS) {
                    List<String> tIds = client.getAllTasksWithState(pId, jId, ExecutionState.SUCCESS, SortingCriterion.NO_SORTING).get();
                    for(String tId : tIds) {
                        TaskDTO task = client.getTask(pId, jId, tId).get();
                        DEFDouble result = client.extractOutParameter(task, DEFDouble.class);
                        System.out.println("Task " + tId + "done. Result: " + result);
                    }
                }
                System.out.println(String.format("  Job (%d/%d) done.", j, jobs));
            }
            client.deleteProgram(pId);
            System.out.println(String.format("Program (%d/%d) deleted.", p, programs));
        }
    }
}
