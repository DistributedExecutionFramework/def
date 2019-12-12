package at.enfilo.def.execlogic.impl;

import at.enfilo.def.common.api.ITimeoutMap;
import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.execlogic.api.rest.IExecLogicService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicService;
import at.enfilo.def.transfer.dto.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service implementation of ExecLogicService Interface/Facade.
 *
 */
public class ExecLogicServiceImpl implements IExecLogicService, ExecLogicService.Iface {

	private final ITicketRegistry ticketRegistry;
	private final IExecLogicController execLogicController;
	private final ITimeoutMap<String, ITicket> lastTasks; // <jobId, TaskCreateTicket>

	public ExecLogicServiceImpl(IExecLogicController execLogicController) {
		this.ticketRegistry = TicketRegistry.getInstance();
		this.execLogicController = execLogicController;
		this.lastTasks = new TimeoutMap<>(
				30, TimeUnit.SECONDS,
				30/2, TimeUnit.SECONDS
		);
	}

	@Override
	public String getAllPrograms(String userId) {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				() -> execLogicController.getAllPrograms(userId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String createProgram(String cId, String uId) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> execLogicController.createProgram(cId, uId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getProgram(String pId) {
		ITicket ticket = ticketRegistry.createTicket(
				ProgramDTO.class,
				() -> execLogicController.getProgram(pId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String deleteProgram(String pId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.deleteProgram(pId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String abortProgram(String pId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.abortProgram(pId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String updateProgramName(String pId, String name) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.updateProgramName(pId, name)
		);
		return ticket.getId().toString();
	}

	@Override
	public String updateProgramDescription(String pId, String description) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.updateProgramDescription(pId, description)
		);
		return ticket.getId().toString();
	}

	@Override
	public String startClientRoutine(String pId, String crId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.startClientRoutine(pId, crId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String markProgramAsFinished(String pId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.markProgramAsFinished(pId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String getAllJobs(String pId) {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				() -> execLogicController.getAllJobs(pId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String createJob(String pId) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> execLogicController.createJob(pId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getJob(String pId, String jId) {
		ITicket ticket = ticketRegistry.createTicket(
				JobDTO.class,
				() -> execLogicController.getJob(pId, jId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String deleteJob(String pId, String jId) {
		lastTasks.remove(jId);
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.deleteJob(pId, jId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getAttachedMapRoutine(String pId, String jId) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> execLogicController.getAttachedMapRoutine(pId, jId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String attachMapRoutine(String pId, String jId, String mapRoutineId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.attachMapRoutine(pId, jId, mapRoutineId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String getAttachedReduceRoutine(String pId, String jId) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> execLogicController.getAttachedReduceRoutine(pId, jId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String attachReduceRoutine(String pId, String jId, String reduceRoutineId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.attachReduceRoutine(pId, jId, reduceRoutineId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String getAllTasks(String pId, String jId, SortingCriterion sortingCriterion) {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				() -> execLogicController.getAllTasks(pId, jId, sortingCriterion)
		);
		return ticket.getId().toString();
	}

	@Override
	public String getAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion) {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				() -> execLogicController.getAllTasksWithState(pId, jId, state, sortingCriterion)
		);
		return ticket.getId().toString();
	}

	@Override
	public String createTask(String pId, String jId, RoutineInstanceDTO objectiveRoutine) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> execLogicController.createTask(pId, jId, objectiveRoutine)
		);
		lastTasks.put(jId, ticket);
		return ticket.getId().toString();
	}

	@Override
	public String getTask(String pId, String jId, String tId) {
		ITicket ticket = ticketRegistry.createTicket(
				TaskDTO.class,
				() -> execLogicController.getTask(pId, jId, tId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getTaskPartial(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters) {
		ITicket ticket = ticketRegistry.createTicket(
				TaskDTO.class,
				() -> execLogicController.getTaskPartial(pId, jId, tId, includeInParameters, includeOutParameters),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String markJobAsComplete(String pId, String jId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> {
					if (lastTasks.containsKey(jId)) {
						lastTasks.get(jId).waitForComplete();
					}
					execLogicController.markJobAsComplete(pId, jId);
				},
				ITicket.LOWER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String abortJob(String pId, String jId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.abortJob(pId, jId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String abortTask(String pId, String jId, String tId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.abortTask(pId, jId, tId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String reRunTask(String pId, String jId, String tId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.reRunTask(pId, jId, tId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getAllSharedResources(String pId) {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				() -> execLogicController.getAllSharedResources(pId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String createSharedResource(String pId, String dataTypeId, ByteBuffer data) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> execLogicController.createSharedResource(pId, dataTypeId, data),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getSharedResource(String pId, String rId) {
		ITicket ticket = ticketRegistry.createTicket(
				ResourceDTO.class,
				() -> execLogicController.getSharedResource(pId, rId),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String deleteSharedResource(String pId, String rId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> execLogicController.deleteSharedResource(pId, rId)
		);
		return ticket.getId().toString();
	}
}
