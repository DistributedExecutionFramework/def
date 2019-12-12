package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.ReducersConfiguration;
import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.reducer.api.IReducerServiceClient;
import at.enfilo.def.reducer.api.ReducerServiceClientFactory;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;

import java.net.InetAddress;
import java.util.*;

public class ReducerController extends NodeController<IReducerServiceClient, UnifiedClientFactory<IReducerServiceClient>> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReducerController.class);

	private static ReducerController instance;

	private final ReducersConfiguration reducersConfiguration;

	public static ReducerController getInstance() {
		if (instance == null) {
			instance = new ReducerController();
		}
		return instance;
	}

	private ReducerController() {
		this(
			new ReducerServiceClientFactory(),
			new LinkedList<>(),
			new HashMap<>(),
			new HashMap<>(),
			null, // null means a TimeOut Map will be created
			new HashMap<>(),
			Cluster.getInstance().getConfiguration().getReducersConfiguration()
		);
	}

	/**
	 * Constructor for internal/unit test usage.
	 * @param nodeServiceClientFactory
	 * @param nodes
	 * @param nodeInstanceMap
	 * @param nodeConnectionMap
	 * @param nodeInfoMap
	 * @param reducersConfiguration
	 */
	ReducerController(
			UnifiedClientFactory<IReducerServiceClient> nodeServiceClientFactory,
			List<String> nodes,
			Map<String, String> nodeInstanceMap,
			Map<String, IReducerServiceClient> nodeConnectionMap,
			Map<String, NodeInfoDTO> nodeInfoMap,
			Map<String, List<FeatureDTO>> nodeFeatureMap,
			ReducersConfiguration reducersConfiguration
	) {
		super(
				NodeType.REDUCER,
				nodeServiceClientFactory,
				nodes,
				nodeInstanceMap,
				nodeConnectionMap,
				nodeInfoMap,
				nodeFeatureMap,
				reducersConfiguration
		);
		this.reducersConfiguration = reducersConfiguration;
	}

	/**
	 * Notification that a node is "down".
	 * This notification is triggered by timeout map, if a node does not send an update periodically.
	 *
	 * @param nId      - Node id
	 * @param nodeInfo
	 */
	@Override
	protected void notifyNodeDown(String nId, NodeInfoDTO nodeInfo) {
		LOGGER.info("Notification: Node ({}) down. Remove reducer.", nId);
		try {
			removeNode(nId);
		} catch (UnknownNodeException e) {
			LOGGER.error("Reducer {} already removed", nId, e);
		}
	}

}
