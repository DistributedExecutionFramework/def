package at.enfilo.def.node.observer.api.client;


import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Node Observer Service Client interface.
 */
public interface INodeObserverServiceClient extends IServiceClient {

	/**
	 * Notification that a list of tasks reached a new execution state.
	 *
	 * @param nId - node id
	 * @param elementIds - list of tasks finished by node (id)
	 * @param newState - new execution state
	 */
	Future<Void> notifyElementsNewState(String nId, List<String> elementIds, ExecutionState newState)
	throws ClientCommunicationException;

	/**
	 * Notification that a node received a list of tasks.
	 *
	 * @param nId - node id
	 * @param taskIds - list of task received by node (id)
	 */
	Future<Void> notifyTasksReceived(String nId, List<String> taskIds)
	throws ClientCommunicationException;

	/**
	 * Notification that a node received a list of programs.
	 *
	 * @param nId - node id
	 * @param programIds - list of programs received by node (id)
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> notifyProgramsReceived(String nId, List<String> programIds)
	throws ClientCommunicationException;

	/**
	 * Notification that a node received a list of resources with reduce keys.
	 *
	 * @param nId - node id
	 * @param reduceKeys - list of all reduce keys the received resources have
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> notifyReduceKeysReceived(String nId, String jId, List<String> reduceKeys)
	throws ClientCommunicationException;

	/**
	 * Notification over Node state.
	 *
	 * @param nId - node id
	 * @param nodeInfo - node information
	 */
	Future<Void> notifyNodeInfo(String nId, NodeInfoDTO nodeInfo)
	throws ClientCommunicationException;
}
