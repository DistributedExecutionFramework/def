package at.enfilo.def.scheduler.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Client interface for SchedulerService.
 */
public interface ISchedulerServiceClient extends IServiceClient {

	/**
	 * Adds a new Job to scheduler.
	 *
	 * @param jId - Job id
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> addJob(String jId) throws ClientCommunicationException;

	/**
	 * Extends a given job to a reduce job. Start a reduce job on each reducer.
	 *
	 * @param jId - job id
	 * @param reduceRoutineId - reduce routine id
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> extendToReduceJob(String jId, String reduceRoutineId) throws ClientCommunicationException;

	/**
	 * Schedule a task.
	 *
	 * @param jId - job id
	 * @param task - task to schedule
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> scheduleTask(String jId, TaskDTO task) throws ClientCommunicationException;

	/**
	 * Mark a job as complete. This means, that all tasks are created for the given job.
	 *
	 * @param jId - job id
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> markJobAsComplete(String jId) throws ClientCommunicationException;

	/**
	 * Remove a finished job or abort a running job.
	 * Aborting means, cancel all tasks on nodes too.
	 *
	 * @param jId - job id to abort
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> removeJob(String jId) throws ClientCommunicationException;

	/**
	 * Add a worker node for scheduling process.
	 *
	 * @param nId - id of worker node
	 * @param endpoint - service endpoint of node
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> addWorker(String nId, ServiceEndpointDTO endpoint) throws ClientCommunicationException;

	/**
	 * Removes a worker node from scheduling process.
	 *
	 * @param nId - node to remove
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> removeWorker(String nId) throws ClientCommunicationException;

	/**
	 * Add a reducer node for scheduling process.
	 *
	 * @param nId - id of reducer node
	 * @param endpoint - service endpoint of node
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> addReducer(String nId, ServiceEndpointDTO endpoint) throws ClientCommunicationException;

	/**
	 * Removes a reducer node from scheduling process.
	 *
	 * @param nId - node to remove
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> removeReducer(String nId) throws ClientCommunicationException;

	/**
	 * Schedule a list of resources to reduce.
	 *
	 * @param jId - job id
	 * @param resources - task to schedule
	 * @return
	 * @throws ClientCommunicationException
	 */
	Future<Void> scheduleResource(String jId, List<ResourceDTO> resources) throws ClientCommunicationException;

	/**
	 * Finalize a reduce.
	 *
	 * @param jId - job id
	 * @return reduced results.
	 */
	Future<List<ResourceDTO>> finalizeReduce(String jId) throws ClientCommunicationException;
}
