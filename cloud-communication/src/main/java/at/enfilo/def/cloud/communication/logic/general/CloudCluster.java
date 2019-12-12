package at.enfilo.def.cloud.communication.logic.general;

import at.enfilo.def.cloud.communication.logic.util.CloudState;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a computing cluster in a cloud environment with one instance of type CLUSTER
 * and any number of instances of type WORKER or REDUCER. Holds all necessary data for the communication
 * with the cloud environment as {@link CloudSpecification} and an object of type {@link ICloudFactory}
 * which handles the creation of the appropriate objects for the specific cloud environment
 */
public abstract class CloudCluster {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(CloudCluster.class);

    protected String cloudClusterId;
    protected CloudInstance clusterInstance;
    protected Map<String, CloudInstance> nodeInstancesMap;
    protected Map<String, InstanceType> removedInstanceIds;
    protected CloudSpecification cloudSpecification;
    protected ICloudFactory cloudFactory;
    protected Map<String, String> defIdsToCloudIdsMap;

    /**
     * Creates a {@link CloudCluster} object with the given {@link ICloudFactory} and the given {@link CloudSpecification}
     *
     * @param cloudFactory          the {@link ICloudFactory} this {@link CloudCluster} uses for creating the appropriate objects for
     *                              the communication with the cloud environment
     * @param cloudSpecification    the specification data needed for the communication with the cloud environment as {@link CloudSpecification}
     */
    public CloudCluster(ICloudFactory cloudFactory, CloudSpecification cloudSpecification) {
        if (!cloudSpecification.isCloudSpecificationComplete()) {
            throw new IllegalArgumentException("The cloud specification has to be complete for creating a cloud cluster");
        }
        this.cloudClusterId = UUID.randomUUID().toString();
        this.cloudFactory = cloudFactory;
        this.cloudSpecification = cloudSpecification;

        this.nodeInstancesMap = new HashMap<>();
        this.removedInstanceIds = new HashMap<>();
        this.defIdsToCloudIdsMap = new HashMap<>();
        LOGGER.debug("New CloudCluster created");
    }

    public void mapDEFIdToCloudClusterId(String defId, String cloudInstanceId) {
        if (!isCloudInstanceWithIdRegistered(cloudInstanceId)) {
            throw new IllegalArgumentException(MessageFormat.format("There is no cloud instance with the id {0} registered within this cloud cluster", cloudInstanceId));
        }

        if (!this.defIdsToCloudIdsMap.containsKey(defId)) {
            this.defIdsToCloudIdsMap.put(defId, cloudInstanceId);
        }
    }

    public String getCloudClusterId() {
        return cloudClusterId;
    }

    /**
     * Creates a {@link CloudInstance} object with the {@link InstanceType} CLUSTER if this {@link CloudCluster} doesn't hold
     * one yet and boots it if it isn't already running
     *
     * @return  the ID the booted cluster instance has been assigned in the cloud environment
     */
    public String bootClusterInstance() {
        LOGGER.info("Booting cluster instance");
        if (this.clusterInstance == null) {
            this.clusterInstance = this.cloudFactory.createCloudInstance(InstanceType.CLUSTER, this.cloudSpecification, this);
            LOGGER.debug("Created new cluster instance");
        }

        if (this.clusterInstance.cloudState == CloudState.CREATED || this.clusterInstance.getCloudState() == CloudState.STOPPED) {
            this.clusterInstance.boot();
            applyCloudSettingsForBootingCloudInstance(this.clusterInstance);
        }

        String clusterInstanceId = this.clusterInstance.getCloudInstanceId();
        LOGGER.info(MessageFormat.format("Cluster instance with id {0} booted, waiting until instance has booted properly", clusterInstanceId));

        try {
            TimeUnit.SECONDS.sleep(this.cloudSpecification.getTimeoutForInstanceBootingInSeconds());
        } catch (InterruptedException e) {
            LOGGER.error("Error while waiting until instance has booted properly", e);
            Thread.currentThread().interrupt();
        }
        return clusterInstanceId;
    }

    /**
     * Creates the given number of {@link CloudInstance} objects with the given {@link InstanceType} and boots them in
     * the cloud environment. This method is only for booting instances with the {@link InstanceType} WORKER or REDUCER.
     *
     * @param type                          the {@link InstanceType} the newly booted instances will have
     * @param nrOfNodes                     the number of instances that will be booted in the cloud environment
     * @return                              the IDs of the newly booted instances as {@link List<String>}
     * @throws IllegalArgumentException     if the given {@link InstanceType} is anything else than WORKER or REDUCER
     */
    public List<String> bootNodes(InstanceType type, int nrOfNodes) {
        LOGGER.info(MessageFormat.format("Booting {0} nodes of type {1}", nrOfNodes, type));
        if (type == InstanceType.CLUSTER) {
            throw new IllegalArgumentException("This method is only for booting WORKER or REDUCER instances.");
        }

        List<String> bootedNodesIds = new ArrayList<>();

        for(int i = 0; i < nrOfNodes; i ++) {
            CloudInstance nodeInstance = this.cloudFactory.createCloudInstance(type, this.cloudSpecification, this);
            nodeInstance.boot();
            this.nodeInstancesMap.put(nodeInstance.getCloudInstanceId(), nodeInstance);
            bootedNodesIds.add(nodeInstance.getCloudInstanceId());
            applyCloudSettingsForBootingCloudInstance(nodeInstance);
        }

        LOGGER.info(MessageFormat.format("Number of booted nodes of type {0}: {1}", type, nrOfNodes));
        LOGGER.info("Waiting until instances have booted properly");
        try {
            TimeUnit.SECONDS.sleep(this.cloudSpecification.getTimeoutForInstanceBootingInSeconds());
        } catch (InterruptedException e) {
            LOGGER.error("Error while waiting until instances have booted properly", e);
            Thread.currentThread().interrupt();
        }
        return bootedNodesIds;
    }

    protected void terminateNodeCloudInstances(Collection<String> cloudInstanceIds) {
        if (cloudInstanceIds == null) {
            throw new IllegalArgumentException("The collection of cloud instance ids must not be null");
        }

        LOGGER.info(MessageFormat.format("Terminating {0} node cloud instances", cloudInstanceIds.size()));
        for (String cloudInstanceId: cloudInstanceIds) {

            if (this.clusterInstance != null && this.clusterInstance.cloudInstanceId == cloudInstanceId) {
                throw new IllegalArgumentException("This method is only for terminating cloud instances of type WORKER or REDUCER");
            }
            if (!this.nodeInstancesMap.containsKey(cloudInstanceId)) {
                throw new IllegalStateException(MessageFormat.format("CloudInstance with id {0} is not registered in this cloud cluster", cloudInstanceId));
            }

            CloudInstance instance = this.nodeInstancesMap.get(cloudInstanceId);
            if (canCloudInstanceBeTerminated(instance)) {
                instance.terminate();
                this.nodeInstancesMap.remove(cloudInstanceId);
                this.removedInstanceIds.put(cloudInstanceId, instance.getInstanceType());
            }
        }
        LOGGER.info(MessageFormat.format("Terminated {0} nodes", cloudInstanceIds.size()));
    }

    /**
     * Terminates the instances with {@link InstanceType} WORKER or REDUCER in the cloud environment with the given IDs
     *
     * @param defInstanceIds              the IDs of the instances that shall be terminated as {@link Collection<String>}
     * @throws IllegalArgumentException     if one given instance ID is the instance of the cluster instance in this {@link CloudCluster}
     * @throws IllegalStateException        if there is no instance registered in this {@link CloudCluster} with a given ID
     */
    public void terminateNodes(Collection<String> defInstanceIds) {
        if (defInstanceIds == null) {
            throw new IllegalArgumentException("The collection of DEF instance ids must not be null");
        }

        List<String> cloudInstanceIds = new LinkedList<>();

        for (String defInstanceId: defInstanceIds) {
            String cloudInstanceId = getCloudInstanceId(defInstanceId);
            cloudInstanceIds.add(cloudInstanceId);
            this.defIdsToCloudIdsMap.remove(defInstanceId);
        }

        terminateNodeCloudInstances(cloudInstanceIds);
    }

    /**
     * Terminates the cluster instance of this {@link CloudCluster} if there is a cluster instance that can be terminated
     *
     * @throws IllegalStateException    if there is no cluster instance defined or if the cluster instance can't be terminated
     *                                  because of its current {@link CloudState}
     */
    protected void terminateClusterInstance() {
        if (this.clusterInstance == null) {
            throw new IllegalStateException("There is no cluster instance defined that can be terminated");
        }

        String clusterInstanceId = this.clusterInstance.getCloudInstanceId();
        LOGGER.info(MessageFormat.format("Terminating cluster instance with ID {0}", clusterInstanceId));

        if (!canCloudInstanceBeTerminated(this.clusterInstance)) {
            throw new IllegalStateException(MessageFormat.format("The cluster instance can not be terminated because of its current state: {0}", this.clusterInstance.getCloudState()));
        }

        this.clusterInstance.terminate();
        this.removedInstanceIds.put(this.clusterInstance.getCloudInstanceId(), this.clusterInstance.getInstanceType());
        this.clusterInstance = null;
        LOGGER.info(MessageFormat.format("Terminated cluster instance with ID {0}", clusterInstanceId));
    }

    /**
     * Checks if the given {@link CloudInstance} can be terminated depending on its current {@link CloudState}
     *
     * @param cloudInstance     the {@link CloudInstance} that will be checked if it can be terminated
     * @return                  true, if the {@link CloudInstance} has the currently the {@link CloudState} BOOTING, RUNNING, STOPPING or STOPPED and
     *                          false, if the it has any other state
     */
    protected boolean canCloudInstanceBeTerminated(CloudInstance cloudInstance) {
        CloudState cloudState = cloudInstance.getCloudState();
        if (cloudState == CloudState.CREATED ||
                cloudState == CloudState.SHUTTING_DOWN ||
                cloudState == CloudState.TERMINATED ||
                cloudState == CloudState.ERROR ||
                cloudState == CloudState.UNDEFINED) {
            LOGGER.debug("Cloud instance with id {0} can not be terminated", cloudInstance.getCloudInstanceId());
            return false;
        }
        LOGGER.debug("Cloud instance with id {0} can be terminated", cloudInstance.getCloudInstanceId());
        return true;
    }

    /**
     * Fetches the public IP address the {@link CloudInstance} with the given ID has been assigned in the cloud environment
     *
     * @param cloudInstanceId   the ID of the {@link CloudInstance} the public IP address should be returned of
     * @return                  the public IP address of the {@link CloudInstance} with the given ID
     */
    public InetAddress getPublicIPAddressOfCloudInstance(String cloudInstanceId) {
        LOGGER.debug(MessageFormat.format("Fetching public IP address of instance with id {0}", cloudInstanceId));
        CloudInstance instance = getCloudInstance(cloudInstanceId);
        return instance.getPublicIpAddress();
    }

    /**
     * Fetches the private IP address the {@link CloudInstance} with the given ID has been assigned in the cloud environment
     *
     * @param cloudInstanceId   the ID of the {@link CloudInstance} the private IP address should be returned of
     * @return                  the private IP address of the {@link CloudInstance} with the given ID
     */
    public InetAddress getPrivateIPAddressOfCloudInstance(String cloudInstanceId) {
        LOGGER.debug(MessageFormat.format("Fetching private IP address of instance with id {0}", cloudInstanceId));
        CloudInstance instance = getCloudInstance(cloudInstanceId);
        return instance.getPrivateIpAddress();
    }

    /**
     * Returns the {@link CloudInstance} with the given ID of any {@link InstanceType}
     *
     * @param cloudInstanceId               the ID of the {@link CloudInstance} that shall be returned
     * @return                              the {@link CloudInstance} with the given ID
     * @throws IllegalArgumentException     if this {@link CloudCluster} doesn't hold a {@link CloudInstance} with the given ID
     */
    protected CloudInstance getCloudInstance(String cloudInstanceId) {
        LOGGER.debug(MessageFormat.format("Fetching instance with id {0}", cloudInstanceId));
        if (this.clusterInstance != null && this.clusterInstance.getCloudInstanceId().equals(cloudInstanceId)) {
            return this.clusterInstance;
        }

        if (this.nodeInstancesMap.containsKey(cloudInstanceId)) {
            return this.nodeInstancesMap.get(cloudInstanceId);
        }

        throw new IllegalStateException("There is no cloud instance with the given ID registered in this cloud cluster");
    }

    /**
     * Checks if a cloud instance with the given id is registered within this cloud cluster
     *
     * @param cloudInstanceId   the ID of the instance that shall be checked for if it is registered within this cloud cluster or nor
     * @return                  true, if either the cluster or a node instance has the given ID, otherwhise false
     */
    protected boolean isCloudInstanceWithIdRegistered(String cloudInstanceId) {
        LOGGER.debug(MessageFormat.format("Checking if instance with id {0} is registered in cloud cluster", cloudInstanceId));
        if ((this.clusterInstance != null && this.clusterInstance.getCloudInstanceId().equals(cloudInstanceId)) || this.nodeInstancesMap.containsKey(cloudInstanceId)) {
            LOGGER.debug(MessageFormat.format("Instance with id {0} is registered in cloud cluster", cloudInstanceId));
            return  true;
        }

        LOGGER.debug(MessageFormat.format("Instance with id {0} is not registered in cloud cluster", cloudInstanceId));
        return false;
    }

    protected String getCloudInstanceId(String defInstanceId) {
        if (!this.defIdsToCloudIdsMap.containsKey(defInstanceId)) {
            throw new IllegalArgumentException(MessageFormat.format("There is no DEF instance with the id {0} registered within this cloud cluster", defInstanceId));
        }
        return this.defIdsToCloudIdsMap.get(defInstanceId);
    }

    /**
     * Initializes this {@link CloudCluster} with cloud specific actions
     */
    protected abstract void initialize();

    /**
     * Shuts down all {@link CloudInstance} objects of any {@link InstanceType} including cloud specific actions
     */
    public abstract void shutdown();

    /**
     * Cloud specific actions that have to be applied when booting a {@link CloudInstance}
     *
     * @param cloudInstance the {@link CloudInstance} that has just been booted
     */
    protected abstract void applyCloudSettingsForBootingCloudInstance(CloudInstance cloudInstance);
}
