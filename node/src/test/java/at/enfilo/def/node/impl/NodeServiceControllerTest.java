package at.enfilo.def.node.impl;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Extension;
import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.communication.exception.TakeControlException;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.QueueNotExistsException;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.queue.Queue;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class NodeServiceControllerTest {

	private NodeServiceController controller;
	private List<INodeObserverServiceClient> observers;
	private NodeObserverServiceClientFactory observerServiceClientFactory;
	private NodeConfiguration configuration;
	private QueuePriorityWrapper queuePriorityWrapper;
	private Queue queue;
	private String qId;
	private boolean queueReleased;
	private List<ExecutorService> executorServices;
	private boolean elementFromQueuesRemoved;
	private List<String> queuedTasks;
	private QueueInfoDTO queueInfo;
	private Set<String> finishedTasks;

	@Before
	public void setUp() throws Exception {
		observers = new LinkedList<>();
		finishedTasks = new HashSet<>();
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
		when(configuration.getQueueLifeTime()).thenReturn(30);
		when(configuration.getQueueLifeTimeUnit()).thenReturn(PeriodUnit.SECONDS);
		observerServiceClientFactory = Mockito.mock(NodeObserverServiceClientFactory.class);
		queuePriorityWrapper = Mockito.mock(QueuePriorityWrapper.class);
		queue = Mockito.mock(Queue.class);
		queuedTasks = new LinkedList<>();
		queueInfo = Mockito.mock(QueueInfoDTO.class);
		doAnswer(answer -> {
			queuedTasks.add(((TaskDTO)answer.getArguments()[0]).getId());
			return null;
		}).when(queue).queue(any());
		when(queue.getQueuedElements()).thenReturn(queuedTasks);
		when(queue.size()).thenReturn(queuedTasks.size());
		when(queue.toQueueInfoDTO()).thenReturn(queueInfo);
		when(queueInfo.getNumberOfTasks()).thenAnswer(answer -> {
			return queuedTasks.size();
		});
		qId = UUID.randomUUID().toString();
		queueReleased = false;
		elementFromQueuesRemoved = false;
		executorServices = new LinkedList<>();
		when(queuePriorityWrapper.getQueue(qId)).thenReturn(queue);
		when(queuePriorityWrapper.getQueue(not(eq(qId)))).thenThrow(QueueNotExistsException.class);
		when(queue.getQueueId()).thenReturn(qId);
		doAnswer(answer -> {
			queueReleased = true;
			return null;
		}).when(queue).release();
		doAnswer(answer -> {
			queueReleased = false;
			return null;
		}).when(queue).pause();
		when(queue.isReleased()).then(answer -> {
			return queueReleased;
		});
		controller = new NodeServiceController<TaskDTO>(
				NodeType.WORKER,
				observers,
				finishedTasks,
				configuration,
				observerServiceClientFactory,
				"NodeServiceControllerTest",
				TaskDTO.class,
				DEFLoggerFactory.getLogger(NodeServiceControllerTest.class)
		) {
			@Override
			public List<String> getQueueIds() {
				return Collections.singletonList(qId);
			}

			@Override
			protected void throwException(String eId, String message) throws Exception {

			}

			@Override
			protected Set<ITuple<ContextIndicator, ?>> getLogContext(TaskDTO element) {
				return null;
			}

			@Override
			protected Set<ITuple<ContextIndicator, ?>> getLogContext(String elementId) {
				return null;
			}

			@Override
			protected void removeElementFromQueues(String eId) {
				elementFromQueuesRemoved = true;
			}

			@Override
			protected void setState(TaskDTO element, ExecutionState state) {
				element.setState(state);
			}

			@Override
			protected List<String> getElementIds(List<TaskDTO> elements) {
				return elements.stream().map(TaskDTO::getId).collect(Collectors.toList());
			}

			@Override
			protected List<? extends Queue> getQueues() throws QueueNotExistsException {
				return Collections.singletonList(queuePriorityWrapper.getQueue(qId));
			}

			@Override
			protected Future<Void> queueElements(String qId, List<TaskDTO> elementsToQueue, ServiceEndpointDTO targetNodeEndpoint) throws ClientCreationException, ClientCommunicationException {
				return null;
			}

			@Override
			protected void notifyObservers(String nId, List<String> eIds) {

			}

			@Override
			protected void finishedExecutionOfElement(String eId) {

			}

			@Override
			protected Queue createQueueInstance(String qId) {
				return queue;
			}

			@Override
			protected QueuePriorityWrapper getQueuePriorityWrapper() {
				return queuePriorityWrapper;
			}

			@Override
			protected List<? extends ExecutorService> getExecutorServices() {
				return executorServices;
			}

			@Override
			protected String getElementName() {
				return "Task";
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

	@Test
	public void notifyObserverNodeInfo() throws Exception {
		INodeObserverServiceClient observer = Mockito.mock(INodeObserverServiceClient.class);

		controller.notifyObserverNodeInfo(observer);

		verify(observer, times(1)).notifyNodeInfo(any(), any());
	}

	@Test
	public void getSetStoreRoutine() {
		String id = UUID.randomUUID().toString();
		controller.setStoreRoutineId(id);
		assertEquals(id, controller.getStoreRoutineId());
	}

	@Test
	public void getQueueInfo() throws Exception {
		controller.getQueueInfo(qId);
		verify(queuePriorityWrapper, times(1)).getQueue(qId);
		verify(queue, times(1)).toQueueInfoDTO();
	}

	@Test
	public void createQueue_queueContained() throws Exception {
		when(queuePriorityWrapper.containsQueue(qId)).thenReturn(true);
		controller.createQueue(qId);

		verify(queuePriorityWrapper, times(1)).containsQueue(qId);
		verify(queue, times(0)).release();
		verify(queuePriorityWrapper, times(0)).addQueue(queue);
	}

	@Test
	public void createQueue_queueNotContained() throws Exception {
		when(queuePriorityWrapper.containsQueue(qId)).thenReturn(false);
		controller.createQueue(qId);

		verify(queuePriorityWrapper, times(1)).containsQueue(qId);
		verify(queue, times(1)).release();
		verify(queuePriorityWrapper, times(1)).addQueue(queue);
	}

	@Test
	public void pauseQueue() throws Exception {
		controller.pauseQueue(qId);
		verify(queuePriorityWrapper, times(1)).getQueue(qId);
		verify(queue, times(1)).pause();
	}

	@Test
	public void deleteQueue() throws Exception {
		controller.deleteQueue(qId);
		verify(queuePriorityWrapper, times(1)).deleteQueue(qId);
	}

	@Test
	public void releaseQueue() throws Exception {
		controller.releaseQueue(qId);
		verify(queuePriorityWrapper, times(1)).getQueue(qId);
		verify(queue, times(1)).release();
	}

	@Test
	public void getNodeInfoParameters() throws Exception {
		Map<String, String> params = controller.getNodeInfoParameters();

		assertEquals(5, params.size());
		assertTrue(params.containsKey("numberOfQueues"));
		assertTrue(params.containsKey("numberOfQueuedElements"));
		assertTrue(params.containsKey("numberOfRunningElements"));
		assertTrue(params.containsKey("runningElements"));
		assertTrue(params.containsKey("storeRoutineId"));
	}

	@Test
	public void queueElements() throws Exception {
		// Prepare observers
		INodeObserverServiceClient observerClient = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observerClient);

		queuePriorityWrapper.addQueue(queue);

		// Prepare program and two jobs
		String pId = UUID.randomUUID().toString();
		String jId1 = UUID.randomUUID().toString();
		Random rnd = new Random();
		String t11Id = UUID.randomUUID().toString();
		String t12Id = UUID.randomUUID().toString();
		List<TaskDTO> tasks1 = new LinkedList<>();
		TaskDTO t11 = new TaskDTO(t11Id, jId1, pId, ExecutionState.FAILED, rnd.nextInt(), rnd.nextInt(),  rnd.nextInt(), null, null, null, null, null, rnd.nextLong());
		TaskDTO t12 = new TaskDTO(t12Id, jId1, pId, ExecutionState.FAILED, rnd.nextInt(), rnd.nextInt(), rnd.nextInt(), null, null, null, null, null, rnd.nextLong());
		tasks1.add(t11);
		tasks1.add(t12);

		controller.queueElements(qId, tasks1);

		assertEquals(2, controller.getQueuedElements(qId).size());
		assertEquals(2, controller.getQueueInfo(qId).getNumberOfTasks());
		assertTrue(controller.getQueuedElements(qId).contains(t11Id));
		assertTrue(controller.getQueuedElements(qId).contains(t12Id));
	}

	@Test (expected = QueueNotExistsException.class)
	public void queueElements_unknownQueue() throws Exception {
		List<TaskDTO> tasks = new LinkedList<>();
		tasks.add(new TaskDTO());
		controller.queueElements(UUID.randomUUID().toString(), tasks);
	}

	@Test (expected = QueueNotExistsException.class)
	public void moveElements_unknownQueue() throws Exception {
		List<String> eIdsToMove = new LinkedList<>();
		eIdsToMove.add(UUID.randomUUID().toString());
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		controller.moveElements(UUID.randomUUID().toString(), eIdsToMove, endpoint);
	}

	@Test
	public void abortElement_scheduledElement() throws Exception {
		TaskDTO task = new TaskDTO();
		String taskId = UUID.randomUUID().toString();
		task.setId(taskId);
		task.setState(ExecutionState.SCHEDULED);
		controller.elementCache.cache(taskId, task);
		assertFalse(elementFromQueuesRemoved);

		controller.abortElement(taskId, task, ExecutionState.SCHEDULED);

		assertTrue(elementFromQueuesRemoved);
		assertEquals(ExecutionState.FAILED, ((TaskDTO)controller.elementCache.fetch(taskId)).getState());
	}

	@Test
	public void abortElement_runningElement() throws Exception {
		TaskDTO task = new TaskDTO();
		String taskId = UUID.randomUUID().toString();
		task.setId(taskId);
		task.setState(ExecutionState.RUN);
		controller.runningElements.add(taskId);
		controller.elementCache.cache(taskId, task);
		assertFalse(elementFromQueuesRemoved);
		assertTrue(controller.runningElements.contains(taskId));

		controller.abortElement(taskId, task, ExecutionState.RUN);

		assertFalse(elementFromQueuesRemoved);
		assertEquals(ExecutionState.FAILED, ((TaskDTO)controller.elementCache.fetch(taskId)).getState());
		assertFalse(controller.runningElements.contains(taskId));
	}

	@Test
	public void abortRunningElement() throws Exception {
		String taskId = UUID.randomUUID().toString();
		ExecutorService service1 = Mockito.mock(ExecutorService.class);
		ExecutorService service2 = Mockito.mock(ExecutorService.class);
		ExecutorService service3 = Mockito.mock(ExecutorService.class);
		when(service1.getRunningElement()).thenReturn(null);
		when(service2.getRunningElement()).thenReturn(UUID.randomUUID().toString());
		when(service3.getRunningElement()).thenReturn(taskId);

		List<ExecutorService> services = Arrays.asList(service1, service2, service3);
		controller.getExecutorServices().addAll(services);

		controller.abortRunningElement(taskId);

		verify(service1, times(1)).getRunningElement();
		verify(service1, times(0)).cancelRunningElement();
		verify(service2, times(2)).getRunningElement();
		verify(service2, times(0)).cancelRunningElement();
		verify(service3, times(2)).getRunningElement();
		verify(service3, times(1)).cancelRunningElement();
	}

	@Test
	public void fetchFinishedElement() throws Exception {
		// Prepare finished tasks
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		TaskDTO t2 = new TaskDTO();
		t2.setId(t2Id);
		TaskDTO t3 = new TaskDTO();
		t3.setId(t3Id);

		DTOCache<TaskDTO> cache =  DTOCache.getInstance("NodeServiceControllerTest", TaskDTO.class);
		finishedTasks.add(t1Id);
		cache.cache(t1.getId(), t1);
		finishedTasks.add(t2Id);
		cache.cache(t2.getId(), t2);
		finishedTasks.add(t3Id);
		cache.cache(t3.getId(), t3);

		TaskDTO t1Finished = (TaskDTO) controller.fetchFinishedElement(t1Id);
		assertEquals(t1, t1Finished);
		TaskDTO t3Finished = (TaskDTO)controller.fetchFinishedElement(t3Id);
		assertEquals(t3, t3Finished);

		assertEquals(1, finishedTasks.size());
	}

	@Test
	public void notifyElementSuccess() throws Exception {
		// Prepare task
		String t1Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		List<String> notifyTaskIds = new LinkedList<>();
		notifyTaskIds.add(t1Id);

		// Prepare observers
		INodeObserverServiceClient observer = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observer);

		assertTrue(finishedTasks.isEmpty());
		controller.notifyStateChanged(t1Id, ExecutionState.RUN, ExecutionState.SUCCESS);

		assertTrue(finishedTasks.contains(t1Id));
		verify(observer).notifyElementsNewState(any(), eq(notifyTaskIds), eq(ExecutionState.SUCCESS));
	}

	@Test
	public void notifyTaskFailed() throws Exception {
		// Prepare task
		String t1Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		List<String> notifyTaskIds = new LinkedList<>();
		notifyTaskIds.add(t1Id);

		// Prepare observers
		INodeObserverServiceClient observer = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observer);

		assertTrue(finishedTasks.isEmpty());
		controller.notifyStateChanged(t1Id, ExecutionState.RUN, ExecutionState.FAILED);

		assertTrue(finishedTasks.contains(t1Id));
		verify(observer).notifyElementsNewState(any(), eq(notifyTaskIds), eq(ExecutionState.FAILED));
	}

	@Test
	public void notifyTaskRun() throws Exception {
		// Prepare task
		String t1Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		List<String> notifyTaskIds = new LinkedList<>();
		notifyTaskIds.add(t1Id);

		// Prepare observers
		INodeObserverServiceClient observer = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observer);

		controller.notifyStateChanged(t1Id, ExecutionState.SCHEDULED, ExecutionState.RUN);

		verify(observer).notifyElementsNewState(any(), eq(notifyTaskIds), eq(ExecutionState.RUN));
	}
}
