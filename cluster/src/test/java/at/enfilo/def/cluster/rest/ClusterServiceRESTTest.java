package at.enfilo.def.cluster.rest;

import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.impl.ClusterResponseServiceImpl;
import at.enfilo.def.cluster.impl.ClusterServiceImpl;
import at.enfilo.def.cluster.impl.ClusterServiceTest;
import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.rest.RESTServer;

import java.util.LinkedList;
import java.util.List;

public class ClusterServiceRESTTest extends ClusterServiceTest {
	@Override
	protected IServer getServer() throws Exception {
		List<IResource> webResources = new LinkedList<>();
		webResources.add(new ClusterServiceImpl(
            workerController,
			reducerController,
			clientRoutineWorkerController,
			clusterResource,
			execLogicController
		));
		webResources.add(new ClusterResponseServiceImpl());

		return RESTServer.getInstance(
			Cluster.getInstance().getConfiguration().getServerHolderConfiguration().getRESTConfiguration(),
			webResources
		);
	}

}
