package at.enfilo.def.cloud.communication.impl;

import at.enfilo.def.cloud.communication.logic.general.CloudCluster;
import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.specific.aws.AWSCluster;
import at.enfilo.def.cloud.communication.logic.specific.aws.AWSFactory;
import at.enfilo.def.cloud.communication.logic.specific.aws.AWSSpecification;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudCommunicationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudCommunicationController.class);

    private Map<String, CloudCluster> cloudClusterMap;

    /**
     * Private class to provide thread safe singleton
     */
    private static class ThreadSafeLazySingletonWrapper {
        private static final CloudCommunicationController INSTANCE = new CloudCommunicationController();

        private ThreadSafeLazySingletonWrapper() {}
    }

    /**
     * Private constructor, use getInstance();
     */
    private CloudCommunicationController() {
        this.cloudClusterMap = new HashMap<>();
    }

    /**
     * Singleton pattern
     *
     * @return  a {@link CloudCommunicationController} instance
     */
    public static CloudCommunicationController getInstance() {
        return ThreadSafeLazySingletonWrapper.INSTANCE;
    }

    /**
     * Creates an {@link AWSCluster} object with the given {@link AWSSpecification} that handles
     * of communication with the AWS environment
     *
     * @param specification     the {@link AWSSpecification} with the data
     * @return
     */
    public String createAWSCluster(AWSSpecification specification) {
        LOGGER.info("Trying to create AWS cluster");
        CloudCluster cluster = new AWSCluster(new AWSFactory(), specification);
        this.cloudClusterMap.put(cluster.getCloudClusterId(), cluster);
        return cluster.getCloudClusterId();
    }

    /**
     * Creates a {@link CloudInstance} object with the {@link InstanceType} CLUSTER if the {@link CloudCluster} with the
     * given ID doesn't hold one yet and boots it if it isn't already running
     *
     * @param cloudClusterId    the ID of the {@link CloudCluster} the DEF cluster instance shall be booted in
     * @return                  the ID the booted cluster instance has been assigned in the cloud environment
     */
    public String bootClusterInstance(String cloudClusterId) {
        LOGGER.info(MessageFormat.format("Trying to boot cluster instance in cloud cluster with id {0}", cloudClusterId));
        CloudCluster cluster = getCloudCluster(cloudClusterId);
        return cluster.bootClusterInstance();
    }

    /**
     * Creates the given number of {@link CloudInstance} objects with the give {@link InstanceType} and boots them in the
     * cloud environment. This method is only for booting instances with the {@link InstanceType} WORKER or REDUCER.
     *
     * @param cloudClusterId    the ID of the {@link CloudCluster} the DEF node instances shall be booted in
     * @param instanceType      the {@link InstanceType} the newly booted instances will have
     * @param nrOfNodes         the number of instances that will be booted in the cloud environment
     * @return                  the IDs of the newly booted instances as {@link List<String>}
     */
    public List<String> bootNodes(String cloudClusterId, InstanceType instanceType, int nrOfNodes) {
        LOGGER.info(MessageFormat.format("Trying to boot {0} node instances of type {1} in cloud cluster with id {2}",
                nrOfNodes,
                instanceType,
                nrOfNodes
                ));
        CloudCluster cluster = getCloudCluster(cloudClusterId);
        return cluster.bootNodes(instanceType, nrOfNodes);
    }

    /**
     * Terminates the DEF node instances with the given IDs in the {@link CloudCluster} with the given ID
     *
     * @param cloudClusterId    the ID of the {@link CloudCluster} the DEF node instances shall be terminated in
     * @param defInstanceIds  the IDs of the DEF node instances that shall be terminated
     */
    public void terminateNodes(String cloudClusterId, List<String> defInstanceIds) {
        LOGGER.info(MessageFormat.format("Trying to terminate node instances in cloud cluster with id {0}", cloudClusterId));
        CloudCluster cluster = getCloudCluster(cloudClusterId);
        cluster.terminateNodes(defInstanceIds);
    }

    /**
     * Fetches the public IP address the {@link CloudInstance} with the given ID has been assigned in the cloud environment
     *
     * @param cloudClusterId    the ID of the {@link CloudCluster} the {@link CloudInstance} with the given ID is part of
     * @param cloudInstanceId   the ID of the {@link CloudInstance} the public IP address shall be returned of
     * @return                  the public IP address of the {@link CloudInstance} with the given ID as {@link String}
     */
    public String getPublicIPAddressOfCloudInstance(String cloudClusterId, String cloudInstanceId) {
        LOGGER.info(MessageFormat.format("Trying to fetch public IP address of cloud instance with id {0} in cloud cluster with id {1}", cloudInstanceId, cloudClusterId));
        CloudCluster cluster = getCloudCluster(cloudClusterId);
        return cluster.getPublicIPAddressOfCloudInstance(cloudInstanceId).getHostAddress();
    }

    /**
     * Fetches the private IP address of the {@link CloudInstance} with the given ID has been assigned in the cloud environment
     *
     * @param cloudClusterId    the ID of the {@link CloudCluster} the {@link CloudInstance} with the given ID is part of
     * @param cloudInstanceId   the ID of the {@link CloudInstance} the private IP address shall be returned of
     * @return                  the private IP address of the {@link CloudInstance} with the given ID as {@link String}
     */
    public String getPrivateIPAddressOfCloudInstance(String cloudClusterId, String cloudInstanceId) {
        LOGGER.info(MessageFormat.format("Trying to fetch private IP address of cloud instance with id {0} in cloud cluster with id {1}", cloudInstanceId, cloudClusterId));
        CloudCluster cluster = getCloudCluster(cloudClusterId);
        return cluster.getPrivateIPAddressOfCloudInstance(cloudInstanceId).getHostAddress();
    }

    /**
     * Shuts down all {@link CloudInstance} objects of any {@link InstanceType} including some cloud specific actions
     *
     * @param cloudClusterId    the ID of the {@link CloudCluster} that shall be shut down
     */
    public void shutdownCloudCluster(String cloudClusterId) {
        LOGGER.info(MessageFormat.format("Trying to shut down cloud cluster with id {0}", cloudClusterId));
        CloudCluster cluster = getCloudCluster(cloudClusterId);
        cluster.shutdown();
        this.cloudClusterMap.remove(cloudClusterId);
    }

    public void mapDEFIdToCloudInstanceId(String cloudClusterId, String defId, String cloudInstanceId) {
        LOGGER.info(MessageFormat.format("Trying to map id {0} of DEF cluster or node instance to id {1} of cloud instance", defId, cloudInstanceId));
        CloudCluster cloudCluster = getCloudCluster(cloudClusterId);
        cloudCluster.mapDEFIdToCloudClusterId(defId, cloudInstanceId);
    }

    /**
     * Checks if a {@link CloudCluster} with the given ID is registered and returns it if it is
     *
     * @param cloudClusterId                the ID of the {@link CloudCluster} that shall be returned
     * @return                              the registered {@link CloudCluster} with the given ID
     * @throws IllegalArgumentException     if there is no {@link CloudCluster} with the given ID registered
     */
    private CloudCluster getCloudCluster(String cloudClusterId) {
        if (!this.cloudClusterMap.containsKey(cloudClusterId)) {
            throw new IllegalArgumentException(MessageFormat.format("There is no cloud cluster with the id {0} registered.", cloudClusterId));
        }
        return this.cloudClusterMap.get(cloudClusterId);
    }
}









