package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.node.impl.NodeServiceImpl;
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
	public String getQueuedTasks(String qId) {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				() -> controller.getQueuedElements(qId)
		);
		return ticket.getId().toString();
	}

	@Override
	public String queueTasks(String qId, List<TaskDTO> taskList) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.queueElements(qId, taskList)
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
				() -> controller.moveElements(
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
				() -> controller.moveAllElements(targetNodeEndpoint),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String fetchFinishedTask(String tId) {
		ITicket ticket = ticketRegistry.createTicket(
				TaskDTO.class,
				() -> controller.fetchFinishedElement(tId)
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
}
