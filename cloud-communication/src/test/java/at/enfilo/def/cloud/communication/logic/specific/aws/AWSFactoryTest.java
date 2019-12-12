package at.enfilo.def.cloud.communication.logic.specific.aws;

import at.enfilo.def.cloud.communication.logic.general.CloudCluster;
import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.general.ICloudFactory;
import at.enfilo.def.cloud.communication.logic.specific.aws.mocks.AWSClusterMock;
import at.enfilo.def.cloud.communication.logic.specific.aws.mocks.AWSFactoryMock;
import at.enfilo.def.cloud.communication.logic.specific.aws.mocks.AWSSpecificationMock;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceSpecification;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AWSFactoryTest {

    private AWSFactoryMock factory;
    private AWSSpecificationMock awsSpecification;
    private InstanceType instanceType;
    private AWSClusterMock awsClusterMock;
    private CloudCluster otherCluster;

    @Before
    public void initialize() {
        this.factory = new AWSFactoryMock();

        this.instanceType = InstanceType.WORKER;

        this.awsSpecification = new AWSSpecificationMock();
        this.awsSpecification.setAccessKeyID("123");
        this.awsSpecification.setSecretKey("ThisIsASecretKey");
        this.awsSpecification.setInstanceTypeSpecification(this.instanceType, "1234", "huge");

        this.awsClusterMock = new AWSClusterMock(this.factory, this.awsSpecification, false);
        Map<InstanceType, InstanceNetworkInterfaceSpecification> instanceNetworInterfaceSpecificationsMap = new HashMap<>();
        instanceNetworInterfaceSpecificationsMap.put(this.instanceType, new InstanceNetworkInterfaceSpecification());
        this.awsClusterMock.setInstanceNetworkInterfaceSpecifications(instanceNetworInterfaceSpecificationsMap);
        this.otherCluster = new CloudCluster(this.factory, this.awsSpecification) {
            @Override
            protected void initialize() {
            }

            @Override
            public void shutdown() {
            }

            @Override
            protected void applyCloudSettingsForBootingCloudInstance(CloudInstance cloudInstance) {

            }
        };
    }

    @Test
    public void createCloudInstanceTest_withAWSCluster() {
        CloudInstance instance = this.factory.createCloudInstance(this.instanceType, this.awsSpecification, this.awsClusterMock);

        Assert.assertTrue(instance instanceof  AWSInstance);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createCloudInstanceTest_withOtherCloudCluster() {
        this.factory.createCloudInstance(this.instanceType, this.awsSpecification, this.otherCluster);
    }

    @Test
    public void createCloudSpecificationTest() {
        ICloudFactory factory = new AWSFactory();

        CloudSpecification specification = factory.createCloudSpecification();
        Assert.assertTrue(specification instanceof AWSSpecification);
    }

    @Test
    public void createCloudClusterTest() {
        CloudCluster cluster = this.factory.createCloudCluster(this.factory, this.awsSpecification);

        Assert.assertTrue(cluster instanceof AWSCluster);
    }
}
