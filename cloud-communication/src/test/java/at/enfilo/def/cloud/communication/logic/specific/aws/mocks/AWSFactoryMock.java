package at.enfilo.def.cloud.communication.logic.specific.aws.mocks;

import at.enfilo.def.cloud.communication.logic.general.CloudCluster;
import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.general.ICloudFactory;
import at.enfilo.def.cloud.communication.logic.specific.aws.AWSFactory;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;

public class AWSFactoryMock extends AWSFactory {

    @Override
    public CloudInstance createCloudInstance(InstanceType type, CloudSpecification cloudSpecification, CloudCluster cloudCluster) {
        return super.createCloudInstance(type, cloudSpecification, cloudCluster);
    }

    @Override
    public CloudSpecification createCloudSpecification() {
        return super.createCloudSpecification();
    }

    @Override
    public CloudCluster createCloudCluster(ICloudFactory cloudFactory, CloudSpecification cloudSpecification) {
        return new AWSClusterMock(cloudFactory, cloudSpecification, false);
    }
}
