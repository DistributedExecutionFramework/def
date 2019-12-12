package at.enfilo.def.library.api.client.factory;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.thrift.LibraryResponseService;
import at.enfilo.def.library.api.thrift.LibraryService;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.LibraryInfoDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;

import java.util.List;
import java.util.concurrent.Future;

class LibraryServiceClient<T extends LibraryService.Iface, R extends LibraryResponseService.Iface>
implements ILibraryServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketClient;

	/**
	 * Constructor used by ClusterServiceClientFactory.
	 *
	 * @param requestClient
	 * @param responseClient
	 * @param ticketClient
	 */
	LibraryServiceClient(
		IClient<T> requestClient,
		IClient<R> responseClient,
		ITicketServiceClient ticketClient
	) {
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
	}

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
	public Future<RoutineBinaryDTO> getRoutineBinary(String rbId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getRoutineBinary(rbId));
		return new TicketFutureBuilder<R, RoutineBinaryDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getRoutineBinary)
				.via(responseClient);
	}

	@Override
	public Future<Void> setDataEndpoint(ServiceEndpointDTO dataEndpoint) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.setDataEndpoint(dataEndpoint));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public void close() {
		requestClient.close();
		responseClient.close();
		ticketClient.close();
	}
}
