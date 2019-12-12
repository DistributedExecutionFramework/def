package at.enfilo.def.library.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryAdminServiceClientFactory;
import at.enfilo.def.transfer.dto.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public abstract class LibraryAdminServiceTest {

    private IServer server;
    private Thread serverThread;
    private ILibraryAdminServiceClient client;

    private RoutineDTO routine;
    private List<String> routineIds;
    private RoutineBinaryDTO routineBinary;
    private DataTypeDTO dataType;
    private List<String> dataTypeIds;
    private TagDTO tag;
    private List<TagDTO> tags;

    protected LibraryController libraryController;

    @Before
    public void setUp() throws Exception {
        TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

        libraryController = Mockito.mock(LibraryController.class);

        routine = new RoutineDTO();
        routine.setId(UUID.randomUUID().toString());
        routineIds = new LinkedList<>();
        routineIds.add(routine.getId());
        routineBinary = new RoutineBinaryDTO();
        routineBinary.setId(UUID.randomUUID().toString());
        routineBinary.setMd5("md5");
		routineBinary.setSizeInBytes(4);
		routineBinary.setData(new byte[]{0x00, 0x01, 0x03, 0x04});
        routine.addToRoutineBinaries(routineBinary);
        dataType = new DataTypeDTO();
        dataType.setId(UUID.randomUUID().toString());
        dataType.setName(UUID.randomUUID().toString());
		dataType.setSchema(UUID.randomUUID().toString());
        dataTypeIds = new LinkedList<>();
        dataTypeIds.add(dataType.getId());
        tag = new TagDTO();
        tag.setId(UUID.randomUUID().toString());
        tags = new LinkedList<>();
        tags.add(tag);

        this.server = getServer();
        serverThread = new Thread(server);
        serverThread.start();

        await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
        client = new LibraryAdminServiceClientFactory().createClient(server.getServiceEndpoint());
    }

    @After
    public void tearDown() throws Exception {
        client.close();
        server.close();
        serverThread.join();
        ServiceRegistry.getInstance().closeAll();
    }

    protected abstract IServer getServer() throws ServerCreationException;

    @Test
    public void findRoutines() throws Exception {
    	when(libraryController.findRoutines("*")).thenReturn(routineIds);

        Future<List<String>> futureRoutines = client.findRoutines("*");
        await().atMost(30, TimeUnit.SECONDS).until(futureRoutines::isDone);

        assertEquals(routineIds, futureRoutines.get());
    }

    @Test
    public void removeRoutine() throws Exception {
        Future<Void> removeStatus = client.removeRoutine(routine.getId());
        await().atMost(30, TimeUnit.SECONDS).until(removeStatus::isDone);

        verify(libraryController, times(1)).removeRoutine(routine.getId());
    }

    @Test
    public void createRoutine() throws Exception {
    	String rId = UUID.randomUUID().toString();
		RoutineDTO toCreate = routine.deepCopy();
		toCreate.setId(null);

		when(libraryController.createRoutine(toCreate)).thenReturn(rId);

        Future<String> futureId = client.createRoutine(toCreate);
        await().atMost(30, TimeUnit.SECONDS).until(futureId::isDone);

        assertEquals(rId, futureId.get());
    }

    @Test
    public void updateRoutine() throws Exception {
		String rId = UUID.randomUUID().toString();

		when(libraryController.updateRoutine(routine)).thenReturn(rId);

        Future<String> futureId = client.updateRoutine(routine);
        await().atMost(30, TimeUnit.SECONDS).until(futureId::isDone);

        assertEquals(rId, futureId.get());
    }

    @Test
    public void uploadRoutineBinary() throws Exception {
    	when(libraryController.uploadRoutineBinary(
    			routine.getId(),
				routineBinary.getMd5(),
				routineBinary.getSizeInBytes(),
				routineBinary.isPrimary(),
				routineBinary.bufferForData()
		)).thenReturn(routineBinary.getId());

        Future<String> futureId = client.uploadRoutineBinary(
            routine.getId(),
            routineBinary.getMd5(),
            routineBinary.getSizeInBytes(),
            routineBinary.isPrimary(),
			routineBinary.bufferForData()
        );
        await().atMost(30, TimeUnit.SECONDS).until(futureId::isDone);

        assertEquals(routineBinary.getId(), futureId.get());
    }

    @Test
    public void removeRoutineBinary() throws Exception {
        Future<Void> removeStatus = client.removeRoutineBinary(
            routine.getId(),
            routineBinary.getId()
        );
        await().atMost(30, TimeUnit.SECONDS).until(removeStatus::isDone);

        verify(libraryController, times(1)).removeRoutineBinary(routine.getId(), routineBinary.getId());
    }

    @Test
    public void findDataTypes() throws Exception {
    	when(libraryController.findDataTypes("*")).thenReturn(dataTypeIds);

        Future<List<String>> futureDataTypes = client.findDataTypes("*");
        await().atMost(30, TimeUnit.SECONDS).until(futureDataTypes::isDone);

        assertEquals(dataTypeIds, futureDataTypes.get());
    }

    @Test
    public void createDataType() throws Exception {
    	when(libraryController.createDataType(
    			dataType.getName(),
				dataType.getSchema()
		)).thenReturn(dataType.getId());

        Future<String> futureId = client.createDataType(
            dataType.getName(),
            dataType.getSchema()
        );
        await().atMost(30, TimeUnit.SECONDS).until(futureId::isDone);

        assertEquals(dataType.getId(), futureId.get());
    }

    @Test
    public void getDataType() throws Exception {
    	when(libraryController.getDataType(dataType.getId())).thenReturn(dataType);

        Future<DataTypeDTO> futureDataType = client.getDataType(dataType.getId());
        await().atMost(30, TimeUnit.SECONDS).until(futureDataType::isDone);

        assertEquals(dataType, futureDataType.get());
    }

    @Test
    public void removeDataType() throws Exception {
        Future<Void> removeStatus = client.removeDataType(dataType.getId());
        await().atMost(30, TimeUnit.SECONDS).until(removeStatus::isDone);

        verify(libraryController, times(1)).removeDataType(dataType.getId());
    }

    @Test
    public void findTags() throws Exception {
    	when(libraryController.findTags("*")).thenReturn(tags);

        Future<List<TagDTO>> futureTags = client.findTags("*");
        await().atMost(30, TimeUnit.SECONDS).until(futureTags::isDone);

        assertEquals(tags, futureTags.get());
    }

    @Test
    public void createTag() throws Exception {
        Future<Void> createStatus = client.createTag(tag.getId(), tag.getDescription());
        await().atMost(30, TimeUnit.SECONDS).until(createStatus::isDone);

        verify(libraryController, times(1)).createTag(tag.getId(), tag.getDescription());
    }

    @Test
    public void removeTag() throws Exception {
        Future<Void> removeStatus = client.removeTag(tag.getId());
        await().atMost(30, TimeUnit.SECONDS).until(removeStatus::isDone);

		verify(libraryController, times(1)).removeTag(tag.getId());
    }

    @Test
    public void createFeature() throws Exception {
        String fId = UUID.randomUUID().toString();

        when(libraryController.createFeature("java", "1.8", "language")).thenReturn(fId);

        Future<String> futureId = client.createFeature("java", "1.8", "language");
        await().atMost(30, TimeUnit.SECONDS).until(futureId::isDone);

        assertEquals(fId, futureId.get());
    }

    @Test
    public void addExtension() throws Exception {
        String fId = UUID.randomUUID().toString();
        String eId = UUID.randomUUID().toString();

        when(libraryController.addExtension(fId, "numpy", "1.8")).thenReturn(eId);

        Future<String> futureId = client.addExtension(fId, "numpy", "1.8");
        await().atMost(30, TimeUnit.SECONDS).until(futureId::isDone);

        assertEquals(eId, futureId.get());
    }

    @Test
    public void findFeatures() throws Exception {
        List<FeatureDTO> featureDTOS = Collections.singletonList(new FeatureDTO());
        when(libraryController.getFeatures("*")).thenReturn(featureDTOS);

        Future<List<FeatureDTO>> future = client.getFeatures("*");
        await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

        assertEquals(featureDTOS, future.get());
    }
}
