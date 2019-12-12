package at.enfilo.def.cloud.communication.logic.specific.aws;

import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.general.mocks.CloudSpecificationMock;
import at.enfilo.def.cloud.communication.logic.specific.aws.mocks.AWSClusterMock;
import at.enfilo.def.cloud.communication.logic.specific.aws.mocks.AWSFactoryMock;
import at.enfilo.def.cloud.communication.logic.specific.aws.mocks.AWSInstanceMock;
import at.enfilo.def.cloud.communication.logic.specific.aws.mocks.AWSSpecificationMock;
import at.enfilo.def.cloud.communication.logic.util.AWSInstanceStatus;
import at.enfilo.def.cloud.communication.logic.util.CloudState;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.MessageFormat;
import java.util.*;

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AWSClusterTest {

    private AWSFactoryMock factory;
    private AWSSpecificationMock specification;
    private AmazonEC2 awsClient;
    private String securityGroupId;
    private String subnetId;
    private String ipAddressString;
    private int subnetMask;
    private String protocol;
    private int fromPort;
    private int toPort;
    private InstanceNetworkInterfaceSpecification instanceNetworkInterfaceSpecifications;
    private Map<InstanceType, InstanceNetworkInterfaceSpecification> instanceNetworkInterfaceSpecificationsMap;
    private AWSInstanceMock clusterInstance;
    private AWSInstanceMock workerInstance;
    private AWSInstanceMock reducerInstance;
    private Map<InstanceType, String> securityGroupIds;
    private Map<String, CloudInstance> nodeInstancesMap;

    @Before
    public void initialize() {
        this.factory = new AWSFactoryMock();
        this.specification = new AWSSpecificationMock();
        this.specification.setAccessKeyID(UUID.randomUUID().toString());
        this.specification.setSecretKey(UUID.randomUUID().toString());
        this.specification.setRegion("Region One");
        this.specification.setInstanceTypeSpecification(InstanceType.CLUSTER, UUID.randomUUID().toString(), "huge");
        this.specification.setInstanceTypeSpecification(InstanceType.WORKER, UUID.randomUUID().toString(), "huge");
        this.specification.setInstanceTypeSpecification(InstanceType.REDUCER, UUID.randomUUID().toString(), "huge");
        this.awsClient = Mockito.mock(AmazonEC2.class);
        this.securityGroupId = UUID.randomUUID().toString();
        this.subnetId = UUID.randomUUID().toString();
        this.ipAddressString = "0.0.0.0";
        this.subnetMask = 20;
        this.protocol = "tcp";
        this.fromPort = 22;
        this.toPort = 33;

        this.instanceNetworkInterfaceSpecifications = new InstanceNetworkInterfaceSpecification();
        this.instanceNetworkInterfaceSpecificationsMap = new HashMap<>();
        this.instanceNetworkInterfaceSpecificationsMap.put(InstanceType.CLUSTER, this.instanceNetworkInterfaceSpecifications);

        this.clusterInstance = new AWSInstanceMock(
                InstanceType.CLUSTER,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK);
        this.clusterInstance.setPublicIpAddress("127.0.0.1");
        this.clusterInstance.setPrivateIpAddress("127.0.0.2");

        this.workerInstance = new AWSInstanceMock(
                InstanceType.WORKER,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );
        this.workerInstance.setPublicIpAddress("127.0.0.1");
        this.workerInstance.setPrivateIpAddress("127.0.0.2");

        this.reducerInstance = new AWSInstanceMock(
                InstanceType.REDUCER,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );
        this.reducerInstance.setPublicIpAddress("127.0.0.1");
        this.reducerInstance.setPrivateIpAddress("127.0.0.2");

        this.securityGroupIds = new HashMap<>();
        this.nodeInstancesMap = new HashMap<>();
    }

    @Test
    public void AWSClusterConstructorTest_running() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        Assert.assertNotNull(clusterMock.getInstanceNetworInterfaceSpecificationsSet());
        Assert.assertNotNull(clusterMock.getSecurityGroupIdsMap());
        Assert.assertSame(this.specification, clusterMock.getAWSSpecificationSet());
    }

    @Test (expected = IllegalArgumentException.class)
    public void AWSClusterConstructorTest_illegalCloudSpecification() {
        CloudSpecification cloudSpecification = new CloudSpecificationMock();

        new AWSClusterMock(this.factory, cloudSpecification, false);
    }

    @Test
    public void initializeTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, true);

        clusterMock.initialize();

        Assert.assertNotNull(clusterMock.awsClient);
        Assert.assertFalse(clusterMock.getSecurityGroupIdsMap().isEmpty());
        Assert.assertFalse(clusterMock.getInstanceNetworInterfaceSpecificationsSet().isEmpty());
        Assert.assertTrue(clusterMock.counterAllowManagerTrafficForSecurityGroup > 0);
    }

    @Test
    public void initializeSecurityGroupTest_securityGroupIsExisting() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        this.securityGroupIds.put(InstanceType.CLUSTER, UUID.randomUUID().toString());
        clusterMock.getAWSSpecificationSet().setVpcId(clusterMock.getVpcId());
        int counterGetSecurityGroupIdCalls = clusterMock.counterGetSecurityGroupIdCalls;
        int counterClearSecurityGroupCalls = clusterMock.counterClearSecurityGroupCalls;
        int counterCreateSecurityGroupCalls = clusterMock.counterCreateSecurityGroupCalls;

        String securityGroupId = clusterMock.initializeSecurityGroup(InstanceType.CLUSTER, clusterMock.getSecurityGroupName(), "Security Group Description");

        Assert.assertNotNull(securityGroupId);
        Assert.assertEquals(counterGetSecurityGroupIdCalls + 1, clusterMock.counterGetSecurityGroupIdCalls);
        Assert.assertEquals(counterClearSecurityGroupCalls + 1, clusterMock.counterClearSecurityGroupCalls);
        Assert.assertEquals(counterCreateSecurityGroupCalls, clusterMock.counterCreateSecurityGroupCalls);
    }

    @Test
    public void initializeSecurityGroupTest_securityGroupIsNotExisting() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        int counterGetSecurityGroupIdCalls = clusterMock.counterGetSecurityGroupIdCalls;
        int counterClearSecurityGroupCalls = clusterMock.counterClearSecurityGroupCalls;
        int counterCreateSecurityGroupCalls = clusterMock.counterCreateSecurityGroupCalls;

        String securityGroupId = clusterMock.initializeSecurityGroup(InstanceType.CLUSTER, "Name", "Description");

        Assert.assertNotNull(securityGroupId);
        Assert.assertEquals(counterGetSecurityGroupIdCalls, clusterMock.counterGetSecurityGroupIdCalls);
        Assert.assertEquals(counterClearSecurityGroupCalls, clusterMock.counterClearSecurityGroupCalls);
        Assert.assertEquals(counterCreateSecurityGroupCalls + 1, clusterMock.counterCreateSecurityGroupCalls);
    }

    @Test
    public void allowManagerTrafficForSecurityGroupTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        int counterAuthorizeSecurityGroupIngressCalls = clusterMock.counterAuthorizeSecurityGroupIngressCalls;
        int counterCreateIpPermissionCalls = clusterMock.counterCreateIpPermissionCalls;

        clusterMock.allowManagerTrafficForSecurityGroup(this.securityGroupId);

        Assert.assertEquals(counterAuthorizeSecurityGroupIngressCalls + 1, clusterMock.counterAuthorizeSecurityGroupIngressCalls);
        Assert.assertEquals(counterCreateIpPermissionCalls + 1, clusterMock.counterCreateIpPermissionCalls);
    }

    @Test
    public void shutdownTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        clusterMock.setClusterInstance(this.clusterInstance);
        this.securityGroupIds.put(InstanceType.CLUSTER, UUID.randomUUID().toString());
        this.securityGroupIds.put(InstanceType.REDUCER, UUID.randomUUID().toString());
        this.securityGroupIds.put(InstanceType.WORKER, UUID.randomUUID().toString());
        clusterMock.setSecurityGroupIdsMap(this.securityGroupIds);
        this.workerInstance.setCloudState(CloudState.RUNNING);
        this.reducerInstance.setCloudState(CloudState.RUNNING);
        this.nodeInstancesMap.put(UUID.randomUUID().toString(), this.workerInstance);
        this.nodeInstancesMap.put(UUID.randomUUID().toString(), this.reducerInstance);
        clusterMock.setNodesInstancesMap(this.nodeInstancesMap);
        int counterTerminateNodeCloudInstancesCalls = clusterMock.counterTerminateNodeCloudInstanceCalls;
        int counterTerminateClusterInstanceCalls = clusterMock.counterTerminateClusterInstanceCalls;
        int counterClearSecurityGroupCalls = clusterMock.counterClearSecurityGroupCalls;

        clusterMock.shutdown();

        Assert.assertEquals(counterTerminateNodeCloudInstancesCalls + 1, clusterMock.counterTerminateNodeCloudInstanceCalls);
        Assert.assertEquals(counterTerminateClusterInstanceCalls + 1, clusterMock.counterTerminateClusterInstanceCalls);
        Assert.assertEquals(counterClearSecurityGroupCalls + 3, clusterMock.counterClearSecurityGroupCalls);
    }

    @Test
    public void applyCloudSettingsForBootingCloudInstanceTest_clusterInstance_running() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        this.securityGroupIds.put(InstanceType.WORKER, UUID.randomUUID().toString());
        this.securityGroupIds.put(InstanceType.REDUCER, UUID.randomUUID().toString());
        clusterMock.setSecurityGroupIdsMap(this.securityGroupIds);

        clusterMock.applyCloudSettingsForBootingCloudInstance(this.clusterInstance);

        verify(clusterMock.awsClient, times(2)).authorizeSecurityGroupIngress((AuthorizeSecurityGroupIngressRequest)notNull());
    }

    @Test (expected = IllegalStateException.class)
    public void applyCloudSettingsForBootingCloudInstanceTest_clusterInstance_missingWorkerSecurityGroup() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        clusterMock.applyCloudSettingsForBootingCloudInstance(this.clusterInstance);
    }

    @Test
    public void applyCloudSettingsForBootingCloudInstanceTest_workerInstance() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        this.securityGroupIds.put(InstanceType.CLUSTER, UUID.randomUUID().toString());
        clusterMock.setSecurityGroupIdsMap(this.securityGroupIds);

        clusterMock.applyCloudSettingsForBootingCloudInstance(this.workerInstance);

        verify(clusterMock.awsClient, times(1)).authorizeSecurityGroupIngress((AuthorizeSecurityGroupIngressRequest)notNull());
    }

    @Test
    public void applyCloudSettingsForBootingCloudInstanceTest_reducerInstance() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        this.securityGroupIds.put(InstanceType.CLUSTER, UUID.randomUUID().toString());
        clusterMock.setSecurityGroupIdsMap(this.securityGroupIds);

        clusterMock.applyCloudSettingsForBootingCloudInstance(this.reducerInstance);

        verify(clusterMock.awsClient, times(1)).authorizeSecurityGroupIngress((AuthorizeSecurityGroupIngressRequest)notNull());
    }

    @Test
    public void createTCPIpPermissionsForCloudInstanceTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        String publicIpCIDR = MessageFormat.format("{0}/{1}", this.clusterInstance.getPublicIpAddress().getHostAddress(), "32");
        String privateIpCIDR = MessageFormat.format("{0}/{1}", this.clusterInstance.getPrivateIpAddress().getHostAddress(), "32");

        IpPermission tcpPermission = clusterMock.createTCPIpPermissionsForCloudInstance(this.clusterInstance);

        Assert.assertNotNull(tcpPermission);
        boolean publicIpIsContained = false;
        boolean privateIpIsContained = false;
        for (IpRange ipRange: tcpPermission.getIpv4Ranges()) {
            if (ipRange.getCidrIp().equals(publicIpCIDR)) {
                publicIpIsContained = true;
            } else if (ipRange.getCidrIp().equals(privateIpCIDR)) {
                privateIpIsContained = true;
            }
        }
        Assert.assertTrue(publicIpIsContained);
        Assert.assertTrue(privateIpIsContained);
        Assert.assertEquals("tcp", tcpPermission.getIpProtocol());
        Assert.assertEquals(40000, tcpPermission.getFromPort().intValue());
        Assert.assertEquals(40072, tcpPermission.getToPort().intValue());
    }

    @Test
    public void createIpPermissionTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        IpPermission ipPermission = clusterMock.createIpPermission(this.ipAddressString, this.subnetMask, this.protocol, this.fromPort, this.toPort);

        Assert.assertNotNull(ipPermission);
        Assert.assertTrue(ipPermission.getIpv4Ranges().get(0).getCidrIp().startsWith(this.ipAddressString));
        Assert.assertTrue(ipPermission.getIpv4Ranges().get(0).getCidrIp().endsWith(Integer.toString(this.subnetMask)));
        Assert.assertEquals(this.protocol, ipPermission.getIpProtocol());
        Assert.assertEquals(this.fromPort, ipPermission.getFromPort().intValue());
        Assert.assertEquals(this.toPort, ipPermission.getToPort().intValue());
    }

    @Test
    public void authorizeSecurityGroupIngressTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        clusterMock.authorizeSecurityGroupIngress(UUID.randomUUID().toString(), null);

        verify(clusterMock.awsClient, times(1)).authorizeSecurityGroupIngress((AuthorizeSecurityGroupIngressRequest)notNull());
    }

    //@Test
    public void buildAWSClientTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        AmazonEC2 awsClient = clusterMock.buildAWSClient();

        Assert.assertNotNull(awsClient);
    }

    @Test
    public void buildInstanceNetworkInterfaceSpecificationsTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        InstanceNetworkInterfaceSpecification instanceNetworkInterfaceSpecification = clusterMock.buildInstanceNetworkInterfaceSpecification(this.securityGroupId, this.subnetId);

        Assert.assertNotNull(instanceNetworkInterfaceSpecification);
        Assert.assertEquals(this.securityGroupId, instanceNetworkInterfaceSpecification.getGroups().get(0));
        Assert.assertEquals(this.subnetId, instanceNetworkInterfaceSpecification.getSubnetId());
    }

    @Test
    public void createSecurityGroupTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        String securityGroupId = clusterMock.createSecurityGroup(
                "Security Group One",
                "Security Group Description",
                UUID.randomUUID().toString());

        Assert.assertNotNull(securityGroupId);
        verify(clusterMock.awsClient, times(1)).createSecurityGroup((CreateSecurityGroupRequest)notNull());
    }

    @Test
    public void isSecurityGroupExisting_securityGroupIsExisting() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        List<SecurityGroup> securityGroups = clusterMock.getAllSecurityGroups();
        SecurityGroup securityGroup = securityGroups.get(0);

        Assert.assertTrue(clusterMock.isSecurityGroupExisting(securityGroup.getGroupName(), securityGroup.getVpcId()));
    }

    @Test
    public void isSecurityGroupExisting_securityGroupIsNotExisting() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        Assert.assertFalse(clusterMock.isSecurityGroupExisting("Test", UUID.randomUUID().toString()));
    }

    @Test
    public void getAllSecurityGroupsTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        List<SecurityGroup> securityGroups = clusterMock.getAllSecurityGroups();

        Assert.assertNotNull(securityGroups);
        Assert.assertNotEquals(0, securityGroups.size());
        verify(clusterMock.awsClient, times(1)).describeSecurityGroups((DescribeSecurityGroupsRequest)notNull());
    }

    @Test
    public void getSecurityGroupIdTest_securityGroupContained() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        List<SecurityGroup> securityGroups = clusterMock.getAllSecurityGroups();
        SecurityGroup securityGroup = securityGroups.get(0);

        String securityGroupId = clusterMock.getSecurityGroupId(securityGroup.getGroupName(), securityGroup.getVpcId());

        Assert.assertEquals(securityGroup.getGroupId(), securityGroupId);
    }

    @Test (expected = IllegalArgumentException.class)
    public void getSecurityGroupIdTest_securityGroupNotContained() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        clusterMock.getSecurityGroupId(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test
    public void clearSecurityGroupTest_securityGroupContained() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        List<SecurityGroup> securityGroups = clusterMock.getAllSecurityGroups();

        clusterMock.clearSecurityGroup(securityGroups.get(0).getGroupId(), securityGroups.get(0).getVpcId());

        verify(clusterMock.awsClient, times(1)).revokeSecurityGroupIngress((RevokeSecurityGroupIngressRequest)notNull());
    }

    @Test
    public void clearSecurityGroupTest_securityGroupNotContained() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        clusterMock.clearSecurityGroup(UUID.randomUUID().toString(), UUID.randomUUID().toString());

        verify(clusterMock.awsClient, times(0)).revokeSecurityGroupIngress((RevokeSecurityGroupIngressRequest)notNull());
    }

    @Test
    public void getNetworkInterfaceSpecificationsTest_running() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        clusterMock.setInstanceNetworkInterfaceSpecifications(this.instanceNetworkInterfaceSpecificationsMap);

        InstanceNetworkInterfaceSpecification instanceNetworkInterfaceSpecification = clusterMock.getNetworkInterfaceSpecifications(InstanceType.CLUSTER);

        Assert.assertSame(this.instanceNetworkInterfaceSpecifications, instanceNetworkInterfaceSpecification);
    }

    @Test (expected = IllegalStateException.class)
    public void getNetworkInterfaceSpecificationsTest_missingSpecifications() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        clusterMock.getNetworkInterfaceSpecifications(InstanceType.CLUSTER);
    }

    @Test (expected = IllegalStateException.class)
    public void getNetworkInterfaceSpecificationsTest_nullParameter() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);

        clusterMock.getNetworkInterfaceSpecifications(null);
    }

    @Test
    public void getAwsClientTest() {
        AWSClusterMock clusterMock = new AWSClusterMock(this.factory, this.specification, false);
        clusterMock.setAWSClient(this.awsClient);

        AmazonEC2 client = clusterMock.getAwsClient();

        Assert.assertSame(this.awsClient, client);
    }

}
