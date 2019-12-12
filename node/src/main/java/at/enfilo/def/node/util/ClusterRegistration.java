package at.enfilo.def.node.util;

import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.NodeType;

import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ClusterRegistration extends TimerTask {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ClusterRegistration.class);

	private final ServiceEndpointDTO clusterEndpoint;
	private final ServiceEndpointDTO nodeEndpoint;
	private final NodeType nodeType;

	public ClusterRegistration(ServiceEndpointDTO clusterEndpoint, ServiceEndpointDTO nodeEndpoint, NodeType nodeType) {
		this.clusterEndpoint = clusterEndpoint;
		this.nodeEndpoint = nodeEndpoint;
		this.nodeType = nodeType;
	}

	@Override
	public void run() {
		LOGGER.debug("Try to register on cluster {}.", clusterEndpoint);
		try {
			IClusterServiceClient cluster = new ClusterServiceClientFactory().createClient(clusterEndpoint);
			Future<Void> addNodeFuture = cluster.addNode(nodeEndpoint, nodeType);
			addNodeFuture.get();
			LOGGER.info("Successfully registered on cluster {}.", clusterEndpoint);

		} catch (ClientCreationException | ClientCommunicationException | InterruptedException | ExecutionException e) {
			LOGGER.error("Error while register on cluster {}.", clusterEndpoint, e);
		}
	}
}
