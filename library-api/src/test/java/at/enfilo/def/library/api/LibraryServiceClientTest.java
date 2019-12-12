package at.enfilo.def.library.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.library.api.thrift.LibraryResponseService;
import at.enfilo.def.library.api.thrift.LibraryService;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class LibraryServiceClientTest {
	private ILibraryServiceClient client;
	private LibraryService.Iface requestServiceMock;
	private LibraryResponseService.Iface responseServiceMock;
	private TicketService.Iface ticketServiceMock;

	@Before
	public void setUp() throws Exception {
		requestServiceMock = Mockito.mock(LibraryService.Iface.class);
		responseServiceMock = Mockito.mock(LibraryResponseService.Iface.class);
		ticketServiceMock = Mockito.mock(TicketService.Iface.class);

		client = new LibraryServiceClientFactory().createDirectClient(
				requestServiceMock, responseServiceMock, ticketServiceMock,
				ILibraryServiceClient.class
		);
	}

	@Test
	public void getInfo() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		LibraryInfoDTO libraryInfoDTO = new LibraryInfoDTO();

		when(requestServiceMock.getLibraryInfo()).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getLibraryInfo(ticketId)).thenReturn(libraryInfoDTO);

		Future<LibraryInfoDTO> futureResult = client.getInfo();

		assertEquals(libraryInfoDTO, futureResult.get());
	}

	@Test
	public void getRoutine() throws Exception {
		String rId = UUID.randomUUID().toString();
		RoutineDTO routine = new RoutineDTO();
		routine.setId(rId);
		String ticketId = UUID.randomUUID().toString();

		// Set up mock methods
		when(requestServiceMock.getRoutine(rId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getRoutine(ticketId)).thenReturn(routine);

		Future<RoutineDTO> futureRoutine = client.getRoutine(rId);

		assertEquals(routine, futureRoutine.get());
	}

	@Test
	public void getRoutineRequiredFeatures() throws Exception {
		String rId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();

		List<FeatureDTO> requiredFeatures = new ArrayList<>();
		FeatureDTO featureDTO = new FeatureDTO();
		featureDTO.setName("java");
		featureDTO.setVersion("1.8");
		requiredFeatures.add(featureDTO);

		// Set up mock methods
		when(requestServiceMock.getRoutineRequiredFeatures(rId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getRoutineRequiredFeatures(ticketId)).thenReturn(requiredFeatures);

		Future<List<FeatureDTO>> futureRoutine = client.getRoutineRequiredFeatures(rId);

		assertEquals(requiredFeatures, futureRoutine.get());
	}

	@Test
	public void getRoutineBinary() throws Exception {
		String routineId = UUID.randomUUID().toString();
		String routineBinaryId = UUID.randomUUID().toString();
		String routineBinaryName = "binaryName";
		RoutineBinaryDTO routineBinaryDTO = new RoutineBinaryDTO();
		routineBinaryDTO.setId(routineBinaryId);
		routineBinaryDTO.setName(routineBinaryName);
		String ticketId = UUID.randomUUID().toString();

		// Set up mock methods
		when(requestServiceMock.getRoutineBinary(routineBinaryId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getRoutineBinary(ticketId)).thenReturn(routineBinaryDTO);

		Future<RoutineBinaryDTO> futureRoutineBinary = client.getRoutineBinary(routineBinaryId);

		assertEquals(routineBinaryDTO, futureRoutineBinary.get());
	}

	@Test
	public void getRoutineBinaryChunk() throws Exception {
		Random rnd = new Random();
		String rbId = UUID.randomUUID().toString();
		short chunk = (short)rnd.nextInt();
		int chunkSize = rnd.nextInt();
		String ticketId = UUID.randomUUID().toString();
		RoutineBinaryChunkDTO chunkDTO = new RoutineBinaryChunkDTO(chunk, chunk, chunkSize, ByteBuffer.wrap(new byte[1]));

		// Set up mock methods
		when(requestServiceMock.getRoutineBinaryChunk(rbId, chunk, chunkSize)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getRoutineBinaryChunk(ticketId)).thenReturn(chunkDTO);

		Future<RoutineBinaryChunkDTO> futureRoutineBinaryChunk = client.getRoutineBinaryChunk(rbId, chunk, chunkSize);

		assertEquals(chunkDTO, futureRoutineBinaryChunk.get());
	}
}
