package at.enfilo.def.library.api;

import at.enfilo.def.communication.api.ticket.rest.ITicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryAdminServiceClientFactory;
import at.enfilo.def.library.api.rest.ILibraryAdminResponseService;
import at.enfilo.def.library.api.rest.ILibraryAdminService;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class LibraryAdminServiceClientTest {
	private ILibraryAdminService service;
	private ILibraryAdminResponseService responseService;
	private ITicketService ticketService;
	private ILibraryAdminServiceClient client;

	@Before
	public void setUp() throws Exception {
		service = Mockito.mock(ILibraryAdminService.class);
		responseService = Mockito.mock(ILibraryAdminResponseService.class);
		ticketService = Mockito.mock(ITicketService.class);

		client = new LibraryAdminServiceClientFactory().createDirectClient(
				service,
				responseService,
				ticketService,
				ILibraryAdminServiceClient.class
		);
	}

	@Test
	public void findRoutines() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String keyword = UUID.randomUUID().toString();
		List<String> routines = new LinkedList<>();
		routines.add(UUID.randomUUID().toString());
		routines.add(UUID.randomUUID().toString());

		when(service.findRoutines(keyword)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.findRoutines(ticketId)).thenReturn(routines);

		Future<List<String>> future = client.findRoutines(keyword);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(routines, future.get());
	}

	@Test
	public void removeRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(service.removeRoutine(routineId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.removeRoutine(routineId);
		assertNull(future.get());
	}

	@Test
	public void createRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		RoutineDTO routineDTO = new RoutineDTO();
		routineDTO.setId(routineId);
		routineDTO.setName("Test routine name");
		routineDTO.setDescription("Test routine description");
		routineDTO.setRevision((short) 0);
		routineDTO.setType(RoutineType.MASTER);
		routineDTO.setInParameters(new LinkedList<>());
		routineDTO.setOutParameter(new FormalParameterDTO());

		when(service.createRoutine(anyObject())).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.createRoutine(ticketId)).thenReturn(routineId);

		Future<String> future = client.createRoutine(routineDTO);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(routineId, future.get());
	}

	@Test
	public void updateRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		RoutineDTO routineDTO = new RoutineDTO();
		routineDTO.setId(routineId);
		routineDTO.setName("Test routine name");
		routineDTO.setDescription("Test routine description");
		routineDTO.setRevision((short) 0);
		routineDTO.setType(RoutineType.MASTER);
		routineDTO.setInParameters(new LinkedList<>());
		routineDTO.setOutParameter(new FormalParameterDTO());

		when(service.updateRoutine(anyObject())).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.updateRoutine(ticketId)).thenReturn(routineId);

		Future<String> future = client.updateRoutine(routineDTO);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(routineId, future.get());
	}

	@Test
	public void uploadRoutineBinary() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineBinaryId = UUID.randomUUID().toString();

		when(service.uploadRoutineBinary(
			anyString(),
			anyString(),
			anyLong(),
            anyBoolean(),
            anyObject()
		)).thenReturn(ticketId);

		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.uploadRoutineBinary(ticketId)).thenReturn(routineBinaryId);

		Future<String> future = client.uploadRoutineBinary(
			routineBinaryId,
			"1",
			1,
			true,
				ByteBuffer.wrap(UUID.randomUUID().toString().getBytes())
		);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(routineBinaryId, future.get());
	}

	@Test
	public void removeRoutineBinary() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();
		String routineBinaryId = UUID.randomUUID().toString();

		when(service.removeRoutineBinary(routineId, routineBinaryId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.removeRoutineBinary(routineId, routineBinaryId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
	}

	@Test
	public void findDataTypes() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String keyword = UUID.randomUUID().toString();
		List<String> dataTypes = new LinkedList<>();
		dataTypes.add(UUID.randomUUID().toString());
		dataTypes.add(UUID.randomUUID().toString());

		when(service.findDataTypes(keyword)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.findDataTypes(ticketId)).thenReturn(dataTypes);

		Future<List<String>> future = client.findDataTypes(keyword);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(dataTypes, future.get());
	}

	@Test
	public void createDataType() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String dataTypeId = UUID.randomUUID().toString();

		when(service.createDataType(anyString(), anyString())).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.createDataType(ticketId)).thenReturn(dataTypeId);

		Future<String> future = client.createDataType(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString()
		);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(dataTypeId, future.get());
	}

	@Test
	public void getDataType() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String dataTypeId = UUID.randomUUID().toString();
		DataTypeDTO dataType = new DataTypeDTO(dataTypeId, "", "");

		when(service.getDataType(dataTypeId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.getDataType(ticketId)).thenReturn(dataType);

		Future<DataTypeDTO> future = client.getDataType(dataTypeId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(dataType, future.get());
	}

	@Test
	public void removeDataType() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String dataTypeId = UUID.randomUUID().toString();

		when(service.removeDataType(dataTypeId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.removeDataType(dataTypeId);
		assertNull(future.get());
	}

	@Test
	public void findTags() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String keyword = UUID.randomUUID().toString();

		List<TagDTO> tags = new LinkedList<>();
		tags.add(new TagDTO(UUID.randomUUID().toString(), "tag_1 description"));
		tags.add(new TagDTO(UUID.randomUUID().toString(), "tag_2 description"));

		when(service.findTags(keyword)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.findTags(ticketId)).thenReturn(tags);

		Future<List<TagDTO>> future = client.findTags(keyword);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(tags, future.get());
	}

	@Test
	public void createTag() throws Exception {
		String ticketId = UUID.randomUUID().toString();

		when(service.createTag(anyString(), anyString())).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.createTag(UUID.randomUUID().toString(), "typical description.");
		assertNull(future.get());
	}

	@Test
	public void removeTag() throws Exception {
		String ticketId = UUID.randomUUID().toString();

		when(service.removeTag(anyString())).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.removeTag(UUID.randomUUID().toString());
		assertNull(future.get());
	}

	@Test
	public void createFeature() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String featureId = UUID.randomUUID().toString();

		when(service.createFeature(anyString(), anyString(), anyString())).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.createFeature(ticketId)).thenReturn(featureId);

		Future<String> future = client.createFeature("java", "1.8", "language");
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(featureId, future.get());
	}

	@Test
	public void addExtension() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String extensionId = UUID.randomUUID().toString();

		when(service.addExtension(anyString(), anyString(), anyString())).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.addExtension(ticketId)).thenReturn(extensionId);

		Future<String> future = client.addExtension(UUID.randomUUID().toString(), "numpy", "1.8");
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(extensionId, future.get());
	}

	@Test
	public void findFeatures() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String keyword = "java";

		List<FeatureDTO> features = new LinkedList<>();
		FeatureDTO java = new FeatureDTO();
		java.setName("java");
		java.setVersion("1.8");
		java.setGroup("language");
		features.add(java);

		when(service.getFeatures(keyword)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.getFeatures(ticketId)).thenReturn(features);

		Future<List<FeatureDTO>> future = client.getFeatures(keyword);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(features, future.get());
	}
}
