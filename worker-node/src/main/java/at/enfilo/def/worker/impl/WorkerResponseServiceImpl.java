package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.NodeResponseServiceImpl;
import at.enfilo.def.transfer.dto.NodeEnvironmentDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.rest.IWorkerResponseService;
import at.enfilo.def.worker.api.thrift.WorkerResponseService;

import java.util.List;

/**
 * Worker Response Service.
 */
public class WorkerResponseServiceImpl extends NodeResponseServiceImpl implements IWorkerResponseService {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(WorkerResponseServiceImpl.class);

	/**
	 * Public constructor.
	 */
	public WorkerResponseServiceImpl() {
		super(LOGGER);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getQueues(String ticket) {
		return getResult(ticket, List.class);
	}

	@Override
	public QueueInfoDTO getQueueInfo(String ticket) {
		return getResult(ticket, QueueInfoDTO.class);
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
