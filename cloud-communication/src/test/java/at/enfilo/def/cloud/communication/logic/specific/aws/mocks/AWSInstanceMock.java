package at.enfilo.def.cloud.communication.logic.specific.aws.mocks;

import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.specific.aws.*;
import at.enfilo.def.cloud.communication.logic.util.AWSInstanceStatus;
import at.enfilo.def.cloud.communication.logic.util.CloudState;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

import org.mockito.Mockito;

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;

public class AWSInstanceMock extends AWSInstance {

    private AWSInstanceStatus instanceStatus;

    /**
     * Creates an {@link AWSInstance} object with the given {@link InstanceType}
     *
     * @param instanceType                   the {@link InstanceType} the {@link AWSInstance} will have
     * @param cloudSpecification             the {@link CloudSpecification} with the data needed for connecting to the AWS environment,
     *                                       has to be of the specific type {@link AWSSpecification}
     * @param networkInterfaceSpecifications the {@link InstanceNetworkInterfaceSpecification}s that will be set in AWS when booting this instance
     * @param awsClient                      the {@link AmazonEC2} client that will be used for connecting to the AWS environment
     * @throws IllegalArgumentException if the given {@link CloudSpecification} is not of the specific type {@link AWSSpecification}
     */
    public AWSInstanceMock(InstanceType instanceType,
                           CloudSpecification cloudSpecification,
                           InstanceNetworkInterfaceSpecification networkInterfaceSpecifications,
                           AmazonEC2 awsClient,
                           boolean useMocking,
                           AWSInstanceStatus instanceStatus) {
        super(instanceType, cloudSpecification, networkInterfaceSpecifications, awsClient);

        if (useMocking) {
            initMockingComponents();
        }

        this.instanceStatus = instanceStatus;
    }

    private void initMockingComponents() {
        this.awsClient = Mockito.mock(AmazonEC2.class);

        InstanceState instanceStateRunning = new InstanceState();
        instanceStateRunning.setName("running");

        InstanceState instanceStateTerminated = new InstanceState();
        instanceStateTerminated.setName("terminated");

        Instance instance = new Instance();
        instance.setState(instanceStateRunning);
        instance.setInstanceId("12345");
        instance.setPublicIpAddress("0.0.0.0");
        instance.setPrivateIpAddress("1.1.1.1");

        Reservation reservation = new Reservation();
        reservation.setInstances(new LinkedList<>(Arrays.asList(instance)));

        InstanceStateChange instanceStateChange = new InstanceStateChange();
        instanceStateChange.setCurrentState(instanceStateTerminated);

        InstanceStatusSummary instanceStatusSummaryOk = new InstanceStatusSummary();
        instanceStatusSummaryOk.setStatus("ok");

        InstanceStatus instanceStatusOk = new InstanceStatus();
        instanceStatusOk.setInstanceStatus(instanceStatusSummaryOk);

        InstanceStatusSummary instanceStatusSummaryError = new InstanceStatusSummary();
        instanceStatusSummaryError.setStatus("error");

        InstanceStatus instanceStatusError = new InstanceStatus();
        instanceStatusError.setInstanceStatus(instanceStatusSummaryError);

        when(this.awsClient.runInstances((RunInstancesRequest)notNull())).then(invocation -> {
            RunInstancesResult runInstancesResult = new RunInstancesResult();
            runInstancesResult.setReservation(reservation);
            return runInstancesResult;
        });

        when(this.awsClient.describeInstances((DescribeInstancesRequest)notNull())).then(invocation -> {
            DescribeInstancesResult describeInstancesResult = new DescribeInstancesResult();
            describeInstancesResult.setReservations(new LinkedList<>(Arrays.asList(reservation)));
            return describeInstancesResult;
        });

        when(this.awsClient.terminateInstances((TerminateInstancesRequest)notNull())).then(invocation -> {
            TerminateInstancesResult terminateInstancesResult = new TerminateInstancesResult();
            terminateInstancesResult.setTerminatingInstances(new LinkedList<>(Arrays.asList(instanceStateChange)));
            return terminateInstancesResult;
        });

        when(this.awsClient.describeInstanceStatus((DescribeInstanceStatusRequest)notNull())).then(invocation -> {
           DescribeInstanceStatusResult describeInstanceStatusResult = new DescribeInstanceStatusResult();

           switch (this.instanceStatus) {
               case OK:
                   describeInstanceStatusResult.setInstanceStatuses(new LinkedList<>(Arrays.asList(instanceStatusOk)));
                   break;
               case ERROR:
                   describeInstanceStatusResult.setInstanceStatuses(new LinkedList<>(Arrays.asList(instanceStatusError)));
                   break;
               case EMPTY:
                   break;
           }
           return describeInstanceStatusResult;
        });
    }

    public AmazonEC2 getAWSClient() {
        return this.awsClient;
    }

    public InstanceNetworkInterfaceSpecification getNetworkInterfaceSpecifications() {
        return this.networkInterfaceSpecifications;
    }

    public String getKeypairName() {
        return this.keypairName;
    }

    public String getInstanceSize() {
        return this.instanceSize;
    }

    public String getImageId() {
        return this.imageId;
    }

    @Override
    public String getCloudInstanceId() {
        return super.getCloudInstanceId();
    }

    @Override
    public CloudState getCloudState() {
        return super.getCloudState();
    }

    public CloudState getCurrentlySetCloudState() {
        return this.cloudState;
    }

    @Override
    public InetAddress getPrivateIpAddress() {
        return super.getPrivateIpAddress();
    }

    @Override
    public InetAddress getPublicIpAddress() {
        return super.getPublicIpAddress();
    }

    public void initialize(AWSSpecification awsSpecification) {
        super.initialize(awsSpecification);
    }

    public boolean isInstanceStatusOk() {
        return super.isInstanceStatusOk();
    }

    public void setErrorCloudState() {
        super.setErrorCloudState();
    }

    public void setCloudState(InstanceState instanceState) {
        super.setCloudState(instanceState);
    }

    public void setCloudState(CloudState cloudState) {
        this.cloudState = cloudState;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        try {
            this.publicIpAddress = InetAddress.getByName(publicIpAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        try {
            this.privateIpAddress = InetAddress.getByName(privateIpAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
