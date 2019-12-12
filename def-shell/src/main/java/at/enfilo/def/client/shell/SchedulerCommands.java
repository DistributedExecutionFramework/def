package at.enfilo.def.client.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import static at.enfilo.def.client.shell.Constants.*;

@Component
public class SchedulerCommands implements CommandMarker {

	@Autowired
	private DEFShellSession session;
	@Autowired
	private ObjectCommands objects;

	@CliAvailabilityIndicator({
			CMD_SCHEDULER_SCHEDULE,
			CMD_SCHEDULER_JOB_MARK_COMPLETE,
			CMD_SCHEDULER_JOB_REMOVE,
			CMD_SCHEDULER_JOB_ADD,
			CMD_SCHEDULER_WORKER_ADD,
			CMD_SCHEDULER_WORKER_REMOVE
	})
	public boolean isSchedulerServiceActive() {
		return session.getActiveService() == Service.SCHEDULER;
	}


	@CliCommand(value = CMD_SCHEDULER_SCHEDULE, help = "Schedules (add) a Task to Scheduler")
	public String schedule(
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id for Task scheduling") final String jId,
		@CliOption(key = OPT_TASK, mandatory = true, help = "Task object name to schedule") final String task
	) {
		return null;
	}

	@CliCommand(value = CMD_SCHEDULER_JOB_MARK_COMPLETE, help = "Mark Job as complete (all Tasks scheduled).")
	public String markJobAsComplete(
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id to mark as complete") final String jId
	) {
		return null;
	}

	@CliCommand(value = CMD_SCHEDULER_JOB_REMOVE, help = "Delete given Job")
	public String removeJob(
		@CliOption(key = OPT_JOB_ID, mandatory = true, help = "Job Id to abort") final String jId
	) {
		return null;
	}

	@CliCommand(value = CMD_SCHEDULER_WORKER_ADD, help = "Add a worker to Scheduler")
	public String addWorker(
		@CliOption(key = OPT_NODE_ID, mandatory = true, help = "Worker Id") final String workerId,
		@CliOption(key = OPT_SERVICE_ENDPOINT, mandatory = true, help = "Worker ServiceEndpoint Object name") final String serviceEndpoint
	) {
		return null;
	}

	@CliCommand(value = CMD_SCHEDULER_JOB_ADD, help = "Add job to Scheduler")
	public String addJob(
		@CliOption(key = OPT_JOB, mandatory = true, help = "Job object name to add") final String job
	) {
		return null;
	}
}
