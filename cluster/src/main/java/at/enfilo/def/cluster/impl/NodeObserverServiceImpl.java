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
	private final ClientRoutineWorkerController clientRoutineWorkerController;

	public NodeObserverServiceImpl() {
		this(
				TicketRegistry.getInstance(),
				ClusterExecLogicController.getInstance(),
				WorkerController.getInstance(),
				ReducerController.getInstance(),
				ClientRoutineWorkerController.getInstance()
		);
	}

	public NodeObserverServiceImpl(
			ITicketRegistry ticketRegistry,
			ClusterExecLogicController execLogicController,
			WorkerController workerController,
			ReducerController reducerController,
			ClientRoutineWorkerController clientRoutineWorkerController
	) {
		this.ticketRegistry = ticketRegistry;
		this.execLogicController = execLogicController;
		this.workerController = workerController;
		this.reducerController = reducerController;
		this.clientRoutineWorkerController = clientRoutineWorkerController;
	}

	@Override
	public String notifyElementsNewState(String nId, List<String> elementIds, ExecutionState newState) {
		ITicket ticket = ticketRegistry.createTicket(() -> {
			if (workerController.containsNode(nId)) {
				execLogicController.notifyTasksNewState(nId, elementIds, newState);
				workerController.notifyTasksNewState(nId, elementIds, newState);
			} else if (reducerController.containsNode(nId)) {
				reducerController.notifyJobsNewState(nId, elementIds, newState);
			} else if (clientRoutineWorkerController.containsNode(nId)) {
				execLogicController.notifyProgramsNewState(nId, elementIds, newState);
				clientRoutineWorkerController.notifyProgramsNewState(nId, elementIds, newState);
			}
		});
		return ticket.getId().toString();
	}

	@Override
	public String notifyTasksReceived(String nId, List<String> elementIds) {
		ITicket ticket = ticketRegistry.createTicket(() ->
				workerController.notifyTasksReceived(nId, elementIds)
		);
		return ticket.getId().toString();
	}

	@Override
	public String notifyProgramsReceived(String nId, List<String> programIds) {
		ITicket ticket = ticketRegistry.createTicket(() ->
				clientRoutineWorkerController.notifyProgramsReceived(nId, programIds)
		);
		return ticket.getId().toString();
	}

	@Override
	public String notifyReduceKeysReceived(String nId, String jId, List<String> reduceKeys) {
		ITicket ticket = ticketRegistry.createTicket(() ->
				reducerController.notifyReduceKeysReceived(nId, jId, reduceKeys)
		);
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
					} else if (clientRoutineWorkerController.containsNode(nId)) {
						clientRoutineWorkerController.notifyNodeInfo(nId, nodeInfo);
					}
				},
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}
}
