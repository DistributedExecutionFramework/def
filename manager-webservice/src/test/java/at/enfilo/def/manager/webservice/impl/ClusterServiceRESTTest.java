package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.manager.webservice.ServiceRESTTest;
import at.enfilo.def.manager.webservice.ServiceTest;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeType;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;

public class ClusterServiceRESTTest extends ServiceTest {

    private String requestBaseUrl;

    public ClusterServiceRESTTest() {
        requestBaseUrl = "http://" + ServiceRESTTest.restEndpoint.getBindAddress() + ":" + ServiceRESTTest.restEndpoint.getPort() + ServiceRESTTest.restEndpoint.getUrlPattern().replace("*", "");
    }

    @Test
    public void getAllClusterIdsTest() {
        try {
            reset(ServiceRESTTest.clusterService.managerServiceClient);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterIds()).thenReturn(ServiceRESTTest.clusterService.clusterIdFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "clusterIds");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.clusterService.managerServiceClient, times(1)).getClusterIds();

        } catch (IOException | ClientCommunicationException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllClustersTest() {
        try {
            reset(ServiceRESTTest.clusterService.managerServiceClient);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterIds()).thenReturn(ServiceRESTTest.clusterService.clusterIdFuture);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterInfo((String)notNull())).thenReturn(ServiceRESTTest.clusterService.clusterFuture);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "clusters");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.clusterService.managerServiceClient, times(1)).getClusterIds();
            verify(ServiceRESTTest.clusterService.managerServiceClient, times(ServiceRESTTest.clusterService.clusterIds.size())).getClusterInfo((String)notNull());
            verify(ServiceRESTTest.clusterService.managerServiceClient, times(ServiceRESTTest.clusterService.clusterIds.size())).getClusterEndpoint((String)notNull());

        } catch (IOException | ClientCommunicationException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void getClusterInfoTest() {
        try {
            reset(ServiceRESTTest.clusterService.managerServiceClient);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterInfo((String)notNull())).thenReturn(ServiceRESTTest.clusterService.clusterFuture);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "clusters/" + ServiceRESTTest.clusterService.clusterIds.get(0));
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.clusterService.managerServiceClient, times(1)).getClusterInfo((String)notNull());
            verify(ServiceRESTTest.clusterService.managerServiceClient, times(1)).getClusterEndpoint((String)notNull());

        } catch (IOException | ClientCommunicationException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllWorkerIdsOfClusterTest() {
        try {
            reset(ServiceRESTTest.clusterService.managerServiceClient);
            reset(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);
            when(ServiceRESTTest.clusterService.clusterClientMap.get(notNull())).thenReturn(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.clusterClientMap.containsKey(notNull())).thenReturn(true);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getAllNodes(NodeType.WORKER)).thenReturn(ServiceRESTTest.clusterService.workerIdsFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "clusters/" + ServiceRESTTest.clusterService.clusterIds.get(0) + "/workerIds");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.clusterService.clusterServiceClient, times(1)).getAllNodes(NodeType.WORKER);

        } catch (IOException | ClientCommunicationException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllWorkersOfClusterTest() {
        try {
            reset(ServiceRESTTest.clusterService.managerServiceClient);
            reset(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);
            when(ServiceRESTTest.clusterService.clusterClientMap.get(notNull())).thenReturn(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.clusterClientMap.containsKey(notNull())).thenReturn(true);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getAllNodes(NodeType.WORKER)).thenReturn(ServiceRESTTest.clusterService.workerIdsFuture);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getNodeInfo((String)notNull())).thenReturn(ServiceRESTTest.clusterService.workerFuture);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getNodeServiceEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "clusters/" + ServiceRESTTest.clusterService.clusterIds.get(0) + "/workers");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.clusterService.clusterServiceClient, times(1)).getAllNodes(NodeType.WORKER);
            verify(ServiceRESTTest.clusterService.clusterServiceClient, times(ServiceRESTTest.clusterService.workerIds.size())).getNodeInfo((String)notNull());
            verify(ServiceRESTTest.clusterService.clusterServiceClient, times(ServiceRESTTest.clusterService.workerIds.size())).getNodeServiceEndpoint((String)notNull());

        } catch (IOException | ClientCommunicationException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void getNodeInfoTest() {
        try {
            reset(ServiceRESTTest.clusterService.managerServiceClient);
            reset(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);
            when(ServiceRESTTest.clusterService.clusterClientMap.get(notNull())).thenReturn(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.clusterClientMap.containsKey(notNull())).thenReturn(true);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getAllNodes(NodeType.WORKER)).thenReturn(ServiceRESTTest.clusterService.workerIdsFuture);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getNodeInfo((String)notNull())).thenReturn(ServiceRESTTest.clusterService.workerFuture);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getNodeServiceEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "clusters/" + ServiceRESTTest.clusterService.clusterIds.get(0) + "/nodes/" + ServiceRESTTest.clusterService.workerIds.get(0));
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            verify(ServiceRESTTest.clusterService.clusterServiceClient, times(1)).getNodeInfo((String)notNull());
            verify(ServiceRESTTest.clusterService.clusterServiceClient, times(1)).getNodeServiceEndpoint((String)notNull());

        } catch (IOException | ClientCommunicationException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void getNodeEnvironmentTest() {
        try {
            reset(ServiceRESTTest.clusterService.managerServiceClient);
            reset(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);
            when(ServiceRESTTest.clusterService.clusterClientMap.get(notNull())).thenReturn(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.clusterClientMap.containsKey(notNull())).thenReturn(true);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getAllNodes(NodeType.WORKER)).thenReturn(ServiceRESTTest.clusterService.workerIdsFuture);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getNodeEnvironment((String)notNull())).thenReturn(ServiceRESTTest.clusterService.featureFuture);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getNodeServiceEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "clusters/" + ServiceRESTTest.clusterService.clusterIds.get(0) + "/nodes/" + ServiceRESTTest.clusterService.workerIds.get(0) + "/environment");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            verify(ServiceRESTTest.clusterService.clusterServiceClient, times(1)).getNodeEnvironment((String)notNull());

        } catch (IOException | ClientCommunicationException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void getClusterEnvironmentTest() {
        try {
            reset(ServiceRESTTest.clusterService.managerServiceClient);
            reset(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.managerServiceClient.getClusterEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);
            when(ServiceRESTTest.clusterService.clusterClientMap.get(notNull())).thenReturn(ServiceRESTTest.clusterService.clusterServiceClient);
            when(ServiceRESTTest.clusterService.clusterClientMap.containsKey(notNull())).thenReturn(true);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getAllNodes(NodeType.WORKER)).thenReturn(ServiceRESTTest.clusterService.workerIdsFuture);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getEnvironment()).thenReturn(ServiceRESTTest.clusterService.featureFuture);
            when(ServiceRESTTest.clusterService.clusterServiceClient.getNodeServiceEndpoint((String)notNull())).thenReturn(ServiceRESTTest.clusterService.endpointFuture);

            HttpUriRequest request = new HttpGet(requestBaseUrl + "clusters/" + ServiceRESTTest.clusterService.clusterIds.get(0) + "/environment");
            HttpResponse response = HttpClientBuilder.create().build().execute(request);

            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            verify(ServiceRESTTest.clusterService.clusterServiceClient, times(1)).getEnvironment();

        } catch (IOException | ClientCommunicationException e ) {
            e.printStackTrace();
        }
    }
}
