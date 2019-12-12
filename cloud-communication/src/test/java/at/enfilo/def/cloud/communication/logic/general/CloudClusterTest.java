package at.enfilo.def.cloud.communication.logic.general;

import at.enfilo.def.cloud.communication.logic.general.mocks.CloudClusterMock;
import at.enfilo.def.cloud.communication.logic.general.mocks.CloudFactoryMock;
import at.enfilo.def.cloud.communication.logic.general.mocks.CloudInstanceMock;
import at.enfilo.def.cloud.communication.logic.general.mocks.CloudSpecificationMock;
import at.enfilo.def.cloud.communication.logic.util.CloudState;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


public class CloudClusterTest {

    private InstanceType instanceTypeWorker;
    private InstanceType instanceTypeCluster;
    private int nrOfNodesToBoot;
    private InetAddress publicIpAddress;
    private InetAddress privateIpAddress;
    private List<String> cloudInstanceIds;
    private Map<String, CloudInstance> nodeInstancesMap;

    private CloudFactoryMock factory;
    private CloudSpecificationMock specification;
    private CloudInstanceMock clusterInstance;

    @Before
    public void initialize() {
        this.instanceTypeWorker = InstanceType.WORKER;
        this.instanceTypeCluster = InstanceType.CLUSTER;
        this.nrOfNodesToBoot = 3;
        try {
            this.publicIpAddress = InetAddress.getByName("0.0.0.0");
            this.privateIpAddress = InetAddress.getByName("1.1.1.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.cloudInstanceIds = new LinkedList<>();
        for (int i = 0; i < this.nrOfNodesToBoot; i++) {
            this.cloudInstanceIds.add(UUID.randomUUID().toString());
        }

        this.nodeInstancesMap = new HashMap<>();
        for (String cloudInstanceId: cloudInstanceIds) {
            CloudInstanceMock cloudInstanceMock = new CloudInstanceMock(this.instanceTypeWorker);
            cloudInstanceMock.setCloudInstanceId(cloudInstanceId);
            cloudInstanceMock.setCloudState(CloudState.CREATED);
            this.nodeInstancesMap.put(cloudInstanceId, cloudInstanceMock);
        }

        this.factory = new CloudFactoryMock();
        this.specification = new CloudSpecificationMock();
        this.clusterInstance = new CloudInstanceMock(this.instanceTypeWorker);
        this.clusterInstance.setCloudInstanceId(UUID.randomUUID().toString());
    }

    @Test
    public void CloudClusterConstructorTest() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);

        Assert.assertEquals(this.factory, cloudClusterMock.getCloudFactorySet());
        Assert.assertEquals(this.specification, cloudClusterMock.getCloudSpecificationSet());
        Assert.assertNotNull(cloudClusterMock.getNodeInstancesMapSet());
        Assert.assertNotNull(cloudClusterMock.getRemovedInstanceIdsMapSet());
    }

    @Test
    public void mapDEFIdToCloudClusterIdTest_cloudInstanceIsRegistered() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        String defId = "12345";
        String cloudInstanceId = "54321";
        CloudInstanceMock clusterInstance = new CloudInstanceMock(InstanceType.CLUSTER);
        clusterInstance.setCloudInstanceId(cloudInstanceId);
        cloudClusterMock.setClusterInstance(clusterInstance);
        int numberOfEntries = cloudClusterMock.getDEFIdsToCloudIsMap().size();
        Assert.assertFalse(cloudClusterMock.getDEFIdsToCloudIsMap().containsKey(defId));

        cloudClusterMock.mapDEFIdToCloudClusterId(defId, cloudInstanceId);

        Assert.assertEquals(numberOfEntries + 1, cloudClusterMock.getDEFIdsToCloudIsMap().size());
        Assert.assertTrue(cloudClusterMock.getDEFIdsToCloudIsMap().containsKey(defId));
    }

    @Test (expected = IllegalArgumentException.class)
    public void mapDEFIdToCloudClusterIdTest_cloudInstanceIsNotRegistered() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        String defId = "12345";
        String cloudInstanceId = "54321";
        Assert.assertFalse(cloudClusterMock.getDEFIdsToCloudIsMap().containsKey(defId));

        cloudClusterMock.mapDEFIdToCloudClusterId(defId, cloudInstanceId);
    }

    @Test
    public void getCloudClusterIdTest() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        String cloudClusterId = "1234";
        Assert.assertNotEquals(cloudClusterId, cloudClusterMock.getCloudClusterId());
        cloudClusterMock.setCloudClusterId(cloudClusterId);
        Assert.assertEquals(cloudClusterId, cloudClusterMock.getCloudClusterIdSet());

        Assert.assertEquals(cloudClusterId, cloudClusterMock.getCloudClusterId());
    }

    @Test
    public void bootClusterInstanceTest_clusterInstanceNull(){
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        Assert.assertNull(cloudClusterMock.getClusterInstanceSet());
        int counterCreateCloudInstanceCalls = this.factory.counterCreateCloudInstanceCalls;
        int counterApplyCloudSettingsForBootingCloudInstanceCalls = cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls;

        cloudClusterMock.bootClusterInstance();

        Assert.assertNotNull(cloudClusterMock.getClusterInstanceSet());
        Assert.assertEquals(counterCreateCloudInstanceCalls + 1, this.factory.counterCreateCloudInstanceCalls);
        Assert.assertEquals(counterApplyCloudSettingsForBootingCloudInstanceCalls + 1, cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls);
    }

    @Test
    public void bootClusterInstanceTest_cloudStateCreated() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        this.clusterInstance.setCloudState(CloudState.CREATED);
        cloudClusterMock.setClusterInstance(this.clusterInstance);
        Assert.assertNotNull(cloudClusterMock.getClusterInstanceSet());
        int counterCreateCloudInstanceCalls = this.factory.counterCreateCloudInstanceCalls;
        int counterApplyCloudSettingsForBootingCloudInstanceCalls = cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls;
        Assert.assertEquals(CloudState.CREATED, cloudClusterMock.getClusterInstanceSet().getCloudState());

        cloudClusterMock.bootClusterInstance();

        Assert.assertSame(this.clusterInstance, cloudClusterMock.getClusterInstanceSet());
        Assert.assertEquals(counterCreateCloudInstanceCalls, this.factory.counterCreateCloudInstanceCalls);
        Assert.assertEquals(counterApplyCloudSettingsForBootingCloudInstanceCalls + 1, cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls);
    }

    @Test
    public void bootClusterInstanceTest_cloudStateStopped() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        this.clusterInstance.setCloudState(CloudState.STOPPED);
        cloudClusterMock.setClusterInstance(this.clusterInstance);
        Assert.assertNotNull(cloudClusterMock.getClusterInstanceSet());
        int counterCreateCloudInstanceCalls = this.factory.counterCreateCloudInstanceCalls;
        int counterApplyCloudSettingsForBootingCloudInstanceCalls = cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls;
        Assert.assertEquals(CloudState.STOPPED, cloudClusterMock.getClusterInstanceSet().getCloudState());

        cloudClusterMock.bootClusterInstance();

        Assert.assertSame(this.clusterInstance, cloudClusterMock.getClusterInstanceSet());
        Assert.assertEquals(counterCreateCloudInstanceCalls, this.factory.counterCreateCloudInstanceCalls);
        Assert.assertEquals(counterApplyCloudSettingsForBootingCloudInstanceCalls + 1, cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls);
    }

    @Test
    public void bootClusterInstanceTest_otherCloudState() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        this.clusterInstance.setCloudState(CloudState.TERMINATED);
        cloudClusterMock.setClusterInstance(this.clusterInstance);
        Assert.assertNotNull(cloudClusterMock.getClusterInstanceSet());
        int counterCreateCloudInstanceCalls = this.factory.counterCreateCloudInstanceCalls;
        int counterApplyCloudSettingsForBootingCloudInstanceCalls = cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls;
        Assert.assertEquals(CloudState.TERMINATED, cloudClusterMock.getClusterInstanceSet().getCloudState());

        cloudClusterMock.bootClusterInstance();

        Assert.assertSame(this.clusterInstance, cloudClusterMock.getClusterInstanceSet());
        Assert.assertEquals(counterCreateCloudInstanceCalls, this.factory.counterCreateCloudInstanceCalls);
        Assert.assertEquals(counterApplyCloudSettingsForBootingCloudInstanceCalls, cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls);
    }

    @Test
    public void bootNodesTest_running() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        int counterCreateCloudInstanceCalls = this.factory.counterCreateCloudInstanceCalls;
        int nrOfNodesInInstancesMap = cloudClusterMock.getNodeInstancesMapSet().size();
        int counterApplyCloudSettingsForBootingCloudInstanceCalls = cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls;

        Collection<String> bootedNodesIds = cloudClusterMock.bootNodes(this.instanceTypeWorker, this.nrOfNodesToBoot);

        Assert.assertEquals(counterCreateCloudInstanceCalls + this.nrOfNodesToBoot, this.factory.counterCreateCloudInstanceCalls);
        Assert.assertEquals(nrOfNodesInInstancesMap + this.nrOfNodesToBoot, cloudClusterMock.getNodeInstancesMapSet().size());
        Assert.assertEquals(counterApplyCloudSettingsForBootingCloudInstanceCalls + this.nrOfNodesToBoot, cloudClusterMock.counterApplyCloudSettingsForBootingCloudInstanceCalls);
        Assert.assertEquals(this.nrOfNodesToBoot, bootedNodesIds.size());
    }

    @Test (expected = IllegalArgumentException.class)
    public void bootNodesTest_instanceTypeCluster() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);

        cloudClusterMock.bootNodes(this.instanceTypeCluster, this.nrOfNodesToBoot);
    }

    @Test
    public void terminateNodeCloudInstancesTest_running() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        cloudClusterMock.setNodeInstancesMap(this.nodeInstancesMap);
        int nrOfEntriesInNodesInstancesMap = cloudClusterMock.getNodeInstancesMapSet().size();
        int nrOfRemovedInstances = cloudClusterMock.getRemovedInstanceIdsMapSet().size();

        cloudClusterMock.terminateNodeCloudInstances(this.cloudInstanceIds);

        Assert.assertEquals(nrOfEntriesInNodesInstancesMap - this.cloudInstanceIds.size(), cloudClusterMock.getNodeInstancesMapSet().size());
        Assert.assertEquals(nrOfRemovedInstances + this.cloudInstanceIds.size(), cloudClusterMock.getRemovedInstanceIdsMapSet().size());
    }

    @Test (expected = IllegalArgumentException.class)
    public void terminateNodeCloudInstancesTest_terminateCluster() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        cloudClusterMock.setClusterInstance(this.clusterInstance);

        cloudClusterMock.terminateNodes(new LinkedList<>(Arrays.asList(this.clusterInstance.getCloudInstanceId())));
    }

    @Test (expected = IllegalStateException.class)
    public void terminateNodeCloudInstancesTest_wrongCloudInstanceId() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);

        cloudClusterMock.terminateNodeCloudInstances(this.cloudInstanceIds);
    }

    @Test
    public void terminateNodeCloudInstancesTest_cloudInstancesCantBeTerminated() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, true);
        cloudClusterMock.setNodeInstancesMap(this.nodeInstancesMap);
        int nrOfEntriesInNodeInstancesMap = cloudClusterMock.getNodeInstancesMapSet().size();
        int nrOfRemovedInstances = cloudClusterMock.getRemovedInstanceIdsMapSet().size();

        cloudClusterMock.terminateNodeCloudInstances(this.cloudInstanceIds);

        Assert.assertEquals(nrOfEntriesInNodeInstancesMap, cloudClusterMock.getNodeInstancesMapSet().size());
        Assert.assertEquals(nrOfRemovedInstances, cloudClusterMock.getRemovedInstanceIdsMapSet().size());
    }

    @Test (expected = IllegalArgumentException.class)
    public void terminateNodeCloudInstancesTest_nullParameter() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);

        cloudClusterMock.terminateNodes(null);
    }

    @Test
    public void terminateNodesTest_running() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        String defInstanceId1 = "12345";
        String defInstanceId2 = "23456";
        List<String> defInstanceIds = new LinkedList<>();
        defInstanceIds.add(defInstanceId1);
        defInstanceIds.add(defInstanceId2);
        String cloudInstanceId1 = "54321";
        String cloudInstanceId2 = "65432";
        CloudInstanceMock cloudInstance1 = new CloudInstanceMock(InstanceType.WORKER);
        cloudInstance1.setCloudInstanceId(cloudInstanceId1);
        CloudInstanceMock cloudInstance2 = new CloudInstanceMock(InstanceType.WORKER);
        cloudInstance2.setCloudInstanceId(cloudInstanceId2);
        Map<String, CloudInstance> nodeMap = new HashMap<>();
        nodeMap.put(cloudInstanceId1, cloudInstance1);
        nodeMap.put(cloudInstanceId2, cloudInstance2);
        cloudClusterMock.setNodeInstancesMap(nodeMap);
        cloudClusterMock.mapDEFIdToCloudClusterId(defInstanceId1, cloudInstanceId1);
        cloudClusterMock.mapDEFIdToCloudClusterId(defInstanceId2, cloudInstanceId2);
        int nrOfDEFIdsMappedToCloudIs = cloudClusterMock.getDEFIdsToCloudIsMap().size();
        int counterTerminateNodeCloudInstancesCalls = cloudClusterMock.counterTerminateNodeCloudInstancesCalls;

        cloudClusterMock.terminateNodes(defInstanceIds);

        Assert.assertEquals(nrOfDEFIdsMappedToCloudIs - defInstanceIds.size(), cloudClusterMock.getDEFIdsToCloudIsMap().size());
        Assert.assertEquals(counterTerminateNodeCloudInstancesCalls + 1, cloudClusterMock.counterTerminateNodeCloudInstancesCalls);
    }

    @Test (expected = IllegalArgumentException.class)
    public void terminateNodesTest_wrongDEFInstanceId() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        List<String> nodeIds = new LinkedList<>();
        nodeIds.add("12345");

        cloudClusterMock.terminateNodes(nodeIds);
    }

    @Test (expected = IllegalArgumentException.class)
    public void terminateNodesTest_nullParameter() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);

        cloudClusterMock.terminateNodes(null);
    }

    @Test
    public void terminateClusterInstanceTest_running() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, true);
        this.clusterInstance.setCloudState(CloudState.RUNNING);
        cloudClusterMock.setClusterInstance(this.clusterInstance);
        Assert.assertNotNull(cloudClusterMock.getClusterInstanceSet());
        int nrOfRemovedInstances = cloudClusterMock.getRemovedInstanceIdsMapSet().size();

        cloudClusterMock.terminateClusterInstance();

        Assert.assertEquals(nrOfRemovedInstances + 1, cloudClusterMock.getRemovedInstanceIdsMapSet().size());
        Assert.assertNull(cloudClusterMock.getClusterInstanceSet());
    }

    @Test (expected = IllegalStateException.class)
    public void terminateClusterInstanceTest_clusterInstanceNull() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        Assert.assertNull(cloudClusterMock.getClusterInstanceSet());

        cloudClusterMock.terminateClusterInstance();
    }

    @Test (expected = IllegalStateException.class)
    public void terminateClusterInstanceTest_clusterInstanceCantBeTerminated() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, true);
        this.clusterInstance.setCloudState(CloudState.SHUTTING_DOWN);
        cloudClusterMock.setClusterInstance(this.clusterInstance);

        cloudClusterMock.terminateClusterInstance();
    }

    @Test
    public void canCloudInstanceBeTerminatedTest_true() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, true);

        this.clusterInstance.setCloudState(CloudState.RUNNING);
        Assert.assertTrue(cloudClusterMock.canCloudInstanceBeTerminated(this.clusterInstance));

        this.clusterInstance.setCloudState(CloudState.STOPPED);
        Assert.assertTrue(cloudClusterMock.canCloudInstanceBeTerminated(this.clusterInstance));

        this.clusterInstance.setCloudState(CloudState.BOOTING);
        Assert.assertTrue(cloudClusterMock.canCloudInstanceBeTerminated(this.clusterInstance));

        this.clusterInstance.setCloudState(CloudState.STOPPING);
        Assert.assertTrue(cloudClusterMock.canCloudInstanceBeTerminated(this.clusterInstance));
    }

    @Test
    public void canCloudInstanceBeTerminatedTest_false() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, true);

        this.clusterInstance.setCloudState(CloudState.CREATED);
        Assert.assertFalse(cloudClusterMock.canCloudInstanceBeTerminated(this.clusterInstance));

        this.clusterInstance.setCloudState(CloudState.SHUTTING_DOWN);
        Assert.assertFalse(cloudClusterMock.canCloudInstanceBeTerminated(this.clusterInstance));

        this.clusterInstance.setCloudState(CloudState.TERMINATED);
        Assert.assertFalse(cloudClusterMock.canCloudInstanceBeTerminated(this.clusterInstance));

        this.clusterInstance.setCloudState(CloudState.ERROR);
        Assert.assertFalse(cloudClusterMock.canCloudInstanceBeTerminated(this.clusterInstance));

        this.clusterInstance.setCloudState(CloudState.UNDEFINED);
        Assert.assertFalse(cloudClusterMock.canCloudInstanceBeTerminated(this.clusterInstance));
    }

    @Test
    public void getPublicIPAddressOfCloudInstanceTest() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        this.clusterInstance.setPublicIpAddress(this.publicIpAddress);
        cloudClusterMock.setClusterInstance(this.clusterInstance);

        InetAddress publicIpAddress = cloudClusterMock.getPublicIPAddressOfCloudInstance(this.clusterInstance.getCloudInstanceId());

        Assert.assertEquals(this.publicIpAddress, publicIpAddress);
    }

    @Test
    public void getPrivateIPAddressOfCloudInstanceTest() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        this.clusterInstance.setPrivateIpAddress(this.privateIpAddress);
        cloudClusterMock.setClusterInstance(this.clusterInstance);

        InetAddress privateIpAddress = cloudClusterMock.getPrivateIPAddressOfCloudInstance(this.clusterInstance.getCloudInstanceId());

        Assert.assertEquals(this.privateIpAddress, privateIpAddress);
    }

    @Test
    public void getCloudInstanceTest_isClusterInstance() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        cloudClusterMock.setClusterInstance(this.clusterInstance);

        CloudInstance instance = cloudClusterMock.getCloudInstance(this.clusterInstance.getCloudInstanceId());

        Assert.assertSame(this.clusterInstance, instance);
    }

    @Test
    public void getCloudInstanceTest_isNodeInstance() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        cloudClusterMock.setNodeInstancesMap(this.nodeInstancesMap);
        String instanceId = this.cloudInstanceIds.get(0);
        CloudInstance nodeInstance = this.nodeInstancesMap.get(instanceId);

        CloudInstance instance = cloudClusterMock.getCloudInstance(instanceId);

        Assert.assertSame(nodeInstance, instance);
    }

    @Test (expected = IllegalStateException.class)
    public void getCloudInstanceTest_noCloudInstance() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);

        cloudClusterMock.getCloudInstance(this.cloudInstanceIds.get(0));
    }

    @Test (expected = IllegalStateException.class)
    public void getCloudInstanceTest_nullParameter() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);

        cloudClusterMock.getCloudInstance(null);
    }

    @Test
    public void isCloudInstanceWithIdRegisteredTest_isRegisteredAsCluster() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        String cloudInstanceId = "1234";
        CloudInstanceMock clusterInstance = new CloudInstanceMock(InstanceType.CLUSTER);
        clusterInstance.setCloudInstanceId(cloudInstanceId);
        cloudClusterMock.setClusterInstance(clusterInstance);

        Assert.assertTrue(cloudClusterMock.isCloudInstanceWithIdRegistered(cloudInstanceId));
    }

    @Test
    public void isCloudInstanceWithIdRegisteredTest_isRegisteredAsNode() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        String cloudInstanceId = "1234";
        CloudInstanceMock cloudInstance = new CloudInstanceMock(InstanceType.WORKER);
        cloudInstance.setCloudInstanceId(cloudInstanceId);
        Map<String, CloudInstance> nodeInstancesMap = new HashMap<>();
        nodeInstancesMap.put(cloudInstanceId, cloudInstance);
        cloudClusterMock.setNodeInstancesMap(nodeInstancesMap);

        Assert.assertTrue(cloudClusterMock.isCloudInstanceWithIdRegistered(cloudInstanceId));
    }

    @Test
    public void isCloudInstanceWithIdRegisteredTest_isNotRegistered() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);

        Assert.assertFalse(cloudClusterMock.isCloudInstanceWithIdRegistered("1234"));
    }

    @Test
    public void getCloudInstanceIdTest_DEFInstanceIsRegistered() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);
        String defId = "12345";
        String cloudInstanceId = "54321";
        CloudInstanceMock cloudInstance = new CloudInstanceMock(InstanceType.CLUSTER);
        cloudInstance.setCloudInstanceId(cloudInstanceId);
        cloudClusterMock.setClusterInstance(cloudInstance);
        cloudClusterMock.mapDEFIdToCloudClusterId(defId, cloudInstanceId);

        Assert.assertEquals(cloudInstanceId, cloudClusterMock.getCloudInstanceId(defId));
    }

    @Test (expected = IllegalArgumentException.class)
    public void getCloudInstanceIdTest_DEFInstanceIsNotRegistered() {
        CloudClusterMock cloudClusterMock = new CloudClusterMock(this.factory, this.specification, false);

        cloudClusterMock.getCloudInstanceId("1234");
    }

}
