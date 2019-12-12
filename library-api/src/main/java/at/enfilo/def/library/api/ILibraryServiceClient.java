package at.enfilo.def.library.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.*;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Library Service Client interface.
 */
public interface ILibraryServiceClient extends IServiceClient {

	/**
	 * Returns library info.
	 *
	 * @return requested LibraryInfo.
	 */
	Future<LibraryInfoDTO> getInfo() throws ClientCommunicationException;

	/**
	 * Returns the requested Routine object.
	 *
	 * @param rId routine id.
	 * @return requested Routine.
	 */
	Future<RoutineDTO> getRoutine(String rId) throws ClientCommunicationException;

	/**
	 * Returns the requested RoutineBinary object.
	 *
	 * @param rbId routine binary id.
	 * @return future of requested RoutineBinary.
	 */
	Future<RoutineBinaryDTO> getRoutineBinary(String rbId) throws ClientCommunicationException;

	/**
	 * Returns request RoutineBinary chunk.
	 * @param rbId routine binary id
	 * @param chunk chunk #
	 * @param chunkSize chunk size
	 * @return chunk
	 */
	Future<RoutineBinaryChunkDTO> getRoutineBinaryChunk(String rbId, short chunk, int chunkSize) throws ClientCommunicationException;

	/**
	 * Returns the requested Routine required features.

	/**
	 * Returns the requested Routine required features.
	 *
	 * @param rId routine id.
	 * @return requested Routine features.
	 */
	Future<List<FeatureDTO>> getRoutineRequiredFeatures(String rId)  throws ClientCommunicationException;

}
