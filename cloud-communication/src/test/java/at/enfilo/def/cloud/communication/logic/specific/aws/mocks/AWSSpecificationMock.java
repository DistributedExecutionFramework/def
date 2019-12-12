package at.enfilo.def.cloud.communication.logic.specific.aws.mocks;

import at.enfilo.def.cloud.communication.logic.specific.aws.AWSSpecification;

public class AWSSpecificationMock extends AWSSpecification {

    @Override
    public boolean isCloudSpecificationComplete() {
        return true;
    }
}
