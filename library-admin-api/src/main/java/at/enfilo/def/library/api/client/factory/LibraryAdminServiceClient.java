package at.enfilo.def.library.api.client.factory;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.api.thrift.LibraryAdminResponseService;
import at.enfilo.def.library.api.thrift.LibraryAdminService;
import at.enfilo.def.transfer.dto.*;

import java.util.List;
import java.util.concurrent.Future;

class LibraryAdminServiceClient<T extends LibraryAdminService.Iface, R extends LibraryAdminResponseService.Iface>
extends LibraryServiceClient<T, R> implements ILibraryAdminServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketClient;

	LibraryAdminServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient) {
		super(requestClient, responseClient, ticketClient);
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
	}

	@Override
	public Future<Void> setMasterLibrary(ServiceEndpointDTO masterLibraryEndpoint) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.setMasterLibrary(masterLibraryEndpoint));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<ServiceEndpointDTO> getMasterLibrary() throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getMasterLibrary());
		return new TicketFutureBuilder<R, ServiceEndpointDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getMasterLibrary)
				.via(responseClient);
	}

	@Override
	public Future<List<String>> findRoutines(String searchPattern) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.findRoutines(searchPattern));
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::findRoutines)
				.via(responseClient);
	}

	@Override
	public Future<Void> removeRoutine(String rId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.removeRoutine(rId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<String> createRoutine(RoutineDTO routineDTO)
    throws ClientCommunicationException {

		String ticketId = requestClient.execute(c -> c.createRoutine(routineDTO));
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createRoutine)
				.via(responseClient);
	}

	@Override
	public Future<String> updateRoutine(RoutineDTO routineDTO) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.updateRoutine(routineDTO));
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::updateRoutine)
				.via(responseClient);
	}

	@Override
	public Future<String> createRoutineBinary(String rId, String name, String md5, long sizeInBytes, boolean isPrimary) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.createRoutineBinary(rId, name, md5, sizeInBytes, isPrimary));
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createRoutineBinary)
				.via(responseClient);
	}

	@Override
	public Future<Void> removeRoutineBinary(String rId, String bId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.removeRoutineBinary(rId, bId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> uploadRoutineBinaryChunk(String rbId, RoutineBinaryChunkDTO chunk) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.uploadRoutineBinaryChunk(rbId, chunk));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Boolean> verifyRoutineBinary(String rbId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.verifyRoutineBinary(rbId));
		return new TicketFutureBuilder<R, Boolean>()
				.dataTicket(ticketId, ticketClient)
				.request(R::verifyRoutineBinary)
				.via(responseClient);
	}

	@Override
	public Future<List<String>> findDataTypes(String searchPattern) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.findDataTypes(searchPattern));
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::findDataTypes)
				.via(responseClient);
	}

	@Override
	public Future<String> createDataType(String name, String schema) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.createDataType(name, schema));
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createDataType)
				.via(responseClient);
	}

	@Override
	public Future<DataTypeDTO> getDataType(String dId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getDataType(dId));
		return new TicketFutureBuilder<R, DataTypeDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getDataType)
				.via(responseClient);
	}

	@Override
	public Future<Void> removeDataType(String dId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.removeDataType(dId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<List<TagDTO>> findTags(String searchPattern) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.findTags(searchPattern));
		return new TicketFutureBuilder<R, List<TagDTO>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::findTags)
				.via(responseClient);
	}

	@Override
	public Future<Void> createTag(String label, String description) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.createTag(label, description));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

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
	public Future<FeatureDTO> getFeatureByNameAndVersion(String name, String version) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getFeatureByNameAndVersion(name, version));
		return new TicketFutureBuilder<R, FeatureDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getFeatureByNameAndVersion)
				.via(responseClient);
	}

	@Override
	public void close() {
		requestClient.close();
		responseClient.close();
		ticketClient.close();
	}
}
