package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.manager.webservice.ServiceRESTTest;
import at.enfilo.def.manager.webservice.ServiceTest;
import at.enfilo.def.transfer.dto.ProgramDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;

public class ProgramServiceRESTTest extends ServiceTest {

    private String requestBaseUrl;

    public ProgramServiceRESTTest() {
        requestBaseUrl = "http://" + ServiceRESTTest.restEndpoint.getBindAddress() + ":" + ServiceRESTTest.restEndpoint.getPort() + ServiceRESTTest.restEndpoint.getUrlPattern().replace("*", "");
    }

    @Test
    public void getAllProgramIdsTest() {
        try {
            reset(ServiceRESTTest.programService.serviceClient);
            when(ServiceRESTTest.programService.serviceClient.getAllPrograms((String)notNull())).thenReturn(ServiceRESTTest.programService.idsFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programIds/user/admin");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.programService.serviceClient, times(1)).getAllPrograms((String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllProgramsTest() {
        try {
            reset(ServiceRESTTest.programService.serviceClient);
            when(ServiceRESTTest.programService.serviceClient.getAllPrograms((String)notNull())).thenReturn(ServiceRESTTest.programService.idsFuture);
            when(ServiceRESTTest.programService.serviceClient.getProgram((String)notNull())).thenReturn(ServiceRESTTest.programService.programDTOFuture);
            when(ServiceRESTTest.programService.serviceClient.getAllJobs((String)notNull())).thenReturn(ServiceRESTTest.programService.idsFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/user/admin");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.programService.serviceClient, times(1)).getAllPrograms((String)notNull());
            verify(ServiceRESTTest.programService.serviceClient, times(ServiceRESTTest.programService.ids.size())).getProgram((String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getProgramTest() {
        try {
            reset(ServiceRESTTest.programService.serviceClient);
            when(ServiceRESTTest.programService.serviceClient.getProgram((String)notNull())).thenReturn(ServiceRESTTest.programService.programDTOFuture);
            when(ServiceRESTTest.programService.serviceClient.getAllJobs((String)notNull())).thenReturn(ServiceRESTTest.programService.idsFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "programs/pId");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.programService.serviceClient, times(1)).getProgram((String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteProgramTest() {
        try {
            reset(ServiceRESTTest.programService.serviceClient);
            when(ServiceRESTTest.programService.serviceClient.deleteProgram((String)notNull())).thenReturn(ServiceRESTTest.programService.doneTicketStatusFuture);

            HttpUriRequest request = new HttpDelete(requestBaseUrl + "programs/pId");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.programService.serviceClient, times(1)).deleteProgram((String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void abortProgramTest() {
        try {
            reset(ServiceRESTTest.programService.serviceClient);
            when(ServiceRESTTest.programService.serviceClient.abortProgram((String)notNull())).thenReturn(ServiceRESTTest.programService.doneTicketStatusFuture);

            HttpUriRequest request = new HttpPut(requestBaseUrl + "programs/pId/abort");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.programService.serviceClient, times(1)).abortProgram((String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateProgramNameTest() {
        try {
            reset(ServiceRESTTest.programService.serviceClient);
            when(ServiceRESTTest.programService.serviceClient.updateProgramName((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.programService.doneTicketStatusFuture);
            when(ServiceRESTTest.programService.serviceClient.updateProgramDescription((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.programService.doneTicketStatusFuture);

            ProgramDTO programToSend = new ProgramDTO();
            programToSend.setDescription("Description");
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(programToSend);
            HttpPut request = new HttpPut(requestBaseUrl + "programs/pId");
            StringEntity jsonEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            request.setEntity(jsonEntity);

            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.programService.serviceClient, times(0)).updateProgramName((String)notNull(), (String)notNull());
            verify(ServiceRESTTest.programService.serviceClient, times(1)).updateProgramDescription((String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateProgramDescriptionTest() {
        try {
            reset(ServiceRESTTest.programService.serviceClient);
            when(ServiceRESTTest.programService.serviceClient.updateProgramName((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.programService.doneTicketStatusFuture);
            when(ServiceRESTTest.programService.serviceClient.updateProgramDescription((String)notNull(), (String)notNull())).thenReturn(ServiceRESTTest.programService.doneTicketStatusFuture);

            ProgramDTO programToSend = new ProgramDTO();
            programToSend.setName("Name");
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(programToSend);
            HttpPut request = new HttpPut(requestBaseUrl + "programs/pId");
            StringEntity jsonEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            request.setEntity(jsonEntity);

            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.programService.serviceClient, times(1)).updateProgramName((String)notNull(), (String)notNull());
            verify(ServiceRESTTest.programService.serviceClient, times(0)).updateProgramDescription((String)notNull(), (String)notNull());

        } catch (ClientCommunicationException | IOException e) {
            e.printStackTrace();
        }
    }
}
