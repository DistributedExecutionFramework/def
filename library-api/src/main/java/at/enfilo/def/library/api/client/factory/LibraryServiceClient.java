package at.enfilo.def.library.api.client.factory;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.thrift.LibraryResponseService;
import at.enfilo.def.library.api.thrift.LibraryService;
import at.enfilo.def.transfer.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

class LibraryServiceClient<T extends LibraryService.Iface, R extends LibraryResponseService.Iface>
implements ILibraryServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketClient;
	private final Map<String, Future<RoutineDTO>> routineDtoCache;
	private final Map<String, Future<List<FeatureDTO>>> routineFeatureDtoCache;

	LibraryServiceClient(
		IClient<T> requestClient,
		IClient<R> responseClient,
		ITicketServiceClient ticketClient
	) {
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
		this.routineDtoCache = new HashMap<>();
		this.routineFeatureDtoCache = new HashMap<>();
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
	public synchronized Future<RoutineDTO> getRoutine(String rId) throws ClientCommunicationException {
		if (!routineDtoCache.containsKey(rId)) {
			if (!routineDtoCache.containsKey(rId)) {
				String ticketId = requestClient.execute(t -> t.getRoutine(rId));
				Future<RoutineDTO> future = new TicketFutureBuilder<R, RoutineDTO>()
						.dataTicket(ticketId, ticketClient)
						.request(R::getRoutine)
						.via(responseClient);

				routineDtoCache.put(rId, new ThreadSafeFuture<>(future));
			}
		}
		return routineDtoCache.get(rId);
	}

	@Override
	public synchronized Future<List<FeatureDTO>> getRoutineRequiredFeatures(String rId) throws ClientCommunicationException {
		if (!routineFeatureDtoCache.containsKey(rId)) {
			if (!routineFeatureDtoCache.containsKey(rId)) {
				String ticketId = requestClient.execute(t -> t.getRoutineRequiredFeatures(rId));
				Future<List<FeatureDTO>> future = new TicketFutureBuilder<R, List<FeatureDTO>>()
						.dataTicket(ticketId, ticketClient)
						.request(R::getRoutineRequiredFeatures)
						.via(responseClient);

				routineFeatureDtoCache.put(rId, new ThreadSafeFuture<>(future));
			}
		}
		return routineFeatureDtoCache.get(rId);
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
	public Future<RoutineBinaryChunkDTO> getRoutineBinaryChunk(String rbId, short chunk, int chunkSize) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getRoutineBinaryChunk(rbId, chunk, chunkSize));
		return new TicketFutureBuilder<R, RoutineBinaryChunkDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getRoutineBinaryChunk)
				.via(responseClient);
	}

	@Override
	public void close() {
		requestClient.close();
		responseClient.close();
		ticketClient.close();
	}
}
