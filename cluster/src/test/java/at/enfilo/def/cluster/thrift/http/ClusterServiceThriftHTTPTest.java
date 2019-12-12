package at.enfilo.def.cluster.thrift.http;

import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.api.thrift.ClusterResponseService;
import at.enfilo.def.cluster.api.thrift.ClusterService;
import at.enfilo.def.cluster.impl.ClusterResponseServiceImpl;
import at.enfilo.def.cluster.impl.ClusterServiceImpl;
import at.enfilo.def.cluster.impl.ClusterServiceTest;
import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;

import java.util.LinkedList;
import java.util.List;

public class ClusterServiceThriftHTTPTest extends ClusterServiceTest {
	@Override
	protected IServer getServer() throws Exception {
		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		ThriftProcessor<ClusterServiceImpl> clusterServiceProcessor = new ThriftProcessor<>(
				ClusterService.class.getName(),
				new ClusterServiceImpl(
                        workerController,
						reducerController,
						clusterResource,
						execLogicController),
				ClusterService.Processor<ClusterService.Iface>::new
		);
		thriftProcessors.add(clusterServiceProcessor);
		ThriftProcessor<ClusterResponseServiceImpl> clusterResponseServiceProcessor = new ThriftProcessor<>(
				ClusterResponseService.class.getName(),
				new ClusterResponseServiceImpl(),
				ClusterResponseService.Processor<ClusterResponseService.Iface>::new
		);
		thriftProcessors.add(clusterResponseServiceProcessor);

		return ThriftHTTPServer.getInstance(
			Cluster.getInstance().getConfiguration().getServerHolderConfiguration().getThriftHTTPConfiguration(),
			thriftProcessors
		);
	}

}
