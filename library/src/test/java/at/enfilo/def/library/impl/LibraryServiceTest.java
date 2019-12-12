package at.enfilo.def.library.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.LibraryInfoDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class LibraryServiceTest {
    private IServer server;
    private Thread serverThread;
    private ILibraryServiceClient client;

    private RoutineDTO routine;
    private RoutineBinaryDTO routineBinary1;
    private RoutineBinaryDTO routineBinary2;
    protected LibraryController libraryController;

    @Before
    public void setUp() throws Exception {
        TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

        routine = new RoutineDTO();
        routine.setId(UUID.randomUUID().toString());
        routine.setName(UUID.randomUUID().toString());
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setName("java");
        featureDTO.setVersion(">1.8");
        routine.setRequiredFeatures(Collections.singletonList(featureDTO));
        routineBinary1 = new RoutineBinaryDTO();
        routineBinary1.setId(UUID.randomUUID().toString());
        routineBinary1.setName("binary1");
        routineBinary2 = new RoutineBinaryDTO();
        routineBinary2.setId(UUID.randomUUID().toString());
        routineBinary2.setName("binary2");
        routine.addToRoutineBinaries(routineBinary1);
        routine.addToRoutineBinaries(routineBinary2);

        libraryController = Mockito.mock(LibraryController.class);

        this.server = getServer();
        serverThread = new Thread(server);
        serverThread.start();

        await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
        client = new LibraryServiceClientFactory().createClient(server.getServiceEndpoint());
    }

    protected abstract IServer getServer() throws ServerCreationException;

    @After
    public void tearDown() throws Exception {
        client.close();
        server.close();
        serverThread.join();
        ServiceRegistry.getInstance().closeAll();
    }

    @Test
    public void getInfo() throws Exception {
    	LibraryInfoDTO libraryInfo = new LibraryInfoDTO();
    	libraryInfo.setId(UUID.randomUUID().toString());

    	when(libraryController.getInfo()).thenReturn(libraryInfo);

        Future<LibraryInfoDTO> futureLibraryInfo = client.getInfo();
        await().atMost(30, TimeUnit.SECONDS).until(futureLibraryInfo::isDone);

        assertEquals(libraryInfo, futureLibraryInfo.get());
    }

    @Test
    public void getRoutine() throws Exception {
        when(libraryController.getRoutine(routine.getId())).thenReturn(routine);

        Future<RoutineDTO> futureRoutine = client.getRoutine(routine.getId());
        await().atMost(30, TimeUnit.SECONDS).until(futureRoutine::isDone);

        assertEquals(routine, futureRoutine.get());
    }

    @Test
    public void getRoutineRequiredFeatures() throws Exception {
        when(libraryController.getRoutineRequiredFeatures(routine.getId())).thenReturn(routine.getRequiredFeatures());
        Future<List<FeatureDTO>> featuresFuture = client.getRoutineRequiredFeatures(routine.getId());
        await().atMost(30, TimeUnit.SECONDS).until(featuresFuture::isDone);

        List<FeatureDTO> features = featuresFuture.get();
        assertNotNull(features);
        assertEquals(this.routine.getRequiredFeatures(), features);
    }

    @Test
    public void getRoutineBinary() throws Exception {
		when(libraryController.getRoutineBinary(routineBinary1.getId())).thenReturn(routineBinary1);

		Future<RoutineBinaryDTO> futureRoutineBinary = client.getRoutineBinary(routineBinary1.getId());
		await().atMost(30, TimeUnit.SECONDS).until(futureRoutineBinary::isDone);

		assertEquals(routineBinary1, futureRoutineBinary.get());
	}
}