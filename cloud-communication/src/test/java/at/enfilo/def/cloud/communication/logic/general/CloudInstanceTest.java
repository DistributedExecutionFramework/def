package at.enfilo.def.cloud.communication.logic.general;

import at.enfilo.def.cloud.communication.logic.general.mocks.CloudInstanceMock;
import at.enfilo.def.cloud.communication.logic.util.CloudState;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import org.junit.Assert;
import org.junit.Test;

public class CloudInstanceTest {

    private final InstanceType instanceType = InstanceType.WORKER;
    private final CloudState createdCloudState = CloudState.CREATED;
    private final String instanceId = "12345";

    @Test
    public void CloudInstanceConstructorTest() {
        CloudInstanceMock instanceMock = new CloudInstanceMock(this.instanceType);

        Assert.assertEquals(this.instanceType, instanceMock.getInstanceTypeSet());
        Assert.assertEquals(this.createdCloudState, instanceMock.getCloudState());
    }

    @Test
    public void getCloudInstanceIdTest() {
        CloudInstanceMock instanceMock = new CloudInstanceMock(this.instanceType);
        instanceMock.setCloudInstanceId(instanceId);

        String instanceId = instanceMock.getCloudInstanceId();

        Assert.assertEquals(this.instanceId, instanceId);
    }
}
