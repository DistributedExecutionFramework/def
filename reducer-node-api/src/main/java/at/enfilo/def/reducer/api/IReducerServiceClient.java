package at.enfilo.def.reducer.api;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.node.api.INodeServiceClient;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;
import java.util.concurrent.Future;


/**
 * Reducer Client Interface.
 */
public interface IReducerServiceClient extends INodeServiceClient {

	/**
	 * Fetches all queued jobs of a program with a given id
	 * @param pId	- program id
	 * @return	List with all reducejob ids
	 * @throws ClientCommunicationException
	 */
	Future<List<String>> getQueuedJobs(String pId) throws ClientCommunicationException;

	/**
	 * Create a new ReduceJob with given jId (reduce reducejob id).
	 * @param job - reducejob to create a reduce reducejob for
	 * @return TicketStatusDTO as {@code Future}.
	 * @throws ClientCommunicationException
	 */
	Future<Void> createReduceJob(JobDTO job) throws ClientCommunicationException;

	/**
	 * Removes/Abort a reduce reducejob.
	 * @param jId - reduce reducejob to abort
	 * @return
	 */
	Future<Void> abortReduceJob(String jId) throws ClientCommunicationException;

	/**
	 * Add resources to reduce reducejob.
	 * @param jId - reduce reducejob id.
	 * @param resources - resources to add
	 * @return TicketStatusDTO as {@code Future}.
	 * @throws ClientCommunicationException
	 */
	Future<Void> addResourcesToReduce(String jId, List<ResourceDTO> resources) throws ClientCommunicationException;

	/**
	 * Do reduce: reduce all added task to a single result (resource).
	 * @param jId - reduce reducejob id.
	 * @return TicketStatusDTO as {@code Future}.
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<Void> reduceJob(String jId) throws ClientCommunicationException;

	/**
	 * Fetch reduced result.
	 * @param jId - ReduceJob id.
	 * @return ResourceDTO as {@code Future}.
	 * @throws ClientCommunicationException
	 */
	Future<List<ResourceDTO>> fetchResults(String jId) throws ClientCommunicationException;
}
