package at.enfilo.def.cloud.communication.api;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.InstanceTypeDTO;
import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

public interface ICloudCommunicationServiceClient extends IServiceClient {

    /**
     * Creates a new cloud cluster object that communicates with the AWS environment including some AWS specific actions
     * for setting up the specific AWS environment
     *
     * @param specification                     the specification of type {@link AWSSpecificationDTO} with the data
     *                                          that is used for communicating with AWS
     * @return                                  Future - with the ID of the created cloud cluster object
     * @throws ClientCommunicationException
     */
    Future<String> createAWSCluster(AWSSpecificationDTO specification) throws ClientCommunicationException;

    /**
     * Boots a DEF cluster instance in the specific cloud environment
     *
     * @param cloudClusterId                    the id of cloud cluster in which a DEF cluster instance shall be booted
     * @return                                  Future - with the id of the booted DEF cluster instance in the specific cloud environment
     * @throws ClientCommunicationException
     */
    Future<String> bootClusterInstance(String cloudClusterId) throws ClientCommunicationException;

    /**
     * Boots a given number of DEF nodes (workers or reducers) in the specific cloud environment
     *
     * @param cloudClusterId                    the id of the cloud cluster in which the DEF node instances shall be booted
     * @param type                              the types the booted node instances shall have as {@link InstanceTypeDTO}
     * @param nrOfNodes                         the number of DEF nodes that shall be booted of the given type
     * @return                                  Future - with a {@link List} with the IDs of all booted node instances
     * @throws ClientCommunicationException
     */
    Future<List<String>> bootNodes(String cloudClusterId, InstanceTypeDTO type, int nrOfNodes) throws ClientCommunicationException;

    /**
     * Terminates the DEF nodes with the given IDs in the specific cloud environment
     *
     * @param cloudClusterId                    the id of the cloud cluster in which the DEF nodes shall be terminated
     * @param cloudInstanceIds                  a {@link Collection} with the IDs of all DEF instances that shall be
     *                                          terminated in the cloud environment
     * @return                                  Future - with a {@link TicketStatusDTO} for status information
     * @throws ClientCommunicationException
     */
    Future<Void> terminateNodes(String cloudClusterId, List<String> cloudInstanceIds) throws ClientCommunicationException;

    /**
     * Returns the public IP address of a DEF instance in the specific cloud environment
     *
     * @param cloudClusterId                    the id of the cloud cluster the cloud instance is part of
     * @param cloudInstanceId                   the id of the DEF instance the public IP address shall be returned
     * @return                                  Future - with the public IP address of the cloud instance with the given ID
     * @throws ClientCommunicationException
     */
    Future<String> getPublicIPAddressOfCloudInstance(String cloudClusterId, String cloudInstanceId) throws ClientCommunicationException;

    /**
     * Returns the private IP address of a DEF instance in the specific cloud environment
     *
     * @param cloudClusterId                    the id of the cloud cluster the cloud instance is part of
     * @param cloudInstanceId                   the id of the DEF instance the private IP address shall be returned
     * @return                                  Future - with the private IP address of the cloud instance with the given ID
     * @throws ClientCommunicationException
     */
    Future<String> getPrivateIPAddressOfCloudInstance(String cloudClusterId, String cloudInstanceId) throws ClientCommunicationException;

    /**
     * Shuts down the cloud cluster with the given ID by terminating all DEF nodes and cluster instances in the specific cloud environment.
     * Executes also some cloud specific actions for clearing the specific cloud environment
     *
     * @param cloudClusterId                    the id of the cloud cluster that shall be shut down
     * @return                                  Future - with a {@link TicketStatusDTO} for status information
     * @throws ClientCommunicationException
     */
    Future<Void> shutdownCloudCluster(String cloudClusterId) throws ClientCommunicationException;

    /**
     * Maps an ID of a cluster or node instance to an ID of a cloud instance
     *
     * @param cloudClusterId                    the id of the cloud cluster the cloud instance with the given id is part of
     * @param defId                             the id of the DEF cluster or node instance
     * @param cloudInstanceId                   the id of the cloud instance
     * @return                                  Future - with a {@link TicketStatusDTO} for status information
     * @throws ClientCommunicationException
     */
    Future<Void> mapDEFIdToCloudInstanceId(String cloudClusterId, String defId, String cloudInstanceId) throws ClientCommunicationException;
}
