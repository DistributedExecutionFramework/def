package at.enfilo.def.client.shell;

import at.enfilo.def.client.shell.formatter.ShellOutputFormatter;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static at.enfilo.def.client.shell.Constants.*;

@Component
public class WorkerCommands implements CommandMarker {

	@Autowired
	private ObjectCommands objects;
	@Autowired
	private DEFShellSession session;


	@CliAvailabilityIndicator({
			CMD_WORKER_TAKE_CONTROL,
			CMD_WORKER_REGISTER_OBSERVER,
			CMD_WORKER_DEREGISTER_OBSERVER,
			CMD_WORKER_SHUTDOWN,
			CMD_WORKER_TASKS_LIST,
			CMD_WORKER_TASKS_QUEUE,
			CMD_WORKER_QUEUE_RELEASE,
			CMD_WORKER_QUEUE_PAUSE,
			CMD_WORKER_QUEUE_REMOVE,
			CMD_WORKER_QUEUE_LIST,
			CMD_WORKER_QUEUE_CREATE,
			CMD_WORKER_QUEUE_SHOW,
			CMD_WORKER_SHOW,
			CMD_WORKER_ENV,
			CMD_WORKER_TASKS_MOVE,
			CMD_WORKER_TASKS_MOVE_ALL,
			CMD_WORKER_TASKS_FETCH_FINISHED,
			CMD_WORKER_STORE_ROUTINE_SET,
			CMD_WORKER_STORE_ROUTINE_SHOW
	})
	public boolean isWorkerServiceActive() {
		return session.getActiveService() == Service.WORKER;
	}


	@CliCommand(value = CMD_WORKER_TAKE_CONTROL, help = "Cluster takes control over this worker")
	public String takeControl(
		@CliOption(key = OPT_CLUSTER_ID, mandatory = true, help = "Cluster Id") final String clusterId
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		session.getWorkerServiceClient().takeControl(clusterId).get();
		return MESSAGE_WORKER_TAKE_CONTROL;
	}


	@CliCommand(value = CMD_WORKER_REGISTER_OBSERVER, help = "Register an observer on this worker")
	public String registerObserver(
		@CliOption(key = OPT_SERVICE_ENDPOINT, mandatory = true, help = "Endpoint object name") final String object,
		@CliOption(key = OPT_PERIODICALLY, unspecifiedDefaultValue = "false", help = "Periodically send status information to observer") final Boolean periodically,
		@CliOption(key = OPT_PERIOD_DURATION, unspecifiedDefaultValue = "-1", help = "Period duration between send") final Integer periodDuration,
		@CliOption(key = OPT_PERIOD_UNIT, unspecifiedDefaultValue = "SECONDS", help = "Unit of period duration") final PeriodUnit periodUnit

	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		ServiceEndpointDTO endpoint = objects.getObject(object, ServiceEndpointDTO.class);
		Future<Void> futureStatus = session.getWorkerServiceClient().registerObserver(
			endpoint,
			periodically,
			periodDuration,
			periodUnit
		);

		return String.format(MESSAGE_WORKER_REGISTER_OBSERVER, futureStatus.get());
	}


	@CliCommand(value = CMD_WORKER_DEREGISTER_OBSERVER, help = "Deregister an observer from this worker")
	public String deregisterObserver(
			@CliOption(key = OPT_SERVICE_ENDPOINT, mandatory = true, help = "Endpoint object name") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		ServiceEndpointDTO endpoint = objects.getObject(object, ServiceEndpointDTO.class);
		session.getWorkerServiceClient().deregisterObserver(endpoint).get();
		return MESSAGE_WORKER_DEREGISTER_OBSERVER;
	}


	@CliCommand(value = CMD_WORKER_SHUTDOWN, help = "Shutdown Worker instance")
	public String shutdown() throws ClientCommunicationException {
		session.getWorkerServiceClient().shutdown();
		return MESSAGE_WORKER_SHUTDOWN;
	}


	@CliCommand(value = CMD_WORKER_SHOW, help = "Info about this Worker")
	public String getInfo(
		@CliOption(key = OPT_TO_OBJECT, help = "Store WorkerInfo into an object") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<NodeInfoDTO> future = session.getWorkerServiceClient().getInfo();
		NodeInfoDTO info = future.get();

		if (object == null) {
			return ShellOutputFormatter.format(info);

		} else {
			objects.getObjectMap().put(object, info);
			return String.format(MESSAGE_OBJECT_STORED, object);
		}
	}

	@CliCommand(value = CMD_WORKER_ENV, help = "Show Worker environment")
	public String getEnvironment(
			@CliOption(key = OPT_TO_OBJECT, help = "Store Worker environment into an object") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<NodeEnvironmentDTO> future = session.getWorkerServiceClient().getEnvironment();
		NodeEnvironmentDTO env = future.get();

		if (object == null) {
			return ShellOutputFormatter.format(env);

		} else {
			objects.getObjectMap().put(object, env);
			return String.format(MESSAGE_OBJECT_STORED, object);
		}
	}


	@CliCommand(value = CMD_WORKER_QUEUE_LIST, help = "List Queues on Worker")
	public String getQueues() throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<String>> qIds = session.getWorkerServiceClient().getQueueIds();
		return ShellOutputFormatter.format(qIds.get());
	}


	@CliCommand(value = CMD_WORKER_QUEUE_CREATE, help = "Create a queue on Worker")
	public String createQueue(
		@CliOption(key = OPT_QUEUE_ID, mandatory = true, help = "Queue Id") final String queueId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getWorkerServiceClient().createQueue(queueId).get();
		return MESSAGE_WORKER_QUEUE_CREATED;
	}


	@CliCommand(value = CMD_WORKER_QUEUE_SHOW, help = "Show information about specified queue")
	public String getQueueInfo(
		@CliOption(key = OPT_QUEUE_ID, mandatory = true, help = "Queue Id") final String queueId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store QueueInfo into an object") final String object
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		Future<QueueInfoDTO> futureQueueInfo = session.getWorkerServiceClient().getQueueInfo(queueId);
		QueueInfoDTO info = futureQueueInfo.get();

		if (object == null) {
			return ShellOutputFormatter.format(info);

		} else {
			objects.getObjectMap().put(object, info);
			return String.format(MESSAGE_OBJECT_STORED, object);
		}
	}


	@CliCommand(value = CMD_WORKER_QUEUE_REMOVE, help = "Delete specified queue")
	public String deleteQueue(
		@CliOption(key = OPT_QUEUE_ID, mandatory = true, help = "Queue Id") final String queueId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getWorkerServiceClient().deleteQueue(queueId).get();
		return MESSAGE_WORKER_QUEUE_REMOVE;
	}


	@CliCommand(value = CMD_WORKER_QUEUE_RELEASE, help = "Release given queue on Worker")
	public String releaseQueue(
		@CliOption(key = OPT_QUEUE_ID, mandatory = true, help = "Queue Id") final String queueId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getWorkerServiceClient().releaseQueue(queueId).get();
		return MESSAGE_WORKER_QUEUE_RELEASED;
	}


	@CliCommand(value = CMD_WORKER_TASKS_QUEUE, help = "Queue Tasks to specified Queue on Worker")
	public String queueTasks(
		@CliOption(key = OPT_QUEUE_ID, mandatory = true, help = "Queue Id") final String queueId,
		@CliOption(key = OPT_TASKS, mandatory = true, help = "Task Object namees") final String[] taskObjects
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		List<TaskDTO> tasks = new LinkedList<>();
		for (String object : taskObjects) {
			tasks.add(objects.getObject(object, TaskDTO.class));
		}

		session.getWorkerServiceClient().queueTasks(queueId, tasks).get();
		return MESSAGE_WORKER_TASKS_QUEUE;
	}

	@CliCommand(value = CMD_WORKER_QUEUE_PAUSE, help = "Pause queue on Worker")
	public String pauseQueue(
			@CliOption(key = OPT_QUEUE_ID, mandatory = true, help = "Queue Id") final String queueId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getWorkerServiceClient().pauseQueue(queueId).get();
		return MESSAGE_WORKER_QUEUE_PAUSED;
	}



	@CliCommand(value = CMD_WORKER_TASKS_LIST, help = "List all tasks of a Queue on Worker")
	public String getQueuedTasks(
		@CliOption(key = OPT_QUEUE_ID, mandatory = true, help = "Queue Id") final String queueId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<String>> futureTaskIds = session.getWorkerServiceClient().getQueuedTasks(queueId);

		return ShellOutputFormatter.format(futureTaskIds.get());
	}


	@CliCommand(value = CMD_WORKER_TASKS_MOVE, help = "Move specified Tasks from Queue to another Worker")
	public String moveTasks(
		@CliOption(key = OPT_QUEUE_ID, mandatory = true, help = "Queue Id") final String queueId,
		@CliOption(key = OPT_TASK_IDS, mandatory = true, help = "Task Ids") final String[] taskIds,
		@CliOption(key = OPT_SERVICE_ENDPOINT, mandatory = true, help = "Worker Endpoint object name") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<Void> futureStatus = session.getWorkerServiceClient().moveTasks(
				queueId,
				Arrays.asList(taskIds),
				objects.getObject(object, ServiceEndpointDTO.class)
		);
		return String.format(MESSAGE_WORKER_TASKS_MOVE, futureStatus.get());
	}


	@CliCommand(value = CMD_WORKER_TASKS_MOVE_ALL, help = "Move all Tasks from Queue to another Worker")
	public String moveAllTasks(
		@CliOption(key = OPT_SERVICE_ENDPOINT, mandatory = true, help = "Worker Endpoint object name") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getWorkerServiceClient().moveAllTasks(objects.getObject(object, ServiceEndpointDTO.class)).get();
		return MESSAGE_WORKER_TASKS_MOVE;
	}


	@CliCommand(value = CMD_WORKER_TASKS_FETCH_FINISHED, help = "Fetch finished Tasks from Worker")
	public String fetchFinishedTask(
		@CliOption(key = OPT_QUEUE_ID, mandatory = true, help = "Queue Id") final String queueId,
		@CliOption(key = OPT_TASK_ID, mandatory = true, help = "Task Id") final String taskId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store Task as object") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<TaskDTO> futureTask = session.getWorkerServiceClient().fetchFinishedTask(taskId);
		TaskDTO task = futureTask.get();

		if (object == null) {
			return ShellOutputFormatter.format(task);

		} else {
			objects.getObjectMap().put(object, task);
			return String.format(MESSAGE_OBJECT_STORED, object);
		}
	}

	@CliCommand(value = CMD_WORKER_STORE_ROUTINE_SHOW, help = "Show current store-routine for worker")
	public String getStoreRoutineId() throws ExecutionException, InterruptedException, ClientCommunicationException {
		Future<String> futureStoreRoutine = session.getWorkerServiceClient().getStoreRoutine();
		return futureStoreRoutine.get();
	}

	@CliCommand(value = CMD_WORKER_STORE_ROUTINE_SET, help = "Set new store-routine for this worker")
	public String setStoreRoutineId(
			@CliOption(key = OPT_ROUTINE_ID, mandatory = true, help = "Partition-routine Id") final String routineId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getWorkerServiceClient().setStoreRoutine(routineId).get();
		return MESSAGE_STORE_ROUTINE_SET;
	}
}
