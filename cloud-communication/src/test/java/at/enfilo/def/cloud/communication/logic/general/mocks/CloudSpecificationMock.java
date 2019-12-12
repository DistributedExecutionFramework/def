package at.enfilo.def.cloud.communication.logic.general.mocks;

import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;

import java.util.Map;

public class CloudSpecificationMock extends CloudSpecification {

    public Map<InstanceType, String> getImageIdsMap() {
        return this.imageIds;
    }

    public Map<InstanceType, String> getInstanceSizesMap() {
        return this.instanceSizes;
    }

    public void setImageIdsMap(Map<InstanceType, String> map) {
        this.imageIds = map;
    }

    public void setInstanceSizesMap(Map<InstanceType, String> map) {
        this.instanceSizes = map;
    }

    @Override
    public boolean isCloudSpecificationComplete() {
        return true;
    }

    @Override
    public int getTimeoutForInstanceBootingInSeconds() {
        return 0;
    }
}
