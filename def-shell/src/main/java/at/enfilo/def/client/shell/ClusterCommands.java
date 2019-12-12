package at.enfilo.def.client.shell;

import at.enfilo.def.client.shell.formatter.ShellOutputFormatter;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
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
public class ClusterCommands implements CommandMarker {

	@Autowired
	private DEFShellSession session;
	@Autowired
	private ObjectCommands objects;


	@CliAvailabilityIndicator({
			CMD_CLUSTER_TAKE_CONTROL,
			CMD_CLUSTER_SHOW,
			CMD_CLUSTER_NODE_ADD_ENDPOINT,
			CMD_CLUSTER_NODE_ADD_DIRECT,
			CMD_CLUSTER_NODE_SHOW,
			CMD_CLUSTER_NODE_LIST,
			CMD_CLUSTER_DESTROY,
			CMD_CLUSTER_NODE_GET_ENDPOINT,
			CMD_CLUSTER_NODE_REMOVE,
			CMD_CLUSTER_SCHEDULER_GET_ENDPOINT,
			CMD_CLUSTER_SCHEDULER_SET_ENDPOINT,
			CMD_CLUSTER_ROUTINE_SET_MAP,
			CMD_CLUSTER_ROUTINE_SET_STORE
	})
	public boolean isClusterServiceActive() {
		return session.getActiveService() == Service.CLUSTER;
	}


	@CliCommand(value = CMD_CLUSTER_TAKE_CONTROL, help = "Manager takes control over this cluster")
	public String takeControl(
		@CliOption(key = OPT_MANAGER_ID, mandatory = true, help = "Manager Id") final String managerId
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		session.getClusterServiceClient().takeControl(managerId).get();
		return MESSAGE_CLUSTER_TAKE_CONTROL;
	}


	@CliCommand(value = CMD_CLUSTER_SHOW, help = "Info about cluster")
	public String getClusterInfo(
		@CliOption(key = OPT_TO_OBJECT, help = "Store ClusterInfo into an object") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<ClusterInfoDTO> futureClusterInfo = session.getClusterServiceClient().getClusterInfo();
		ClusterInfoDTO info = futureClusterInfo.get();

		if (object == null) {
			return ShellOutputFormatter.format(futureClusterInfo.get());
		} else {
			objects.getObjectMap().put(object, info);
			return String.format(MESSAGE_OBJECT_STORED, object);
		}
	}


	@CliCommand(value = CMD_CLUSTER_DESTROY, help = "Destroy this Cluster")
	public String destroyCluster() throws ClientCommunicationException {

		session.getClusterServiceClient().destroyCluster();

		return MESSAGE_CLUSTER_DESTROYED;
	}

	@CliCommand(value = CMD_CLUSTER_NODE_LIST, help = "List all Node Ids managed by this Cluster")
	public String getAllNodes(
		@CliOption(key = OPT_NODE_TYPE, mandatory = true, help = "Node type") final NodeType type
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		Future<List<String>> futureNodeIds = session.getClusterServiceClient().getAllNodes(type);
		return ShellOutputFormatter.format(futureNodeIds.get());
	}

	@CliCommand(value = CMD_CLUSTER_NODE_SHOW, help = "Shows information about specified Node")
	public String getNodeInfo(
		@CliOption(key = OPT_NODE_ID, mandatory = true) final String nId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store NodeInfo into an object") final String object
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<NodeInfoDTO> future = session.getClusterServiceClient().getNodeInfo(nId);
		NodeInfoDTO info = future.get();

		if (object == null) {
			return ShellOutputFormatter.format(info);

		} else {
			objects.getObjectMap().put(object, info);
			return String.format(MESSAGE_OBJECT_STORED, object);
		}
	}

	@CliCommand(value = CMD_CLUSTER_NODE_ADD_DIRECT, help = "Add a Node to this Cluster")
	public String addNode(
		@CliOption(key = OPT_NODE_TYPE, mandatory = true, help = "Node type") final NodeType type,
		@CliOption(key = OPT_HOST, mandatory = true, help = "Node host") final String host,
		@CliOption(key = OPT_PORT, mandatory = true, help = "Node port") final int port,
		@CliOption(key = OPT_PROTOCOL, mandatory = true, help = "Node protocol") final Protocol protocol,
		@CliOption(key = OPT_URL_PATTERN, unspecifiedDefaultValue = "/*", specifiedDefaultValue = "/*", help = "Node url pattern") final String pattern
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(host, port, protocol);
		endpoint.setPathPrefix(pattern);

		return addNode(endpoint, type);
	}

	@CliCommand(value = CMD_CLUSTER_NODE_ADD_ENDPOINT, help = "Add a Node to this Cluster")
	public String addNode(
		@CliOption(key = OPT_NODE_TYPE, mandatory = true, help = "Node type") final NodeType type,
		@CliOption(key = OPT_SERVICE_ENDPOINT, mandatory = true, help = "Cluster host") final String object
	) throws InterruptedException, ExecutionException, ClientCommunicationException {

		return addNode(objects.getObject(object, ServiceEndpointDTO.class), type);
	}


	private String addNode(ServiceEndpointDTO nodeEndpoint, NodeType type)
	throws ExecutionException, InterruptedException, ClientCommunicationException {

		session.getClusterServiceClient().addNode(nodeEndpoint, type).get();
		return MESSAGE_CLUSTER_ADD_NODE;
	}


	@CliCommand(value = CMD_CLUSTER_NODE_REMOVE, help = "Remove a node from this Cluster")
	public String removeNode(
		@CliOption(key = OPT_NODE_ID, mandatory = true, help = "Node Id to remove") final String nId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getClusterServiceClient().removeNode(nId).get();
		return MESSAGE_CLUSTER_REMOVE_NODE;
	}


	@CliCommand(value = CMD_CLUSTER_NODE_GET_ENDPOINT, help = "Shows ServiceEndpoint of given Node")
	public String getNodeServiceEndpoint(
		@CliOption(key = OPT_NODE_ID, mandatory = true, help = "Node Id to get ServiceEndpoint") final String nId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store ClusterInfo into an object") final String object
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		Future<ServiceEndpointDTO> futureEndpoint = session.getClusterServiceClient().getNodeServiceEndpoint(nId);

		if (object != null) {
			objects.getObjectMap().put(object, futureEndpoint.get());
			return String.format(MESSAGE_OBJECT_STORED, object);
		} else {
			return ShellOutputFormatter.format(futureEndpoint.get());
		}
	}


	@CliCommand(value = CMD_CLUSTER_SCHEDULER_GET_ENDPOINT, help = "Shows ServiceEndpoint of given Node")
	public String getSchedulerServiceEndpoint(
			@CliOption(key = OPT_NODE_TYPE, mandatory = true, help = "Node type") final NodeType nodeType,
			@CliOption(key = OPT_TO_OBJECT, help = "Store ClusterInfo into an object") final String object
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		Future<ServiceEndpointDTO> futureEndpoint = session.getClusterServiceClient().getSchedulerServiceEndpoint(
			nodeType
		);

		if (object != null) {
			objects.getObjectMap().put(object, futureEndpoint.get());
			return String.format(MESSAGE_OBJECT_STORED, object);
		} else {
			return ShellOutputFormatter.format(futureEndpoint.get());
		}
	}

	@CliCommand(value = CMD_CLUSTER_SCHEDULER_SET_ENDPOINT, help = "Sets node scheduler ServiceEndpoint")
	public String setSchedulerServiceEndpoint(
			@CliOption(key = OPT_NODE_TYPE, mandatory = true, help = "Node type") final NodeType nodeType,
			@CliOption(key = OPT_SERVICE_ENDPOINT, mandatory = true, help = "Scheduler ServiceEndpoint object name") final String object
	) throws ExecutionException, InterruptedException, ClientCommunicationException {

		Future<Void> futureEndpoint = session.getClusterServiceClient().setSchedulerServiceEndpoint(
			nodeType,
			objects.getObject(object, ServiceEndpointDTO.class)
		);

		return String.format(MESSAGE_CLUSTER_SET_SCHEDULER, futureEndpoint.get());
	}


	@CliCommand(value = CMD_CLUSTER_ROUTINE_SET_MAP, help = "Set default MapRoutine to Cluster and Workers")
	public String setDefaultMapRoutineId(
			@CliOption(key = OPT_ROUTINE_ID, mandatory = true, help = "Routine Id of default MapRoutine") final String rId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getClusterServiceClient().setDefaultMapRoutine(rId).get();
		return MESSAGE_CLUSTER_ROUTINE_SET_MAP;
	}


	@CliCommand(value = CMD_CLUSTER_ROUTINE_SET_STORE, help = "Set StoreRoutine to Cluster and Workers")
	public String setStoreRoutineId(
			@CliOption(key = OPT_ROUTINE_ID, mandatory = true, help = "Routine Id of StoreRoutine") final String rId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getClusterServiceClient().setStoreRoutine(rId).get();
		return MESSAGE_CLUSTER_ROUTINE_SET_STORE;
	}
}
