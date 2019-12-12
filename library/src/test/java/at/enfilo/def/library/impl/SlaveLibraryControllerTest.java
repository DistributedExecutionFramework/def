package at.enfilo.def.library.impl;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.LibraryConfiguration;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class SlaveLibraryControllerTest {
	private SlaveLibraryController slaveLibraryController;
	private IBinaryStoreDriver storeDriver;
	private ILibraryServiceClient libraryClient;

	private String rId;
	private RoutineDTO routine;
	private Future<RoutineDTO> futureRoutine;
	private String rbId;
	private RoutineBinaryDTO routineBinary;
	private Future<RoutineBinaryDTO> futureRoutineBinary;

	@Before
	public void setUp() throws Exception {
		storeDriver = Mockito.mock(IBinaryStoreDriver.class);
		libraryClient = Mockito.mock(ILibraryServiceClient.class);
		slaveLibraryController = new SlaveLibraryController(LibraryConfiguration.getDefault(), storeDriver);
		slaveLibraryController.setLibraryServiceClient(libraryClient);

		rId = UUID.randomUUID().toString();
		routine = new RoutineDTO();
		routine.setId(rId);

		rbId = UUID.randomUUID().toString();
		routineBinary = new RoutineBinaryDTO();
		routineBinary.setId(rbId);
		routineBinary.setData(new byte[]{0x00, 0x01, 0x02, 0x03});
		routine.addToRoutineBinaries(routineBinary);

		futureRoutine = Mockito.mock(Future.class);
		when(futureRoutine.isDone()).thenReturn(true);
		when(futureRoutine.get()).thenReturn(routine);

		futureRoutineBinary = Mockito.mock(Future.class);
		when(futureRoutineBinary.isDone()).thenReturn(true);
		when(futureRoutineBinary.get()).thenReturn(routineBinary);
	}

	@Test
	public void getRoutine() throws Exception {
		when(libraryClient.getRoutine(rId)).thenReturn(futureRoutine);
		when(libraryClient.getRoutineBinary(rbId)).thenReturn(futureRoutineBinary);
		URL rbUrl = new URL("file:/path/to/" + rbId);
		when(storeDriver.store(routineBinary)).thenReturn(rbUrl);

		// First time getRoutine --> fetch from master library
		RoutineDTO r = slaveLibraryController.getRoutine(rId);
		assertEquals(routine, r);
		assertEquals(rbUrl.toString(), routineBinary.getUrl());
		assertTrue(routineBinary.getData() == null || routineBinary.getData().length == 0);

		// Second time getRoutine --> should be cached
		r = slaveLibraryController.getRoutine(rId);
		assertEquals(routine, r);

		verify(libraryClient, times(1)).getRoutine(rId);
		verify(libraryClient, times(1)).getRoutineBinary(rbId);
		verify(storeDriver, times(1)).exists(rbId);
		verify(storeDriver, times(1)).store(routineBinary);
	}

	@Test
	public void getRoutineWithExistingBinary() throws Exception {
		long size = new Random().nextLong();
		String md5 = UUID.randomUUID().toString();
		routineBinary.setSizeInBytes(size);
		routineBinary.setMd5(md5);
		URL rbUrl = new URL("file:/path/to/" + rbId);

		when(libraryClient.getRoutine(rId)).thenReturn(futureRoutine);
		when(storeDriver.exists(rbId)).thenReturn(true);
		when(storeDriver.getSizeInBytes(rbId)).thenReturn(size);
		when(storeDriver.md5(rbId)).thenReturn(md5);
		when(storeDriver.getURL(rbId)).thenReturn(rbUrl);

		RoutineDTO r = slaveLibraryController.getRoutine(rId);
		assertEquals(routine, r);
		// Check if URL was successfully updated
		assertEquals(rbUrl.toString(), routineBinary.getUrl());
	}

	@Test(expected = ExecutionException.class)
	public void getRoutineWrongId() throws ClientCommunicationException, ExecutionException {
		when(libraryClient.getRoutine(anyString())).thenThrow(new ClientCommunicationException(""));

		slaveLibraryController.getRoutine(UUID.randomUUID().toString());
	}

	@Test
	public void findRoutines() {
		RoutineDTO r1 = new RoutineDTO();
		r1.setId(UUID.randomUUID().toString());
		r1.setName("pattern");
		RoutineDTO r2 = new RoutineDTO();
		r2.setId(UUID.randomUUID().toString());
		r2.setName(" pATTerN    xxx");
		RoutineDTO r3 = new RoutineDTO();
		r3.setId(UUID.randomUUID().toString());
		r3.setDescription(" PATTERN  1234");
		RoutineDTO r4 = new RoutineDTO();
		r4.setId(UUID.randomUUID().toString());
		r4.setName("1234");
		r4.setDescription("1234");
		slaveLibraryController.routineRegistry.put(r1.getId(), r1);
		slaveLibraryController.routineRegistry.put(r2.getId(), r2);
		slaveLibraryController.routineRegistry.put(r3.getId(), r3);
		slaveLibraryController.routineRegistry.put(r4.getId(), r4);

		List<String> result = slaveLibraryController.findRoutines("pattern");
		assertEquals(3, result.size());
		assertTrue(result.contains(r1.getId()));
		assertTrue(result.contains(r2.getId()));
		assertTrue(result.contains(r3.getId()));
	}

	@Test
	public void removeRoutine() {
		slaveLibraryController.routineRegistry.put(rId, routine);
		slaveLibraryController.routineRegistry.put(UUID.randomUUID().toString(), new RoutineDTO());

		slaveLibraryController.removeRoutine(rId);
		assertFalse(slaveLibraryController.routineRegistry.containsKey(rId));
		assertFalse(slaveLibraryController.routineRegistry.isEmpty());
	}

	@Test
	public void createRoutine() throws ExecutionException {
		assertTrue(slaveLibraryController.routineRegistry.isEmpty());

		String rId = slaveLibraryController.createRoutine(routine);
		assertTrue(slaveLibraryController.routineRegistry.containsKey(rId));
	}

	@Test
	public void updateRoutine() throws ExecutionException {
		slaveLibraryController.routineRegistry.put(rId, routine);

		String updatedId = slaveLibraryController.updateRoutine(routine);
		assertFalse(updatedId.equals(rId));
		assertTrue(slaveLibraryController.routineRegistry.containsKey(rId));
		assertTrue(slaveLibraryController.routineRegistry.containsKey(updatedId));
		assertTrue(routine.getRevision() < slaveLibraryController.getRoutine(updatedId).getRevision());
	}

	@Test
	public void uploadRoutineBinary() throws ExecutionException, IOException {
		String md5 = UUID.randomUUID().toString();
		Random rnd = new Random();
		int size = rnd.nextInt();
		boolean primary = rnd.nextBoolean();
		byte[] data = new byte[16];
		rnd.nextBytes(data);
		URL url = new URL("file:/" + UUID.randomUUID().toString());

		when(storeDriver.store(any())).thenReturn(url);

		String id = slaveLibraryController.uploadRoutineBinary(
				rId,
				md5,
				size,
				primary,
				ByteBuffer.wrap(data)
		);

		RoutineBinaryDTO binary = new RoutineBinaryDTO();
		binary.setId(id);
		binary.setMd5(md5);
		binary.setSizeInBytes(size);
		binary.setPrimary(primary);
		binary.setData(data);

		verify(storeDriver, times(1)).store(binary);
	}

	@Test
	public void removeRoutineBinary() throws ExecutionException, IOException {
		slaveLibraryController.routineRegistry.put(rId, routine);

		slaveLibraryController.removeRoutineBinary(rId, rbId);
		assertTrue(routine.getRoutineBinaries().isEmpty());
		assertTrue(slaveLibraryController.routineRegistry.containsKey(rId));

		verify(storeDriver, times(1)).delete(rbId);
	}

	@Test
	public void getCachedRoutineBinary() throws IOException, NoSuchAlgorithmException, ExecutionException {
		when(storeDriver.exists(rbId)).thenReturn(true);
		when(storeDriver.read(rbId)).thenReturn(routineBinary);

		RoutineBinaryDTO b = slaveLibraryController.getRoutineBinary(rbId);
		assertEquals(routineBinary, b);
	}

	@Test
	public void getNonCachedRoutineBinary() throws ExecutionException, ClientCommunicationException, IOException {
		when(storeDriver.exists(rbId)).thenReturn(false);
		when(libraryClient.getRoutineBinary(rbId)).thenReturn(futureRoutineBinary);
		URL url = new URL("file:/" + rbId);
		when(storeDriver.store(routineBinary)).thenReturn(url);

		RoutineBinaryDTO b = slaveLibraryController.getRoutineBinary(rbId);
		routineBinary.setUrl(url.toString());
		assertEquals(routineBinary, b);
	}

	@Test(expected = ExecutionException.class)
	public void findDataTypes() throws ExecutionException {
		slaveLibraryController.findDataTypes("");
	}

	@Test(expected = ExecutionException.class)
	public void createDataType() throws ExecutionException {
		slaveLibraryController.createDataType("name", "schema");
	}

	@Test(expected = ExecutionException.class)
	public void getDataType() throws ExecutionException {
		slaveLibraryController.getDataType("dId");
	}

	@Test(expected = ExecutionException.class)
	public void removeDataType() throws ExecutionException {
		slaveLibraryController.removeDataType("");
	}

	@Test(expected = ExecutionException.class)
	public void findTags() throws ExecutionException {
		slaveLibraryController.findTags("");
	}

	@Test(expected = ExecutionException.class)
	public void createTags() throws ExecutionException {
		slaveLibraryController.createTag("label", "desc");
	}

	@Test(expected = ExecutionException.class)
	public void removeTag() throws ExecutionException {
		slaveLibraryController.removeTag("label");
	}

	@Test(expected = ExecutionException.class)
	public void createFeature() throws ExecutionException {
		slaveLibraryController.createFeature("java", "language", "1.8");
	}

	@Test(expected = ExecutionException.class)
	public void addExtension() throws ExecutionException {
		slaveLibraryController.addExtension(UUID.randomUUID().toString(), "numpy", "1.8");
	}

	@Test(expected = ExecutionException.class)
	public void getFeatures() throws ExecutionException {
		slaveLibraryController.getFeatures("*");
	}
}
