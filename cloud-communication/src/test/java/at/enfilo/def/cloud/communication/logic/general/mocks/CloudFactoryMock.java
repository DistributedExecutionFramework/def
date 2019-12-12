package at.enfilo.def.cloud.communication.logic.general.mocks;

import at.enfilo.def.cloud.communication.logic.general.CloudCluster;
import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.general.ICloudFactory;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;

import java.util.UUID;

public class CloudFactoryMock implements ICloudFactory{

    public int counterCreateCloudInstanceCalls = 0;
    public int counterCreateCloudSpecificationCalls = 0;
    public int counterCreateCloudClusterCalls = 0;

    @Override
    public CloudInstance createCloudInstance(InstanceType type, CloudSpecification cloudSpecification, CloudCluster cloudCluster) {
        this.counterCreateCloudInstanceCalls++;
        CloudInstanceMock cloudInstanceMock = new CloudInstanceMock(type);
        cloudInstanceMock.setCloudInstanceId(UUID.randomUUID().toString());
        return cloudInstanceMock;
    }

    @Override
    public CloudSpecification createCloudSpecification() {
        this.counterCreateCloudSpecificationCalls++;
        return new CloudSpecificationMock();
    }

    @Override
    public CloudCluster createCloudCluster(ICloudFactory cloudFactory, CloudSpecification cloudSpecification) {
        this.counterCreateCloudClusterCalls++;
        return new CloudClusterMock(cloudFactory, cloudSpecification, true);
    }
}
