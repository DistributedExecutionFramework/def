package at.enfilo.def.cloud.communication;

import at.enfilo.def.cloud.communication.logic.specific.aws.AWSFactory;
import at.enfilo.def.cloud.communication.logic.specific.aws.AWSSpecification;
import at.enfilo.def.cloud.communication.logic.general.CloudCluster;
import at.enfilo.def.cloud.communication.logic.general.ICloudFactory;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;

public class CloudCommunicationIntegrationTest {

    public static void main(String[] args) {

        ICloudFactory cloudFactory = new AWSFactory();
        AWSSpecification cloudSpecification = (AWSSpecification)cloudFactory.createCloudSpecification();

        cloudSpecification.setInstanceTypeSpecification(InstanceType.CLUSTER, "ami-03a2772319da76304", "c4.xlarge");
        cloudSpecification.setInstanceTypeSpecification(InstanceType.WORKER, "ami-09db9de6d69efbfcc", "c4.xlarge");
        cloudSpecification.setInstanceTypeSpecification(InstanceType.REDUCER, "ami-0639bcd2dcc52197d", "c4.xlarge");

        cloudSpecification.setAccessKeyID(""); // set access key
        cloudSpecification.setSecretKey(""); // set secret key
        cloudSpecification.setKeypairName("MyKeyPair");
        cloudSpecification.setPublicSubnetId("subnet-022c6bc747def8248");
        cloudSpecification.setPrivateSubnetId("subnet-0d89a052821c89f84");
        cloudSpecification.setVpcId("vpc-0b81e363da048958a");
        cloudSpecification.setRegion("eu-central-1");
        cloudSpecification.setVpnDynamicIpNetworkAddress("172.27.224.0");
        cloudSpecification.setVpnDynamicIpSubnetMaskSuffix(20);

        CloudCluster cloudCluster = cloudFactory.createCloudCluster(cloudFactory, cloudSpecification);

        cloudCluster.bootClusterInstance();
        cloudCluster.bootNodes(InstanceType.WORKER, 1);

        cloudCluster.shutdown();
    }
}
