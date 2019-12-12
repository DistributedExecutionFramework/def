package at.enfilo.def.execlogic.impl;

import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.execlogic.api.rest.IExecLogicResponseService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.util.List;

public class ExecLogicResponseServiceImpl extends ResponseService
implements IExecLogicResponseService, ExecLogicResponseService.Iface {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ExecLogicResponseServiceImpl.class);

	public ExecLogicResponseServiceImpl() {
		super(LOGGER);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllPrograms(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public String createProgram(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public ProgramDTO getProgram(String ticketId) {
		return getResult(ticketId, ProgramDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllJobs(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public String createJob(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public JobDTO getJob(String ticketId) {
		return getResult(ticketId, JobDTO.class);
	}

	@Override
	public String getAttachedMapRoutine(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public String getAttachedReduceRoutine(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllTasks(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllTasksWithState(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public String createTask(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public TaskDTO getTask(String ticketId) {
		return getResult(ticketId, TaskDTO.class);
	}

	@Override
	public TaskDTO getTaskPartial(String ticketId) {
		return getResult(ticketId, TaskDTO.class);
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllSharedResources(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public String createSharedResource(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public ResourceDTO getSharedResource(String ticketId) {
		return getResult(ticketId, ResourceDTO.class);
	}

}
