package at.enfilo.def.worker.impl;

import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.NodeResponseServiceImpl;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.rest.IWorkerResponseService;
import at.enfilo.def.worker.api.thrift.WorkerResponseService;

import java.util.List;

/**
 * Worker Response Service.
 */
public class WorkerResponseServiceImpl extends NodeResponseServiceImpl
implements WorkerResponseService.Iface, IWorkerResponseService {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(WorkerResponseServiceImpl.class);

	/**
	 * Public constructor.
	 */
	public WorkerResponseServiceImpl() {
		super(LOGGER);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getQueuedTasks(String ticket) {
		return getResult(ticket, List.class);
	}

	@Override
	public TaskDTO fetchFinishedTask(String ticket) {
		return getResult(ticket, TaskDTO.class);
	}

	@Override
	public String getStoreRoutine(String ticketId) {
		return getResult(ticketId, String.class);
	}
}
