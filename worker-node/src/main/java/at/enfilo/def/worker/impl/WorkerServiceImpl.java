package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.node.impl.NodeServiceImpl;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.rest.IWorkerService;
import at.enfilo.def.worker.api.thrift.WorkerService;

import java.util.List;


/**
 * Implementation of {@link WorkerService.Iface} (Thrift) and {@link IWorkerService} (RESTful)
 */
public class WorkerServiceImpl extends NodeServiceImpl implements WorkerService.Iface, IWorkerService {

	private final WorkerServiceController controller;
	private final ITicketRegistry ticketRegistry;


	public WorkerServiceImpl() {
		this(WorkerServiceController.getInstance(), TicketRegistry.getInstance());
	}

	public WorkerServiceImpl(WorkerServiceController controller, ITicketRegistry ticketRegistry) {
		super(controller, ticketRegistry);
		this.controller = controller;
		this.ticketRegistry = ticketRegistry;
	}

	@Override
	public String getQueues() {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				controller::getQueues
		);
		return ticket.getId().toString();
	}

	@Override
	public String getQueueInfo(String qId) {
		ITicket ticket = ticketRegistry.createTicket(
				QueueInfoDTO.class,
				() -> controller.getQueueInfo(qId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String createQueue(String qId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.createQueue(qId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String pauseQueue(String qId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.pauseQueue(qId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String deleteQueue(String qId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.deleteQueue(qId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String releaseQueue(String qId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.releaseQueue(qId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String getQueuedTasks(String qId) {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				() -> controller.getQueuedTasks(qId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String queueTasks(String qId, List<TaskDTO> taskList) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.queueTasks(qId, taskList)
		);
		return ticket.getId().toString();
	}

	@Override
	public String moveTasks(
			String qId,
			List<String> taskIds,
			ServiceEndpointDTO targetNodeEndpoint
	) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.moveTasks(
						qId,
						taskIds,
						targetNodeEndpoint
				)
		);
		return ticket.getId().toString();
	}

	@Override
	public String moveAllTasks(ServiceEndpointDTO targetNodeEndpoint) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.moveAllTasks(targetNodeEndpoint),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String fetchFinishedTask(String tId) {
		ITicket ticket = ticketRegistry.createTicket(
				TaskDTO.class,
				() -> controller.fetchFinishedTask(tId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String abortTask(String tId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.abortTask(tId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getStoreRoutine() {
		ITicket ticket = ticketRegistry.createTicket(
			String.class,
			controller::getStoreRoutineId
		);
		return ticket.getId().toString();
	}

	@Override
	public String setStoreRoutine(String routineId) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.setStoreRoutineId(routineId));
		return ticket.getId().toString();
	}
}
