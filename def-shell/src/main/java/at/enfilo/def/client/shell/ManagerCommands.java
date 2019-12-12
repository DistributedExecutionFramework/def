package at.enfilo.def.client.shell;

import at.enfilo.def.client.shell.formatter.ShellOutputFormatter;
import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.SupportedCloudEnvironment;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static at.enfilo.def.client.shell.Constants.*;

@Component
public class ManagerCommands implements CommandMarker {

	@Autowired
	private DEFShellSession session;
	@Autowired
	private ObjectCommands objects;


	@CliAvailabilityIndicator({
			CMD_MANAGER_CLUSTER_LIST,
			CMD_MANAGER_CLUSTER_ADD_DIRECT,
			CMD_MANAGER_CLUSTER_ADD_ENDPOINT,
			CMD_MANAGER_CLUSTER_CREATE,
			CMD_MANAGER_CLUSTER_REMOVE,
			CMD_MANAGER_CLUSTER_SHOW,
			CMD_MANAGER_CLUSTER_ENDPOINT,
			CMD_MANAGER_CLUSTER_ADJUST_NODE_POOL_SIZE
	})
	public boolean isManagerServiceActive() {
		return session.getActiveService() == Service.MANAGER;
	}
	

	@CliCommand(value = CMD_MANAGER_CLUSTER_LIST, help = "List all available cluster")
	public String getClusterIds()
	throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<String>> futureClusterIds = session.getManagerServiceClient().getClusterIds();
		StringBuilder ids = new StringBuilder();
		futureClusterIds.get().forEach(id -> ids.append(id).append("\n"));
		return ids.toString();
	}


	@CliCommand(value = CMD_MANAGER_CLUSTER_SHOW, help = "Show information of a specified cluster")
	public String getClusterInfo(
		@CliOption(key = OPT_CLUSTER_ID, mandatory = true, help = "Cluster Id") final String cId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store ClusterInfo into an object") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<ClusterInfoDTO> futureClusterInfo = session.getManagerServiceClient().getClusterInfo(cId);
		ClusterInfoDTO clusterInfo = futureClusterInfo.get();

		if (object == null) {
			return ShellOutputFormatter.format(futureClusterInfo.get());

		} else {
			objects.getObjectMap().put(object, clusterInfo);
			return String.format(MESSAGE_OBJECT_STORED, object);
		}
	}


	@CliCommand(value = CMD_MANAGER_CLUSTER_ENDPOINT, help = "Show ServiceEndpoint of specified cluster")
	public String getClusterEndpoint(
			@CliOption(key = OPT_CLUSTER_ID, mandatory = true, help = "Cluster Id") final String cId,
			@CliOption(key = OPT_TO_OBJECT, help = "Store ServiceEndpoint into an object") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<ServiceEndpointDTO> futureServiceEndpoint = session.getManagerServiceClient().getClusterEndpoint(cId);
		ServiceEndpointDTO serviceEndpointDTO = futureServiceEndpoint.get();

		if (object == null) {
			return ShellOutputFormatter.format(futureServiceEndpoint.get());

		} else {
			objects.getObjectMap().put(object, serviceEndpointDTO);
			return String.format(MESSAGE_OBJECT_STORED, object);
		}
	}

	@CliCommand(value = CMD_MANAGER_CLUSTER_CREATE, help = "Create a new cluster")
	public String createCluster(
			@CliOption(key = OPT_NUMBER_OF_WORKERS, help = "Number of workers", mandatory = true) final int numberOfWorkers,
			@CliOption(key = OPT_NUMBER_OF_REDUCERS, help = "Number of reducers", mandatory = true) final int numberOfReducers,
			@CliOption(key = OPT_CLOUD_ENVIRONMENT, help = "Cloud environment", mandatory = true) SupportedCloudEnvironment cloudEnvironment,
			@CliOption(key = OPT_CLOUD_SPECIFICATION, help = "Cloud specification object name", mandatory = true) final String object
	) throws ClientCreationException, ExecutionException, InterruptedException, ClientCommunicationException {

		switch (cloudEnvironment) {
			case AWS:
				return createAWSCluster(numberOfWorkers, numberOfReducers, objects.getObject(object, AWSSpecificationDTO.class));
			default:
				return null;
		}
	}

	private String createAWSCluster(int numberOfWorkers, int numberOfReducers, AWSSpecificationDTO awsSpecification)
	throws ClientCreationException, ExecutionException, InterruptedException, ClientCommunicationException {
		Future<String> futureClusterId = session.getManagerServiceClient().createAWSCluster(numberOfWorkers, numberOfReducers, awsSpecification);
		return String.format(MESSAGE_MANAGER_CLUSTER_CREATED, futureClusterId.get());
	}

	@CliCommand(value = CMD_MANAGER_CLUSTER_ADJUST_NODE_POOL_SIZE, help = "Adjust the node pool size of an existing cluster")
	public String adjustNodePoolSize(
			@CliOption(key = OPT_CLUSTER_ID, help = "Cluster Id", mandatory = true) String cId,
			@CliOption(key = OPT_NEW_NODE_POOL_SIZE, help = "New node pool size", mandatory = true) int newNodePoolSize,
			@CliOption(key = OPT_NODE_TYPE, help = "Node type", mandatory = true) NodeType nodeType)
		throws ClientCreationException, ExecutionException, InterruptedException, ClientCommunicationException {

		session.getManagerServiceClient().adjustNodePoolSize(cId, newNodePoolSize, nodeType).get();
		return MESSAGE_MANAGER_NODE_POOL_SIZE_ADJUSTED;
	}

	@CliCommand(value = CMD_MANAGER_CLUSTER_ADD_DIRECT, help = "Add an active cluster to this manager, using direct host, port and protocol information")
	public String addCluster(
		@CliOption(key = OPT_HOST, mandatory = true, help = "Cluster host") final String host,
		@CliOption(key = OPT_PORT, mandatory = true, help = "Cluster port") final int port,
		@CliOption(key = OPT_PROTOCOL, mandatory = true, help = "Cluster protocol") final Protocol protocol,
		@CliOption(key = OPT_URL_PATTERN, unspecifiedDefaultValue = "/*", specifiedDefaultValue = "/*", help = "Manager url pattern") final String pattern
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(host, port, protocol);
		endpoint.setPathPrefix(pattern);

		return addCluster(endpoint);
	}


	@CliCommand(value = CMD_MANAGER_CLUSTER_ADD_ENDPOINT, help = "Add an active cluster to this manager, using endpoint object")
	public String addCluster(
		@CliOption(key = OPT_SERVICE_ENDPOINT, mandatory = true, help = "Endpoint object name") final String object
	) throws InterruptedException, ExecutionException, ClientCommunicationException {

		return addCluster(objects.getObject(object, ServiceEndpointDTO.class));
	}


	private String addCluster(ServiceEndpointDTO endpoint) throws ClientCommunicationException, ExecutionException, InterruptedException {
		session.getManagerServiceClient().addCluster(endpoint).get();
		return MESSAGE_MANAGER_ADD_CLUSTER;
	}


	@CliCommand(value = CMD_MANAGER_CLUSTER_REMOVE, help = "Add an active cluster to this manager")
	public String deleteCluster(
		@CliOption(key = OPT_CLUSTER_ID, mandatory = true, help = "Cluster Id") final String cId
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		session.getManagerServiceClient().deleteCluster(cId).get();
		return MESSAGE_MANAGER_REMOVE_CLUSTER;
	}

}
