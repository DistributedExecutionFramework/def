package at.enfilo.def.scheduler.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.scheduler.api.rest.ISchedulerService;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.util.Collections;
import java.util.List;

/**
 * Service implementation of ISchedulerService (REST) and SchedulerService.Iface (Thrift)
 * This service includes tasks and reduce scheduling.
 */
public class SchedulerServiceImpl implements ISchedulerService {

	private final ITicketRegistry ticketRegistry;
	private final SchedulingController controller;

	public SchedulerServiceImpl() {
		this(SchedulingController.getInstance());
	}

	public SchedulerServiceImpl(SchedulingController controller) {
		this.controller = controller;
		this.ticketRegistry = TicketRegistry.getInstance();
	}

	@Override
	public String addJob(String jId) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.addJob(jId));
		return ticket.getId().toString();
	}

	@Override
	public String extendToReduceJob(String jId, String reduceRoutineId) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.extendToReduceJob(jId, reduceRoutineId));
		return ticket.getId().toString();
	}

	@Override
	public String scheduleTask(String jId, TaskDTO task) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.scheduleTask(jId, Collections.singletonList(task)));
		return ticket.getId().toString();
	}

	@Override
	public String scheduleReduce(String jId, List<ResourceDTO> resources) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.scheduleReduce(jId, resources));
		return ticket.getId().toString();
	}

	@Override
	public String markJobAsComplete(String jId) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.markJobAsComplete(jId));
		return ticket.getId().toString();
	}

	@Override
	public String removeJob(String jId) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.removeJob(jId));
		return ticket.getId().toString();
	}

	@Override
	public String finalizeReduce(String jId) {
		ITicket ticket = ticketRegistry.createTicket(List.class, () -> controller.finalizeReduce(jId));
		return ticket.getId().toString();
	}

	@Override
	public String addWorker(String nId, ServiceEndpointDTO endpoint) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.addWorker(nId, endpoint));
		return ticket.getId().toString();
	}

	@Override
	public String removeWorker(String nId) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.removeWorker(nId));
		return ticket.getId().toString();
	}

	@Override
	public String addReducer(String nId, ServiceEndpointDTO endpoint) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.addReducer(nId, endpoint));
		return ticket.getId().toString();
	}

	@Override
	public String removeReducer(String nId) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.removeReducer(nId));
		return ticket.getId().toString();
	}
}
