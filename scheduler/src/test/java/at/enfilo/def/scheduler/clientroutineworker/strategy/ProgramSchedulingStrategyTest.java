package at.enfilo.def.scheduler.clientroutineworker.strategy;

import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import at.enfilo.def.clientroutine.worker.api.IClientRoutineWorkerServiceClient;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.scheduler.clientroutineworker.api.strategy.IProgramSchedulingStrategy;
import at.enfilo.def.scheduler.general.util.SchedulerConfiguration;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public abstract class ProgramSchedulingStrategyTest {

    protected Map<String, IClientRoutineWorkerServiceClient> nodes;
    protected Map<String, Environment> nodeEnvironments;
    protected SchedulerConfiguration schedulerConfiguration;
    protected IProgramSchedulingStrategy strategy;
    protected IClientRoutineWorkerServiceClient serviceClient;
    protected ILibraryServiceClient libraryServiceClient;

    @Before
    public void setUp() throws Exception {
        nodes = new HashMap<>();
        nodeEnvironments = new HashMap<>();
        ClientRoutineWorkerServiceClientFactory factory = Mockito.mock(ClientRoutineWorkerServiceClientFactory.class);
        serviceClient = Mockito.mock(IClientRoutineWorkerServiceClient.class);
        when(factory.createClient(any())).thenReturn(serviceClient);
        libraryServiceClient = Mockito.mock(ILibraryServiceClient.class);
        schedulerConfiguration = SchedulerConfiguration.getDefault();
        strategy = createStrategy();
    }

    protected abstract IProgramSchedulingStrategy createStrategy();

    @Test
    public void addGetAndRemoveUser() throws Exception {
        // Create mocks and two client routine workers
        nodes.put(UUID.randomUUID().toString(), serviceClient);
        nodes.put(UUID.randomUUID().toString(), serviceClient);
        String uId = UUID.randomUUID().toString();

        Future<Void> futureStatus = Mockito.mock(Future.class);
        when(serviceClient.createQueue(uId)).thenReturn(futureStatus);
        when(serviceClient.deleteQueue(uId)).thenReturn(futureStatus);
        when(futureStatus.isDone()).thenReturn(true);
        when(futureStatus.get()).thenReturn(null);

        // Add a user
        assertTrue(strategy.getUsers().isEmpty());
        strategy.addUser(uId);
        assertTrue(strategy.getUsers().contains(uId));
        verify(serviceClient, times(2)).createQueue(uId);

        // Remove user
        strategy.removeUser(uId);
        assertTrue(strategy.getUsers().isEmpty());
        verify(serviceClient, times(2)).deleteQueue(uId);
    }

    @Test
    public void schedulePrograms() throws Exception {
        // Create mocks and two client routine workers
        nodes.put(UUID.randomUUID().toString(), serviceClient);
        nodes.put(UUID.randomUUID().toString(), serviceClient);
        String uId = UUID.randomUUID().toString();

        Future<Void> futureStatus = Mockito.mock(Future.class);
        when(serviceClient.createQueue(uId)).thenReturn(futureStatus);
        when(serviceClient.queueProgram(eq(uId), any())).thenReturn(futureStatus);
        when(futureStatus.isDone()).thenReturn(true);
        when(futureStatus.get()).thenReturn(null);

        // Add a user and schedule programs
        strategy.addUser(uId);
        Random rnd = new Random();
        int programs = rnd.nextInt(20);
        for (int i = 0; i < programs; i++) {
            ProgramDTO program = new ProgramDTO();
            program.setId(UUID.randomUUID().toString());
            strategy.scheduleProgram(uId, program);
        }

        verify(serviceClient, times(programs)).queueProgram(eq(uId), any());
    }

    @Test
    public void abortProgram() throws Exception {
        // Create mocks and two client routine workers
        String wId = UUID.randomUUID().toString();
        nodes.put(wId, serviceClient);
        String pId = UUID.randomUUID().toString();

        Future<Void> futureStatus = Mockito.mock(Future.class);
        when(serviceClient.abortProgram(pId)).thenReturn(futureStatus);
        when(futureStatus.isDone()).thenReturn(true);
        when(futureStatus.get()).thenReturn(null);

        strategy.abortProgram(wId, pId);

        verify(serviceClient).abortProgram(pId);
    }
}
