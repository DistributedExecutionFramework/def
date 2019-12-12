package at.enfilo.def.cloud.communication.logic.specific.aws;

import at.enfilo.def.cloud.communication.logic.util.CloudState;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import at.enfilo.def.cloud.communication.logic.general.CloudCluster;
import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.general.ICloudFactory;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a computing cluster in the AWS environment with one {@link AWSInstance} of type CLUSTER
 * and any number of {@link AWSInstance}s of type WORKER or REDUCER. Holds all necessary data for the
 * communication with AWS as {@link AWSSpecification} and an object of type {@link AWSFactory} which
 * handles the creation of the appropriate objects for AWS
 */
public class AWSCluster extends CloudCluster {

    private static final String CLUSTER_SECURITY_GROUP_NAME = "ClusterSecurityGroup";
    private static final String CLUSTER_SECURITY_GROUP_DESCRIPTION = "Security group for DEF clusters";
    private static final String WORKER_SECURITY_GROUP_NAME = "WorkerSecurityGroup";
    private static final String WORKER_SECURITY_GROUP_DESCRIPTION = "Security group for DEF workers";
    private static final String REDUCER_SECURITY_GROUP_NAME = "ReducerSecurityGroup";
    private static final String REDUCER_SECURITY_GROUP_DESCRIPTION = "Security group for DEF reducers";

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(AWSCluster.class);

    protected Map<InstanceType, InstanceNetworkInterfaceSpecification> instanceNetworkInterfaceSpecifications;

    protected AWSSpecification awsSpecification;
    protected AmazonEC2 awsClient;

    protected Map<InstanceType, String> securityGroupIds;

    /**
     * Creates a {@link CloudCluster} object with the given {@link ICloudFactory} and the given {@link CloudSpecification}
     *
     * @param cloudFactory          the {@link ICloudFactory} this {@link CloudCluster} uses for creating the appropriate objects for
     *                              the communication with the cloud environment
     * @param cloudSpecification    the specification data needed for the communication with the cloud environment as {@link CloudSpecification}
     */


    /**
     * Creates an {@link AWSCluster} object with the given {@link ICloudFactory} and the given {@link CloudSpecification}
     *
     * @param cloudFactory                  the {@link ICloudFactory} of the specific type {@link AWSFactory}
     * @param cloudSpecification            the {@link CloudSpecification} of the specific type {@link AWSSpecification}
     * @throws IllegalArgumentException     if the given {@link CloudSpecification} is not of type {@link AWSSpecification}
     */
    public AWSCluster(ICloudFactory cloudFactory, CloudSpecification cloudSpecification) {
        super(cloudFactory, cloudSpecification);

        this.instanceNetworkInterfaceSpecifications = new HashMap<>();
        this.securityGroupIds = new HashMap<>();

        if (!(this.cloudSpecification instanceof AWSSpecification)) {
            throw new IllegalArgumentException("An AWSCluster needs a cloud specification of type AWSCloudSpecification");
        }
        this.awsSpecification = (AWSSpecification)this.cloudSpecification;

        initialize();
        LOGGER.debug("New AWSCluster created");
    }

    /**
     * Initializes this {@link AWSCluster} by preparing the security groups and {@link InstanceNetworkInterfaceSpecification}s
     * for all different {@link InstanceType}s
     */
    @Override
    protected void initialize() {
        LOGGER.info("Initializing new AWS cluster");
        this.awsClient = buildAWSClient();

        // initialize cluster
        String clusterSecurityGroupId = initializeSecurityGroup(InstanceType.CLUSTER, AWSCluster.CLUSTER_SECURITY_GROUP_NAME, AWSCluster.CLUSTER_SECURITY_GROUP_DESCRIPTION);
        this.securityGroupIds.put(InstanceType.CLUSTER, clusterSecurityGroupId);
        allowManagerTrafficForSecurityGroup(clusterSecurityGroupId);

        InstanceNetworkInterfaceSpecification clusterNetworkInterfaceSpecification = buildInstanceNetworkInterfaceSpecification(this.securityGroupIds.get(InstanceType.CLUSTER), this.awsSpecification.getPrivateSubnetId());
        this.instanceNetworkInterfaceSpecifications.put(InstanceType.CLUSTER, clusterNetworkInterfaceSpecification);

        // initialize worker
        String workerSecurityGroupId = initializeSecurityGroup(InstanceType.WORKER, AWSCluster.WORKER_SECURITY_GROUP_NAME, AWSCluster.WORKER_SECURITY_GROUP_DESCRIPTION);
        this.securityGroupIds.put(InstanceType.WORKER, workerSecurityGroupId);
        allowManagerTrafficForSecurityGroup(workerSecurityGroupId);

        InstanceNetworkInterfaceSpecification workerNetworkInterfaceSpecification = buildInstanceNetworkInterfaceSpecification(this.securityGroupIds.get(InstanceType.WORKER), this.awsSpecification.getPrivateSubnetId());
        this.instanceNetworkInterfaceSpecifications.put(InstanceType.WORKER, workerNetworkInterfaceSpecification);

        // initialize reducer
        String reducerSecurityGroupId = initializeSecurityGroup(InstanceType.REDUCER, AWSCluster.REDUCER_SECURITY_GROUP_NAME, AWSCluster.REDUCER_SECURITY_GROUP_DESCRIPTION);
        this.securityGroupIds.put(InstanceType.REDUCER, reducerSecurityGroupId);
        allowManagerTrafficForSecurityGroup(reducerSecurityGroupId);

        InstanceNetworkInterfaceSpecification reducerNetworkInterfaceSpecification = buildInstanceNetworkInterfaceSpecification(this.securityGroupIds.get(InstanceType.REDUCER), this.awsSpecification.getPrivateSubnetId());
        this.instanceNetworkInterfaceSpecifications.put(InstanceType.REDUCER, reducerNetworkInterfaceSpecification);

        LOGGER.debug("New AWS cluster initialized by creating security groups and instance network interface specifications for all instance types");
    }

    /**
     * Checks if the security group for the instances of the given {@link InstanceType} already exists and creates it if it doesn't.
     * If the security group already exists, all existing rules are removed from it.
     *
     * @param instanceType                  the {@link InstanceType} the security group is initialized for
     * @param securityGroupName             the name of the security group, needed if the security group has to be created
     * @param securityGroupDescription      the description of the security group, needed if the security group has to be created
     * @return                              the id of the security group as {@link String}
     */
    protected String initializeSecurityGroup(InstanceType instanceType, String securityGroupName, String securityGroupDescription) {
        LOGGER.debug(MessageFormat.format("Initialize security group for type {0}", instanceType));
        String securityGroupId;
        if (isSecurityGroupExisting(securityGroupName, this.awsSpecification.getVpcId())) {
            securityGroupId = getSecurityGroupId(securityGroupName, this.awsSpecification.getVpcId());
            clearSecurityGroup(securityGroupId, this.awsSpecification.getVpcId());
        } else {
            securityGroupId = createSecurityGroup(securityGroupName, securityGroupDescription, this.awsSpecification.getVpcId());
        }
        return securityGroupId;
    }

    /**
     * Creates an {@link IpPermission} for inbound traffic from the DEF Manager and adds it to the security group with the given id
     *
     * @param securityGroupId   the id of the security group the inbound rule shall be added
     */
    protected void allowManagerTrafficForSecurityGroup(String securityGroupId) {
        IpPermission managerTrafficIpPermission = createIpPermission(this.awsSpecification.getVpnDynamicIpNetworkAddress(), this.awsSpecification.getVpnDynamicIpSubnetMaskSuffix(), "tcp", 40000, 40072);
        authorizeSecurityGroupIngress(securityGroupId, managerTrafficIpPermission);
    }

    /**
     * Shuts down all running instances of {@link InstanceType} WORKER and REDUCER and the single instance of {@link InstanceType}
     * CLUSTER. Removes all inbound rules from all security groups.
     */
    @Override
    public void shutdown() {
        LOGGER.info("Shutting down all instances in AWS cluster");
        Set<String> nodeIds = new HashSet<>(nodeInstancesMap.keySet());
        List<String> nodesToTerminate = new LinkedList<>();

        for(String nodeId: nodeIds) {
            CloudInstance cloudInstance = this.nodeInstancesMap.get(nodeId);
            CloudState cloudState = cloudInstance.getCloudState();
            if (cloudState == CloudState.RUNNING) {
                nodesToTerminate.add(nodeId);
            }
        }
        terminateNodeCloudInstances(nodesToTerminate);
        LOGGER.debug("All node instances shut down in AWS cluster");

        terminateClusterInstance();
        LOGGER.debug("Cluster instance shut down in AWS cluster");

        for (Map.Entry<InstanceType, String> securityGroupIdEntry: securityGroupIds.entrySet()) {
            String securityGroupId = securityGroupIdEntry.getValue();
            clearSecurityGroup(securityGroupId, this.awsSpecification.getVpcId());
        }
        LOGGER.debug("All security groups cleared in AWS cluster");

        try {
            LOGGER.info("Waiting until all instances in AWS have shut down properly");
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            LOGGER.error("Error while waiting until all instances in AWS have shut down properly");
            Thread.currentThread().interrupt();
        }
        LOGGER.info("All instances shut down in AWS cluster");
    }

    /**
     * Adds appropriate inbound rules to the security groups so the just booted {@link AWSInstance} can communicate with
     * {@link AWSInstance}s of other {@link InstanceType}s
     *
     * @param cloudInstance the {@link CloudInstance} that has just been booted
     */
    @Override
    protected void applyCloudSettingsForBootingCloudInstance(CloudInstance cloudInstance) {
        LOGGER.debug(MessageFormat.format("Applying cloud settings for booting instance of type {0}", cloudInstance.getInstanceType()));

        List<InstanceType> accessToInstanceTypes = new LinkedList<>();
        switch (cloudInstance.getInstanceType()) {
            case CLUSTER:
                accessToInstanceTypes.add(InstanceType.WORKER);
                accessToInstanceTypes.add(InstanceType.REDUCER);
                break;
            case WORKER:
                accessToInstanceTypes.add(InstanceType.CLUSTER);
                break;
            case REDUCER:
                accessToInstanceTypes.add(InstanceType.CLUSTER);
                break;
        }

        IpPermission ipPermission = createTCPIpPermissionsForCloudInstance(cloudInstance);

        for (InstanceType type: accessToInstanceTypes) {
            if (!this.securityGroupIds.containsKey(type)) {
                throw new IllegalStateException(MessageFormat.format("There is no security group id set for instance type {0}", type));
            }
            authorizeSecurityGroupIngress(this.securityGroupIds.get(type), ipPermission);
        }
    }

    /**
     * Creates {@link IpPermission}s for the private and public IP addresses of the given {@link CloudInstance} needed for
     * the TCP connections between the modules of the DEF
     *
     * @param cloudInstance the {@link CloudInstance} the {@link IpPermission}s are created for
     * @return              the created {@link IpPermission}
     */
    protected IpPermission createTCPIpPermissionsForCloudInstance(CloudInstance cloudInstance) {
        LOGGER.debug(MessageFormat.format("Creating TCP permission for instance with id {0}", cloudInstance.getCloudInstanceId()));
        List<String> ipAddressesWithSubnetMask = new LinkedList<>();
        ipAddressesWithSubnetMask.add(MessageFormat.format("{0}/{1}", cloudInstance.getPrivateIpAddress().getHostAddress(), "32"));
        ipAddressesWithSubnetMask.add(MessageFormat.format("{0}/{1}", cloudInstance.getPublicIpAddress().getHostAddress(), "32"));

        IpPermission ipPermission = createIpPermission(ipAddressesWithSubnetMask, "tcp", 40000, 40072);
        return ipPermission;
    }

    /**
     * Creates an {@link IpPermission} for the given IP address, subnet mask, protocol and port range
     *
     * @param ipAddress             the IP address this {@link IpPermission} is created for as {@link String}
     * @param subnetMaskSuffix      the subnet mask suffix for the given IP address as {@link String}
     * @param protocol              the protocol the {@link IpPermission} should allow as {@link String}
     * @param fromPort              the starting port of the port range for the {@link IpPermission}
     * @param toPort                the ending port of the port range for the {@link IpPermission}
     * @return                      the created {@link IpPermission} for the given IP address, subnet mask, protocol and port range
     */
    protected IpPermission createIpPermission(String ipAddress, int subnetMaskSuffix, String protocol, int fromPort, int toPort) {
        List<String> ipAddressesWithSubnetMasks = new LinkedList<>(Arrays.asList(MessageFormat.format("{0}/{1}", ipAddress, subnetMaskSuffix)));
        return createIpPermission(ipAddressesWithSubnetMasks, protocol, fromPort, toPort);
    }

    /**
     * Creates an {@link IpPermission} for the given IP addresses with their subnet masks, the given protocol and port range
     *
     * @param ipAddressesWithSubnetMasks    the IP addresses with their subnet masks in CIDR notation this {@link IpPermission} is created for as {@link Collection<String>}
     * @param protocol                      the protocol the {@link IpPermission} should allow as {@link String}
     * @param fromPort                      the starting point of the port range for the {@link IpPermission}
     * @param toPort                        the ending point of the port range for the {@link IpPermission}
     * @return                              the created {@link IpPermission} for the given IP addresses with their subnetmasks, the given protocol and port range
     */
    private IpPermission createIpPermission(Collection<String> ipAddressesWithSubnetMasks, String protocol, int fromPort, int toPort) {
        IpPermission ipPermission = new IpPermission();

        List<IpRange> ipRanges = new LinkedList<>();
        for (String ipAddress: ipAddressesWithSubnetMasks) {
            IpRange ipRange = new IpRange().withCidrIp(ipAddress);
            ipRanges.add(ipRange);
        }

        ipPermission.withIpv4Ranges(ipRanges)
                .withIpProtocol(protocol)
                .withFromPort(fromPort)
                .withToPort(toPort);
        return ipPermission;
    }

    /**
     * Authorizes ingress with the given {@link IpPermission} for the security group with the given ID
     *
     * @param securityGroupId   the ID of the security group the given {@link IpPermission} should be applied on as {@link String}
     * @param ipPermission      the {@link IpPermission} that will be set for the security group with the given ID
     */
    protected void authorizeSecurityGroupIngress(String securityGroupId, IpPermission ipPermission) {
        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
        authorizeSecurityGroupIngressRequest.withGroupId(securityGroupId).withIpPermissions(ipPermission);
        this.awsClient.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
    }

    /**
     * Builds the {@link AmazonEC2} client needed for the communication with AWS with the credentials and the
     * corresponding region specified
     *
     * @return  the configured {@link AmazonEC2} client
     */
    protected AmazonEC2 buildAWSClient() {
        LOGGER.debug("Building AWS client");
        BasicAWSCredentials credentials = new BasicAWSCredentials(this.awsSpecification.getAccessKeyID(), this.awsSpecification.getSecretKey());
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        return AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(this.awsSpecification.getRegion())
                .build();
    }

    /**
     * Builds the {@link InstanceNetworkInterfaceSpecification}s that handed over to new {@link AWSInstance}s at creation with
     * the specified security group ID and subnet ID
     *
     * @param securityGroupId   the ID of the security group this {@link InstanceNetworkInterfaceSpecification}s are associated with as {@link String}
     * @param subnetId          the ID of the subnet this {@link InstanceNetworkInterfaceSpecification}s are associated with as {@link String}
     * @return                  the specified {@link InstanceNetworkInterfaceSpecification}s as {@link Collection<InstanceNetworkInterfaceSpecification>}
     */
    protected InstanceNetworkInterfaceSpecification buildInstanceNetworkInterfaceSpecification(String securityGroupId, String subnetId) {
        LOGGER.debug(MessageFormat.format("Building instance network interface specifications for security group with id {0} and subnet with id {1}", securityGroupId, subnetId));

        InstanceNetworkInterfaceSpecification networkInterfaceSpecification = new InstanceNetworkInterfaceSpecification();
        networkInterfaceSpecification.setDeviceIndex(0);
        networkInterfaceSpecification.setSubnetId(subnetId);
        networkInterfaceSpecification.setAssociatePublicIpAddress(true);
        networkInterfaceSpecification.withGroups(securityGroupId);

        return networkInterfaceSpecification;
    }

    /**
     * Creates a security group with a given name and description in a virtual private cloud (VPC) in AWS with the given ID
     *
     * @param securityGroupName         the name of the new security group as {@link String}
     * @param securityGroupDescription  the description of the new security group as {@link String}
     * @param vpcId                     the ID of the virtual private cloud (VPC) in AWS the security group will be created in as {@link String}
     * @return
     */
    protected String createSecurityGroup(String securityGroupName, String securityGroupDescription, String vpcId) {
        LOGGER.debug(MessageFormat.format("Creating security group with name {0} and description {1} in VPC with id {2}", securityGroupName, securityGroupDescription, vpcId));
        CreateSecurityGroupRequest securityGroupRequest = new CreateSecurityGroupRequest();
        securityGroupRequest.withGroupName(securityGroupName);
        securityGroupRequest.withDescription(securityGroupDescription);
        securityGroupRequest.withVpcId(vpcId);

        CreateSecurityGroupResult securityGroupResult = this.awsClient.createSecurityGroup(securityGroupRequest);
        return securityGroupResult.getGroupId();
    }

    /**
     * Checks if a security group with a given name already exists in a virtual private cloud (VPC) in AWS with the given ID
     *
     * @param securityGroupName     the name of the security group as {@link String}
     * @param vpcId                 the ID of the virtual private cloud (VPC) as {@link String}
     * @return                      true if the security group with the given name is already available in AWS,
     *                              false if it is not available
     */
    protected boolean isSecurityGroupExisting(String securityGroupName, String vpcId) {
        List<SecurityGroup> securityGroups = getAllSecurityGroups();

        for (SecurityGroup securityGroup: securityGroups) {
            if (securityGroup.getGroupName().equals(securityGroupName) && securityGroup.getVpcId().equals(vpcId)) {
                LOGGER.debug("Security group with name {0} is existing in VPC with id {1}", securityGroupName, vpcId);
                return true;
            }
        }
        LOGGER.debug("Security group with name {0} isn not existing in VPC with id {1}", securityGroupName, vpcId);
        return false;
    }

    /**
     * Fetches all security groups that are currently available in AWS
     *
     * @return  all currently available security groups in AWS as {@link List<SecurityGroup>}
     */
    protected List<SecurityGroup> getAllSecurityGroups() {
        LOGGER.debug("Fetching all security groups");
        DescribeSecurityGroupsRequest securityGroupsRequest = new DescribeSecurityGroupsRequest();

        DescribeSecurityGroupsResult securityGroupsResult = this.awsClient.describeSecurityGroups(securityGroupsRequest);

        return securityGroupsResult.getSecurityGroups();
    }

    /**
     * Fetches the ID a security group with the given name and in the virtual private cloud (VPC) with the given ID has assigned in AWS
     *
     * @param securityGroupName     the name of the security group the ID is fetched as {@link String}
     * @param vpcId                 the ID of the virtual private cloud (VPC) the security group should be located in as {@link String}
     * @return                      the ID of the security group as {@link String}
     */
    protected String getSecurityGroupId(String securityGroupName, String vpcId) {
        LOGGER.debug(MessageFormat.format("Fetching id of security group with name {0} in VPC with id {1}", securityGroupName, vpcId));
        List<SecurityGroup> securityGroups = getAllSecurityGroups();

        for (SecurityGroup securityGroup: securityGroups) {
            if (securityGroup.getGroupName().equals(securityGroupName) && securityGroup.getVpcId().equals(vpcId)) {
                return securityGroup.getGroupId();
            }
        }

        throw new IllegalArgumentException(MessageFormat.format("There is no security group available with the name {0} in VPC with id {1}", securityGroupName, vpcId));
    }

    /**
     * Removes all inbound rules from a security group with the given ID
     *
     * @param securityGroupId   the ID of the security group in AWS from which all inbound rules should be deleted
     * @param vpcId             the ID of the virtual private cloud (VPC) the security group is located in
     */
    protected void clearSecurityGroup(String securityGroupId, String vpcId) {
        LOGGER.debug(MessageFormat.format("Clearing security group with id {0} in VPC with id {1}", securityGroupId, vpcId));
        List<SecurityGroup> securityGroups = getAllSecurityGroups();

        for (SecurityGroup securityGroup: securityGroups) {
            if (securityGroup.getGroupId().equals(securityGroupId) && securityGroup.getVpcId().equals(vpcId)) {
                List<IpPermission> ipPermissions = securityGroup.getIpPermissions();

                if (ipPermissions.size() > 0) {
                    RevokeSecurityGroupIngressRequest securityGroupIngressRequest = new RevokeSecurityGroupIngressRequest();
                    securityGroupIngressRequest.withGroupId(securityGroupId);
                    securityGroupIngressRequest.withIpPermissions(ipPermissions);

                    this.awsClient.revokeSecurityGroupIngress(securityGroupIngressRequest);
                    break;
                }
            }
        }
    }

    /**
     * Returns the {@link InstanceNetworkInterfaceSpecification}s for the given {@link InstanceType}
     *
     * @param instanceType              the {@link InstanceType} the {@link InstanceNetworkInterfaceSpecification}s should be returned for
     * @return                          the {@link InstanceNetworkInterfaceSpecification}s for the given {@link InstanceType}
     * @throws IllegalStateException    if there is are no {@link InstanceNetworkInterfaceSpecification}s specified for this {@link InstanceType}
     */
    public InstanceNetworkInterfaceSpecification getNetworkInterfaceSpecifications(InstanceType instanceType) {
        LOGGER.info(MessageFormat.format("Fetching instance network interface specifications for type {0}", instanceType));
        if (!this.instanceNetworkInterfaceSpecifications.containsKey(instanceType)) {
            throw new IllegalStateException(MessageFormat.format("There is are no network interface specifications set for instance type {0}", instanceType));
        }
        return this.instanceNetworkInterfaceSpecifications.get(instanceType);
    }

    /**
     * Returns the previously configured {@link AmazonEC2} client
     *
     * @return  the {@link AmazonEC2} client needed for the communication with the AWS environment
     */
    public AmazonEC2 getAwsClient() {
        LOGGER.info("Fetching AWS client");
        return awsClient;
    }
}
