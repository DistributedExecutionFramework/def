package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.manager.webservice.ServiceRESTTest;
import at.enfilo.def.manager.webservice.ServiceTest;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.SortingCriterion;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;

public class TaskServiceRESTTest extends ServiceTest {

    private String requestBaseUrl;

    public TaskServiceRESTTest() {
        requestBaseUrl = "http://" + ServiceRESTTest.restEndpoint.getBindAddress() + ":" + ServiceRESTTest.restEndpoint.getPort() + ServiceRESTTest.restEndpoint.getUrlPattern().replace("*", "");
    }

    @Test
    public void getAllTasksWithStateTest() {
        try {
            reset(ServiceRESTTest.taskService.serviceClient);
            when(ServiceRESTTest.taskService.serviceClient.getAllTasksWithState((String)notNull(), (String)notNull(), (ExecutionState)notNull(), (SortingCriterion)notNull())).thenReturn(ServiceRESTTest.taskService.taskIdsFuture);
            when(ServiceRESTTest.taskService.serviceClient.getTask((String)notNull(), (String)notNull(), (String)notNull(), eq(false), eq(false))).thenReturn(ServiceRESTTest.taskService.taskDTOFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs/jId/tasks/state/SUCCESS/sort/NO_SORTING");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.taskService.serviceClient, times(1)).getAllTasksWithState((String)notNull(), (String)notNull(), (ExecutionState)notNull(), (SortingCriterion)notNull());
            verify(ServiceRESTTest.taskService.serviceClient, times(ServiceRESTTest.taskService.taskIds.size())).getTask((String)notNull(), (String)notNull(), (String)notNull(), eq(false), eq(false));

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getSpecificNumberOfTasksWithStateTest() {
        try {
            reset(ServiceRESTTest.taskService.serviceClient);
            when(ServiceRESTTest.taskService.serviceClient.getAllTasksWithState((String)notNull(), (String)notNull(), (ExecutionState)notNull(), (SortingCriterion)notNull())).thenReturn(ServiceRESTTest.taskService.taskIdsFuture);
            when(ServiceRESTTest.taskService.serviceClient.getTask((String)notNull(), (String)notNull(), (String)notNull(), eq(false), eq(false))).thenReturn(ServiceRESTTest.taskService.taskDTOFuture);
            int nrOfTasks = 2;

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs/jId/tasks/state/SUCCESS/sort/NO_SORTING/nrOfTasks/" + Integer.toString(nrOfTasks));
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.taskService.serviceClient, times(1)).getAllTasksWithState((String)notNull(), (String)notNull(), (ExecutionState)notNull(), (SortingCriterion)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getTaskInfoTest() {

        try {
            reset(ServiceRESTTest.taskService.serviceClient);
            when(ServiceRESTTest.taskService.serviceClient.getTask((String)notNull(), (String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.taskService.taskDTOFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs/jId/tasks/tId");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.taskService.serviceClient, times(1)).getTask((String)notNull(), (String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getDataValueOfTaskInputParameterTest() {
        try {
            reset(ServiceRESTTest.taskService.serviceClient);
            reset(ServiceRESTTest.taskService.dataConverter);
            when(ServiceRESTTest.taskService.serviceClient.getTask((String)notNull(), (String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.taskService.taskDTOFuture);
            when(ServiceRESTTest.taskService.dataConverter.convertResourceData((ResourceDTO)notNull())).thenReturn(new String("4.0"));

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs/jId/tasks/tId/inParam/" + ServiceRESTTest.taskService.inParameterName);
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.taskService.serviceClient, times(1)).getTask((String)notNull(), (String)notNull(), (String)notNull());
            verify(ServiceRESTTest.taskService.dataConverter, times(1)).convertResourceData((ResourceDTO)notNull());

        } catch (ClientCommunicationException | TException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getDataValueOfTaskOutputParameter() {
        try {
            reset(ServiceRESTTest.taskService.serviceClient);
            reset(ServiceRESTTest.taskService.dataConverter);
            when(ServiceRESTTest.taskService.serviceClient.getTask((String)notNull(), (String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.taskService.taskDTOFuture);
            when(ServiceRESTTest.taskService.dataConverter.convertResourceData((ResourceDTO)notNull())).thenReturn(new String("4.0"));

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs/jId/tasks/tId/outParam/" + ServiceRESTTest.taskService.outParameter.getId());
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.taskService.serviceClient, times(1)).getTask((String)notNull(), (String)notNull(), (String)notNull());
            verify(ServiceRESTTest.taskService.dataConverter, times(1)).convertResourceData((ResourceDTO)notNull());

        } catch (ClientCommunicationException | TException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void abortTaskTest() {

        try {
            reset(ServiceRESTTest.taskService.serviceClient);
            when(ServiceRESTTest.taskService.serviceClient.abortTask((String)notNull(), (String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.taskService.doneTicketStatusFuture);

            HttpUriRequest request = new HttpPut(requestBaseUrl + "programs/pId/jobs/jId/tasks/tId/abort");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.taskService.serviceClient, times(1)).abortTask((String)notNull(), (String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }
}
