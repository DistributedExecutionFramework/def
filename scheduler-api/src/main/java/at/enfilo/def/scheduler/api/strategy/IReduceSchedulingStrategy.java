package at.enfilo.def.scheduler.api.strategy;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.node.api.NodeCommunicationException;
import at.enfilo.def.scheduler.api.ScheduleReduceException;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;

public interface IReduceSchedulingStrategy {
	/**
	 * Adds a new reduce job: Starts the reduce routine on each worker.
	 * @param jId - job id
	 * @param reduceRoutineId
	 */
	void addJob(String jId, String reduceRoutineId) throws ScheduleReduceException;

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
	List<ResourceDTO> finalizeReduce(String jId) throws ScheduleReduceException;

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
	void scheduleReduce(String jId, List<ResourceDTO> resources) throws ScheduleReduceException;

	/**
	 * Notification about new task state.
	 * @param nId - notification from worker node
	 * @param taskIds - list of task id's
	 * @param newState - new state
	 */
	void notifyTasksNewState(String nId, List<String> taskIds, ExecutionState newState);

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
	void deleteJob(String jId) throws UnknownJobException, ScheduleReduceException;
}
