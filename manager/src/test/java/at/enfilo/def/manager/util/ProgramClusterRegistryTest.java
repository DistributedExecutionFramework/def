package at.enfilo.def.manager.util;

import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.manager.impl.UnknownClusterException;
import at.enfilo.def.transfer.UnknownProgramException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.UUID;

import static org.junit.Assert.*;

public class ProgramClusterRegistryTest {
	private ProgramClusterRegistry registry;
	private ClusterServiceClientFactory factory;

	@Before
	public void setUp() throws Exception {
		Constructor<ProgramClusterRegistry> constructor = ProgramClusterRegistry.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		registry = constructor.newInstance();
		factory = new ClusterServiceClientFactory();
	}

	@Test
	public void addAndRemoveCluster() throws Exception {
		String cId1 = UUID.randomUUID().toString();
		String cId2 = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint1 = new ServiceEndpointDTO();
		endpoint1.setHost(UUID.randomUUID().toString());
		endpoint1.setProtocol(Protocol.REST);
		ServiceEndpointDTO endpoint2 = new ServiceEndpointDTO();
		endpoint2.setHost(UUID.randomUUID().toString());
		endpoint2.setProtocol(Protocol.THRIFT_TCP);
		IClusterServiceClient clusterClient1 = factory.createClient(endpoint1);
		IClusterServiceClient clusterClient2 = factory.createClient(endpoint2);

		assertTrue(registry.getClusterIds().isEmpty());

		registry.addCluster(cId1, clusterClient1);
		assertTrue(registry.isClusterRegistered(cId1));
		assertTrue(registry.getClusterIds().contains(cId1));
		assertEquals(endpoint1, registry.getClusterEndpoint(cId1));

		// add again with other endpoint --> should be ignored
		registry.addCluster(cId1, clusterClient1);
		assertEquals(endpoint1, registry.getClusterEndpoint(cId1));

		// adds a second cluster
		registry.addCluster(cId2, clusterClient2);
		assertTrue(registry.isClusterRegistered(cId2));
		assertTrue(registry.getClusterIds().contains(cId2));
		assertEquals(endpoint2, registry.getClusterEndpoint(cId2));

		// remove second cluster
		registry.deleteCluster(cId2);
		assertFalse(registry.getClusterIds().contains(cId2));
	}


	@Test(expected = UnknownClusterException.class)
	public void getEndpointUnknown() throws Exception {
		registry.getClusterEndpoint(UUID.randomUUID().toString());
	}


	@Test
	public void rebindCluster() throws Exception {
		String cId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint1 = new ServiceEndpointDTO();
		endpoint1.setHost(UUID.randomUUID().toString());
		endpoint1.setProtocol(Protocol.REST);
		ServiceEndpointDTO endpoint2 = new ServiceEndpointDTO();
		endpoint2.setHost(UUID.randomUUID().toString());
		endpoint2.setProtocol(Protocol.THRIFT_TCP);
		IClusterServiceClient clusterClient1 = factory.createClient(endpoint1);
		IClusterServiceClient clusterClient2 = factory.createClient(endpoint2);

		registry.addCluster(cId, clusterClient1);
		registry.rebindCluster(cId, clusterClient2);

		assertEquals(endpoint2, registry.getClusterEndpoint(cId));
	}


	@Test(expected = UnknownClusterException.class)
	public void rebindUnknownCluster() throws Exception {
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		endpoint.setHost(UUID.randomUUID().toString());
		endpoint.setProtocol(Protocol.REST);
		registry.rebindCluster(UUID.randomUUID().toString(), factory.createClient(endpoint));
	}

	@Test
	public void bindAndUnbindProgram() throws Exception {
		String cId1 = UUID.randomUUID().toString();
		String cId2 = UUID.randomUUID().toString();
		String pId1 = UUID.randomUUID().toString();
		String pId2 = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint1 = new ServiceEndpointDTO();
		endpoint1.setHost(UUID.randomUUID().toString());
		endpoint1.setProtocol(Protocol.REST);
		ServiceEndpointDTO endpoint2 = new ServiceEndpointDTO();
		endpoint2.setHost(UUID.randomUUID().toString());
		endpoint2.setProtocol(Protocol.REST);
		IClusterServiceClient client1 = factory.createClient(endpoint1);
		IClusterServiceClient client2 = factory.createClient(endpoint2);

		registry.addCluster(cId1, client1);
		registry.addCluster(cId2, client2);

		registry.bindProgramToCluster(pId1, cId1);
		registry.bindProgramToCluster(pId2, cId2);

		assertTrue(registry.isProgramRegistered(pId1));
		assertTrue(registry.isProgramRegistered(pId2));
		assertEquals(cId1, registry.getClusterId(pId1));
		assertEquals(cId2, registry.getClusterId(pId2));
		assertTrue(registry.getProgramIds().contains(pId1));
		assertTrue(registry.getProgramIds().contains(pId2));

		registry.unbindProgram(pId1);
		assertFalse(registry.getProgramIds().contains(pId1));
		assertTrue(registry.getProgramIds().contains(pId2));
	}


	@Test(expected = UnknownClusterException.class)
	public void bindToUnknownCluster() throws Exception {
		registry.bindProgramToCluster(UUID.randomUUID().toString(), UUID.randomUUID().toString());
	}


	@Test(expected = UnknownProgramException.class)
	public void getClusterUnknownProgram() throws Exception {
		registry.getClusterId(UUID.randomUUID().toString());
	}

	@Test
	public void getInstance() throws Exception {
		ProgramClusterRegistry instance1 = ProgramClusterRegistry.getInstance();
		ProgramClusterRegistry instance2 = ProgramClusterRegistry.getInstance();
		assertSame(instance1, instance2);
	}
}
