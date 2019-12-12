package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.manager.webservice.ServiceRESTTest;
import at.enfilo.def.manager.webservice.ServiceTest;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
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

public class JobServiceRESTTest extends ServiceTest {

    private String requestBaseUrl;

    public JobServiceRESTTest() {
        requestBaseUrl = "http://" + ServiceRESTTest.restEndpoint.getBindAddress() + ":" + ServiceRESTTest.restEndpoint.getPort() + ServiceRESTTest.restEndpoint.getUrlPattern().replace("*", "");
    }

    @Test
    public void getAllJobIdsTest() {
        try {
            reset(ServiceRESTTest.jobService.serviceClient);
            when(ServiceRESTTest.jobService.serviceClient.getAllJobs((String)notNull())).thenReturn(ServiceRESTTest.jobService.jobIdsFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobIds");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.jobService.serviceClient, times(1)).getAllJobs((String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllJobsTest() {
        try {
            reset(ServiceRESTTest.jobService.serviceClient);
            when(ServiceRESTTest.jobService.serviceClient.getAllJobs((String)notNull())).thenReturn(ServiceRESTTest.jobService.jobIdsFuture);
            when(ServiceRESTTest.jobService.serviceClient.getJob((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.jobService.jobDTOFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.jobService.serviceClient, times(1)).getAllJobs((String)notNull());
            verify(ServiceRESTTest.jobService.serviceClient, times(ServiceRESTTest.jobService.jobIds.size())).getJob((String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getNrOfFinishedJobsTest() {
        try {
            reset(ServiceRESTTest.jobService.serviceClient);
            when(ServiceRESTTest.jobService.serviceClient.getAllJobs((String)notNull())).thenReturn(ServiceRESTTest.jobService.jobIdsFuture);
            when(ServiceRESTTest.jobService.serviceClient.getJob((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.jobService.jobDTOFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/nrOfFinishedJobs");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.jobService.serviceClient, times(1)).getAllJobs((String)notNull());
            verify(ServiceRESTTest.jobService.serviceClient, times(ServiceRESTTest.jobService.jobIds.size())).getJob((String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getJobInfoTest() {
        try {
            reset(ServiceRESTTest.jobService.serviceClient);
            when(ServiceRESTTest.jobService.serviceClient.getJob((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.jobService.jobDTOFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs/jId");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.jobService.serviceClient, times(1)).getJob((String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteJobTest() {
        try {
            reset(ServiceRESTTest.jobService.serviceClient);
            when(ServiceRESTTest.jobService.serviceClient.deleteJob((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.jobService.doneTicketStatusFuture);

            HttpUriRequest request = new HttpDelete(requestBaseUrl + "programs/pId/jobs/jId");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.jobService.serviceClient, times(1)).deleteJob((String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void abortJobTest() {

        try {
            reset(ServiceRESTTest.jobService.serviceClient);
            when(ServiceRESTTest.jobService.serviceClient.abortJob((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.jobService.doneTicketStatusFuture);

            HttpUriRequest request = new HttpPut(requestBaseUrl + "programs/pId/jobs/jId/abort");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.jobService.serviceClient, times(1)).abortJob((String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAttachedMapRoutineTest() {
        try {
            reset(ServiceRESTTest.jobService.serviceClient);
            when(ServiceRESTTest.jobService.serviceClient.getAttachedMapRoutine((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.jobService.routineIdFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs/jId/map");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.jobService.serviceClient, times(1)).getAttachedMapRoutine((String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAttachedReduceRoutineTest() {
        try {
            reset(ServiceRESTTest.jobService.serviceClient);
            when(ServiceRESTTest.jobService.serviceClient.getAttachedReduceRoutine((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.jobService.routineIdFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs/jId/reduce");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.jobService.serviceClient, times(1)).getAttachedReduceRoutine((String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getDataValueOfJobReducedResultTest() {
        try {
            reset(ServiceRESTTest.jobService.serviceClient);
            when(ServiceRESTTest.jobService.serviceClient.getJob((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.jobService.jobDTOFuture);
            when(ServiceRESTTest.jobService.dataConverter.convertResourceData((ResourceDTO)notNull())).thenReturn(new String("4.5"));

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId/jobs/jId/reduce/" + ServiceRESTTest.jobService.resourceId);
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.jobService.serviceClient, times(1)).getJob((String)notNull(), (String)notNull());
            verify(ServiceRESTTest.jobService.dataConverter, times(1)).convertResourceData((ResourceDTO)notNull());

        } catch (ClientCommunicationException | TException | IOException e) {
            e.printStackTrace();
        }
    }
}
