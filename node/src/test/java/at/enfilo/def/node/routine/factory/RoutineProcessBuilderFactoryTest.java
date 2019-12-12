package at.enfilo.def.node.routine.factory;

import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.node.routine.exec.SequenceStep;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.dto.RoutineType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;

public class RoutineProcessBuilderFactoryTest {

    private NodeConfiguration configuration;
    private ILibraryServiceClient libraryServiceClient;
    private RoutineDTO storeRoutine;
    private RoutineDTO objectiveRoutine;
    private SequenceStep storeSequenceStep;
    private SequenceStep objectiveSequenceStep;
    private ProcessBuilder storeProcessBuilder;
    private ProcessBuilder objectiveProcessBuilder;
    private IRoutineProcessBuilderFactory processBuilderFactory;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        storeRoutine = new RoutineDTO();
        storeRoutine.setId(UUID.randomUUID().toString());
        storeRoutine.setType(RoutineType.STORE);
        objectiveRoutine = new RoutineDTO();
        objectiveRoutine.setId(UUID.randomUUID().toString());
        objectiveRoutine.setType(RoutineType.OBJECTIVE);
        storeSequenceStep = Mockito.mock(SequenceStep.class);
        when(storeSequenceStep.getRoutineId()).thenReturn(storeRoutine.getId());
        when(storeSequenceStep.getRoutineType()).thenReturn(storeRoutine.getType());
        objectiveSequenceStep = Mockito.mock(SequenceStep.class);
        when(objectiveSequenceStep.getRoutineId()).thenReturn(objectiveRoutine.getId());
        when(objectiveSequenceStep.getRoutineType()).thenReturn(objectiveRoutine.getType());

        configuration = NodeConfiguration.getDefault();
        libraryServiceClient = Mockito.mock(ILibraryServiceClient.class);
        Future<RoutineDTO> storeFuture = Mockito.mock(Future.class);
        Future<RoutineDTO> objectiveFuture = Mockito.mock(Future.class);
        when(storeFuture.get()).thenReturn(storeRoutine);
        when(objectiveFuture.get()).thenReturn(objectiveRoutine);
        when(libraryServiceClient.getRoutine(storeRoutine.getId())).thenReturn(storeFuture);
        when(libraryServiceClient.getRoutine(objectiveRoutine.getId())).thenReturn(objectiveFuture);

        storeProcessBuilder = new ProcessBuilder();
        objectiveProcessBuilder = new ProcessBuilder();

        processBuilderFactory = Mockito.mock(IRoutineProcessBuilderFactory.class);
        when(processBuilderFactory.getRoutineProcessBuilder(any(), any(),any(), eq(true))).thenReturn(objectiveProcessBuilder);
        when(processBuilderFactory.getRoutineProcessBuilder(any(), any(), any(), eq(false))).thenReturn(storeProcessBuilder);

    }

    @Test
    public void build() throws Exception {
        /*RoutineProcessBuilderFactory factory = new RoutineProcessBuilderFactory(libraryServiceClient, configuration, processBuilderFactory);
        Path path = Paths.get(configuration.getWorkingDir());
        assertSame(factory.build(path, storeSequenceStep), storeProcessBuilder);
        assertSame(factory.build(path, objectiveSequenceStep), objectiveProcessBuilder);

        assertSame(factory.build(path, storeRoutine, storeSequenceStep), storeProcessBuilder);
        assertSame(factory.build(path, objectiveRoutine, objectiveSequenceStep), objectiveProcessBuilder);

        assertEquals(factory.build(path, storeSequenceStep).directory().getAbsolutePath(), path.toFile().getAbsolutePath());*/
    }
}
