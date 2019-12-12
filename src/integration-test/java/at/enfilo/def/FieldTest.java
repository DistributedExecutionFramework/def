package at.enfilo.def;

import at.enfilo.def.client.api.RoutineInstanceBuilder;
import at.enfilo.def.common.util.DaemonThreadFactory;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.RoutineInstanceDTO;
import at.enfilo.def.transfer.dto.SortingCriterion;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.junit.Assert;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertNotNull;

public class FieldTest {

	public static void main(String[] args) throws Exception {
		if (args.length != 6) {
			System.out.println("Arguments: <host> <port> <protocol> <cluster-id> <#jobs> <#tasks>");
			System.out.println("  e.g.: 127.0.0.1 40012 THRIFT_TCP cluster1 10 10 100");
			//return;
			args = new String[] {
					"manager",
					"40002",
					"THRIFT_TCP",
					"cluster1",
					"1",
					"2",
					"100"
			};
		}

		String host = args[0];
		int port = Integer.valueOf(args[1]);
		Protocol protocol = Protocol.valueOf(args[2]);
		String clusterId = args[3];
		int programs = Integer.valueOf(args[4]);
		int jobs = Integer.valueOf(args[5]);
		int tasks = Integer.valueOf(args[6]);

		FieldTest ft = new FieldTest();
		ft.moreTasks(host, port, protocol, clusterId, programs, jobs, tasks);
	}

	//@Test
	public void moreTasks(String host, int port, Protocol protocol, String clusterId, int programs, int jobs, int tasks) throws Exception {
		IExecLogicServiceClient execClient = new ExecLogicServiceClientFactory().createClient(
				new ServiceEndpointDTO(host, port, protocol)
		);

		ExecutorService service = Executors.newFixedThreadPool(
				4,
				r -> DaemonThreadFactory.newDaemonThread(r, "FieldTestPool")
		);

//		String defaultMapRoutineId = UUID.nameUUIDFromBytes(DefaultMapper.class.getCanonicalName().getBytes()).toString();
//		String storeRoutineId = UUID.nameUUIDFromBytes(DefaultMemoryStorer.class.getCanonicalName().getBytes()).toString();

//		clusterClient.setDefaultMapRoutine(defaultMapRoutineId);
//		clusterClient.setStoreRoutine(storeRoutineId);
//		clusterClient.setPartitionRoutine(partitionRoutineId);


		System.out.println("service switch --service EXEC_LOGIC --host " + execClient.getServiceEndpoint().getHost() + " --port " + execClient.getServiceEndpoint().getPort() + " --protocol " + execClient.getServiceEndpoint().getProtocol());

		// Create Program, Job, Task
		String userId = "user";

		for (int p = 1; p <= programs; p++) {
			String pId = execClient.createProgram(clusterId, userId).get();
			System.out.println(String.format("Created Program (%d/%d): %s", p, programs, pId));

			// Create a shared resource
			DEFDouble stepSize = new DEFDouble(1e-9);
			String rId = execClient.createSharedResource(
					pId,
					stepSize.get_id(),
					ByteBuffer.wrap(new TSerializer().serialize(stepSize))
			).get();

			// Prepare PiCalc
			RoutineInstanceDTO piCalc = new RoutineInstanceBuilder("cfec958c-e34f-3240-bcea-cdeebd186cf6")
					.addParameter("start", new DEFDouble(0))
					.addParameter("end", new DEFDouble(1e9))
					.addParameter("stepSize", rId)
					.build();

			for (int j = 1; j <= jobs; j++) {
				String jId = execClient.createJob(pId).get();
				assertNotNull(jId);
				System.out.println("  Created Job (" + j + "/" + jobs + "): " + jId);
				System.out.println("    exec-logic job show --pId " + pId + " --jId " + jId);

//				execClient.attachReduceRoutine(pId, jId, UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString());

				System.out.println(String.format("    Create %d tasks.", tasks));
				for (int i = 1; i <= tasks; i++) {
					Future<String> future = execClient.createTask(pId, jId, piCalc);
					service.submit(() -> future.get());
				}
				execClient.markJobAsComplete(pId, jId);


				System.out.println(String.format("    Waiting for Job (%d/%d) is done.", j, jobs));
				await()
						.atMost(10, TimeUnit.HOURS)
						.pollInterval(10, TimeUnit.SECONDS)
						.until(
								() -> ExecutionState.SUCCESS == execClient.getJob(pId, jId).get().getState()
						);

				Future<List<String>> futureTasks = execClient.getAllTasksWithState(pId, jId, ExecutionState.SUCCESS, SortingCriterion.NO_SORTING);

				// Check result of tasks
				TDeserializer deserializer = new TDeserializer();
				Random rnd = new Random();
				for (int i = 0; i < 10; i++) {
					int x = rnd.nextInt(futureTasks.get().size());
					String tId = futureTasks.get().get(x);
					TaskDTO task = execClient.getTask(pId, jId, tId).get();
					DEFDouble pi = new DEFDouble();
					deserializer.deserialize(pi, task.getOutParameters().get(0).data.array());
					Assert.assertEquals(Math.PI, pi.getValue(), 1e4);
				}
				System.out.println(String.format("  Job (%d/%d) done.", j, jobs));

//				JobDTO job = execClient.waitForJob(pId, jId);
//				if (job.getState() == ExecutionState.SUCCESS) {
//					List<ResourceDTO> reducedResults = job.getReducedResults();
//					for (ResourceDTO reducedResult : reducedResults) {
//						DEFDouble value = DEFDouble.class.newInstance();
//						new TDeserializer().deserialize(value, reducedResult.getData());
//						System.out.println(value);
//					}
//				}
			}
			Future<Void> futureDeleteProgram = execClient.deleteProgram(pId);
			await().atMost(60, TimeUnit.SECONDS).until(futureDeleteProgram::isDone);
			System.out.println(String.format("Program (%d/%d) deleted.", p, programs));
		}

		System.out.println("end");
	}


	//@Test
	public void deleteProgram() throws Exception {
		IExecLogicServiceClient execClient = new ExecLogicServiceClientFactory().createClient(
//				new ServiceEndpointDTO("manager", 40002, Protocol.THRIFT_TCP)
//				se

				new ServiceEndpointDTO("cluster", 40012, Protocol.THRIFT_TCP)
		);

		DEFDouble defDouble = new DEFDouble();
		defDouble.clear();

		String pId = "1b0d6706-5915-4c8d-a471-e3ac04803de9";
		Future<Void> futureDeleteProgram = execClient.deleteProgram(pId);
		await().atMost(60, TimeUnit.SECONDS).until(futureDeleteProgram::isDone);
	}
}
