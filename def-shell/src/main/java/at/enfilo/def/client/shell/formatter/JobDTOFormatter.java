package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.transfer.dto.JobDTO;

import java.time.Instant;

class JobDTOFormatter extends ShellFormatter<JobDTO> {

	public JobDTOFormatter() {
		super(JobDTO.class);
	}

	@Override
	public String doFormat(JobDTO job, char[] shifted) {
		StringBuilder sb = new StringBuilder();

		sb.append(shifted).append("Id: ").append(job.getId()).append("\n");
		sb.append(shifted).append("ProgramId: ").append(job.getProgramId()).append("\n");
		sb.append(shifted).append("State: ").append(job.getState()).append("\n");
		sb.append(shifted).append("CreationTime: ").append(Instant.ofEpochMilli(job.getCreateTime())).append("\n");
		sb.append(shifted).append("FinishTime: ").append(Instant.ofEpochMilli(job.getFinishTime())).append("\n");
		sb.append(shifted).append("MapRoutine: ").append(job.getMapRoutineId()).append("\n");
		sb.append(shifted).append("ReduceRoutine: ").append(job.getReduceRoutineId()).append("\n");
		sb.append(shifted).append("# Tasks: ").append(job.getScheduledTasks() + job.getRunningTasks() + job.getSuccessfulTasks() + job.getFailedTasks()).append("\n");
		sb.append(shifted).append("# scheduled Tasks: ").append(job.getScheduledTasks()).append("\n");
		sb.append(shifted).append("# running Tasks: ").append(job.getRunningTasks()).append("\n");
		sb.append(shifted).append("# successful Tasks: ").append(job.getSuccessfulTasks()).append("\n");
		sb.append(shifted).append("# failed Tasks: ").append(job.getFailedTasks()).append("\n");

		return sb.toString();
	}
}
