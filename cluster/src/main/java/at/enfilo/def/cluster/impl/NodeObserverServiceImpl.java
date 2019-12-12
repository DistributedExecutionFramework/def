package at.enfilo.def.cluster.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.node.observer.api.rest.INodeObserverService;
import at.enfilo.def.node.observer.api.thrift.NodeObserverService;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import java.util.List;

public class NodeObserverServiceImpl implements INodeObserverService, NodeObserverService.Iface {

	private final ITicketRegistry ticketRegistry;
	private final ClusterExecLogicController execLogicController;
	private final WorkerController workerController;
	private final ReducerController reducerController;

	public NodeObserverServiceImpl() {
		this(
				TicketRegistry.getInstance(),
				ClusterExecLogicController.getInstance(),
				WorkerController.getInstance(),
				ReducerController.getInstance()
		);
	}

	public NodeObserverServiceImpl(
		ITicketRegistry ticketRegistry,
		ClusterExecLogicController execLogicController,
		WorkerController workerController,
		ReducerController reducerController
	) {
		this.ticketRegistry = ticketRegistry;
		this.execLogicController = execLogicController;
		this.workerController = workerController;
		this.reducerController = reducerController;
	}

	@Override
	public String notifyTasksNewState(String nId, List<String> taskIds, ExecutionState newState) {
		ITicket ticket = ticketRegistry.createTicket(() -> {
				execLogicController.notifyTasksNewState(nId, taskIds, newState);
				workerController.notifyTasksNewState(nId, taskIds, newState);
		});
		return ticket.getId().toString();
	}

	@Override
	public String notifyTasksReceived(String nId, List<String> taskIds) {
		ITicket ticket = ticketRegistry.createTicket(() -> workerController.notifyTasksReceived(nId, taskIds));
		return ticket.getId().toString();
	}

	@Override
	public String notifyNodeInfo(String nId, NodeInfoDTO nodeInfo) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> {
					if (workerController.containsNode(nId)) {
						workerController.notifyNodeInfo(nId, nodeInfo);
					} else if (reducerController.containsNode(nId)) {
						reducerController.notifyNodeInfo(nId, nodeInfo);
					}
				},
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}
}
