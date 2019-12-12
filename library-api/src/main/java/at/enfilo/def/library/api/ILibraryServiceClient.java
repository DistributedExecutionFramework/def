package at.enfilo.def.library.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.LibraryInfoDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;

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
	 * Returns the requested Routine required features.
	 *
	 * @param rId routine id.
	 * @return requested Routine features.
	 */
	Future<List<FeatureDTO>> getRoutineRequiredFeatures(String rId)
			throws ClientCommunicationException;

	/**
	 * Sets the data endpoint for pulling routines
	 */
	Future<Void> setDataEndpoint(ServiceEndpointDTO dataEndpoint) throws ClientCommunicationException;
}
