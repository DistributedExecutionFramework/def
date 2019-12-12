package at.enfilo.def.cluster.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Cluster Service Interface for client.
 */
public interface IClusterServiceClient extends IServiceClient {

	/**
	 * Manager (DEF Module) take control over this cluster.
	 * This method can only be called one time.
	 *
	 * @param managerId - id of manager
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> takeControl(String managerId)
	throws ClientCommunicationException;

	/**
	 * Request assembly of ClusterDTO (cluster information).
	 *
	 * @return Future - {@link ClusterInfoDTO} instance
	 */
	Future<ClusterInfoDTO> getClusterInfo()
	throws ClientCommunicationException;

	/**
	 * Destroys (removes / deletes) this Cluster and every assigned Worker.
	 */
	void destroyCluster()
	throws ClientCommunicationException;

	/**
	 * Requests a list of Nodes (nId), workers or reducers, managed by this cluster.
	 *
	 * @param type - node type: worker or reducer
	 * @return Future - list of worker ids
	 */
	Future<List<String>> getAllNodes(NodeType type)
	throws ClientCommunicationException;

	/**
	 * Request the info about a specific Node (nId).
	 *
	 * @param nId - node id
	 * @return Future - {@link NodeInfoDTO} instance
	 */
	Future<NodeInfoDTO> getNodeInfo(String nId)
	throws ClientCommunicationException;

	/**
	 * Request the info about the cluster environment.
	 *
	 * @return Future - {@link FeatureDTO} features
	 */
	Future<List<FeatureDTO>> getEnvironment()
			throws ClientCommunicationException;

	/**
	 * Request the info about a specific Node environment (nId).
	 *
	 * @param nId - node id
	 * @return Future - {@link FeatureDTO} features
	 */
	Future<List<FeatureDTO>> getNodeEnvironment(String nId)
			throws ClientCommunicationException;

	/**
	 * Add a new already running worker instance to this cluster.
	 * Worker cannot be under control from another cluster instance.
	 *
	 * @param serviceEndpoint - service endpoint of the new worker
	 * @param type - node type: worker or reducer
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 **/
	Future<Void> addNode(ServiceEndpointDTO serviceEndpoint, NodeType type)
	throws ClientCommunicationException;

	/**
	 * Removes a worker or reducer instance from this cluster.
	 *
	 * @param nId - worker or reducer id to remove
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> removeNode(String nId)
	throws ClientCommunicationException;

	/**
	 * Returns the ServiceEndpoint of the specified worker or reducer.
	 *
	 * @param nId - node id.
	 * @return Future - {@link ServiceEndpointDTO} of worker service.
	 */
	Future<ServiceEndpointDTO> getNodeServiceEndpoint(String nId)
	throws ClientCommunicationException;

	/**
	 * Returns the ServiceEndpoint of node scheduler service.
	 *
	 * @param nodeType - type of the node: worker or reducer (used for referencing).
	 * @return Future - {@link ServiceEndpointDTO} of scheduler service.
	 */
	Future<ServiceEndpointDTO> getSchedulerServiceEndpoint(NodeType nodeType)
	throws ClientCommunicationException;

	/**
	 * Set new node scheduler service for this cluster.
	 *
	 * @param nodeType - type of the node: worker or reducer (used for referencing).
	 * @param schedulerServiceEndpoint - new service endpoint object.
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> setSchedulerServiceEndpoint(
		NodeType nodeType,
		ServiceEndpointDTO schedulerServiceEndpoint
	) throws ClientCommunicationException;

	/**
	 * Returns current active StoreRoutine Id for every task.
	 * @return StoreRoutine Id
	 */
	Future<String> getStoreRoutine(NodeType nodeType)
	throws ClientCommunicationException;

	/**
	 * Set StoreRoutine for future tasks.
	 * @param routineId - StoreRoutine Id
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> setStoreRoutine(String routineId, NodeType nodeType)
	throws ClientCommunicationException;

	/**
	 * Returns default MapRoutine Id which is used if no MapRoutine is specified on job.
	 * @return MapRoutine Id
	 */
	Future<String> getDefaultMapRoutine()
	throws ClientCommunicationException;

	/**
	 * Set default MapRoutine Id which is used if no MapRoutine is specified on job.
	 * @param routineId - MapRoutineId
	 * @return Future - {@link TicketStatusDTO}, void function with more status info
	 */
	Future<Void> setDefaultMapRoutine(String routineId)
	throws ClientCommunicationException;

	/**
	 * Returns a {@link ServiceEndpointDTO} with the corresponding port and protocol set for the given {@link NodeType}
	 *
	 * @param nodeType							the {@link NodeType} for which the {@link ServiceEndpointDTO} shall be returned
	 * @return									a {@link ServiceEndpointDTO} with the port and protocol set corresponding to the given {@link NodeType}
	 * @throws ClientCommunicationException
	 */
	Future<ServiceEndpointDTO> getNodeServiceEndpointConfiguration(NodeType nodeType)
		throws ClientCommunicationException;

	/**
	 * Returns a {@link ServiceEndpointDTO} with the port and protocol set for accessing the library service on the cluster and node instances
	 *
	 * @return									a {@link ServiceEndpointDTO} with the port and protocol set for accessing the library service
	 * @throws ClientCommunicationException
	 */
	Future<ServiceEndpointDTO> getLibraryEndpointConfiguration()
		throws ClientCommunicationException;

	/**
	 * Finds the given number of nodes of the given {@link NodeType} that have the least number of scheduled tasks
	 *
	 * @param nodeType				the {@link NodeType} of which nodes shall be shut down
	 * @param nrOfNodesToShutdown	the number of nodes that shall be shut down
	 * @return						Future - a list with the ids of the nodes to shut down
	 * @throws ClientCommunicationException
	 */
	Future<List<String>> findNodesForShutdown(NodeType nodeType, int nrOfNodesToShutdown)
		throws ClientCommunicationException;
}
