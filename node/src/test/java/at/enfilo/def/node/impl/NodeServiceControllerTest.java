package at.enfilo.def.node.impl;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Extension;
import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.TakeControlException;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class NodeServiceControllerTest {

	private NodeServiceController controller;
	private NodeType nodeType;
	private Map<String, String> nodeInfoParameters;
	private List<INodeObserverServiceClient> observers;
	private NodeObserverServiceClientFactory observerServiceClientFactory;
	private NodeConfiguration configuration;

	@Before
	public void setUp() throws Exception {
		nodeType = NodeType.REDUCER;
		nodeInfoParameters = new HashMap<>();
		observers = new LinkedList<>();
		configuration = Mockito.mock(NodeConfiguration.class);
		when(configuration.getId()).thenReturn(UUID.randomUUID().toString());
		Environment environment = new Environment();
		Feature java = new Feature("java", "1.8");
		java.setGroup("language");
		Feature python3 = new Feature("python", "3.7");
		python3.setGroup("language");
		python3.addExtension(new Extension("numpy", "2"));
		environment.addFeature(java);
		environment.addFeature(python3);
		when(configuration.getFeatureEnvironment()).thenReturn(environment);
		observerServiceClientFactory = Mockito.mock(NodeObserverServiceClientFactory.class);
		controller = new NodeServiceController(
				NodeType.WORKER,
				observers,
				configuration,
				observerServiceClientFactory,
				DEFLoggerFactory.getLogger(NodeServiceControllerTest.class)
		) {
			@Override
			protected Map<String, String> getNodeInfoParameters() {
				return nodeInfoParameters;
			}
		};
	}

	@Test
	public void takeControl() throws Exception {
		String cId = UUID.randomUUID().toString();

		controller.takeControl(cId);
		NodeInfoDTO info = controller.getInfo();
		assertEquals(cId, info.getClusterId());

		// Two times take control
		controller.takeControl(cId);
		info = controller.getInfo();
		assertEquals(cId, info.getClusterId());
	}

	@Test(expected = TakeControlException.class)
	public void takeControlFailed() throws Exception {
		controller.takeControl(UUID.randomUUID().toString());
		controller.takeControl(UUID.randomUUID().toString());
	}

	@Test
	public void registerObserver() throws Exception {
		ServiceEndpointDTO endpoint1 = new ServiceEndpointDTO();
		endpoint1.setHost("localhost");
		endpoint1.setProtocol(Protocol.THRIFT_HTTP);
		endpoint1.setPort(-1);

		ServiceEndpointDTO endpoint2 = new ServiceEndpointDTO();
		endpoint2.setHost("localhost");
		endpoint2.setProtocol(Protocol.THRIFT_TCP);
		endpoint2.setPort(-1);

		ServiceEndpointDTO endpoint3 = new ServiceEndpointDTO();
		endpoint3.setHost("localhost");
		endpoint3.setProtocol(Protocol.THRIFT_TCP);
		endpoint3.setPort(-1);

		assertTrue(observers.isEmpty());

		// Setup mocks
		NodeObserverServiceClientFactory factory = new NodeObserverServiceClientFactory();
		INodeObserverServiceClient client1 = factory.createClient(endpoint1);
		INodeObserverServiceClient client2 = factory.createClient(endpoint2);
		INodeObserverServiceClient client3 = factory.createClient(endpoint3);

		// Add first observer
		when(observerServiceClientFactory.createClient(endpoint1)).thenReturn(client1);
		controller.registerObserver(endpoint1, false, -1, PeriodUnit.SECONDS);
		assertEquals(1, observers.size());
		assertEquals(endpoint1, observers.get(0).getServiceEndpoint());

		// Add second observer
		when(observerServiceClientFactory.createClient(endpoint2)).thenReturn(client2);
		controller.registerObserver(endpoint2, false, -1, PeriodUnit.SECONDS);
		assertEquals(2, observers.size());
		assertEquals(endpoint2, observers.get(1).getServiceEndpoint());

		// Add third observer (same as second observer)
		when(observerServiceClientFactory.createClient(endpoint3)).thenReturn(client3);
		controller.registerObserver(endpoint3, false, -1, PeriodUnit.SECONDS);
		assertEquals(2, observers.size());
	}

	@Test
	public void registerObserversPeriodic() throws Exception {
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		endpoint.setHost("localhost");
		endpoint.setProtocol(Protocol.THRIFT_HTTP);
		endpoint.setPort(-1);

		// Setup mock
		INodeObserverServiceClient observerClient = Mockito.mock(INodeObserverServiceClient.class);
		when(observerServiceClientFactory.createClient(endpoint)).thenReturn(observerClient);

		// Add observer
		controller.registerObserver(endpoint, true, 2, PeriodUnit.SECONDS);
		assertEquals(1, observers.size());

		Thread.sleep(11 * 1000); // wait 11 seconds
		// 11/2 = (int)5
		verify(observerClient, times(5)).notifyNodeInfo(any(), any());
	}


	@Test
	public void deregisterObserver() throws Exception {
		// Prepare observers
		ServiceEndpointDTO endpointDTO1 = new ServiceEndpointDTO(UUID.randomUUID().toString(), 0, Protocol.REST);
		ServiceEndpointDTO endpointDTO2 = new ServiceEndpointDTO(UUID.randomUUID().toString(), 0, Protocol.REST);
		ServiceEndpointDTO endpointDTO3 = new ServiceEndpointDTO(UUID.randomUUID().toString(), 0, Protocol.REST);
		NodeObserverServiceClientFactory factory = new NodeObserverServiceClientFactory();
		INodeObserverServiceClient client1 = factory.createClient(endpointDTO1);
		INodeObserverServiceClient client2 = factory.createClient(endpointDTO2);
		INodeObserverServiceClient client3 = factory.createClient(endpointDTO3);
		observers.add(client1);
		observers.add(client2);
		observers.add(client3);

		// Deregister
		assertEquals(3, observers.size());
		controller.deregisterObserver(endpointDTO2);
		assertEquals(2, observers.size());
		assertTrue(observers.contains(client1));
		assertTrue(observers.contains(client3));

		controller.deregisterObserver(endpointDTO2);
		assertEquals(2, observers.size());
	}

	@Test
	public void getNodeInfoDTO() throws Exception {
		NodeInfoDTO info1 = controller.getInfo();
		assertNotNull(info1);

		Thread.sleep(1000);

		NodeInfoDTO info2 = controller.getInfo();
		assertNotEquals(info1, info2);
	}

	@Test
	public void getFeatures() {
		List<Feature> features = configuration.getFeatureEnvironment().getFeatures();
		List<FeatureDTO> featureDTOS = controller.getFeatures();

		assertNotNull(featureDTOS);
		assertEquals(features.size(), featureDTOS.size());

		for (Feature feature : features) {
			for (FeatureDTO featureDTO : featureDTOS) {
				if (feature.getName().equals(featureDTO.getName())) {
					assertEquals(feature.getName(), featureDTO.getName());
					assertEquals(feature.getVersion(), featureDTO.getVersion());
					assertEquals(feature.getGroup(), featureDTO.getGroup());
					if (feature.getExtensions() != null && !feature.getExtensions().isEmpty()) {
						Extension extension = feature.getExtensions().get(0);
						assertNotNull(featureDTO.getExtensions());
						assertEquals(1, featureDTO.getExtensions().size());
						FeatureDTO extensionDTO = featureDTO.getExtensions().get(0);
						assertEquals(extension.getName(), extensionDTO.getName());
						assertEquals(extension.getVersion(), extensionDTO.getVersion());
						assertNull(extensionDTO.getGroup());
					}
				}
			}
		}
	}

	@Test
	public void addSharedResource() throws Exception {
		ResourceDTO sharedResource = new ResourceDTO();
		sharedResource.setData(new byte[]{0x00, 0x01, 0x02});
		sharedResource.setId(UUID.randomUUID().toString());

		controller.addSharedResource(sharedResource);

		ResourceDTO r = controller.getSharedResource(sharedResource.getId());
		assertEquals(sharedResource, r);
	}

	@Test
	public void removeSharedResources() throws Exception {
		ResourceDTO sharedResource1 = new ResourceDTO();
		sharedResource1.setId(UUID.randomUUID().toString());
		sharedResource1.setData(new byte[]{0x00, 0x01, 0x02});
		ResourceDTO sharedResource2 = new ResourceDTO();
		sharedResource2.setId(UUID.randomUUID().toString());
		sharedResource2.setData(new byte[]{0x00, 0x01, 0x02});

		controller.addSharedResource(sharedResource1);
		controller.addSharedResource(sharedResource2);
		List<String> rIds = new LinkedList<>();
		rIds.add(sharedResource1.getId());
		rIds.add(sharedResource2.getId());
		controller.removeSharedResources(rIds);

		try {
			controller.getSharedResource(sharedResource1.getId());
			fail();
		} catch (UnknownCacheObjectException ex) {
			// expected
		}

		try {
			controller.getSharedResource(sharedResource2.getId());
			fail();
		} catch (UnknownCacheObjectException ex) {
			// expected
		}
	}
}
