package at.enfilo.def.cluster.impl;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.TakeControlException;
import at.enfilo.def.transfer.dto.CloudType;
import at.enfilo.def.transfer.dto.NodeType;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

public class ClusterResourceTest {

	private ClusterResource clusterResource;

	@Before
	public void setUp() throws Exception {
		Constructor<ClusterResource> constructor = ClusterResource.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		clusterResource = constructor.newInstance();
	}

	@Test
	public void getInstance() throws Exception {
		ClusterResource inst1 = ClusterResource.getInstance();
		ClusterResource inst2 = ClusterResource.getInstance();

		assertSame(inst1, inst2);
	}

	@Test
	public void takeControl() throws Exception {
		String managerId = UUID.randomUUID().toString();

		clusterResource.takeControl(managerId);
		assertEquals(managerId, clusterResource.getManagerId());

		// Twice, should be work
		clusterResource.takeControl(managerId);
		assertEquals(managerId, clusterResource.getManagerId());
	}

	@Test(expected = TakeControlException.class)
	public void takeControlFailed() throws Exception {
		String managerId1 = UUID.randomUUID().toString();
		String managerId2 = UUID.randomUUID().toString();

		clusterResource.takeControl(managerId1);
		clusterResource.takeControl(managerId2);
	}

	@Test
	public void getAndSetName() throws Exception {
		String name = UUID.randomUUID().toString();

		clusterResource.setName(name);
		assertEquals(name, clusterResource.getName());
	}

	@Test
	public void initialParameters() throws Exception {
		Instant now = Instant.now();
		Instant startTime = clusterResource.getStartTime();
		assertTrue(now.getNano() >= startTime.getNano());

		String id = clusterResource.getId();
		assertEquals(4, UUID.fromString(id).version());
	}

	@Test
	public void getAndSetCloudType() throws Exception {
		assertNull(clusterResource.getCloudType());

		clusterResource.setCloudType(CloudType.COMMUNITY);
		assertEquals(CloudType.COMMUNITY, clusterResource.getCloudType());

		clusterResource.setCloudType(CloudType.PRIVATE);
		assertEquals(CloudType.PRIVATE, clusterResource.getCloudType());
	}

	@Test
	public void getSetReducerSchedulerService() throws Exception {
		getSetSchedulerService(NodeType.REDUCER);
	}

	@Test
	public void getSetWorkerSchedulerService() throws Exception {
		getSetSchedulerService(NodeType.WORKER);
	}

	public void getSetSchedulerService(NodeType nodeType) throws Exception {
		assertNotNull(clusterResource.getSchedulerServiceClient(nodeType));

		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(
				UUID.randomUUID().toString(),
				new Random().nextInt(),
				Protocol.REST);

		clusterResource.setSchedulerService(nodeType, endpoint);

		assertEquals(endpoint, clusterResource.getSchedulerServiceClient(nodeType).getServiceEndpoint());
	}

	@Test
	public void getAndSetDefaultRoutines() throws Exception {
		assertNotNull(clusterResource.getDefaultMapRoutineId());

		String mapRoutineId = UUID.randomUUID().toString();

		clusterResource.setDefaultMapRoutineId(mapRoutineId);

		assertEquals(mapRoutineId, clusterResource.getDefaultMapRoutineId());
	}
}
