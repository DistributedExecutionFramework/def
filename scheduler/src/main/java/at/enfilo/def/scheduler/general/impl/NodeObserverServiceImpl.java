package at.enfilo.def.scheduler.general.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.node.observer.api.rest.INodeObserverService;
import at.enfilo.def.scheduler.reducer.api.strategy.IReduceSchedulingStrategy;
import at.enfilo.def.scheduler.worker.api.strategy.ITaskSchedulingStrategy;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import java.util.List;

public class NodeObserverServiceImpl implements INodeObserverService {

	private static final ITicketRegistry TICKET_REGISTRY = TicketRegistry.getInstance();

	private ITaskSchedulingStrategy taskSchedulingStrategy;
	private IReduceSchedulingStrategy reduceSchedulingStrategy;

	public NodeObserverServiceImpl(ITaskSchedulingStrategy taskSchedulingStrategy, IReduceSchedulingStrategy reduceSchedulingStrategy) {
	    this.taskSchedulingStrategy = taskSchedulingStrategy;
	    this.reduceSchedulingStrategy = reduceSchedulingStrategy;
	}

	@Override
	public String notifyElementsNewState(String nId, List<String> taskIds, ExecutionState newState) {
		// Notification not used by scheduler
		ITicket ticket = TICKET_REGISTRY.createTicket(() -> {});
		return ticket.getId().toString();
	}

	@Override
	public String notifyTasksReceived(String nId, List<String> taskIds) {
        // Notification not used by scheduler
		ITicket ticket = TICKET_REGISTRY.createTicket(() -> {});
		return ticket.getId().toString();
	}

	@Override
	public String notifyProgramsReceived(String nId, List<String> programIds) {
        // Notification not used by scheduler
		ITicket ticket = TICKET_REGISTRY.createTicket(() -> {});
		return ticket.getId().toString();
	}

	@Override
	public String notifyReduceKeysReceived(String nId, String jId, List<String> reduceKeys) {
        // Notification not used by scheduler
		ITicket ticket = TICKET_REGISTRY.createTicket(() -> {});
		return ticket.getId().toString();
	}

	@Override
	public String notifyNodeInfo(String nId, NodeInfoDTO nodeInfo) {
		ITicket ticket = TICKET_REGISTRY.createTicket(() -> {
		    taskSchedulingStrategy.notifyNodeInfo(nId, nodeInfo);
		    reduceSchedulingStrategy.notifyNodeInfo(nId, nodeInfo);
        });
		return ticket.getId().toString();
	}
}
