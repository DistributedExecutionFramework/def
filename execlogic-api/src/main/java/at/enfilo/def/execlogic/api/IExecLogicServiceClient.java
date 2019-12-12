package at.enfilo.def.execlogic.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Client interface for execution domain.
 */
public interface IExecLogicServiceClient extends IServiceClient {

	/**
	 * Request all programs stored on Manager/Cluster, which given user id has access
	 *
	 * @param userId - user id
	 * @return list of program ids.
	 * @throws ClientCommunicationException
	 */
	Future<List<String>> getAllPrograms(String userId) throws ClientCommunicationException;

	/**
	 * Request the creation of a new Program on a specified Cluster (cId).
	 *
	 * @param cId - cluster id
	 * @return Future - program id
	 */
	Future<String> createProgram(String cId, String uId) throws ClientCommunicationException;

	/**
	 * Returns info about Program (pId).
	 *
	 * @param pId - program id
	 * @return Future - {@link ProgramDTO}
	 */
	Future<ProgramDTO> getProgram(String pId) throws ClientCommunicationException;

	/**
	 * Delete existing Program (pId).
	 *
	 * @param pId - program id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> deleteProgram(String pId) throws ClientCommunicationException;

	/**
	 * Aborts running Program (pId).
	 * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
	 *
	 * @param pId - program id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> abortProgram(String pId) throws ClientCommunicationException;

	/**
	 * Updates the name of the program with the given id
	 *
	 * @param pId	the id of the program the name should be updated
	 * @param name	the new name of the program
	 * @return Future {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> updateProgramName(String pId, String name) throws ClientCommunicationException;

	/**
	 * Updates the description of the program with the given id
	 *
	 * @param pId	the id of the program the description should be updated
	 * @param description	the new description of the program
	 * @return Future {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> updateProgramDescription(String pId, String description) throws ClientCommunicationException;

	/**
	 * Mark Program (pId) as finished.
	 *
	 * @param pId - program id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> markProgramAsFinished(String pId) throws ClientCommunicationException;

	/**
	 * Request the list of Jobs (jId) for a given Program (pId).
	 *
	 * @param pId - program id
	 * @return Future - list of job ids
	 */
	Future<List<String>> getAllJobs(String pId) throws ClientCommunicationException;

	/**
	 * Request the creation of a new Job on a specified Program (pId).
	 *
	 * @param pId - program id
	 * @return Future - job id
	 */
	Future<String> createJob(String pId) throws ClientCommunicationException;

	/**
	 * Requests info about specific Job given by Program Id (pId) and Job Id (jId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return Future - {@link JobDTO} instance
	 */
	Future<JobDTO> getJob(String pId, String jId) throws ClientCommunicationException;

	/**
	 * Requests deletion of a specific Job given by Program Id (pId) and Job Id (jId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> deleteJob(String pId, String jId) throws ClientCommunicationException;

	/**
	 * Request the MapRoutine which is attached to the specified Job by Program Id (pId) and Job Id (jId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return Future - map routine id
	 */
	Future<String> getAttachedMapRoutine(String pId, String jId) throws ClientCommunicationException;

	/**
	 * Attaches a MapRoutine (mapRoutineId) to an Job (specified by pId and jId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param mapRoutineId
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> attachMapRoutine(String pId, String jId, String mapRoutineId) throws ClientCommunicationException;

	/**
	 * Returns attached ReduceRoutine (rlId) of the requested Program (pId) and Job (jId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return Future - reduce routine id
	 */
	Future<String> getAttachedReduceRoutine(String pId, String jId) throws ClientCommunicationException;

	/**
	 * Attaches a ReduceRoutine (rlId) to an Job (specified by pId and jId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param reduceRoutineId
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> attachReduceRoutine(String pId, String jId, String reduceRoutineId) throws ClientCommunicationException;

	/**
	 * Requests a list of all Tasks (tId) for a given Program (pId) and Job (jId).
	 *
	 * @param pId		- program id
	 * @param jId		- job id
	 * @return Future 	- list of task ids
	 * @throws ClientCommunicationException
	 */
	Future<List<String>> getAllTasks(String pId, String jId, SortingCriterion sortingCriterion) throws ClientCommunicationException;

	/**
	 * Requests a list of all Tasks (tId) with the given state for a given Program (pId) and Job (jId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return Future - list of task ids
	 */
	Future<List<String>> getAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion) throws ClientCommunicationException;

	/**
	 * Request the creation of a new Task on a specified Program (pId) and Job (jId) using
	 * given SimpleRoutine (routineId) and additional input-parameters.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param objectiveRoutine - Routine Instance with Parameters
	 * @return Future - task id
	 */
	Future<String> createTask(String pId, String jId, RoutineInstanceDTO objectiveRoutine) throws ClientCommunicationException;

	/**
	 * Returns info about Task by a combination of Program (pId), Job (jId) and Task (tId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param tId - task id
	 * @return Future - {@link TaskDTO} instance
	 */
	Future<TaskDTO> getTask(String pId, String jId, String tId) throws ClientCommunicationException;

	/**
	 * Returns info about Task by a combination of Program (pId), Job (jId) and Task (tId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param tId - task id
	 * @param includeInParameters - include input resources/parameters
	 * @param includeOutParameters - include output resources/parameters
	 * @return Future - {@link TaskDTO} instance
	 */
	Future<TaskDTO> getTask(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters) throws ClientCommunicationException;

	/**
	 * Mark the given Job (jId) on Program (pId) as complete. This means all Tasks are created.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> markJobAsComplete(String pId, String jId) throws ClientCommunicationException;

	/**
	 * Abort Job (jId) on Program (pId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> abortJob(String pId, String jId) throws ClientCommunicationException;

	/**
	 * Abort given Task (tId) on a specified Job (jId) and Program (pId).
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param tId - task id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> abortTask(String pId, String jId, String tId) throws ClientCommunicationException;

	/**
	 * Re-run a given Task (tId) on a specified Job (jId) and Program (pId).
	 * Task must be on state SUCCESS or FAILED.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param tId - task id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> reRunTask(String pId, String jId, String tId) throws ClientCommunicationException;

	/**
	 * Request a list of all SharedResources of a Program (pId).
	 *
	 * @param pId - program id
	 * @return Future - list of resource ids
	 */
	Future<List<String>> getAllSharedResources(String pId) throws ClientCommunicationException;

	/**
	 * Request the creation of a new SharedResource on a specified Program (pId) of a given DatyType (dataType).
	 *
	 * @param pId - program id
	 * @param dataTypeId - dataType id
	 * @param data - data
	 * @return Future - resource id
	 */
	Future<String> createSharedResource(String pId, String dataTypeId, ByteBuffer data) throws ClientCommunicationException;

	/**
	 * Request the SharedResource by a combination of Program (pId) and SharedResource (rId).
	 *
	 * @param pId - program id
	 * @param rId - resource id
	 * @return Future - {@link ResourceDTO} instance
	 */
	Future<ResourceDTO> getSharedResource(String pId, String rId) throws ClientCommunicationException;

	/**
	 * Request the deletion of a SharedResource by a combination of Program (pId) and SharedResource (rId).
	 *
	 * @param pId - program id
	 * @param rId - resource id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> deleteSharedResource(String pId, String rId) throws ClientCommunicationException;

	/**
	 * Wait for job is either {@link ExecutionState::SUCESS} or {@link ExecutionState::FAILED}
	 * @param jId
	 * @return
	 */
	JobDTO waitForJob(String pId, String jId) throws ClientCommunicationException, InterruptedException;
}
