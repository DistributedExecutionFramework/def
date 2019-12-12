package at.enfilo.def.library.api.client.factory;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.api.thrift.LibraryAdminResponseService;
import at.enfilo.def.library.api.thrift.LibraryAdminService;
import at.enfilo.def.library.api.thrift.LibraryService;
import at.enfilo.def.transfer.dto.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Future;

class LibraryAdminServiceClient<T extends LibraryAdminService.Iface, R extends LibraryAdminResponseService.Iface>
extends LibraryServiceClient<T, R>
implements ILibraryAdminServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketClient;

	LibraryAdminServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient) {
		super(requestClient, responseClient, ticketClient);
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
	}

	/**
	 * Returns service endpoint of this client.
	 *
	 * @return ServiceEndpoint instance
	 */
	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return requestClient.getServiceEndpoint();
	}

    @Override
    public Future<LibraryInfoDTO> getInfo() throws ClientCommunicationException {
        String ticketId = requestClient.execute(LibraryService.Iface::getLibraryInfo);
        return new TicketFutureBuilder<R, LibraryInfoDTO>()
                .dataTicket(ticketId, ticketClient)
                .request(R::getLibraryInfo)
                .via(responseClient);
    }

    @Override
    public Future<RoutineDTO> getRoutine(String rId) throws ClientCommunicationException {
        String ticketId = requestClient.execute(t -> t.getRoutine(rId));
        return new TicketFutureBuilder<R, RoutineDTO>()
                .dataTicket(ticketId, ticketClient)
                .request(R::getRoutine)
                .via(responseClient);
    }

	@Override
	public Future<List<FeatureDTO>> getRoutineRequiredFeatures(String rId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getRoutineRequiredFeatures(rId));
		return new TicketFutureBuilder<R, List<FeatureDTO>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getRoutineRequiredFeatures)
				.via(responseClient);
	}

	@Override
	public Future<Void> setDataEndpoint(ServiceEndpointDTO dataEndpoint) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.setDataEndpoint(dataEndpoint));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	/**
	 * Find all Routines by a given searchPattern. If searchPattern is empty, all Routine (Id's) will be returned.
	 *
	 * @param searchPattern - to find in name and description
	 * @return List of Routine Id's as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<List<String>> findRoutines(String searchPattern) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.findRoutines(searchPattern));
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::findRoutines)
				.via(responseClient);
	}

	/**
	 * Remove the given Routine from Library.
	 *
	 * @param rId - Routine to remove
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<Void> removeRoutine(String rId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.removeRoutine(rId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	/**
	 * Creates a new Routine.
	 *
	 * @param routineDTO - Routine to be created
	 * @return Id of Routine as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<String> createRoutine(RoutineDTO routineDTO)
    throws ClientCommunicationException {

		String ticketId = requestClient.execute(c -> c.createRoutine(routineDTO));
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createRoutine)
				.via(responseClient);
	}

	/**
	 * Update a specified Routine. An update creates a new Routine-Version.
	 *
     * @param routineDTO - Routine to be updated
	 * @return Id of new Routine-Version as Future.
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<String> updateRoutine(RoutineDTO routineDTO)
    throws ClientCommunicationException {

		String ticketId = requestClient.execute(c -> c.updateRoutine(routineDTO));
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::updateRoutine)
				.via(responseClient);
	}

	/**
	 * Uploads a binary to a specified Routine.
	 *
	 * @param rId         - Routine
	 * @param md5         - Checksum as md5
	 * @param sizeInBytes - Size in bytes
	 * @param isPrimary   - Is binary primary? (e.g. java -jar <primary.jar>)
	 * @param data        - Binary data
	 * @return RoutineBinary id as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<String> uploadRoutineBinary(
		String rId,
		String md5,
		long sizeInBytes,
		boolean isPrimary,
		ByteBuffer data
	)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.uploadRoutineBinary(
            rId,
            md5,
            sizeInBytes,
            isPrimary,
            data
        ));

		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)

				.request(R::uploadRoutineBinary)
				.via(responseClient);
	}

	/**
	 * Remove a specified RoutineBinary from Routine.
	 *
	 * @param rId       - Routine
	 * @param bId - RoutineBinary to remove
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<Void> removeRoutineBinary(String rId, String bId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.removeRoutineBinary(rId, bId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	/**
	 * Find all DataTypes which match the searchPattern. Empty searchPattern means all DataTypes.
	 *
	 * @param searchPattern - Search pattern to search
	 * @return List of matching DataType Id's as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<List<String>> findDataTypes(String searchPattern) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.findDataTypes(searchPattern));
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::findDataTypes)
				.via(responseClient);
	}

	/**
	 * Create a new DataType.
	 *
	 * @param name   - Name of DataType
	 * @param schema - Thrift Schema
	 * @return Id of new DataType as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<String> createDataType(String name, String schema) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.createDataType(name, schema));
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createDataType)
				.via(responseClient);
	}

	/**
	 * Returns requested DataType Object.
	 *
	 * @param dId - DataType Id
	 * @return DataType as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<DataTypeDTO> getDataType(String dId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getDataType(dId));
		return new TicketFutureBuilder<R, DataTypeDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getDataType)
				.via(responseClient);
	}

	/**
	 * Removes DataType from library.
	 *
	 * @param dId - DataType to remove
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<Void> removeDataType(String dId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.removeDataType(dId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	/**
	 * Find all Tags matching given searchPattern. Empty Search pattern means all Tags.
	 *
	 * @param searchPattern - Search pattern to search
	 * @return List of all matched Tags as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<List<TagDTO>> findTags(String searchPattern) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.findTags(searchPattern));
		return new TicketFutureBuilder<R, List<TagDTO>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::findTags)
				.via(responseClient);
	}

	/**
	 * Creates a new Tag.
	 *
	 * @param label - Label / Name of a Tag
	 * @param description - Description of a Tag
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<Void> createTag(String label, String description) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.createTag(label, description));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	/**
	 * Remove a Tag from library.
	 *
	 * @param name - Tag to remove
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	@Override
	public Future<Void> removeTag(String name) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.removeTag(name));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<String> createFeature(String name, String group, String version) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.createFeature(name, group, version));
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createFeature)
				.via(responseClient);
	}

	@Override
	public Future<String> addExtension(String featureId, String name, String version) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.addExtension(featureId, name, version));
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::addExtension)
				.via(responseClient);
	}

	@Override
	public Future<List<FeatureDTO>> getFeatures(String pattern) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getFeatures(pattern));
		return new TicketFutureBuilder<R, List<FeatureDTO>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getFeatures)
				.via(responseClient);
	}

	@Override
	public void close() {
		requestClient.close();
		responseClient.close();
		ticketClient.close();
	}
}
