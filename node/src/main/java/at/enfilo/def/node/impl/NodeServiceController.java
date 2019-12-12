package at.enfilo.def.node.impl;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Extension;
import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.communication.exception.TakeControlException;
import at.enfilo.def.config.server.core.DEFServerHolderConfiguration;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.node.api.exception.QueueNotExistsException;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.queue.Queue;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.util.ClusterRegistration;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.*;
import org.apache.thrift.TBase;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class NodeServiceController<T extends TBase> implements IStateChangeListener {

	private static final long CLUSTER_REGISTRATION_DELAY = 10;
	private static final TimeUnit CLUSTER_REGISTRATION_DELAY_UNIT = TimeUnit.SECONDS;

	public static final String DTO_RESOURCE_CACHE_CONTEXT = "node-resources";

	private final NodeType nodeType;
    private final List<INodeObserverServiceClient> observers;
    private final NodeConfiguration nodeConfiguration;
    private final NodeObserverServiceClientFactory nodeObserverServiceClientFactory;
    private final IDEFLogger logger;
    private final DTOCache<ResourceDTO> sharedResources;
    protected final List<ExecutorService> executorServices;

    protected final Object elementLock;
    protected final Set<String> runningElements;
    protected final Set<String> finishedElements;
    protected final DTOCache<T> elementCache;

    private String cId; // cluster id.
	private String storeRoutineId;

    protected NodeServiceController(
			NodeType nodeType,
        	List<INodeObserverServiceClient> observers,
        	Set<String> finishedElements,
        	NodeConfiguration nodeConfiguration,
        	NodeObserverServiceClientFactory nodeObserverServiceClientFactory,
        	String dtoCacheContext,
        	Class<T> cls,
        	IDEFLogger logger
    ) {
    	this.nodeType = nodeType;
        this.observers = observers;
		this.nodeObserverServiceClientFactory = nodeObserverServiceClientFactory;
        this.nodeConfiguration = nodeConfiguration;
        this.logger = logger;
        this.sharedResources = DTOCache.getInstance(DTO_RESOURCE_CACHE_CONTEXT, ResourceDTO.class);
        this.executorServices = new LinkedList<>();
		this.elementLock = new Object();
		this.finishedElements = finishedElements;
		this.runningElements = new HashSet<>();
		this.elementCache = DTOCache.getInstance(dtoCacheContext, cls);
		this.storeRoutineId = nodeConfiguration.getStoreRoutineId();

        // Auto registration on cluster
        if (this.nodeConfiguration.isClusterRegistration()) {
			ClusterRegistration clusterRegistration = new ClusterRegistration(
					this.nodeConfiguration.getClusterEndpoint(),
					createNodeEndpoint(this.nodeConfiguration.getServerHolderConfiguration()),
					nodeType
			);
			new Timer("ClusterRegistration", true).schedule(
					clusterRegistration,
					CLUSTER_REGISTRATION_DELAY_UNIT.toMillis(CLUSTER_REGISTRATION_DELAY)
			);
		}
    }

    private ServiceEndpointDTO createNodeEndpoint(DEFServerHolderConfiguration configuration) {
    	ServiceEndpointDTO nodeEndpoint = new ServiceEndpointDTO();
		try {
			nodeEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			nodeEndpoint.setHost(InetAddress.getLoopbackAddress().getHostAddress());
		}
		if (configuration.getThriftTCPConfiguration().isEnabled()) {
			nodeEndpoint.setProtocol(Protocol.THRIFT_TCP);
			nodeEndpoint.setPathPrefix(configuration.getThriftTCPConfiguration().getUrlPattern());
			nodeEndpoint.setPort(configuration.getThriftTCPConfiguration().getPort());
			return nodeEndpoint;
		}
		if (configuration.getThriftHTTPConfiguration().isEnabled()) {
			nodeEndpoint.setProtocol(Protocol.THRIFT_HTTP);
			nodeEndpoint.setPathPrefix(configuration.getThriftHTTPConfiguration().getUrlPattern());
			nodeEndpoint.setPort(configuration.getThriftHTTPConfiguration().getPort());
			return nodeEndpoint;
		}
		if (configuration.getRESTConfiguration().isEnabled()) {
			nodeEndpoint.setProtocol(Protocol.REST);
			nodeEndpoint.setPathPrefix(configuration.getRESTConfiguration().getUrlPattern());
			nodeEndpoint.setPort(configuration.getRESTConfiguration().getPort());
			return nodeEndpoint;
		}
		return nodeEndpoint;
	}

	/**
	 * Cluster takes control over this node.
	 * @param cId
	 * @throws TakeControlException
	 */
    protected void takeControl(String cId) throws TakeControlException {
    	if ((this.cId != null) && !this.cId.equals(cId)) {
            throw new TakeControlException(String.format(
                "Node is already under control of cluster:  \"%s\"",
                cId
            ));
        }
        this.cId = cId;
    }

	/**
	 * Returns Node information.
	 * @return
	 */
	protected NodeInfoDTO getInfo() {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        NodeInfoDTO info = new NodeInfoDTO();
        info.setId(nodeConfiguration.getId());
        info.setType(nodeType);
        info.setClusterId(cId);
        info.setNumberOfCores(operatingSystemMXBean.getAvailableProcessors());
        info.setLoad(operatingSystemMXBean.getSystemLoadAverage());
        info.setTimeStamp(System.currentTimeMillis());
        info.setParameters(getNodeInfoParameters());
        return info;
    }

	/**
	 * Returns Node environment.
	 * @return
	 */
	protected NodeEnvironmentDTO getEnvironment() {
		return new NodeEnvironmentDTO(nodeConfiguration.getId(), nodeConfiguration.getFeatureEnvironment().getAsString());
	}

	public List<FeatureDTO> getFeatures() {
		Environment environment = nodeConfiguration.getFeatureEnvironment();
		List<FeatureDTO> features = new ArrayList<>();
		if (environment == null || environment.getFeatures() == null) {
			return features;
		}
		for (Feature feature : environment.getFeatures()) {
			FeatureDTO dto = new FeatureDTO();
			dto.setName(feature.getName());
			dto.setVersion(feature.getVersion());
			dto.setGroup(feature.getGroup());
			if (feature.getExtensions() != null && !feature.getExtensions().isEmpty()) {
				dto.setExtensions(new ArrayList<>());
				for (Extension extension : feature.getExtensions()) {
					FeatureDTO exDto = new FeatureDTO();
					exDto.setName(extension.getName());
					exDto.setVersion(extension.getVersion());
					dto.getExtensions().add(exDto);
				}
			}
			features.add(dto);
		}
		return features;
	}

	/**
	 * Returns parameters for {@link NodeInfoDTO}. (see getInfo())
	 * @return
	 */
	protected Map<String, String> getNodeInfoParameters() {
    	Map<String, String> params = new HashMap<>();
		params.put("numberOfQueues", Integer.toString(getQueuePriorityWrapper().getNumberOfQueues()));
		params.put("numberOfQueuedElements", Integer.toString(getQueuePriorityWrapper().getNumberOfQueuedElements()));
		params.put("numberOfRunningElements", Integer.toString(getNumberOfRunningElements()));
		params.put("storeRoutineId", storeRoutineId);
		synchronized (elementLock) {
			params.put("runningElements", runningElements.stream().collect(Collectors.joining(" ")));
		}
    	return params;
	}

	private int getNumberOfRunningElements() {
		synchronized (elementLock) {
			logger.debug("Fetch number of running {}s.", getElementName());
			return runningElements.size();
		}
	}

	/**
	 * Register an observer at this node.
	 * @param serviceEndpointDTO - observer endpoint
	 * @param checkPeriodically - periodic notification
	 * @param periodDuration - periodic information
	 * @param periodUnit - periodic information
	 * @throws ClientCreationException
	 */
    protected void registerObserver(
        ServiceEndpointDTO serviceEndpointDTO,
        boolean checkPeriodically,
        long periodDuration,
        PeriodUnit periodUnit
    ) throws ClientCreationException {
        registerObserver(
            nodeObserverServiceClientFactory.createClient(serviceEndpointDTO),
            checkPeriodically,
            periodDuration,
            periodUnit
        );
    }

	/**
	 * Register an observer at this node.
	 * @param observerServiceClient - Observer service client
	 * @param checkPeriodically - periodic notification
	 * @param periodDuration - periodic information
	 * @param periodUnit - periodic information
	 */
    protected void registerObserver(
        INodeObserverServiceClient observerServiceClient,
        boolean checkPeriodically,
        long periodDuration,
        PeriodUnit periodUnit
    )  {
        ServiceEndpointDTO endpointDTO = observerServiceClient.getServiceEndpoint();

        if (observers.stream().map(INodeObserverServiceClient::getServiceEndpoint).noneMatch(
            e -> e.equals(endpointDTO)
        )) {
            observers.add(observerServiceClient);

            if (checkPeriodically) {
                // Schedule a periodic notification
                TimeUnit timeUnit = convertPeriodToTimeUnit(periodUnit);

                new Timer("NotifyObservers", true).scheduleAtFixedRate(
						new TimerTask() {
							@Override
							public void run() {
								notifyObserverNodeInfo(observerServiceClient);
							}
						},
                		timeUnit.toMillis(periodDuration),
						timeUnit.toMillis(periodDuration)
				);
            }
        }
    }

	/**
	 * Deregister a specific observer.
	 * @param endpointDTO
	 */
	protected void deregisterObserver(ServiceEndpointDTO endpointDTO) {
		observers.removeIf(
            currentElement -> currentElement.getServiceEndpoint().equals(endpointDTO)
        );
    }


    /**
     * Helper method that executes specified consumer for all registered observers.
     *
     * @param proxyConsumer actual method to be called for every observer.
     */
    protected void notifyAllObservers(IThrowingConsumer<INodeObserverServiceClient> proxyConsumer) {
    	for (INodeObserverServiceClient observerServiceClient : observers) {
        	try {
                proxyConsumer.accept(observerServiceClient);
            } catch (Exception e) {
        	    logger.warn("Could not notify observer with endpoint {}.", observerServiceClient.getServiceEndpoint(), e);
            }
        }
    }

    /**
     * Notifies an observer with a new NodeInfoDTO object.
     * @param observer - observer client.
     */
    protected void notifyObserverNodeInfo(INodeObserverServiceClient observer) {
    	try {
            NodeInfoDTO info = getInfo();
            logger.debug(
                "Notify observer on {} with new NodeInfo: {}.",
                observer.getServiceEndpoint(),
                info
            );
            observer.notifyNodeInfo(nodeConfiguration.getId(), info);
        } catch (ClientCommunicationException e) {
            logger.error(
                "Error while notify observer on {} with new NodeInfo.",
                observer.getServiceEndpoint(),
                e
            );
        }
    }

    public T fetchFinishedElement(String eId) throws Exception {
    	synchronized (elementLock) {
    		logger.debug(getLogContext(eId), "Fetch finished {}.", getElementName());
    		if(!finishedElements.contains(eId)) {
    			String msg = String.format("%s is not known as finished %s by this node.", getElementName(), getElementName());
    			logger.error(getLogContext(eId), msg);
				throwException(eId, msg);
			}
		}

		try {
			T element = elementCache.fetch(eId);
			synchronized (elementLock) {
				finishedElements.remove(eId);
			}
			elementCache.remove(eId);
			return element;
		} catch (IOException e) {
			logger.error(getLogContext(eId), "Error while fetching finished {} from cache.", getElementName(), e);
			throw e;
		} catch (UnknownCacheObjectException e) {
			logger.error(getLogContext(eId), "Error while fetching finished {} from cache.", getElementName(), e);
			throwException(eId, e.getMessage());
		}
		return null;
	}

    /**
     * Helper method that converts PeriodUnit to TimeUnit.
     *
     * @param periodUnit - instance to be converted.
     * @return converted instance of TimeUnit.
     */
    protected TimeUnit convertPeriodToTimeUnit(PeriodUnit periodUnit) {
        switch (periodUnit) {
            case HOURS: return TimeUnit.HOURS;
            case MINUTES: return TimeUnit.MINUTES;
            case SECONDS: // seconds are default
            default: return TimeUnit.SECONDS;
        }
    }

    protected NodeConfiguration getNodeConfiguration() {
    	return nodeConfiguration;
	}

	void addSharedResource(ResourceDTO sharedResource) {
    	logger.info("Add SharedResource {} ({} bytes) to the cache.", sharedResource.getId(), sharedResource.data.array().length);
    	sharedResources.cache(sharedResource.getId(), sharedResource);
	}

	void removeSharedResources(List<String> rIds) {
    	rIds.forEach(sharedResources::remove);
	}

	public ResourceDTO getSharedResource(String rId) throws IOException, UnknownCacheObjectException {
		return sharedResources.fetch(rId);
	}

	public String getStoreRoutineId() { return storeRoutineId; }

	public void setStoreRoutineId(String storeRoutineId) {
    	this.storeRoutineId = storeRoutineId;
		executorServices.forEach(es -> es.setStoreRoutineId(storeRoutineId));
	}

	public QueueInfoDTO getQueueInfo(String qId) throws QueueNotExistsException {
    	return getQueuePriorityWrapper().getQueue(qId).toQueueInfoDTO();
	}

	public void createQueue(String qId) {
    	// if queue doesn't already exist
		if (!getQueuePriorityWrapper().containsQueue(qId)) {
			// create queue, release queue
			Queue queue = createQueueInstance(qId);
			queue.release();

			// register queue
			getQueuePriorityWrapper().addQueue(queue);
		}
	}

	public void pauseQueue(String qId) throws QueueNotExistsException {
    	getQueuePriorityWrapper().getQueue(qId).pause();
	}

	public void deleteQueue(String qId) throws QueueNotExistsException {
    	getQueuePriorityWrapper().deleteQueue(qId);
	}

	public void releaseQueue(String qId) throws QueueNotExistsException {
    	getQueuePriorityWrapper().getQueue(qId).release();
	}

	public void queueElements(String qId, List<T> elements) throws QueueNotExistsException {
    	Queue<T> queue = getQueuePriorityWrapper().getQueue(qId);
    	try {
    		for (T element : elements) {
    			setState(element, ExecutionState.SCHEDULED);
    			queue.queue(element);
    			logger.info(getLogContext(element), "Queued {} successful.", getElementName());
			}

			// Notify all observers
			List<String> eIds = getElementIds(elements);
    		notifyObservers(getNodeConfiguration().getId(), eIds);

		} catch (InterruptedException e) {
    		logger.error("Interrupted while queueing {}s.", getElementName());
    		Thread.currentThread().interrupt();
		}
	}

	protected void abortElement(String eId, T element, ExecutionState currentState) {
    	if (runningElements.contains(eId)) {
    		logger.info(getLogContext(element), "Try to abort {}.", getElementName());
    		abortRunningElement(eId);
    		logger.info(getLogContext(element), "{} aborted.", getElementName());
		} else {
    		// Remove task from queue
			logger.info(getLogContext(element), "Remove {} from queue.", getElementName());
			removeElementFromQueues(eId);
		}
		setState(element, ExecutionState.FAILED);
    	elementCache.cache(eId, element);
    	notifyStateChanged(eId, currentState, ExecutionState.FAILED);
	}

	protected void abortRunningElement(String eId) {
		logger.debug(getLogContext(eId), "{0} is running. Try to abort.", getElementName());
		for (ExecutorService executor: getExecutorServices()) {
			if (executor.getRunningElement() != null && executor.getRunningElement().equalsIgnoreCase(eId)) {
				executor.cancelRunningElement();
				break;
			}
		}
		logger.info("Running {} aborted.", getElementName());
	}

    /**
     * Move a list of elements to another node and notifies observers dependent on isNotificationRequired flag.
     *
     * @param qId - id of queue (job).
     * @param elementIds - elements (Id list) to move.
     * @param targetNodeEndpoint - service endpoint of target node.
     * @param isNotificationRequired - true: notify registered observers.
     */
	private void moveElements(
			String qId,
			List<String> elementIds,
			ServiceEndpointDTO targetNodeEndpoint,
			boolean isNotificationRequired
	) throws ClientCreationException, QueueNotExistsException {
    	logger.debug("Move {}s from queue with id {}.", getElementName(), qId);
    	Queue queue = getQueuePriorityWrapper().getQueue(qId);

		List<T> elementsToMove = new LinkedList<>();
		for (String eId: elementIds) {
			elementsToMove.add((T)queue.remove(eId));
		}
		try {
			Future<Void> moveElements = queueElements(qId, elementsToMove, targetNodeEndpoint);
			logger.info(
			        "Moving {}s to node \"{}\" done with state \"{}\".",
                    getElementName(),
                    targetNodeEndpoint,
                    moveElements.get()
            );
		} catch (ClientCommunicationException | InterruptedException | ExecutionException | IllegalAccessException e) {
		    logger.error("Error while moving {} to target node.", getElementName(), e);
        }

        // Notify observers
        if (isNotificationRequired) {
            logger.debug("Notify all observer.");
            notifyAllObservers(this::notifyObserverNodeInfo);
        }
	}

	public void moveElements(String qId, List<String> elementIds, ServiceEndpointDTO targetNodeEndpoint)
		throws ClientCreationException, QueueNotExistsException {
    	moveElements(qId, elementIds, targetNodeEndpoint, true);
	}

	public void moveAllElements(ServiceEndpointDTO targetNodeEndpoint) throws ClientCreationException, QueueNotExistsException {
    	logger.debug("Try to move all {}s to target node: \"{}\".", getElementName());

    	for (Queue queue: getQueues()) {
    	    moveElements(queue.getQueueId(), queue.getQueuedElements(), targetNodeEndpoint, false);
        }

        // Notify observers
        logger.debug("Notify all observers.");
    	notifyAllObservers(this::notifyObserverNodeInfo);
	}

	public List<String> getQueuedElements(String qId) throws QueueNotExistsException {
		return getQueuePriorityWrapper().getQueue(qId).getQueuedElements();
	}

	@Override
	public void notifyStateChanged(String eId, ExecutionState oldState, ExecutionState newState) {
		logger.debug(getLogContext(eId), "Notify state changed of {} from state {} to {}.", getElementName(), oldState, newState);
		synchronized (elementLock) {
			switch (oldState) {
				case RUN:
					runningElements.remove(eId);
					break;
				case SCHEDULED:
				case SUCCESS:
				case FAILED:
				default:
					break;
			}
			switch (newState) {
				case RUN:
					runningElements.add(eId);
					break;
				case SUCCESS:
				case FAILED:
					finishedElements.add(eId);
					finishedExecutionOfElement(eId);
					break;
				case SCHEDULED:
				default:
					break;
			}

			// Notify observers
			List<String> elementList = Collections.singletonList(eId);
			logger.debug("Notify all observers.");
			notifyAllObservers(observerClient -> observerClient.notifyElementsNewState(
					getNodeConfiguration().getId(),
					elementList,
					newState
			));
		}
	}

	protected abstract QueuePriorityWrapper getQueuePriorityWrapper();
	protected abstract List<? extends ExecutorService> getExecutorServices();
	protected abstract Queue createQueueInstance(String qId);
	public abstract List<String> getQueueIds();
	protected abstract void throwException(String eId, String message) throws Exception;
	protected abstract Set<ITuple<ContextIndicator, ?>> getLogContext(T element);
	protected abstract Set<ITuple<ContextIndicator, ?>> getLogContext(String elementId);
	protected abstract void removeElementFromQueues(String eId);
	protected abstract void setState(T element, ExecutionState state);
	protected abstract List<String> getElementIds(List<T> elements);
	protected abstract List<? extends Queue> getQueues() throws QueueNotExistsException;
	protected abstract Future<Void> queueElements(String qId, List<T> elementsToQueue, ServiceEndpointDTO targetNodeEndpoint) throws ClientCreationException, ClientCommunicationException, IllegalAccessException;
	protected abstract void notifyObservers(String nId, List<String> eIds);
	protected abstract void finishedExecutionOfElement(String eId);
	protected abstract String getElementName();
}
