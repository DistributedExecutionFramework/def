package at.enfilo.def.scheduler.api.strategy;

import at.enfilo.def.node.api.NodeCommunicationException;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.scheduler.api.ScheduleTaskException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.util.Collection;
import java.util.List;

/**
 * Methods/Interface definition of a Scheduler.
 */
public interface ITaskSchedulingStrategy {

	/**
	 * Add (involve) node into scheduling process.
	 *
	 * @param wId - new worker node id.
	 * @param endpoint - ServiceEndpoint of new node.
	 */
	void addWorker(String wId, ServiceEndpointDTO endpoint) throws NodeCommunicationException;

	/**
	 * Removes a node from scheduling process.
	 *
	 * @param wId - worker to remove.
	 */
	void removeWorker(String wId);

	/**
	 * Notify that tasks {@code taskIds} reached a new state on node {@code nId}.
	 *
	 * @param wId - worker id.
	 * @param taskIds - tasks that were successfully executed (finished) ny node {@code nId}.
	 * @param newState - new state of tasks
	 */
	void notifyTasksNewState(String wId, List<String> taskIds, ExecutionState newState);

	/**
	 * Notify about node state.
	 *
	 * @param nId - node id.
	 * @param nodeInfo - information (nr of tasks, load, etc) about node.
	 */
	void notifyNodeInfo(String nId, NodeInfoDTO nodeInfo);

	/**
	 * Adds a job to this scheduler.
	 *
	 * @param jId - job to add.
	 */
	void addJob(String jId) throws NodeCommunicationException;

	/**
	 * Schedule given tasks.
	 *
	 * @param jId  - job id.
	 * @param tasks - tasks to schedule.
	 * @throws ScheduleTaskException
	 */
	void schedule(String jId, Collection<TaskDTO> tasks) throws ScheduleTaskException;

	/**
	 * Mark the given job as complete, this means all tasks are created for this job.
	 *
	 * @param jId - job id.
	 * @throws ScheduleTaskException
	 */
	void markJobAsComplete(String jId) throws ScheduleTaskException;

	/**
	 * Removes a finished job or abort execution of a running job.
	 *
	 * @param jId - job id to abort.
	 * @throws NodeCommunicationException
	 */
	void removeJob(String jId) throws NodeCommunicationException;

	/**
	 * Returns a list of active jobs.
	 * @return
	 */
	List<String> getJobs();
}