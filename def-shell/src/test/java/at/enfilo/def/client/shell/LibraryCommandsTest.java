package at.enfilo.def.client.shell;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.transfer.dto.DataTypeDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.dto.TagDTO;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.shell.core.CommandResult;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

import static at.enfilo.def.client.shell.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class LibraryCommandsTest extends ShellBaseTest {

	@Test
	public void getRoutine() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String rId = UUID.randomUUID().toString();
		RoutineDTO routine = new RoutineDTO();
		routine.setId(rId);
		Future<RoutineDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getRoutine(rId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(routine);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_ROUTINE_SHOW,
						OPT_ROUTINE_ID, rId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(rId));
	}

	@Test
	public void getRoutineToObject() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String rId = UUID.randomUUID().toString();
		String name = UUID.randomUUID().toString();
		RoutineDTO routine = new RoutineDTO();
		routine.setId(rId);
		Future<RoutineDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getRoutine(rId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(routine);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_LIBRARY_ROUTINE_SHOW,
						OPT_ROUTINE_ID, rId,
						OPT_TO_OBJECT, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(routine, objects.getObjectMap().get(name));
	}

	@Test
	public void findRoutines() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String rId1 = UUID.randomUUID().toString();
		String rId2 = UUID.randomUUID().toString();
		List<String> routines = new LinkedList<>();
		routines.add(rId1);
		routines.add(rId2);
		Future<List<String>> futureMock = Mockito.mock(Future.class);
		when(clientMock.findRoutines(anyString())).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(routines);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_ROUTINE_FIND,
						OPT_SEARCH_PATTERN, UUID.randomUUID().toString()
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(rId1));
		assertTrue(result.getResult().toString().contains(rId2));
	}

	@Test
	public void removeRoutine() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String rId = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.removeRoutine(rId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_ROUTINE_REMOVE,
						OPT_ROUTINE_ID, rId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_LIBRARY_ROUTINE_REMOVE,
				result.getResult()
		);
	}

	@Test
	public void createRoutine() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		RoutineDTO routine = new RoutineDTO();
		String name = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		objects.getObjectMap().put(name, routine);
		Future<String> futureMock = Mockito.mock(Future.class);
		when(clientMock.createRoutine(routine)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(rId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_ROUTINE_CREATE,
						OPT_ROUTINE, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_LIBRARY_ROUTINE_CREATED, rId),
				result.getResult()
		);
	}

	@Test
	public void updateRoutine() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		RoutineDTO routine = new RoutineDTO();
		String name = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		objects.getObjectMap().put(name, routine);
		Future<String> futureMock = Mockito.mock(Future.class);
		when(clientMock.updateRoutine(routine)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(rId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_ROUTINE_UPDATE,
						OPT_ROUTINE, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_LIBRARY_ROUTINE_UPDATED, rId),
				result.getResult()
		);
	}

	@Test
	public void uploadRoutineBinary() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String bId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		char[] data = bId.toCharArray();
		File binaryFile = File.createTempFile("binary", "routine");
		binaryFile.deleteOnExit();
		try (FileWriter writer = new FileWriter(binaryFile)) {
			writer.write(data);
			writer.flush();
		}

		Future<String> futureMock = Mockito.mock(Future.class);
		when(clientMock.uploadRoutineBinary(
				eq(rId),
				anyString(),
				eq(binaryFile.length()),
				anyBoolean(),
				anyObject())
		).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(bId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_LIBRARY_ROUTINE_BINARY_UPLOAD,
						OPT_ROUTINE_ID, rId,
						OPT_FILE_NAME, binaryFile
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_LIBRARY_ROUTINE_BINARY_UPLOAD, bId),
				result.getResult()
		);
	}

	@Test
	public void removeRoutineBinary() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String rId = UUID.randomUUID().toString();
		String bId = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.removeRoutineBinary(rId, bId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_LIBRARY_ROUTINE_BINARY_REMOVE,
						OPT_ROUTINE_ID, rId,
						OPT_ROUTINE_BINARY_ID, bId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_LIBRARY_ROUTINE_BINARY_REMOVE,
				result.getResult()
		);
	}

	@Test
	public void findDataTypes() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String dId1 = UUID.randomUUID().toString();
		String dId2 = UUID.randomUUID().toString();
		List<String> dataTypes = new LinkedList<>();
		dataTypes.add(dId1);
		dataTypes.add(dId2);
		Future<List<String>> futureMock = Mockito.mock(Future.class);
		when(clientMock.findDataTypes(anyString())).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(dataTypes);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_DATA_TYPE_FIND,
						OPT_SEARCH_PATTERN, UUID.randomUUID().toString()
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(dId1));
		assertTrue(result.getResult().toString().contains(dId2));
	}

	@Test
	public void createDataType() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String dId = UUID.randomUUID().toString();
		String name = UUID.randomUUID().toString();
		File schemaFile = File.createTempFile("schema", "file");
		schemaFile.deleteOnExit();
		try (FileWriter writer = new FileWriter(schemaFile)) {
			writer.write(name);
			writer.flush();
		}
		Future<String> futureMock = Mockito.mock(Future.class);
		when(clientMock.createDataType(name, name + '\n')).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(dId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_LIBRARY_DATA_TYPE_CREATE,
						OPT_NAME, name,
						OPT_FILE_NAME, schemaFile
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_LIBRARY_DATA_TYPE_CREATED, dId),
				result.getResult()
		);
	}


	@Test
	public void getDataType() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String dId = UUID.randomUUID().toString();
		DataTypeDTO dataType = new DataTypeDTO();
		dataType.setId(dId);
		Future<DataTypeDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getDataType(dId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(dataType);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_DATA_TYPE_SHOW,
						OPT_DATATYPE_ID, dId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(dId));
	}

	@Test
	public void getDataTypeToObject() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String dId = UUID.randomUUID().toString();
		String name = UUID.randomUUID().toString();
		DataTypeDTO dataType = new DataTypeDTO();
		dataType.setId(dId);
		Future<DataTypeDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getDataType(dId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(dataType);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_LIBRARY_DATA_TYPE_SHOW,
						OPT_DATATYPE_ID, dId,
						OPT_TO_OBJECT, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(dataType, objects.getObjectMap().get(name));
	}

	@Test
	public void removeDataType() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String dId = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.removeDataType(dId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_DATA_TYPE_REMOVE,
						OPT_DATATYPE_ID, dId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_LIBRARY_DATA_TYPE_REMOVE,
				result.getResult()
		);
	}

	@Test
	public void findTag() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		TagDTO tag1 = new TagDTO(UUID.randomUUID().toString(), "");
		TagDTO tag2 = new TagDTO(UUID.randomUUID().toString(), "");
		List<TagDTO> tags = new LinkedList<>();
		tags.add(tag1);
		tags.add(tag2);
		Future<List<TagDTO>> futureMock = Mockito.mock(Future.class);
		when(clientMock.findTags(anyString())).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(tags);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_TAG_FIND,
						OPT_SEARCH_PATTERN, UUID.randomUUID().toString()
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(tag1.getId()));
		assertTrue(result.getResult().toString().contains(tag2.getId()));
	}

	@Test
	public void createTag() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String label = UUID.randomUUID().toString();
		String description = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.createTag(label, description)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_LIBRARY_TAG_CREATE,
						OPT_LABEL, label,
						OPT_DESCRIPTION, description
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_LIBRARY_TAG_CREATED, label),
				result.getResult()
		);
	}

	@Test
	public void removeTag() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();

		String label = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.removeTag(label)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_TAG_REMOVE,
						OPT_LABEL, label
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_LIBRARY_TAG_REMOVE, label),
				result.getResult()
		);
	}

	@Test
	public void setDataEndpoint() throws Exception {
		ILibraryAdminServiceClient clientMock = setupMocks();
		Random rnd = new Random();
		String name = UUID.randomUUID().toString();
		String host = UUID.randomUUID().toString();
		int port = rnd.nextInt();
		Protocol protocol = Protocol.THRIFT_TCP;
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(host, port, protocol);
		objects.getObjectMap().put(name, endpoint);
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.setDataEndpoint(endpoint)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_LIBRARY_SET_DATA_ENDPOINT,
						OPT_SERVICE_ENDPOINT, name

				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_LIBRARY_DATA_ENDPOINT_SET,
				result.getResult().toString()
		);

	}


	private ILibraryAdminServiceClient setupMocks() throws ClientCreationException {
		changeToLibraryContext();
		ILibraryAdminServiceClient clientMock = Mockito.mock(ILibraryAdminServiceClient.class);
		session.setLibraryServiceClient(clientMock);
		session.setLibraryAdminServiceClient(clientMock);
		return clientMock;
	}
}
