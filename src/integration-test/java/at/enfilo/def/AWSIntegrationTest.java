package at.enfilo.def;

import at.enfilo.def.client.api.DEFClientFactory;
import at.enfilo.def.client.api.IDEFClient;
import at.enfilo.def.client.api.RoutineInstanceBuilder;
import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.server.CloudCommunication;
import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.demo.DefaultMapper;
import at.enfilo.def.demo.DoubleSumReducer;
import at.enfilo.def.demo.MemoryStorer;
import at.enfilo.def.demo.PICalc;
import at.enfilo.def.library.Library;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.manager.api.ManagerServiceClientFactory;
import at.enfilo.def.manager.server.Manager;
import at.enfilo.def.manager.webservice.server.ManagerWebservice;
import at.enfilo.def.transfer.dto.*;
import org.junit.After;
import org.junit.Before;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;


public class AWSIntegrationTest extends IntegrationTest {

    @Before
    public void setUp() throws Exception {

        services = new LinkedList<>();
        services.add(Manager.getInstance());
        services.add(Library.getInstance());
        services.add(CloudCommunication.getInstance());
        services.add(ManagerWebservice.getInstance());
        thriftServices = new HashMap<>();
        restServices = new HashMap<>();

        // Adapt configuration
        Library.getInstance().getConfiguration().setLibraryType(LibraryType.MASTER);

        startServices();
    }

    //@Test
    public void AWSIntegrationTest() throws Exception {
        // Create Manager Client
        ServiceEndpointDTO managerEndpoint = thriftServices.get(Manager.class).getServiceEndpoint();
        managerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
        IManagerServiceClient managerClient = new ManagerServiceClientFactory().createClient(managerEndpoint);

        // Create DEF client
        IDEFClient defClient = DEFClientFactory.createClient(managerEndpoint);

        // Create Manager Library Client
        ServiceEndpointDTO libraryEndpoint = thriftServices.get(Library.class).getServiceEndpoint();
        libraryEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
        ILibraryServiceClient libraryClient = new LibraryServiceClientFactory().createClient(libraryEndpoint);

        // Create AWS specification
        AWSSpecificationDTO awsSpecification = new AWSSpecificationDTO();
        awsSpecification.setClusterImageId("ami-03a2772319da76304");
        awsSpecification.setClusterInstanceSize("c4.xlarge");
        awsSpecification.setWorkerImageId("ami-09db9de6d69efbfcc");
        awsSpecification.setWorkerInstanceSize("c4.xlarge");
        awsSpecification.setReducerImageId("ami-0639bcd2dcc52197d");
        awsSpecification.setReducerInstanceSize("c4.xlarge");
        awsSpecification.setAccessKeyID(""); // set access key ID
        awsSpecification.setSecretKey(""); // set secret key
        awsSpecification.setKeypairName("MyKeyPair");
        awsSpecification.setVpcId("vpc-0b81e363da048958a");
        awsSpecification.setVpnDynamicIpNetworkAddress("172.27.224.0");
        awsSpecification.setVpnDynamicIpSubnetMaskSuffix(20);
        awsSpecification.setPrivateSubnetId("subnet-0d89a052821c89f84");
        awsSpecification.setPublicSubnetId("subnet-022c6bc747def8248");
        awsSpecification.setRegion("eu-central-1");

        // Create new AWS cluster
        int numberOfWorkers = 1;
        int numberOfReducers = 1;
        Future<String> futureCreateAWSCluster = managerClient.createAWSCluster(numberOfWorkers, numberOfReducers, awsSpecification);
        await().atMost(300, TimeUnit.SECONDS).until(futureCreateAWSCluster::isDone);
        String clusterId = futureCreateAWSCluster.get();
        assertNotNull(clusterId);
        assertNotNull(managerClient.getClusterIds().get());
        assertEquals(1, managerClient.getClusterIds().get().size());
        Future<ClusterInfoDTO> clusterInfoFuture = managerClient.getClusterInfo(clusterId);
        await().atMost(10, TimeUnit.SECONDS).until(futureCreateAWSCluster::isDone);
        ClusterInfoDTO clusterInfo = clusterInfoFuture.get();
        assertNotNull(clusterInfo);
        assertEquals(numberOfWorkers, clusterInfo.getNumberOfWorkers());
        assertEquals(numberOfReducers, clusterInfo.getNumberOfReducers());
        assertEquals(0, clusterInfo.getActivePrograms().size());
        assertEquals(Manager.getInstance().getConfiguration().getId(), clusterInfo.getManagerId());

        // Create Cluster Client
        ServiceEndpointDTO clusterEndpoint = managerClient.getClusterEndpoint(clusterId).get();
        assertNotNull(clusterEndpoint);
        IClusterServiceClient clusterClient = new ClusterServiceClientFactory().createClient(clusterEndpoint);

        // Set default routines
        String defaultMapRoutineId = UUID.nameUUIDFromBytes(DefaultMapper.class.getCanonicalName().getBytes()).toString();
        String storeRoutineId = UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString();
        assertNotNull(libraryClient.getRoutine(defaultMapRoutineId).get());
        assertNotNull(libraryClient.getRoutine(storeRoutineId).get());
        clusterClient.setDefaultMapRoutine(defaultMapRoutineId);
        clusterClient.setStoreRoutine(storeRoutineId);

        // Create Program and Job with reduce
        String userId = UUID.randomUUID().toString();
        String pId = defClient.createProgram(clusterId, userId).get();
        assertNotNull(pId);
        String jId = defClient.createJob(pId).get();
        assertNotNull(jId);
        Future<Void> futureTicketStatus = defClient.attachReduceRoutine(pId, jId, UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString());
        assertNull(futureTicketStatus.get());

        // Create 10 Tasks with PiCalc routine
        List<Future<String>> futureTaskIds = new LinkedList<>();
        int nrTasks = 10;
        double steps = Math.pow(10.0, 9.0);
        double stepSize = 1.0 / steps;
        double taskSteps = steps / nrTasks;
        double start = 0;
        double end = taskSteps;
        String piCalcRoutineId = UUID.nameUUIDFromBytes(PICalc.class.getCanonicalName().getBytes()).toString();
        for (int i = 0; i < nrTasks; i++) {
            RoutineInstanceDTO piCalc = new RoutineInstanceBuilder(piCalcRoutineId)
                    .addParameter("start", new DEFDouble(start))
                    .addParameter("end", new DEFDouble(end))
                    .addParameter("stepSize", new DEFDouble(stepSize))
                    .build();

            Future<String> futureTaskId = defClient.createTask(pId, jId, piCalc);
            futureTaskIds.add(futureTaskId);
            futureTaskId.get();

            start = end + 1;
            end += taskSteps;
        }
        List<String> tIds = new LinkedList<>();
        for (Future<String> futureTaskId : futureTaskIds) {
            String tId = futureTaskId.get();
            tIds.add(tId);
        }

        futureTicketStatus = defClient.markJobAsComplete(pId, jId);
        await().atMost(30, TimeUnit.SECONDS).until(futureTicketStatus::isDone);

        // Boot new workers
        numberOfWorkers = 2;
        futureTicketStatus = managerClient.adjustNodePoolSize(clusterId, numberOfWorkers, NodeType.WORKER);
        await().atMost(120, TimeUnit.SECONDS).until(futureTicketStatus::isDone);
        assertNotNull(futureTicketStatus.get());
        assertNull(futureTicketStatus.get());

        clusterInfoFuture = managerClient.getClusterInfo(clusterId);
        await().atMost(10, TimeUnit.SECONDS).until(futureCreateAWSCluster::isDone);
        clusterInfo = clusterInfoFuture.get();
        assertEquals(numberOfWorkers, clusterInfo.getNumberOfWorkers());
        assertEquals(numberOfReducers, clusterInfo.getNumberOfReducers());

        // Wait for job and fetch task result
        JobDTO job = defClient.waitForJob(pId, jId);
        if (job.getState() == ExecutionState.SUCCESS) {
            for (String tId : tIds) {
                TaskDTO task = defClient.getTask(pId, jId, tId).get();
                assertEquals(tId, task.getId());
                assertEquals(jId, task.getJobId());
                assertEquals(pId, task.getProgramId());
                assertEquals(piCalcRoutineId, task.getObjectiveRoutineId());
                assertEquals(defaultMapRoutineId, task.getMapRoutineId());
                assertTrue(task.isSetOutParameters());
                assertEquals(1, task.getOutParametersSize());
                DEFDouble piPart = defClient.extractOutParameter(task, DEFDouble.class);
                assertTrue(piPart.getValue() > 0);
            }
        }

        //assertEquals(1, job.getReducedResultsSize());

        //DEFDouble pi = defClient.extractReducedResult(job, DEFDouble.class);
        //assertEquals(Math.PI, pi.getValue(), 1e4);

        Future<Void> futureDeleteJob = defClient.deleteJob(pId, jId);
        assertNull(futureDeleteJob.get());
        Future<Void> futureDeleteProgram = defClient.deleteProgram(pId);
        assertNull(futureDeleteProgram.get());

        // Terminate workers
        numberOfWorkers = 1;
        futureTicketStatus = managerClient.adjustNodePoolSize(clusterId, numberOfWorkers, NodeType.WORKER);
        await().atMost(120, TimeUnit.SECONDS).until(futureTicketStatus::isDone);
        assertNotNull(futureTicketStatus.get());
        assertNull(futureTicketStatus.get());

        clusterInfoFuture = managerClient.getClusterInfo(clusterId);
        await().atMost(10, TimeUnit.SECONDS).until(futureCreateAWSCluster::isDone);
        clusterInfo = clusterInfoFuture.get();
        assertEquals(numberOfWorkers, clusterInfo.getNumberOfWorkers());
        assertEquals(numberOfReducers, clusterInfo.getNumberOfReducers());

        // Terminate cluster
        futureTicketStatus = managerClient.deleteCluster(clusterId);
        await().atMost(60, TimeUnit.SECONDS).until(futureTicketStatus::isDone);
        assertNotNull(futureTicketStatus.get());
        assertNull(futureTicketStatus.get());
        assertEquals(0, managerClient.getClusterIds().get().size());
    }

    @After
    public void tearDown() throws Exception {
        TimeUnit.SECONDS.sleep(30);
        stopServices();
    }
}
