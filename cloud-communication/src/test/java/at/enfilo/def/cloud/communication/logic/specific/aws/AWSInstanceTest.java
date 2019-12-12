package at.enfilo.def.cloud.communication.logic.specific.aws;

import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.specific.aws.mocks.AWSInstanceMock;
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

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AWSInstanceTest {

    private InstanceNetworkInterfaceSpecification instanceNetworkInterfaceSpecifications;
    private InstanceType instanceType;
    private AmazonEC2 awsClient;
    private AWSSpecification specification;

    @Before
    public void initialize() {
        this.instanceNetworkInterfaceSpecifications = new InstanceNetworkInterfaceSpecification();
        this.instanceType = InstanceType.WORKER;
        this.awsClient = Mockito.mock(AmazonEC2.class);

        this.specification = new AWSSpecification();
        this.specification.setInstanceTypeSpecification(this.instanceType, "123", "huge");
        this.specification.setKeypairName("KeypairOne");
    }

    @Test
    public void AWSInstanceConstructorTest_running() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                false,
                AWSInstanceStatus.OK);

        Assert.assertEquals(this.instanceNetworkInterfaceSpecifications, awsInstanceMock.getNetworkInterfaceSpecifications());
        Assert.assertEquals(this.awsClient, awsInstanceMock.getAWSClient());
    }

    @Test(expected = IllegalArgumentException.class)
    public void AWSInstanceConstructorTest_failing_illegalCloudSpecification() {
        CloudSpecification cloudSpecification = new CloudSpecification() {
            @Override
            public void setInstanceTypeSpecification(InstanceType instanceType, String imageId, String instanceSize) {
                super.setInstanceTypeSpecification(instanceType, imageId, instanceSize);
            }

            @Override
            public String getImageId(InstanceType instanceType) {
                return super.getImageId(instanceType);
            }

            @Override
            public String getInstanceSize(InstanceType instanceType) {
                return super.getInstanceSize(instanceType);
            }

            @Override
            public boolean isCloudSpecificationComplete() {
                return true;
            }

            @Override
            public int getTimeoutForInstanceBootingInSeconds() {
                return 0;
            }
        };

        new AWSInstanceMock(
                this.instanceType,
                cloudSpecification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                false,
                AWSInstanceStatus.OK);
    }

    @Test
    public void initializeTest() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                false,
                AWSInstanceStatus.OK);
        awsInstanceMock.initialize(specification);

        Assert.assertEquals(this.specification.getKeypairName(), awsInstanceMock.getKeypairName());
        Assert.assertEquals(this.specification.getImageId(this.instanceType), awsInstanceMock.getImageId());
        Assert.assertEquals(this.specification.getInstanceSize(this.instanceType), awsInstanceMock.getInstanceSize());
    }

    @Test
    public void bootTest() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK);
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertNull(awsInstanceMock.getCloudInstanceId());
        Assert.assertNull(awsInstanceMock.getPublicIpAddress());
        Assert.assertNull(awsInstanceMock.getPrivateIpAddress());
        awsInstanceMock.boot();

        Assert.assertNotNull(awsInstanceMock.getCloudInstanceId());
        Assert.assertNotEquals(cloudState, awsInstanceMock.getCurrentlySetCloudState());
        Assert.assertNotNull(awsInstanceMock.getPublicIpAddress());
        Assert.assertNotNull(awsInstanceMock.getPrivateIpAddress());
        verify(awsInstanceMock.getAWSClient(), times(1)).runInstances((RunInstancesRequest)notNull());
        verify(awsInstanceMock.getAWSClient(), times(1)).describeInstances((DescribeInstancesRequest)notNull());
    }

    @Test
    public void terminateTest() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK);
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();

        awsInstanceMock.terminate();

        Assert.assertNotEquals(cloudState, awsInstanceMock.getCurrentlySetCloudState());
        verify(awsInstanceMock.getAWSClient(), times(1)).terminateInstances((TerminateInstancesRequest)notNull());
    }

    @Test
    public void getCloudStateTest_instanceStatusOk() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK);
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();

        CloudState newCloudState = awsInstanceMock.getCloudState();

        Assert.assertNotEquals(cloudState, newCloudState);
        Assert.assertNotEquals(newCloudState, CloudState.ERROR);
        verify(awsInstanceMock.getAWSClient(), times(1)).describeInstances((DescribeInstancesRequest)notNull());
    }

    @Test
    public void getCloudStateTest_instanceStatusError() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.ERROR
        );
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();

        CloudState newCloudState = awsInstanceMock.getCloudState();

        Assert.assertNotEquals(cloudState, newCloudState);
        Assert.assertEquals(newCloudState, CloudState.ERROR);
        verify(awsInstanceMock.getAWSClient(), times(1)).describeInstances((DescribeInstancesRequest)notNull());
    }

    @Test
    public void isInstanceStatusOkTest_instanceStatusOk() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );

        boolean isInstanceStatusOk = awsInstanceMock.isInstanceStatusOk();
        Assert.assertTrue(isInstanceStatusOk);
        verify(awsInstanceMock.getAWSClient(), times(1)).describeInstanceStatus((DescribeInstanceStatusRequest)notNull());
    }

    @Test
    public void isInstanceStatusOkTest_instanceStatusError() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.ERROR
        );

        boolean isInstanceStatusOk = awsInstanceMock.isInstanceStatusOk();
        Assert.assertFalse(isInstanceStatusOk);
        verify(awsInstanceMock.getAWSClient(), times(1)).describeInstanceStatus((DescribeInstanceStatusRequest)notNull());
    }

    @Test
    public void isInstanceStatusOkTest_instanceStatusEmpty() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.EMPTY
        );

        boolean isInstanceStatusOk = awsInstanceMock.isInstanceStatusOk();
        Assert.assertTrue(isInstanceStatusOk);
        verify(awsInstanceMock.getAWSClient(), times(1)).describeInstanceStatus((DescribeInstanceStatusRequest)notNull());
    }

    @Test
    public void setErrorCloudStateTest() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.ERROR
        );
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertNotEquals(cloudState, CloudState.ERROR);

        awsInstanceMock.setErrorCloudState();
        CloudState newCloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertEquals(newCloudState, CloudState.ERROR);
    }

    @Test
    public void setCloudStateTest_booting() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertNotEquals(cloudState, CloudState.BOOTING);
        InstanceState instanceState = new InstanceState();
        instanceState.setName("pending");

        awsInstanceMock.setCloudState(instanceState);
        Assert.assertEquals(awsInstanceMock.getCurrentlySetCloudState(), CloudState.BOOTING);
    }

    @Test
    public void setCloudStateTest_running() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertNotEquals(cloudState, CloudState.RUNNING);
        InstanceState instanceState = new InstanceState();
        instanceState.setName("running");

        awsInstanceMock.setCloudState(instanceState);
        Assert.assertEquals(awsInstanceMock.getCurrentlySetCloudState(), CloudState.RUNNING);
    }

    @Test
    public void setCloudStateTest_shuttingDown() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertNotEquals(cloudState, CloudState.SHUTTING_DOWN);
        InstanceState instanceState = new InstanceState();
        instanceState.setName("shutting-down");

        awsInstanceMock.setCloudState(instanceState);
        Assert.assertEquals(awsInstanceMock.getCurrentlySetCloudState(), CloudState.SHUTTING_DOWN);
    }

    @Test
    public void setCloudStateTest_terminated() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertNotEquals(cloudState, CloudState.TERMINATED);
        InstanceState instanceState = new InstanceState();
        instanceState.setName("terminated");

        awsInstanceMock.setCloudState(instanceState);
        Assert.assertEquals(awsInstanceMock.getCurrentlySetCloudState(), CloudState.TERMINATED);
    }

    @Test
    public void setCloudStateTest_stopping() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertNotEquals(cloudState, CloudState.STOPPING);
        InstanceState instanceState = new InstanceState();
        instanceState.setName("stopping");

        awsInstanceMock.setCloudState(instanceState);
        Assert.assertEquals(awsInstanceMock.getCurrentlySetCloudState(), CloudState.STOPPING);
    }

    @Test
    public void setCloudStateTest_stopped() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertNotEquals(cloudState, CloudState.STOPPED);
        InstanceState instanceState = new InstanceState();
        instanceState.setName("stopped");

        awsInstanceMock.setCloudState(instanceState);
        Assert.assertEquals(awsInstanceMock.getCurrentlySetCloudState(), CloudState.STOPPED);
    }

    @Test
    public void setCloudStateTest_undefined() {
        AWSInstanceMock awsInstanceMock = new AWSInstanceMock(
                this.instanceType,
                this.specification,
                this.instanceNetworkInterfaceSpecifications,
                this.awsClient,
                true,
                AWSInstanceStatus.OK
        );
        CloudState cloudState = awsInstanceMock.getCurrentlySetCloudState();
        Assert.assertNotEquals(cloudState, CloudState.UNDEFINED);
        InstanceState instanceState = new InstanceState();
        instanceState.setName("blabla");

        awsInstanceMock.setCloudState(instanceState);
        Assert.assertEquals(awsInstanceMock.getCurrentlySetCloudState(), CloudState.UNDEFINED);
    }
}
