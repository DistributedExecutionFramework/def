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
import at.enfilo.def.logging.impl.ContextSetBuilder;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.util.ClusterRegistration;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class NodeServiceController {

	private static final long CLUSTER_REGISTRATION_DELAY = 10;
	private static final TimeUnit CLUSTER_REGISTRATION_DELAY_UNIT = TimeUnit.SECONDS;

	public static final String DTO_RESOURCE_CACHE_CONTEXT = "node-resources";

	private final NodeType nodeType;
    private final List<INodeObserverServiceClient> observers;
    private final NodeConfiguration nodeConfiguration;
    private final NodeObserverServiceClientFactory nodeObserverServiceClientFactory;
    private final IDEFLogger logger;
    private final DTOCache<ResourceDTO> sharedResources;

    private String cId; // cluster id.

    protected NodeServiceController(
		NodeType nodeType,
        List<INodeObserverServiceClient> observers,
        NodeConfiguration nodeConfiguration,
        NodeObserverServiceClientFactory nodeObserverServiceClientFactory,
        IDEFLogger logger
    ) {
    	this.nodeType = nodeType;
        this.observers = observers;
		this.nodeObserverServiceClientFactory = nodeObserverServiceClientFactory;
        this.nodeConfiguration = nodeConfiguration;
        this.logger = logger;
        this.sharedResources = DTOCache.getInstance(DTO_RESOURCE_CACHE_CONTEXT, ResourceDTO.class);

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
	 * Returns specific parameters for {@link NodeInfoDTO}. (see getInfo())
	 * @return
	 */
    protected abstract Map<String, String> getNodeInfoParameters();

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
                "Notify observer (\"{}\") with new WorkerInfo: \"{}\".",
                observer.getServiceEndpoint(),
                info
            );
            observer.notifyNodeInfo(nodeConfiguration.getId(), info);
        } catch (ClientCommunicationException e) {
            logger.error(
                "Error while notify observer (\"{}\") with new WorkerInfo.",
                observer.getServiceEndpoint(),
                e
            );
        }
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

    /**
     * Helper method that provides logging context for a given task.
     *
     * @param task - task to be used for logging context.
     * @return logging context index.
     */
    protected Set<ITuple<ContextIndicator, ?>> getLogContext(TaskDTO task) {
        return new ContextSetBuilder()
            .add(ContextIndicator.PROGRAM_CONTEXT, task.getProgramId())
            .add(ContextIndicator.JOB_CONTEXT, task.getJobId())
            .add(ContextIndicator.TASK_CONTEXT, task.getId())
            .build();
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
}
