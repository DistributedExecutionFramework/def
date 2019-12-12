package at.enfilo.def.demo;

import at.enfilo.def.client.api.RoutineInstanceBuilder;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.routine.ClientRoutine;
import at.enfilo.def.routine.RoutineException;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.RoutineInstanceDTO;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PiCalcWithReduceClientRoutine extends ClientRoutine {

    @Override
    protected void routine(String pId, IExecLogicServiceClient client) throws RoutineException {
        try {
            int nrOfJobs = 2;
            int nrOfTasks = 15;

            double steps = Math.pow(10.0, 9.0);
            double stepSize = 1.0 / steps;
            double taskSteps = steps / nrOfTasks;
            double start = 0;
            double end = taskSteps;

            // Create shared resource
            System.out.println("Create shared resource");
            DEFDouble stepSizeResource = new DEFDouble(stepSize);
            String sharedResourceId = client.createSharedResource(
                    pId,
                    stepSizeResource.get_id(),
                    ByteBuffer.wrap(new TSerializer().serialize(stepSizeResource))
            ).get();

            // Prepare pCalc
            System.out.println("Build RoutineInstanceDTO");
            RoutineInstanceDTO piCalc = new RoutineInstanceBuilder(UUID.nameUUIDFromBytes(PICalc.class.getCanonicalName().getBytes()).toString())
                    .addParameter("start", new DEFDouble(start))
                    .addParameter("end", new DEFDouble(end))
                    .addParameter("stepSize", sharedResourceId)
                    .build();

            for (int j = 1; j <= nrOfJobs; j++) {
                System.out.println("Create job");
                String jId = client.createJob(pId).get();
                System.out.println("Attach reduce routine");
                client.attachReduceRoutine(pId, jId, UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString()).get();

                for (int t = 1; t <= nrOfTasks; t++) {
                    System.out.println("Create task");
                    client.createTask(pId, jId, piCalc).get();
                }
                System.out.println("Mark job as complete");
                client.markJobAsComplete(pId, jId).get();
            }

            System.out.println("Fetch all jobs");
            List<String> jobs = client.getAllJobs(pId).get();
            for (String jId: jobs) {
                JobDTO job = client.waitForJob(pId, jId);
                System.out.println("Add reduced result to program results");
                addToResults(jId, job.getReducedResults().get(0));
                System.out.println("Delete job");
                client.deleteJob(pId, jId).get();
            }
            System.out.println("Finished PiCalc client routine with reduce");

        } catch (TException | ClientCommunicationException | ExecutionException | InterruptedException e) {
            throw new RoutineException(e);
        }
    }
}
