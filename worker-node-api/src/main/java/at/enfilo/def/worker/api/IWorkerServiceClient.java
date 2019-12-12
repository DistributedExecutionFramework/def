package at.enfilo.def.worker.api;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.node.api.INodeServiceClient;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.util.List;
import java.util.concurrent.Future;


/**
 * Worker Service Client Interface.
 */
public interface IWorkerServiceClient extends INodeServiceClient {

	/**
	 * Request all queued TaskIDs.
	 * @param qId - queue id
	 */
	Future<List<String>> getQueuedTasks(String qId) throws ClientCommunicationException;

	/**
	 * Queue a task.
	 * @param qId - queue id
	 * @param taskList - tasks to queue
	 */
	Future<Void> queueTasks(
			String qId,
			List<TaskDTO> taskList
	) throws ClientCommunicationException;

	/**
	 * Move a task to another node.
	 * @param queueId - queue
	 * @param taskIds - tasks to move
	 * @param targetNodeEndpoint - receiver endpoint
	 */
	Future<Void> moveTasks(
			String queueId,
			List<String> taskIds,
			ServiceEndpointDTO targetNodeEndpoint
	) throws ClientCommunicationException;

	/**
	 * Move all tasks to another node.
	 * @param targetNodeEndpoint - receiver endpoint
	 */
	Future<Void> moveAllTasks(ServiceEndpointDTO targetNodeEndpoint) throws ClientCommunicationException;

	/**
	 * Fetch (and removes) a finished task from this node.
	 * @param tId - task id
	 */
	Future<TaskDTO> fetchFinishedTask(String tId) throws ClientCommunicationException;

	/**
	 * Abort the given task.
	 * @param tId - task to abort.
	 */
	Future<Void> abortTask(String tId) throws ClientCommunicationException;
}
