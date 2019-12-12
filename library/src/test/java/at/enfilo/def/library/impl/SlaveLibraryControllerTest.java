package at.enfilo.def.library.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.LibraryConfiguration;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.transfer.dto.RoutineBinaryChunkDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class SlaveLibraryControllerTest {
	private SlaveLibraryController slaveLibraryController;
	private IBinaryStoreDriver storeDriver;
	private ILibraryAdminServiceClient masterLibrary;

	private String rId;
	private RoutineDTO routine;
	private Future<RoutineDTO> futureRoutine;
	private String rbId;
	private String rbName;
	private RoutineBinaryDTO routineBinary;
	private Future<RoutineBinaryDTO> futureRoutineBinary;
	private RoutineBinaryChunkDTO routineBinaryChunk1;
	private RoutineBinaryChunkDTO routineBinaryChunk2;
	private Future<RoutineBinaryChunkDTO> futureRoutineBinaryChunk1;
	private Future<RoutineBinaryChunkDTO> futureRoutineBinaryChunk2;
	private Random rnd;

	@Before
	public void setUp() throws Exception {
		rnd = new Random();

		storeDriver = Mockito.mock(IBinaryStoreDriver.class);
		masterLibrary = Mockito.mock(ILibraryAdminServiceClient.class);
		when(masterLibrary.getServiceEndpoint()).thenReturn(new ServiceEndpointDTO());
		slaveLibraryController = new SlaveLibraryController(LibraryConfiguration.getDefault(), storeDriver);
		slaveLibraryController.setMasterLibrary(masterLibrary);

		rId = UUID.randomUUID().toString();
		routine = new RoutineDTO();
		routine.setId(rId);

		rbId = UUID.randomUUID().toString();
		rbName = "name";
		routineBinary = new RoutineBinaryDTO();
		routineBinary.setId(rbId);
		routineBinary.setName(rbName);
		routineBinary.setSizeInBytes(SlaveLibraryController.CHUNK_SIZE + rnd.nextInt(SlaveLibraryController.CHUNK_SIZE));
		routine.addToRoutineBinaries(routineBinary);

		routineBinaryChunk1 = new RoutineBinaryChunkDTO();
		routineBinaryChunk1.setTotalChunks((short)2);
		routineBinaryChunk1.setChunk((short)0);
		routineBinaryChunk1.setChunkSize(SlaveLibraryController.CHUNK_SIZE);
		byte[] buf1 = new byte[SlaveLibraryController.CHUNK_SIZE];
		rnd.nextBytes(buf1);
		routineBinaryChunk1.setData(buf1);

		routineBinaryChunk2 = new RoutineBinaryChunkDTO();
		routineBinaryChunk2.setTotalChunks((short)2);
		routineBinaryChunk2.setChunk((short)1);
		routineBinaryChunk2.setChunkSize(SlaveLibraryController.CHUNK_SIZE);
		byte[] buf2 = new byte[(int) (routineBinary.getSizeInBytes() - SlaveLibraryController.CHUNK_SIZE)];
		rnd.nextBytes(buf2);
		routineBinaryChunk2.setData(buf2);

		futureRoutine = Mockito.mock(Future.class);
		when(futureRoutine.isDone()).thenReturn(true);
		when(futureRoutine.get()).thenReturn(routine);

		futureRoutineBinary = Mockito.mock(Future.class);
		when(futureRoutineBinary.isDone()).thenReturn(true);
		when(futureRoutineBinary.get()).thenReturn(routineBinary);

		futureRoutineBinaryChunk1 = Mockito.mock(Future.class);
		when(futureRoutineBinaryChunk1.isDone()).thenReturn(true);
		when(futureRoutineBinaryChunk1.get()).thenReturn(routineBinaryChunk1);
		futureRoutineBinaryChunk2 = Mockito.mock(Future.class);
		when(futureRoutineBinaryChunk2.isDone()).thenReturn(true);
		when(futureRoutineBinaryChunk2.get()).thenReturn(routineBinaryChunk2);
	}

	@Test
	public void getRoutine() throws Exception {
		when(masterLibrary.getRoutine(rId)).thenReturn(futureRoutine);
		when(masterLibrary.getRoutineBinary(rbId)).thenReturn(futureRoutineBinary);
		when(masterLibrary.getRoutineBinaryChunk(rbId, (short) 0, SlaveLibraryController.CHUNK_SIZE)).thenReturn(futureRoutineBinaryChunk1);
		when(masterLibrary.getRoutineBinaryChunk(rbId, (short) 1, SlaveLibraryController.CHUNK_SIZE)).thenReturn(futureRoutineBinaryChunk2);
		URL rbUrl = new URL("file:/path/to/" + rbId);
		when(storeDriver.getFileURL(rbId)).thenReturn(rbUrl);
		when(storeDriver.getExecutionURL(rId, rbId, rbName)).thenReturn(rbUrl);

		// First time getRoutine --> fetch from master library
		RoutineDTO r = slaveLibraryController.getRoutine(rId);
		assertEquals(routine, r);
		assertEquals(rbUrl.toString(), routineBinary.getUrl());

		// Second time getRoutine --> should be cached
		r = slaveLibraryController.getRoutine(rId);
		assertEquals(routine, r);

		verify(masterLibrary, times(1)).getRoutine(rId);
		verify(masterLibrary, times(1)).getRoutineBinaryChunk(rbId, (short)0, SlaveLibraryController.CHUNK_SIZE);
		verify(masterLibrary, times(1)).getRoutineBinaryChunk(rbId, (short)1, SlaveLibraryController.CHUNK_SIZE);
		verify(storeDriver, times(1)).exists(rbId);
		verify(storeDriver, times(1)).storeChunk(rbId, routineBinaryChunk1);
		verify(storeDriver, times(1)).storeChunk(rbId, routineBinaryChunk2);
	}

	@Test
	public void getRoutineWithExistingBinary() throws Exception {
		long size = new Random().nextLong();
		String md5 = UUID.randomUUID().toString();
		routineBinary.setSizeInBytes(size);
		routineBinary.setMd5(md5);
		URL rbUrl = new URL("file:/path/to/" + rbId);
		URL executionUrl = new URL("file:/path/to/execution/" + rbName);

		when(masterLibrary.getRoutine(rId)).thenReturn(futureRoutine);
		when(masterLibrary.getRoutine(rId)).thenReturn(futureRoutine);
		when(masterLibrary.getRoutineBinary(rbId)).thenReturn(futureRoutineBinary);
		when(storeDriver.exists(rbId)).thenReturn(true);
		when(storeDriver.getSizeInBytes(rbId)).thenReturn(size);
		when(storeDriver.md5(rbId)).thenReturn(md5);
		when(storeDriver.getFileURL(rbId)).thenReturn(rbUrl);
		when(storeDriver.getExecutionURL(rId, rbId, rbName)).thenReturn(executionUrl);

		RoutineDTO r = slaveLibraryController.getRoutine(rId);
		assertEquals(routine, r);
		// Check if URL was successfully updated
		assertEquals(rbUrl.toString(), routineBinary.getUrl());
	}

	@Test(expected = Exception.class)
	public void getRoutineWrongId() throws ClientCommunicationException, Exception {
		when(masterLibrary.getRoutine(anyString())).thenThrow(new ClientCommunicationException(""));

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
	public void createRoutine() throws Exception {
		assertTrue(slaveLibraryController.routineRegistry.isEmpty());

		String rId = slaveLibraryController.createRoutine(routine);
		assertTrue(slaveLibraryController.routineRegistry.containsKey(rId));
	}

	@Test
	public void updateRoutine() throws Exception {
		slaveLibraryController.routineRegistry.put(rId, routine);

		String updatedId = slaveLibraryController.updateRoutine(routine);
		assertFalse(updatedId.equals(rId));
		assertTrue(slaveLibraryController.routineRegistry.containsKey(rId));
		assertTrue(slaveLibraryController.routineRegistry.containsKey(updatedId));
		assertTrue(routine.getRevision() < slaveLibraryController.getRoutine(updatedId).getRevision());
	}

	@Test
	public void removeRoutineBinary() throws Exception, IOException {
		slaveLibraryController.routineRegistry.put(rId, routine);

		slaveLibraryController.removeRoutineBinary(rId, rbId);
		assertTrue(routine.getRoutineBinaries().isEmpty());
		assertTrue(slaveLibraryController.routineRegistry.containsKey(rId));

		verify(storeDriver, times(1)).delete(rId, rbId, rbName);
	}

	@Test
	public void getCachedRoutineBinary() throws Exception {
		slaveLibraryController.routineRegistry.put(rId, routine);

		RoutineBinaryDTO b = slaveLibraryController.getRoutineBinary(rbId);
		assertEquals(routineBinary, b);
	}

	@Test
	public void getNonCachedRoutineBinary() throws Exception {
		when(storeDriver.exists(rbId)).thenReturn(false);
		when(masterLibrary.getRoutineBinary(rbId)).thenReturn(futureRoutineBinary);
		when(masterLibrary.getRoutineBinaryChunk(rbId, (short)0, SlaveLibraryController.CHUNK_SIZE)).thenReturn(futureRoutineBinaryChunk1);
		when(masterLibrary.getRoutineBinaryChunk(rbId, (short)1, SlaveLibraryController.CHUNK_SIZE)).thenReturn(futureRoutineBinaryChunk2);
		URL url = new URL("file:/" + rbId);
		when(storeDriver.create(rId, routineBinary)).thenReturn(url);

		RoutineBinaryDTO b = slaveLibraryController.getRoutineBinary(rbId);
		routineBinary.setUrl(url.toString());
		assertEquals(routineBinary, b);
	}

	@Test
	public void findDataTypes() throws Exception {
		String searchPattern = UUID.randomUUID().toString();
		when(masterLibrary.findDataTypes(searchPattern)).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.findDataTypes(searchPattern);
		verify(masterLibrary).findDataTypes(searchPattern);
	}

	@Test
	public void createDataType() throws Exception {
		String name = UUID.randomUUID().toString();
		String schema = UUID.randomUUID().toString();
		when(masterLibrary.createDataType(name, schema)).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.createDataType(name, schema);
		verify(masterLibrary).createDataType(name, schema);
	}

	@Test
	public void getDataType() throws Exception {
		String dId = UUID.randomUUID().toString();
		when(masterLibrary.getDataType(dId)).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.getDataType(dId);
		verify(masterLibrary).getDataType(dId);
	}

	@Test
	public void removeDataType() throws Exception {
		String dId = UUID.randomUUID().toString();
		when(masterLibrary.removeDataType(dId)).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.removeDataType(dId);
		verify(masterLibrary).removeDataType(dId);
	}

	@Test
	public void findTags() throws Exception {
		String searchPattern = UUID.randomUUID().toString();
		when(masterLibrary.findTags(searchPattern)).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.findTags(searchPattern);
		verify(masterLibrary).findTags(searchPattern);
	}

	@Test
	public void createTags() throws Exception {
		String label = UUID.randomUUID().toString();
		String desc = UUID.randomUUID().toString();
		when(masterLibrary.createTag(label, desc)).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.createTag(label, desc);
		verify(masterLibrary).createTag(label, desc);
	}

	@Test
	public void removeTag() throws Exception {
		String label = UUID.randomUUID().toString();
		when(masterLibrary.removeTag(label)).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.removeTag(label);
		verify(masterLibrary).removeTag(label);
	}

	@Test
	public void createFeature() throws Exception {
		String name = UUID.randomUUID().toString();
		String group = "language";
		String version = "1.2.3";
		when(masterLibrary.createFeature(name, group, version)).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.createFeature(name, group, version);
		verify(masterLibrary).createFeature(name, group, version);
	}

	@Test
	public void addExtension() throws Exception {
		String featureId = UUID.randomUUID().toString();
		String name = UUID.randomUUID().toString();
		String version = "1.2.4";
		when(masterLibrary.addExtension(featureId, name, version)).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.addExtension(featureId, name, version);
		verify(masterLibrary).addExtension(featureId, name, version);
	}

	@Test
	public void getFeatures() throws Exception {
		when(masterLibrary.getFeatures("*")).thenReturn(Mockito.mock(Future.class));
		slaveLibraryController.getFeatures("*");
		verify(masterLibrary).getFeatures("*");
	}
}
