package at.enfilo.def.reducer.api;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.node.api.INodeServiceClient;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;
import java.util.concurrent.Future;


/**
 * Reducer Client Interface.
 */
public interface IReducerServiceClient extends INodeServiceClient {
	/**
	 * Create a new ReduceJob with given jId (reduce job id).
	 * @param jId - reduce job id
	 * @param routineId - reduce routine
	 * @return TicketStatusDTO as {@code Future}.
	 * @throws ClientCommunicationException
	 */
	Future<Void> createReduceJob(String jId, String routineId) throws ClientCommunicationException;

	/**
	 * Add resources to reduce job.
	 * @param jId - reduce job id.
	 * @param resources - resources to add
	 * @return TicketStatusDTO as {@code Future}.
	 * @throws ClientCommunicationException
	 */
	Future<Void> add(String jId, List<ResourceDTO> resources) throws ClientCommunicationException;

	/**
	 * Do reduce: reduce all added task to a single result (resource).
	 * @param jId - reduce job id.
	 * @return TicketStatusDTO as {@code Future}.
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<Void> reduce(String jId) throws ClientCommunicationException;

	/**
	 * Fetch reduced result.
	 * @param jId - reduce job id.
	 * @return ResourceDTO as {@code Future}.
	 * @throws ClientCommunicationException
	 */
	Future<List<ResourceDTO>> fetchResult(String jId) throws ClientCommunicationException;

	/**
	 * Removes/Abort a reduce job.
	 * @param jId - reduce job to abort
	 * @return
	 */
	Future<Void> deleteReduceJob(String jId) throws ClientCommunicationException;
}
