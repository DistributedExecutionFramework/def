package at.enfilo.def.worker.queue;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.queue.Queue;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.impl.WorkerServiceController;

import java.util.Set;

public class TaskQueue extends Queue<TaskDTO> {

	public TaskQueue(String qId) {
		super(qId, WorkerServiceController.DTO_TASK_CACHE_CONTEXT, TaskDTO.class);
	}

	@Override
	protected String getElementId(TaskDTO element) {
		return element.getId();
	}

	@Override
	protected Set<ITuple<ContextIndicator, ?>> getLoggingContext(String eId) {
		return DEFLoggerFactory.createTaskContext(eId);
	}
}
