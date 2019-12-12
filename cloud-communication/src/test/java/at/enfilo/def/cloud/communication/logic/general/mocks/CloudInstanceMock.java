package at.enfilo.def.cloud.communication.logic.general.mocks;

import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.util.CloudState;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;

import java.net.InetAddress;

public class CloudInstanceMock extends CloudInstance {

    /**
     * Creates the {@link CloudInstance} object, sets the given {@link InstanceType} and defines the initial {@link CloudState} as CREATED
     *
     * @param instanceType the {@link InstanceType} the {@link CloudInstance} will have
     */
    public CloudInstanceMock(InstanceType instanceType) {
        super(instanceType);
    }

    @Override
    public void boot() {
    }

    @Override
    public void terminate() {
    }

    @Override
    public CloudState getCloudState() {
        return this.cloudState;
    }

    public void setCloudState(CloudState cloudState) { this.cloudState = cloudState; }

    public InstanceType getInstanceTypeSet() {
        return this.instanceType;
    }

    public void setCloudInstanceId(String cloudInstanceId) {
        this.cloudInstanceId = cloudInstanceId;
    }

    public void setPublicIpAddress(InetAddress publicIpAddress) { this.publicIpAddress = publicIpAddress; }

    public void setPrivateIpAddress(InetAddress privateIpAddress) { this.privateIpAddress = privateIpAddress; }
}
