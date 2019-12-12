package at.enfilo.def.demo;

import at.enfilo.def.client.api.RoutineInstanceBuilder;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.routine.ClientRoutine;
import at.enfilo.def.routine.RoutineException;
import at.enfilo.def.transfer.dto.*;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PiCalcClientRoutine extends ClientRoutine {

    @Override
    protected void routine(String pId, IExecLogicServiceClient client) throws RoutineException {

        try {
            int nrOfJobs = 2;
            int nrOfTasks = 10;

            // Create shared resource
            System.out.println("Create shared resource");
            DEFDouble stepSize = new DEFDouble(1e-9);
            String sharedResourceId = client.createSharedResource(
                    pId,
                    stepSize.get_id(),
                    ByteBuffer.wrap(new TSerializer().serialize(stepSize))
            ).get();

            // Prepare PiCalc
            System.out.println("Build RoutineInstanceDTO");
            RoutineInstanceDTO piCalc = new RoutineInstanceBuilder(UUID.nameUUIDFromBytes(PICalc.class.getCanonicalName().getBytes()).toString())
                    .addParameter("start", new DEFDouble(0))
                    .addParameter("end", new DEFDouble(1e9))
                    .addParameter("stepSize", sharedResourceId)
                    .build();

            for (int j = 1; j <= nrOfJobs; j++) {
                System.out.println("Create job");
                String jId = client.createJob(pId).get();
                for (int t = 1; t <= nrOfTasks; t++) {
                    System.out.println("Create task");
                    client.createTask(pId, jId, piCalc).get();
                }
                System.out.println("Mark job as complete");
                client.markJobAsComplete(pId, jId).get();

                System.out.println("Wait for job");
                JobDTO job = client.waitForJob(pId, jId);
            }

            System.out.println("Fetch all jobs");
            List<String> jobs = client.getAllJobs(pId).get();
            for (String jId : jobs) {
                System.out.println("Fetch all tasks of job");
                List<String> tasks = client.getAllTasks(pId, jId, SortingCriterion.NO_SORTING).get();
                for (String tId: tasks) {
                    TaskDTO task = client.getTask(pId, jId, tId).get();
                    System.out.println("Add task results to program results");
                    addToResults(task.getId(), task.getOutParameters().get(0));
                }
                System.out.println("Delete job");
                client.deleteJob(pId, jId).get();
            }
            System.out.println("Finished PiCalc client routine");
        } catch (TException | ClientCommunicationException | ExecutionException | InterruptedException e) {
            throw new RoutineException(e);
        }
    }
}
