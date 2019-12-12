package at.enfilo.def.cloud.communication.logic.specific.aws;

import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.util.CloudState;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an instance in an AWS cloud environment that can be booted and terminated
 * and that holds information about this instance
 */
public class AWSInstance extends CloudInstance {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(AWSInstance.class);

    protected AmazonEC2 awsClient;
    protected InstanceNetworkInterfaceSpecification networkInterfaceSpecifications;
    protected String keypairName;
    protected String instanceSize;
    protected String imageId;
    protected String cloudInitScript;

    /**
     * Creates an {@link AWSInstance} object with the given {@link InstanceType}
     *
     * @param instanceType                      the {@link InstanceType} the {@link AWSInstance} will have
     * @param cloudSpecification                the {@link CloudSpecification} with the data needed for connecting to the AWS environment,
     *                                          has to be of the specific type {@link AWSSpecification}
     * @param networkInterfaceSpecifications    the {@link InstanceNetworkInterfaceSpecification}s that will be set in AWS when booting this instance
     * @param awsClient                         the {@link AmazonEC2} client that will be used for connecting to the AWS environment
     * @throws IllegalArgumentException         if the given {@link CloudSpecification} is not of the specific type {@link AWSSpecification}
     */
    public AWSInstance(InstanceType instanceType,
                       CloudSpecification cloudSpecification,
                       InstanceNetworkInterfaceSpecification networkInterfaceSpecifications,
                       AmazonEC2 awsClient) {
        super(instanceType);

        this.networkInterfaceSpecifications = networkInterfaceSpecifications;
        this.awsClient = awsClient;

        if (!(cloudSpecification instanceof AWSSpecification)) {
            throw new IllegalArgumentException("The cloud specification for an AWSCloudInstance has to be of type AWSCloudSpecification");
        }

        initialize((AWSSpecification)cloudSpecification);
        LOGGER.debug("New AWSInstance created");
    }

    /**
     * Stores the data from the {@link AWSSpecification} that is needed for booting this {@link AWSInstance}
     *
     * @param awsSpecification  the {@link AWSSpecification} that hold the data for connecting to the AWS environment
     */
    protected void initialize(AWSSpecification awsSpecification) {
        LOGGER.debug("Initializing AWS instance with AWS specification");
        this.keypairName = awsSpecification.getKeypairName();
        this.imageId = awsSpecification.getImageId(this.instanceType);
        this.instanceSize = awsSpecification.getInstanceSize(this.instanceType);
        this.cloudInitScript = awsSpecification.getCloudInitScript(this.instanceType);
    }

    /**
     * Boots the instance in the AWS environment, sets the current {@link CloudState} of the instance and fetches the
     * instance information from AWS to set the public and private IP addresses in this {@link AWSInstance}
     */
    @Override
    public void boot() {
        LOGGER.info("Booting AWS instance");
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

        runInstancesRequest.withImageId(this.imageId)
                .withInstanceType(this.instanceSize)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(this.keypairName)
                .setNetworkInterfaces(new LinkedList<>(Arrays.asList(this.networkInterfaceSpecifications)));

        runInstancesRequest.setUserData(Base64.getEncoder().encodeToString(cloudInitScript.getBytes()));

        RunInstancesResult runInstancesResult = this.awsClient.runInstances(runInstancesRequest);

        Instance bootedInstance = runInstancesResult.getReservation().getInstances().get(0);
        this.cloudInstanceId = bootedInstance.getInstanceId();

        setCloudState(bootedInstance.getState());

        DescribeInstancesResult describeInstancesResult = null;
        while (describeInstancesResult == null || describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error("Error when fetching information about newly booted AWS instance");
                Thread.currentThread().interrupt();
            }
            describeInstancesResult = this.awsClient.describeInstances(new DescribeInstancesRequest().withInstanceIds(this.cloudInstanceId));
        }

        try {
            this.publicIpAddress = InetAddress.getByName(describeInstancesResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress());
            this.privateIpAddress = InetAddress.getByName(describeInstancesResult.getReservations().get(0).getInstances().get(0).getPrivateIpAddress());
        } catch (UnknownHostException e) {
            LOGGER.error("Error when setting public and private IP addresses of newly booted AWS instance");
        }
        LOGGER.info(MessageFormat.format("AWS instance with id {0} booted.", this.cloudInstanceId));
    }

    /**
     * Terminates this instance in the AWS environment
     */
    @Override
    public void terminate() {
        LOGGER.info(MessageFormat.format("Terminating AWS instance with id {0}", this.cloudInstanceId));
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
        terminateInstancesRequest.withInstanceIds(this.cloudInstanceId);

        TerminateInstancesResult terminateInstancesResult = awsClient.terminateInstances(terminateInstancesRequest);

        setCloudState(terminateInstancesResult.getTerminatingInstances().get(0).getCurrentState());
        LOGGER.info(MessageFormat.format("AWS instance with id {0} terminated", this.cloudInstanceId));
    }

    /**
     * Fetches the current {@link CloudState} of this instance in the AWS environment
     *
     * @return the current {@link CloudState} of this instance
     */
    @Override
    public CloudState getCloudState() {
        LOGGER.info(MessageFormat.format("Fetching current cloud state of AWS instance with id {0}", this.cloudInstanceId));
        DescribeInstancesResult instancesResult = awsClient.describeInstances(new DescribeInstancesRequest().withInstanceIds(this.cloudInstanceId));
        setCloudState(instancesResult.getReservations().get(0).getInstances().get(0).getState());

        if (!isInstanceStatusOk()) {
            setErrorCloudState();
        }

        return this.cloudState;
    }

    /**
     * Checks if the status of the instance in the AWS environment is ok (free from defects)
     *
     * @return  true if the status of the instance is ok, false if the status of the instance detects an error
     */
    protected boolean isInstanceStatusOk() {
        DescribeInstanceStatusResult instanceStatusResult = awsClient.describeInstanceStatus(new DescribeInstanceStatusRequest().withInstanceIds(this.cloudInstanceId));
        List<InstanceStatus> statuses = instanceStatusResult.getInstanceStatuses();

        if (!statuses.isEmpty()) {
            String lastStatus = statuses.get(statuses.size() - 1).getInstanceStatus().getStatus();

            if (!lastStatus.equals("ok") && !lastStatus.equals("initializing")) {
                LOGGER.debug(MessageFormat.format("Instance status of AWS instance with id {0} is not ok", this.cloudInstanceId));
                return false;
            }
        }
        LOGGER.debug(MessageFormat.format("Instance status of AWS instance with id {0} is ok", this.cloudInstanceId));
        return true;
    }

    /**
     * Sets the current {@link CloudState} of this {@link AWSInstance} to ERROR
     */
    protected void setErrorCloudState() {
        LOGGER.debug(MessageFormat.format("Setting cloud state of AWS instance with id {0} to ERROR", this.cloudInstanceId));
        this.cloudState = CloudState.ERROR;
    }

    /**
     * Maps the {@link InstanceState} of the AWS SDK to a {@link CloudState}
     * @param instanceState
     */
    protected void setCloudState(InstanceState instanceState) {
        LOGGER.debug(MessageFormat.format("Setting cloud state of AWS instance with id {0}", this.cloudInstanceId));
        switch (instanceState.getName()) {
            case "pending":
                this.cloudState = CloudState.BOOTING;
                break;
            case "running":
                this.cloudState = CloudState.RUNNING;
                break;
            case "shutting-down":
                this.cloudState = CloudState.SHUTTING_DOWN;
                break;
            case "terminated":
                this.cloudState = CloudState.TERMINATED;
                break;
            case "stopping":
                this.cloudState = CloudState.STOPPING;
                break;
            case "stopped":
                this.cloudState = CloudState.STOPPED;
                break;
            default:
                this.cloudState = CloudState.UNDEFINED;
                break;
        }
    }
}
