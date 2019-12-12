package at.enfilo.def.client.shell;

import at.enfilo.def.client.shell.formatter.ShellOutputFormatter;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static at.enfilo.def.client.shell.Constants.*;

@Component
public class ExecLogicCommands implements CommandMarker {

	@Autowired
	private DEFShellSession session;
	@Autowired
	private ObjectCommands objects;


	@CliAvailabilityIndicator({
		CMD_EXEC_PROGRAM_LIST,
		CMD_EXEC_PROGRAM_CREATE,
		CMD_EXEC_PROGRAM_SHOW,
		CMD_EXEC_PROGRAM_REMOVE,
		CMD_EXEC_PROGRAM_MARK_FINISHED,
		CMD_EXEC_JOB_LIST,
		CMD_EXEC_JOB_CREATE,
		CMD_EXEC_JOB_SHOW,
		CMD_EXEC_JOB_REMOVE,
		CMD_EXEC_JOB_SHOW_MAP_ROUTINE,
		CMD_EXEC_JOB_ATTACH_MAP_ROUTINE,
		CMD_EXEC_JOB_SHOW_REDUCE_ROUTINE,
		CMD_EXEC_JOB_ATTACH_REDUCE_ROUTINE,
		CMD_EXEC_JOB_MARK_COMPLETE,
		CMD_EXEC_JOB_ABORT,
		CMD_EXEC_TASK_LIST,
		CMD_EXEC_TASK_CREATE,
		CMD_EXEC_TASK_SHOW,
		CMD_EXEC_TASK_ABORT,
		CMD_EXEC_TASK_RERUN,
		CMD_EXEC_SHARED_RESOURCE_LIST,
		CMD_EXEC_SHARED_RESOURCE_CREATE,
		CMD_EXEC_SHARED_RESOURCE_SHOW,
		CMD_EXEC_SHARED_RESOURCE_REMOVE,
	})
	public boolean isExecLogicServiceActive() {
		return session.getActiveService() == Service.EXEC_LOGIC;
	}


	@CliCommand(value = CMD_EXEC_PROGRAM_LIST, help = "List all programs belong to given user.")
	public String getAllPrograms(
		@CliOption(key = OPT_USER_ID, mandatory = true, help = "User Id") final String uId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<String>> futureProgramIds = session.getExecLogicServiceClient().getAllPrograms(uId);
		return ShellOutputFormatter.format(futureProgramIds.get());
	}


	@CliCommand(value = CMD_EXEC_PROGRAM_CREATE, help = "Create a new Program on given Cluster")
	public String createProgram(
		@CliOption(key = OPT_CLUSTER_ID, mandatory = true, help = "Cluster Id") final String cId,
		@CliOption(key = OPT_USER_ID, mandatory = true, help = "User Id") final String uId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<String> futureProgramId = session.getExecLogicServiceClient().createProgram(cId, uId);
		return String.format(MESSAGE_EXEC_PROGRAM_CREATED, futureProgramId.get(), cId);
	}


	@CliCommand(value = CMD_EXEC_PROGRAM_SHOW, help = "Show Program")
	public String getProgram(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store Program to an object with given name") final String objectName
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<ProgramDTO> futureProgram = session.getExecLogicServiceClient().getProgram(pId);
		ProgramDTO program = futureProgram.get();

		if (objectName == null) {
			return ShellOutputFormatter.format(program);
		} else {
			objects.getObjectMap().put(objectName, program);
			return String.format(MESSAGE_OBJECT_STORED, objectName);
		}
	}


	@CliCommand(value = CMD_EXEC_PROGRAM_REMOVE, help = "Delete given Program (incl. Jobs and Tasks) from Manager/Cluster")
	public String deleteProgram(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getExecLogicServiceClient().deleteProgram(pId).get();
		return MESSAGE_EXEC_PROGRAM_REMOVE;
	}


	@CliCommand(value = CMD_EXEC_PROGRAM_MARK_FINISHED, help = "Mark a Program as finished. All jobs and tasks are created.")
	public String markProgramAsFinished(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getExecLogicServiceClient().markProgramAsFinished(pId).get();
		return MESSAGE_EXEC_PROGRAM_MARK_FINISHED;
	}


	@CliCommand(value = CMD_EXEC_JOB_LIST, help = "List all Jobs of a Program")
	public String getAllJobs(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<String>> futureJobIds = session.getExecLogicServiceClient().getAllJobs(pId);
		return ShellOutputFormatter.format(futureJobIds.get());
	}


	@CliCommand(value = CMD_EXEC_JOB_CREATE, help = "Create a new Job in given Program")
	public String createJob(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<String> futureJobId = session.getExecLogicServiceClient().createJob(pId);
		return String.format(MESSAGE_EXEC_JOB_CREATED, futureJobId.get());
	}


	@CliCommand(value = CMD_EXEC_JOB_SHOW, help = "Show Job")
	public String getJobInfo(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store Job object to given name") final String objectName
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<JobDTO> futureJob = session.getExecLogicServiceClient().getJob(pId, jId);
		JobDTO job = futureJob.get();

		if (objectName == null) {
			return ShellOutputFormatter.format(futureJob.get());
		} else {
			objects.getObjectMap().put(objectName, job);
			return String.format(MESSAGE_OBJECT_STORED, objectName);
		}
	}


	@CliCommand(value = CMD_EXEC_JOB_REMOVE, help = "Delete the given Job (incl. Tasks)")
	public String deleteJob(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getExecLogicServiceClient().deleteJob(pId, jId).get();
		return MESSAGE_EXEC_JOB_REMOVE;
	}


	@CliCommand(value = CMD_EXEC_JOB_SHOW_MAP_ROUTINE, help = "Show attached MapRoutine-Id")
	public String getAttachedMapRoutine(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<String> futureRoutineId = session.getExecLogicServiceClient().getAttachedMapRoutine(pId, jId);
		return String.format(MESSAGE_EXEC_JOB_GET_MAP_ROUTINE, jId, futureRoutineId.get());
	}


	@CliCommand(value = CMD_EXEC_JOB_ATTACH_MAP_ROUTINE, help = "Attach a MapRoutine to given Job")
	public String attachedMapRoutine(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId,
		@CliOption(key = OPT_ROUTINE_ID, mandatory = true, help = "Map-Routine Id") final String mapRoutineId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getExecLogicServiceClient().attachMapRoutine(pId, jId, mapRoutineId).get();
		return MESSAGE_EXEC_JOB_ATTACH_MAP_ROUTINE;
	}


	@CliCommand(value = CMD_EXEC_JOB_SHOW_REDUCE_ROUTINE, help = "Show attached ReduceRoutine-Id")
	public String getAttachedReduceRoutine(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<String> futureRoutineId = session.getExecLogicServiceClient().getAttachedReduceRoutine(pId, jId);
		return String.format(MESSAGE_EXEC_JOB_GET_REDUCE_ROUTINE, jId, futureRoutineId.get());
	}


	@CliCommand(value = CMD_EXEC_JOB_ATTACH_REDUCE_ROUTINE, help = "Attach a ReduceRoutine to given Job")
	public String attachedReduceRoutine(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId,
		@CliOption(key = OPT_ROUTINE_ID, mandatory = true, help = "Reduce-Routine Id") final String reduceRoutineId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getExecLogicServiceClient().attachReduceRoutine(pId, jId, reduceRoutineId).get();
		return MESSAGE_EXEC_JOB_ATTACH_REDUCE_ROUTINE;
	}


	@CliCommand(value = CMD_EXEC_TASK_LIST, help = "List all Task Ids under given Job")
	public String getAllTasks(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId,
		@CliOption(key = OPT_EXECUTION_STATE, mandatory = true, help = "Execution State") final ExecutionState state,
		@CliOption(key = OPT_SORTING_CRITERION, help = "Sorting Criterion") final SortingCriterion sortingCriterion
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<String>> futureTaskIds = session.getExecLogicServiceClient().getAllTasksWithState(pId, jId, state, sortingCriterion != null ? sortingCriterion : SortingCriterion.NO_SORTING);
		return ShellOutputFormatter.format(futureTaskIds.get());
	}


	@CliCommand(value = CMD_EXEC_TASK_CREATE, help = "Create a new Task in given Job")
	public String createTask(
			@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
			@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId,
			@CliOption(key = OPT_ROUTINE_INSTANCE, mandatory = true, help = "Objective RoutineInstance object name") final String routineInstanceName
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		RoutineInstanceDTO routineInstance = objects.getObject(routineInstanceName, RoutineInstanceDTO.class);
		Future<String> futureTaskId = session.getExecLogicServiceClient().createTask(pId, jId, routineInstance);
		return String.format(MESSAGE_EXEC_TASK_CREATED, futureTaskId.get());
	}


	@CliCommand(value = CMD_EXEC_TASK_SHOW, help = "Show Task")
	public String getTask(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId,
		@CliOption(key = OPT_TASK_ID, mandatory = true, help = "Task Id") final String tId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store Task object to given name") final String objectName
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<TaskDTO> futureTask = session.getExecLogicServiceClient().getTask(pId, jId, tId);
		TaskDTO task = futureTask.get();
		if (objectName == null) {
			return ShellOutputFormatter.format(task);
		} else {
			objects.getObjectMap().put(objectName, task);
			return String.format(MESSAGE_OBJECT_STORED, objectName);
		}
	}

	@CliCommand(value = CMD_EXEC_TASK_ABORT, help = "Abort Task")
	public String abortTask(
			@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
			@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId,
			@CliOption(key = OPT_TASK_ID, mandatory = true, help = "Task Id") final String tId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getExecLogicServiceClient().abortTask(pId, jId, tId).get();
		return MESSAGE_EXEC_TASK_ABORTED;
	}

	@CliCommand(value = CMD_EXEC_TASK_RERUN, help = "Re-run Task")
	public String reRunTask(
			@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
			@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId,
			@CliOption(key = OPT_TASK_ID, mandatory = true, help = "Task Id") final String tId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getExecLogicServiceClient().reRunTask(pId, jId, tId).get();
		return MESSAGE_EXEC_TASK_RERUN;
	}


	@CliCommand(value = CMD_EXEC_JOB_MARK_COMPLETE, help = "Mark given Job as complete, this means all Tasks were created")
	public String markJobAsComplete(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getExecLogicServiceClient().markJobAsComplete(pId, jId).get();
		return MESSAGE_EXEC_JOB_MARK_COMPLETE;
	}


	@CliCommand(value = CMD_EXEC_JOB_ABORT, help = "Abort given Job")
	public String abortJob(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id") final String jId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getExecLogicServiceClient().abortJob(pId, jId).get();
		return MESSAGE_EXEC_JOB_ABORTED;
	}


	@CliCommand(value = CMD_EXEC_SHARED_RESOURCE_LIST, help = "List all SharedResources for the given Program")
	public String getAllSharedResources(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<String>> futureResourceIds = session.getExecLogicServiceClient().getAllSharedResources(pId);
		return ShellOutputFormatter.format(futureResourceIds.get());
	}


	@CliCommand(value = CMD_EXEC_SHARED_RESOURCE_CREATE, help = "Create a new SharedResource under the given Program")
	public String createSharedResource(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_DATA_TYPE, mandatory = true, help = "DataType Id") final String dataTypeId,
		@CliOption(key = OPT_DATA, mandatory = true, help = "Data as hex string") final String data
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		// Hex-string to byte[]
		int len = data.length();
		byte[] buffer = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			buffer[i / 2] = (byte) ((Character.digit(data.charAt(i), 16) << 4) + Character.digit(data.charAt(i+1), 16));
		}

		Future<String> futureResourceId = session.getExecLogicServiceClient().createSharedResource(pId, dataTypeId, ByteBuffer.wrap(buffer));
		return String.format(MESSAGE_EXEC_RESOURCE_CREATED, futureResourceId.get());
	}


	@CliCommand(value = CMD_EXEC_SHARED_RESOURCE_SHOW, help = "Show SharedResource")
	public String getSharedResource(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_RESOURCE_ID, mandatory = true, help = "Resource Id") final String rId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store Resource to an object with given name") final String objectName
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<ResourceDTO> futureResource = session.getExecLogicServiceClient().getSharedResource(pId, rId);
		ResourceDTO resource = futureResource.get();
		if (objectName == null) {
			return ShellOutputFormatter.format(resource);
		} else {
			objects.getObjectMap().put(objectName, resource);
			return String.format(MESSAGE_OBJECT_STORED, objectName);
		}
	}


	@CliCommand(value = CMD_EXEC_SHARED_RESOURCE_REMOVE, help = "Delete a SharedResource")
	public String deleteSharedResource(
		@CliOption(key = OPT_PROGRAM_ID, mandatory = true, help = "Program Id") final String pId,
		@CliOption(key = OPT_RESOURCE_ID, mandatory = true, help = "Resource Id") final String rId
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		session.getExecLogicServiceClient().deleteSharedResource(pId, rId).get();
		return MESSAGE_EXEC_RESOURCE_REMOVE;
	}
}
