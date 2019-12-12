package at.enfilo.def.cloud.communication.logic.specific.aws.mocks;

import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.general.ICloudFactory;
import at.enfilo.def.cloud.communication.logic.specific.aws.*;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import org.mockito.Mockito;

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.util.*;

public class AWSClusterMock extends AWSCluster{

    private boolean doInitialize;
    private String securityGroupName;
    private String vpcId;

    public int counterTerminateNodeCloudInstanceCalls = 0;
    public int counterTerminateNodesCalls = 0;
    public int counterTerminateClusterInstanceCalls = 0;
    public int counterClearSecurityGroupCalls = 0;
    public int counterAuthorizeSecurityGroupIngressCalls = 0;
    public int counterCreateIpPermissionCalls = 0;
    public int counterGetSecurityGroupIdCalls = 0;
    public int counterCreateSecurityGroupCalls = 0;
    public int counterAllowManagerTrafficForSecurityGroup = 0;

    /**
     * Creates an {@link AWSCluster} object with the given {@link ICloudFactory} and the given {@link CloudSpecification}
     *
     * @param cloudFactory       the {@link ICloudFactory} of the specific type {@link AWSFactory}
     * @param cloudSpecification the {@link CloudSpecification} of the specific type {@link AWSSpecification}
     * @throws IllegalArgumentException if the given {@link CloudSpecification} is not of type {@link AWSSpecification}
     */
    public AWSClusterMock(ICloudFactory cloudFactory, CloudSpecification cloudSpecification, boolean doInitialize) {
        super(cloudFactory, cloudSpecification);

        this.doInitialize = doInitialize;
        this.securityGroupName = "Security Group One";
        this.vpcId = UUID.randomUUID().toString();

        initMockingComponents();
    }

    private void initMockingComponents() {
        this.awsClient = Mockito.mock(AmazonEC2.class);

        IpPermission ipPermission = new IpPermission();

        SecurityGroup securityGroup = new SecurityGroup();
        securityGroup.setGroupId(UUID.randomUUID().toString());
        securityGroup.setGroupName(this.securityGroupName);
        securityGroup.setVpcId(this.vpcId);
        securityGroup.setIpPermissions(new LinkedList<>(Arrays.asList(ipPermission)));

        when(this.awsClient.revokeSecurityGroupIngress((RevokeSecurityGroupIngressRequest)notNull())).thenReturn(null);

        when(this.awsClient.describeSecurityGroups((DescribeSecurityGroupsRequest)notNull())).then(invocation -> {
            DescribeSecurityGroupsResult describeSecurityGroupsResult = new DescribeSecurityGroupsResult();
            describeSecurityGroupsResult.setSecurityGroups(new LinkedList<>(Arrays.asList(securityGroup)));
            return describeSecurityGroupsResult;
        });

        when(this.awsClient.createSecurityGroup((CreateSecurityGroupRequest)notNull())).then(invocation -> {
            CreateSecurityGroupResult createSecurityGroupResult = new CreateSecurityGroupResult();
            createSecurityGroupResult.setGroupId(UUID.randomUUID().toString());
            return createSecurityGroupResult;
        });

        when(this.awsClient.authorizeSecurityGroupIngress((AuthorizeSecurityGroupIngressRequest)notNull())).thenReturn(null);
    }

    @Override
    public void initialize() {
        if (doInitialize) {
            super.initialize();
        }
    }

    @Override
    public List<SecurityGroup> getAllSecurityGroups() {
        return super.getAllSecurityGroups();
    }

    @Override
    public void clearSecurityGroup(String securityGroupId, String vpcId) {
        this.counterClearSecurityGroupCalls++;
        super.clearSecurityGroup(securityGroupId, vpcId);
    }

    @Override
    public String getSecurityGroupId(String securityGroupName, String vpcId) {
        this.counterGetSecurityGroupIdCalls++;
        return super.getSecurityGroupId(securityGroupName, vpcId);
    }

    @Override
    public boolean isSecurityGroupExisting(String securityGroupName, String vpcId) {
        return super.isSecurityGroupExisting(securityGroupName, vpcId);
    }

    @Override
    public String createSecurityGroup(String securityGroupName, String securityGroupDescription, String vpcId) {
        this.counterCreateSecurityGroupCalls++;
        return super.createSecurityGroup(securityGroupName, securityGroupDescription, vpcId);
    }

    @Override
    public InstanceNetworkInterfaceSpecification buildInstanceNetworkInterfaceSpecification(String securityGroupId, String subnetId) {
        return super.buildInstanceNetworkInterfaceSpecification(securityGroupId, subnetId);
    }

    @Override
    public AmazonEC2 buildAWSClient() {
        if (this.doInitialize) {
            return this.awsClient;
        }
        return super.buildAWSClient();
    }

    @Override
    public void authorizeSecurityGroupIngress(String securityGroupId, IpPermission ipPermission) {
        this.counterAuthorizeSecurityGroupIngressCalls++;
        super.authorizeSecurityGroupIngress(securityGroupId, ipPermission);
    }

    @Override
    public IpPermission createIpPermission(String ipAddress, int subnetMask, String protocol, int fromPort, int toPort) {
        this.counterCreateIpPermissionCalls++;
        return super.createIpPermission(ipAddress, subnetMask, protocol, fromPort, toPort);
    }

    @Override
    public IpPermission createTCPIpPermissionsForCloudInstance(CloudInstance cloudInstance) {
        return super.createTCPIpPermissionsForCloudInstance(cloudInstance);
    }

    @Override
    public void applyCloudSettingsForBootingCloudInstance(CloudInstance cloudInstance) {
        super.applyCloudSettingsForBootingCloudInstance(cloudInstance);
    }

    public void setInstanceNetworkInterfaceSpecifications(Map<InstanceType, InstanceNetworkInterfaceSpecification> instanceNetworkInterfaceSpecifications) {
        this.instanceNetworkInterfaceSpecifications = instanceNetworkInterfaceSpecifications;
    }

    public Map<InstanceType, InstanceNetworkInterfaceSpecification> getInstanceNetworInterfaceSpecificationsSet() {
        return this.instanceNetworkInterfaceSpecifications;
    }

    public AWSSpecification getAWSSpecificationSet() {
        return this.awsSpecification;
    }

    public AmazonEC2 getAWSClientSet() {
        return this.awsClient;
    }

    public void setAWSClient(AmazonEC2 awsClient) {
        this.awsClient = awsClient;
    }

    public Map<InstanceType, String> getSecurityGroupIdsMap() {
        return this.securityGroupIds;
    }

    public void setSecurityGroupIdsMap(Map<InstanceType, String> map) {
        this.securityGroupIds = map;
    }

    @Override
    protected void terminateNodeCloudInstances(Collection<String> cloudInstanceIds) {
        this.counterTerminateNodeCloudInstanceCalls++;
        super.terminateNodeCloudInstances(cloudInstanceIds);
    }

    @Override
    public void terminateNodes(Collection<String> cloudInstanceIds) {
        this.counterTerminateNodesCalls++;
        super.terminateNodes(cloudInstanceIds);
    }

    @Override
    protected void terminateClusterInstance() {
        this.counterTerminateClusterInstanceCalls++;
        super.terminateClusterInstance();
    }

    public void setNodesInstancesMap(Map<String, CloudInstance> map) {
        this.nodeInstancesMap = map;
    }

    public void setClusterInstance(CloudInstance clusterInstance) {
        this.clusterInstance = clusterInstance;
    }

    @Override
    public void allowManagerTrafficForSecurityGroup(String securityGroupId) {
        this.counterAllowManagerTrafficForSecurityGroup++;
        super.allowManagerTrafficForSecurityGroup(securityGroupId);
    }

    @Override
    public String initializeSecurityGroup(InstanceType instanceType, String securityGroupName, String securityGroupDescription) {
        return super.initializeSecurityGroup(instanceType, securityGroupName, securityGroupDescription);
    }

    public String getSecurityGroupName() {
        return securityGroupName;
    }

    public String getVpcId() {
        return vpcId;
    }
}
