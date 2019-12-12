package at.enfilo.def.scheduler.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.node.observer.api.rest.INodeObserverService;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import java.util.List;

public class NodeObserverServiceImpl implements INodeObserverService {

	private static final ITicketRegistry TICKET_REGISTRY = TicketRegistry.getInstance();

	private final SchedulingController controller;

	public NodeObserverServiceImpl() {
		this(SchedulingController.getInstance());
	}

	public NodeObserverServiceImpl(SchedulingController controller) {
		this.controller = controller;
	}

	@Override
	public String notifyTasksNewState(String nId, List<String> taskIds, ExecutionState newState) {
		ITicket ticket = TICKET_REGISTRY.createTicket(() -> controller.notifyTasksNewState(nId, taskIds, newState));
		return ticket.getId().toString();
	}

	@Override
	public String notifyTasksReceived(String nId, List<String> taskIds) {
		// Notification not used by Scheduler
		ITicket ticket = TICKET_REGISTRY.createTicket(() -> {});
		return ticket.getId().toString();
	}

	@Override
	public String notifyNodeInfo(String nId, NodeInfoDTO nodeInfo) {
		ITicket ticket = TICKET_REGISTRY.createTicket(() -> controller.notifyNodeInfo(nId, nodeInfo));
		return ticket.getId().toString();
	}
}
