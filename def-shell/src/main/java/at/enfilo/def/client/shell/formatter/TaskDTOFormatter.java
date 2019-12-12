package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.time.Instant;
import java.util.Map;

class TaskDTOFormatter extends ShellFormatter<TaskDTO> {

	public TaskDTOFormatter() {
		super(TaskDTO.class);
	}

	@Override
	public String doFormat(TaskDTO task, char[] shifted) {
		StringBuilder sb = new StringBuilder();

		sb.append(shifted).append("Id: ").append(task.getId()).append("\n");
		sb.append(shifted).append("JobId: ").append(task.getJobId()).append("\n");
		sb.append(shifted).append("ProgramId: ").append(task.getProgramId()).append("\n");
		sb.append(shifted).append("CreationTime: ").append(Instant.ofEpochMilli(task.getCreateTime())).append("\n");
		sb.append(shifted).append("StartTime: ").append(Instant.ofEpochMilli(task.getStartTime())).append("\n");
		sb.append(shifted).append("FinishTime: ").append(Instant.ofEpochMilli(task.getFinishTime())).append("\n");
		sb.append(shifted).append("State: ").append(task.getState()).append("\n");

		sb.append(shifted).append("InParameters: ").append("\n");
		if (task.getInParameters() != null) {
			for (Map.Entry<String, ResourceDTO> entry : task.getInParameters().entrySet()) {
				sb.append(shifted)
						.append(entry.getKey())
						.append(": ")
						.append(ShellOutputFormatter.format(entry.getValue())).append("\n");
			}
		}

		sb.append(shifted).append("OutParameters: ").append("\n");
		if (task.getOutParameters() != null) {
			for (ResourceDTO resource : task.getOutParameters()) {
				sb.append(shifted).append(ShellOutputFormatter.format(resource, shifted.length + 2)).append("\n");
			}
		}

		return sb.toString();
	}
}
