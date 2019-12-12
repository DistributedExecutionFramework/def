package at.enfilo.def.demo;

import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class PiCalcClientRoutineTest {

    @Test
    public void piProgram() throws Exception {
        PiCalcClientRoutine piCalcClientRoutine = Mockito.mock(PiCalcClientRoutine.class);
        doCallRealMethod().when(piCalcClientRoutine).routine(anyString(), any());

        String pId = UUID.randomUUID().toString();
        IExecLogicServiceClient client = Mockito.mock(IExecLogicServiceClient.class);
        JobDTO job = new JobDTO();
        TaskDTO task = new TaskDTO();
        task.setId(UUID.randomUUID().toString());
        task.setOutParameters(Arrays.asList(new ResourceDTO()));
        List<String> jobIds = Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        List<String> taskIds = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            taskIds.add(UUID.randomUUID().toString());
        }

        Future<String> futureString = Mockito.mock(Future.class);
        when(futureString.get()).thenReturn(UUID.randomUUID().toString());
        Future<Void> futureStatus = Mockito.mock(Future.class);
        when(futureStatus.get()).thenReturn(null);
        Future<List<String>> futureJobList = Mockito.mock(Future.class);
        when(futureJobList.get()).thenReturn(jobIds);
        Future<List<String>> futureTaskList = Mockito.mock(Future.class);
        when(futureTaskList.get()).thenReturn(taskIds);
        Future<TaskDTO> futureTask = Mockito.mock(Future.class);
        when(futureTask.get()).thenReturn(task);

        when(client.createSharedResource(anyString(), anyString(), any())).thenReturn(futureString);
        when(client.createJob(pId)).thenReturn(futureString);
        when(client.createTask(eq(pId), anyString(), any())).thenReturn(futureString);
        when(client.markJobAsComplete(eq(pId), anyString())).thenReturn(futureStatus);
        when(client.waitForJob(eq(pId), anyString())).thenReturn(job);
        when(client.getAllJobs(pId)).thenReturn(futureJobList);
        when(client.getAllTasks(eq(pId), anyString(), any())).thenReturn(futureTaskList);
        when(client.getTask(eq(pId), anyString(), anyString())).thenReturn(futureTask);
        when(client.deleteJob(eq(pId), anyString())).thenReturn(futureStatus);

        piCalcClientRoutine.routine(pId, client);

        verify(client, times(1)).createSharedResource(anyString(), anyString(), any());
        verify(client, times(2)).createJob(anyString());
        verify(client, times(20)).createTask(anyString(), anyString(), any());
        verify(client, times(2)).markJobAsComplete(anyString(), anyString());
        verify(client, times(2)).waitForJob(anyString(), anyString());
        verify(client, times(1)).getAllJobs(anyString());
        verify(client, times(2)).getAllTasks(anyString(), anyString(), any());
        verify(client, times(20)).getTask(anyString(), anyString(), anyString());
        verify(client, times(2)).deleteJob(anyString(), anyString());
    }
}
