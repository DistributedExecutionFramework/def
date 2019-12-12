package at.enfilo.def.scheduler.reducer.api.strategy;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.node.api.exception.NodeCommunicationException;
import at.enfilo.def.scheduler.reducer.api.ReduceOperationException;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;

public interface IReduceSchedulingStrategy {
	/**
	 * Adds a new reduce job: Starts the reduce routine on each worker.
	 * @param job - job to add
	 */
	void addJob(JobDTO job) throws ReduceOperationException;

	/**
	 * Adds a new reducer to this scheduler.
	 * @param nId - reducer node id
	 * @param serviceEndpoint - service endpoint
	 */
	void addReducer(String nId, ServiceEndpointDTO serviceEndpoint) throws NodeCommunicationException;

	/**
	 * Finalize a reduce - this means all resources are added (see {@link IReduceSchedulingStrategy::scheduleReduce})
	 * @param jId - reduce job id
	 * @return
	 */
	JobDTO finalizeReduce(String jId) throws ReduceOperationException;

	/**
	 * Removes a reducer node.
	 * @param nId - reducer node id.
	 */
	void removeReducer(String nId);

	/**
	 * Schedules a list of resources to available reducer nodes.
	 * @param jId - job id
	 * @param resources - resource to scheduler or "reduce"
	 */
	void scheduleReduce(String jId, List<ResourceDTO> resources) throws ReduceOperationException;

	/**
	 * Returns a list of active jobs
	 * @return
	 */
	List<String> getJobs();

	/**
	 * Deletes and aborts the given reduce job.
	 * @param jId - job id to abort/delete.
	 * @throws UnknownJobException
	 */
	void deleteJob(String jId) throws UnknownJobException, ReduceOperationException;

	/**
	 * Notify about node state.
	 *
	 * @param nId - node id.
	 * @param nodeInfo - information (nr of tasks, load, etc) about node.
	 */
	void notifyNodeInfo(String nId, NodeInfoDTO nodeInfo);
}
