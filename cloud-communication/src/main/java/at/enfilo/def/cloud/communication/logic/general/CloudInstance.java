package at.enfilo.def.cloud.communication.logic.general;

import at.enfilo.def.cloud.communication.logic.util.CloudState;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;

import java.net.InetAddress;

/**
 * Represents an instance in a cloud environment that can be booted and terminated
 * and that holds information about this instance
 */
public abstract class CloudInstance {

    protected String cloudInstanceId;
    protected InetAddress privateIpAddress;
    protected InetAddress publicIpAddress;
    protected InstanceType instanceType;
    protected CloudState cloudState;

    /**
     * Creates the {@link CloudInstance} object, sets the given {@link InstanceType} and defines the initial {@link CloudState} as CREATED
     *
     * @param instanceType  the {@link InstanceType} the {@link CloudInstance} will have
     */
    public CloudInstance(InstanceType instanceType) {
        this.instanceType = instanceType;
        this.cloudState = CloudState.CREATED;
    }

    /**
     * Boots the instance of the given {@link InstanceType} in the cloud environment with the appropriate data
     */
    public abstract void boot();

    /**
     * Terminates the instance running in the cloud environment
     */
    public abstract void terminate();

    /**
     * Returns the ID the instance was assigned in the cloud environment
     *
     * @return  the ID of the instance as {@link String}
     */
    public String getCloudInstanceId() {
        return cloudInstanceId;
    }

    /**
     * Returns the private IP address the instance has assigned in the cloud environment
     *
     * @return  the private IP address of the instance as {@link String}
     */
    public InetAddress getPrivateIpAddress() {
        return privateIpAddress;
    }

    /**
     * Returns the public IP address the instance has assigned in the cloud environment
     *
     * @return  the public IP address of the instance as {@link String}
     */
    public InetAddress getPublicIpAddress() {
        return publicIpAddress;
    }

    /**
     * Returns the {@link InstanceType} of this instance
     *
     * @return  the type of the instance as {@link InstanceType}
     */
    public InstanceType getInstanceType() {
        return instanceType;
    }

    /**
     * Fetches the current state of the instance in the cloud environment and returns it
     *
     * @return  the current state of the instance as {@link CloudState}
     */
    public abstract CloudState getCloudState();
}
