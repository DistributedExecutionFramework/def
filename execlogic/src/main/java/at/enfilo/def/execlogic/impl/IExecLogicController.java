package at.enfilo.def.execlogic.impl;

import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.UnknownResourceException;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.*;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Delegation interface from {@link ExecLogicServiceImpl}.
 */
public interface IExecLogicController {

	/**
	 * Returns all programs stored by manager/cluster.
	 *
	 * @return list of program ids
	 * @throws ExecLogicException
	 */
	List<String> getAllPrograms(String userId) throws ExecLogicException;

	/**
	 * Creates a new program (on specified cluster) and returns the id of program.
	 *
	 * @param cId - cluster id to create program
	 * @param uId - user id of program owner
	 * @return id of program
	 */
	String createProgram(String cId, String uId) throws ExecLogicException;

	/**
	 * Returns program info for given program id.
	 *
	 * @param pId - requested program id
	 * @return program info instance
	 */
	ProgramDTO getProgram(String pId) throws ExecLogicException, UnknownProgramException;

	/**
	 * Delete program and all jobs, tasks and data belong to the given program id.
	 *
	 * @param pId - to delete
	 */
	void deleteProgram(String pId) throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException;

	/**
	 * Abort program and all jobs and tasks.
	 *
	 * @param pId - to delete
	 */
	void abortProgram(String pId) throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException;

	/**
	 * Updates the name of the program with the given id
	 *
	 * @param pId	the id of the program the name should be updated
	 * @param name	the new name of the program
	 * @throws ExecLogicException
	 * @throws UnknownProgramException
	 */
	void updateProgramName(String pId, String name) throws ExecLogicException, UnknownProgramException;

	/**
	 * Updates the description of the program with the given id
	 *
	 * @param pId	the id of the program the description should be updated
	 * @param description	the new description of the program
	 * @throws ExecLogicException
	 * @throws UnknownProgramException
	 */
	void updateProgramDescription(String pId, String description) throws ExecLogicException, UnknownProgramException;

	/**
	 * Attaches and starts a client routine
	 *
	 * @param pId - program id to attach client routine
	 * @param crId - id of the client routine to be started
	 * @throws ExecLogicException
	 */
	void startClientRoutine(String pId, String crId) throws ExecLogicException, UnknownProgramException;

	/**
	 * Mark Program as finished.
	 *
	 * @param pId - program id
	 */
	void markProgramAsFinished(String pId) throws ExecLogicException, UnknownProgramException;

	/**
	 * Returns all jobs from the requested Program.
	 *
	 * @param pId - program id
	 * @return list of job ids, sorted by creation timestamp
	 */
	List<String> getAllJobs(String pId) throws ExecLogicException, UnknownProgramException;

	/**
	 * Creates a new Job in the given Program and returns id of new job.
	 *
	 * @param pId - program id
	 * @return - id of new job
	 */
	String createJob(String pId) throws ExecLogicException, UnknownProgramException;

	/**
	 * Returns JobDTO for the requested job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return job info
	 */
	JobDTO getJob(String pId, String jId)
	throws ExecLogicException, UnknownProgramException, UnknownJobException;

	/**
	 * Deletes a job and all tasks and data.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 */
	void deleteJob(String pId, String jId)
	throws ExecLogicException, UnknownProgramException, UnknownJobException;

	/**
	 * Returns associated map routine id of requested job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return map routine id
	 */
	String getAttachedMapRoutine(String pId, String jId)
	throws ExecLogicException, UnknownProgramException, UnknownJobException;

	/**
	 * Set mapRoutineId to the given job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param mapRoutineId - mapRoutineId to attach
	 */
	void attachMapRoutine(String pId, String jId, String mapRoutineId)
	throws ExecLogicException, UnknownProgramException, UnknownJobException;

	/**
	 * Returns attached reduce routine id.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return reduce routine id
	 */
	String getAttachedReduceRoutine(String pId, String jId)
	throws ExecLogicException, UnknownProgramException, UnknownJobException;

	/**
	 * Attach reduce routine to the given job.
	 *
	 * @param pId - program id
	 * @param jId - job id to attach reduce routine
	 * @param reduceRoutineId - reduce routine
	 */
	void attachReduceRoutine(String pId, String jId, String reduceRoutineId)
	throws ExecLogicException, UnknownProgramException, UnknownJobException;

	/**
	 * Returns a list of all task ids for the given program and job.
	 *
	 * @param pId	- program id
	 * @param jId	- job id
	 * @return		- list of task ids sorted by creation timestamp
	 * @throws ExecLogicException
	 * @throws UnknownProgramException
	 * @throws UnknownJobException
	 */
	List<String> getAllTasks(String pId, String jId, SortingCriterion sortingCriterion)
		throws ExecLogicException, UnknownProgramException, UnknownJobException;

	/**
	 * Returns a list of task ids for the given program and job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param state - execution state of requested tasks
	 * @return list of task ids sorted by creation timestamp
	 */
	List<String> getAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion)
	throws ExecLogicException, UnknownProgramException, UnknownJobException;

	/**
	 * Creates a new Task in the given program and job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param objectiveRoutine - objective routine id -> bound to task incl. parameters
	 * @return new task id
	 */
	String createTask(String pId, String jId, RoutineInstanceDTO objectiveRoutine)
	throws ExecLogicException, UnknownProgramException, UnknownJobException;

	/**
	 * Returns task info for given task id.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param tId - task id
	 * @return - task object
	 */
	TaskDTO getTask(String pId, String jId, String tId)
	throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException;

	/**
	 * Returns (partial) task info for given task id.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param tId - task id
	 * @param includeInParameters - include input parameters / data in return value
	 * @param includeInParameters - include output parameters / data in return value
	 * @return - task object
	 */
	TaskDTO getTaskPartial(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters)
	throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException;


	/**
	 * Marks the given job as complete. This means all tasks are created.
	 *
	 * @param pId - program id
	 * @param jId - task id
	 */
	void markJobAsComplete(String pId, String jId)
	throws ExecLogicException, UnknownProgramException, UnknownJobException;


	/**
	 * Abort the given job: set all unfinished task to failed, set job to failed,
	 * stop execution of scheduled tasks.
	 * @param pId - program id
	 * @param jId - job id to abort
	 */
	void abortJob(String pId, String jId)
	throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException;

	/**
	 * Returns a list of shared resources associated to given program.
	 * @param pId - program id
	 * @return list of shared resource ids
	 */
	List<String> getAllSharedResources(String pId) throws ExecLogicException, UnknownProgramException;

	/**
	 * Creates a new shared resource in the given program.
	 *
	 * @param pId - program id
	 * @param dataTypeId - datatype of shared resource
	 * @param data - shared resource payload
	 * @return shared resource if
	 */
	String createSharedResource(String pId, String dataTypeId, ByteBuffer data) throws ExecLogicException, UnknownProgramException;

	/**
	 * Returns shared resource incl. data.
	 *
	 * @param pId - program id
	 * @param rId - shared resource id
	 * @return shared resource
	 */
	ResourceDTO getSharedResource(String pId, String rId)
	throws ExecLogicException, UnknownProgramException, UnknownResourceException;

	/**
	 * Delete and cleanup the requested shared resource.
	 * @param pId - program id
	 * @param rId - shared resource id
	 */
	void deleteSharedResource(String pId, String rId)
	throws ExecLogicException, UnknownProgramException, UnknownResourceException;

	/**
	 * Abort the given Task (tId).
	 * @param pId - program id
	 * @param jId - job id
	 * @param tId - task id
	 */
	void abortTask(String pId, String jId, String tId) throws UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException;

	/**
	 * Re-run the given Task (tId). Task must be in state SUCCESS or FAILED.
	 * @param pId - program id
	 * @param jId - job id
	 * @param tId - task id
	 */
	void reRunTask(String pId, String jId, String tId)
	throws UnknownProgramException, ExecLogicException, UnknownJobException, UnknownTaskException, Exception;

}
