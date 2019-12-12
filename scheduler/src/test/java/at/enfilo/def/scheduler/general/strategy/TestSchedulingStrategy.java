package at.enfilo.def.scheduler.general.strategy;

import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.node.api.exception.NodeCommunicationException;
import at.enfilo.def.scheduler.general.util.SchedulerConfiguration;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;

import java.util.List;
import java.util.Map;

public class TestSchedulingStrategy extends SchedulingStrategy<IWorkerServiceClient, WorkerServiceClientFactory> {

	public TestSchedulingStrategy(
			Map<String, IWorkerServiceClient> nodes,
			Map<String, Environment> nodeEnvironments,
			WorkerServiceClientFactory factory,
			ILibraryServiceClient libraryServiceClient,
			SchedulerConfiguration schedulerConfiguration
	) {
		super(nodes, nodeEnvironments, factory, libraryServiceClient, schedulerConfiguration);
	}

	/**
	 * Adds a new node.
	 *
	 * @param nId      - node id
	 * @param endpoint - service endpoint definition
	 * @throws NodeCommunicationException
	 */
	@Override
	protected void addNode(String nId, ServiceEndpointDTO endpoint) throws NodeCommunicationException {
		super.addNode(nId, endpoint);
	}

	/**
	 * Remove a node.
	 *
	 * @param nId - node id to remove
	 */
	@Override
	protected void removeNode(String nId) {
		super.removeNode(nId);
	}

	/**
	 * Returns client of given node id.
	 *
	 * @param nId - node id
	 * @return
	 */
	@Override
	protected IWorkerServiceClient getNodeClient(String nId) throws UnknownNodeException {
		return super.getNodeClient(nId);
	}

	/**
	 * Get list of registered node ids.
	 *
	 * @return list of node ids.
	 */
	@Override
	protected List<String> getNodes() {
		return super.getNodes();
	}

	/**
	 * Helper method that provides basic IP Address resolvent functionality.
	 *
	 * @return resolved IP Address.
	 */
	@Override
	protected String resolveIP() {
		return super.resolveIP();
	}

	@Override
	public void notifyNodeInfo(String nId, NodeInfoDTO nodeInfo) {

	}
}
