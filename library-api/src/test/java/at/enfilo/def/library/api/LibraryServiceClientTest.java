package at.enfilo.def.library.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.library.api.thrift.LibraryResponseService;
import at.enfilo.def.library.api.thrift.LibraryService;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.LibraryInfoDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
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
	public void setDataEndpoint() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO serviceEndpointDTO = new ServiceEndpointDTO();

		when(requestServiceMock.setDataEndpoint(serviceEndpointDTO)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureResult = client.setDataEndpoint(serviceEndpointDTO);
		assertNull(futureResult.get());
	}

	@Test
	public void getRoutineBinary() throws Exception {
		String rbId = UUID.randomUUID().toString();
		RoutineBinaryDTO routineBinaryDTO = new RoutineBinaryDTO();
		routineBinaryDTO.setId(rbId);
		String ticketId = UUID.randomUUID().toString();

		// Set up mock methods
		when(requestServiceMock.getRoutineBinary(rbId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getRoutineBinary(ticketId)).thenReturn(routineBinaryDTO);

		Future<RoutineBinaryDTO> futureRoutineBinary = client.getRoutineBinary(rbId);

		assertEquals(routineBinaryDTO, futureRoutineBinary.get());
	}
}
