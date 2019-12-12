package at.enfilo.def.cluster;

import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.ClusterConfiguration;
import at.enfilo.def.cluster.util.WorkersConfiguration;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.transfer.dto.CloudType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClusterConfigurationTest {
	private static final String WORKER_IMAGE = "default-image-name";
	private static final String WORKER_SIZE = "t1.nano";

	@Test
	public void readConfiguration() throws Exception {
		ClusterConfiguration config  = Cluster.getInstance().getConfiguration();

		assertNotNull(config);
		assertEquals(CloudType.PRIVATE, config.getCloudType());

		WorkersConfiguration workersConfig = config.getWorkersConfiguration();
		assertNotNull(workersConfig);
		assertEquals(WORKER_IMAGE, workersConfig.getImage());
		assertEquals(WORKER_SIZE, workersConfig.getSize());

		ServiceEndpointDTO endpoint = workersConfig.getNodeServiceEndpoint();
		assertNotNull(endpoint);
		assertEquals(Protocol.THRIFT_TCP, endpoint.getProtocol());
		assertEquals(40032, endpoint.getPort());
	}

}
