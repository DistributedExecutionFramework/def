package at.enfilo.def.manager.api;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.*;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Client Interface for {@link at.enfilo.def.manager.api.thrift.ManagerService}
 * and {@link at.enfilo.def.manager.api.rest.IManagerService}.
 */
public interface IManagerServiceClient extends IServiceClient {

	/**
	 * Returns a list of all registered cluster ids.
	 * @return future list of cluster ids.
	 * @throws ClientCommunicationException if a communication problem happens
	 */
	Future<List<String>> getClusterIds() throws ClientCommunicationException;

	/**
	 * Returns information about the requested cluster.
	 * @param cId - cluster id
	 * @return future of cluster information object
	 * @throws ClientCommunicationException if a communication problem happens
	 */
	Future<ClusterInfoDTO> getClusterInfo(String cId) throws ClientCommunicationException;

	/**
	 * Returns {@link ServiceEndpointDTO} the requested cluster.
	 * @param cId - cluster id
	 * @return future of {@link ServiceEndpointDTO}
	 * @throws ClientCommunicationException if a communication problem happens
	 */
	Future<ServiceEndpointDTO> getClusterEndpoint(String cId) throws ClientCommunicationException;

	/**
	 * Creates a new AWS cluster with an initial worker and reducer pool size with the
	 * given {@link AWSSpecificationDTO}
	 *
	 * @param numberOfWorkers		initial worker pool size
	 * @param numberOfReducers		initial reducer pool size
	 * @param awsSpecification		specification for AWS environment
	 * @return 						Future of newly created cluster id
	 * @throws ClientCommunicationException
	 */
	Future<String> createAWSCluster(int numberOfWorkers, int numberOfReducers, AWSSpecificationDTO awsSpecification) throws ClientCommunicationException;

	/**
	 * Adds an existing cluster to this manager.
	 *
	 * @param endpoint - cluster service endpoint
	 * @return future of status
	 * @throws ClientCommunicationException if a communication problem happens
	 */
	Future<Void> addCluster(ServiceEndpointDTO endpoint) throws ClientCommunicationException;

	/**
	 * Delete the given cluster.
	 * This means all machines belong to the given cluster (controller, worker) will be turned off.
	 * @param cId - cluster id to delete
	 * @return future of ticket status
	 * @throws ClientCommunicationException if a communication problem happens
	 */
	Future<Void> deleteCluster(String cId) throws ClientCommunicationException;

	/**
	 * Adjusts the pool size of the given {@link NodeType} in the cluster with the given id
	 *
	 * @param cId				the id of the cluster the node pool size shall be adjusted of
	 * @param newNodePoolSize	the number of nodes of the given {@link NodeType} that shall be running in the cloud cluster
	 * @param nodeType			the {@link NodeType} of the node pool that shall be adjusted
	 * @throws ClientCommunicationException if a communication problem happens
	 * @return
	 */
	Future<Void> adjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType) throws ClientCommunicationException;

	/**
	 * Creates a new ClientRoutine.
	 * @param routine - {@link RoutineDTO} with required attributes set
	 * @throws ClientCommunicationException if a communication problem happens
	 * @return future of created RoutineId
	 */
	Future<String> createClientRoutine(RoutineDTO routine) throws ClientCommunicationException;

	/**
	 * Uploads a binary to a specified Routine.
	 *
	 * @param rId - Routine
	 * @param binaryName - name of the binary file
	 * @param md5 - checksum
	 * @param sizeInBytes - size of the binary file
	 * @param isPrimary - Is binary primary? (e.g. java -jar <primary.jar>)
	 * @return RoutineBinary id as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<String> createClientRoutineBinary(
			String rId,
			String binaryName,
			String md5,
			long sizeInBytes,
			boolean isPrimary
	) throws ClientCommunicationException;

	/**
	 * Uploads a chunk to the given RoutineBinary (rbId).
	 * @param rbId - RoutineBinary Id
	 * @param chunk - chunk
	 * @return TicketState as future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<Void> uploadClientRoutineBinaryChunk(String rbId, RoutineBinaryChunkDTO chunk) throws ClientCommunicationException;

	/**
	 * Delete a specified ClientRoutine.
	 *
	 * @param rId - Client Routine Id.
	 * @throws ClientCommunicationException if a communication problem happens
	 */
	Future<Void> removeClientRoutine(String rId) throws ClientCommunicationException;

	/**
	 * Fetches a feature with a given name and version.
	 *
	 * @param name - name of the feature
	 * @param version - version of the feature
	 * @return a {@link FeatureDTO} instance with the given name and version
	 * @throws ClientCommunicationException if some error occurs while communicating with the service
	 */
	Future<FeatureDTO> getFeatureByNameAndVersion(String name, String version) throws ClientCommunicationException;
}
