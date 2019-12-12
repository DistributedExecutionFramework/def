package at.enfilo.def.scheduler.general.util;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.config.server.core.DEFRootConfiguration;
import at.enfilo.def.node.observer.api.util.NodeNotificationConfiguration;
import at.enfilo.def.scheduler.clientroutineworker.strategy.ProgramSchedulingStrategy;
import at.enfilo.def.scheduler.clientroutineworker.strategy.RoundRobinProgramSchedulingStrategy;
import at.enfilo.def.scheduler.reducer.strategy.DefaultReduceSchedulingStrategy;
import at.enfilo.def.scheduler.reducer.strategy.ReduceSchedulingStrategy;
import at.enfilo.def.scheduler.worker.strategy.RoundRobinTaskSchedulingStrategy;
import at.enfilo.def.scheduler.worker.strategy.TaskSchedulingStrategy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SchedulerConfiguration extends DEFRootConfiguration {

	private static final String PROPERTY_TASK_STRATEGY = "task-scheduling-strategy";
	private static final String PROPERTY_REDUCE_STRATEGY = "reduce-scheduling-strategy";
	private static final String PROPERTY_PROGRAM_STRATEGY = "program-scheduling-strategy";
	private static final String PROPERTY_NOTIFICATION_FROM_NODE = "notification-from-node";
	private static final String PROPERTY_CLUSTER_ENDPOINT = "cluster-endpoint";
	public static final String PROPERTY_LIBRARY_ENDPOINT = "library-endpoint";

	private static final String CLUSTER_DEFAULT_HOST = "127.0.0.1";
	private static final int CLUSTER_DEFAULT_PORT = 40012;
	private static final Protocol CLUSTER_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

    public static final String LIBRARY_DEFAULT_HOST = "127.0.0.1";
    public static final int LIBRARY_DEFAULT_PORT = 40042;
    public static final Protocol LIBRARY_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

	private Class<? extends TaskSchedulingStrategy> taskSchedulingStrategy = RoundRobinTaskSchedulingStrategy.class;
	private Class<? extends ReduceSchedulingStrategy> reduceSchedulingStrategy = DefaultReduceSchedulingStrategy.class;
	private Class<? extends ProgramSchedulingStrategy> programSchedulingStrategy = RoundRobinProgramSchedulingStrategy.class;
	private NodeNotificationConfiguration notificationFromNode = new NodeNotificationConfiguration();
	private ServiceEndpointDTO clusterEndpoint = new ServiceEndpointDTO(
		CLUSTER_DEFAULT_HOST,
		CLUSTER_DEFAULT_PORT,
		CLUSTER_DEFAULT_PROTOCOL
	);
	private ServiceEndpointDTO libraryEndpoint = new ServiceEndpointDTO(
			LIBRARY_DEFAULT_HOST,
			LIBRARY_DEFAULT_PORT,
			LIBRARY_DEFAULT_PROTOCOL
	);

	@JsonProperty(PROPERTY_TASK_STRATEGY)
	public String getTaskSchedulingStrategy() {
		return taskSchedulingStrategy.getCanonicalName();
	}

	@JsonProperty(PROPERTY_TASK_STRATEGY)
	public void setTaskSchedulingStrategy(String schedulingStrategy)
	throws ClassNotFoundException, IllegalArgumentException {
		Class<?> cls = Class.forName(schedulingStrategy);

		if (TaskSchedulingStrategy.class.isAssignableFrom(cls)) {
			this.taskSchedulingStrategy = cls.asSubclass(TaskSchedulingStrategy.class);
		}
		else throw new IllegalArgumentException("Not an strategy class.");
	}

	@JsonProperty(PROPERTY_REDUCE_STRATEGY)
	public String getReduceSchedulingStrategy() {
		return reduceSchedulingStrategy.getCanonicalName();
	}

	@JsonProperty(PROPERTY_REDUCE_STRATEGY)
	public void setReduceSchedulingStrategy(String schedulingStrategy)
	throws ClassNotFoundException, IllegalArgumentException {
		Class<?> cls = Class.forName(schedulingStrategy);

		if (ReduceSchedulingStrategy.class.isAssignableFrom(cls)) {
			this.reduceSchedulingStrategy = cls.asSubclass(ReduceSchedulingStrategy.class);
		}
		else throw new IllegalArgumentException("Not a strategy class.");
	}

	@JsonProperty(PROPERTY_PROGRAM_STRATEGY)
	public String getProgramSchedulingStrategy() {
		return programSchedulingStrategy.getCanonicalName();
	}

	@JsonProperty(PROPERTY_PROGRAM_STRATEGY)
	public void setProgramSchedulingStrategy(String schedulingStrategy)
		throws ClassNotFoundException, IllegalArgumentException {
		Class<?> cls = Class.forName(schedulingStrategy);

		if (ProgramSchedulingStrategy.class.isAssignableFrom(cls)) {
			this.programSchedulingStrategy = cls.asSubclass(ProgramSchedulingStrategy.class);
		}
		else throw new IllegalArgumentException("Not a strategy class.");
	}

	@JsonProperty(PROPERTY_NOTIFICATION_FROM_NODE)
	public NodeNotificationConfiguration getNotificationFromNode() {
		return notificationFromNode;
	}

	@JsonProperty(PROPERTY_NOTIFICATION_FROM_NODE)
	public void setNotificationFromNode(NodeNotificationConfiguration notificationFromNode) {
		this.notificationFromNode = notificationFromNode;
	}

	@JsonProperty(PROPERTY_CLUSTER_ENDPOINT)
	public ServiceEndpointDTO getClusterEndpoint() {
		return clusterEndpoint;
	}

	@JsonProperty(PROPERTY_CLUSTER_ENDPOINT)
	public void setClusterEndpoint(ServiceEndpointDTO clusterEndpoint) {
		this.clusterEndpoint = clusterEndpoint;
	}

	@JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
	public ServiceEndpointDTO getLibraryEndpoint() {
		return libraryEndpoint;
	}

	@JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
	public void setLibraryEndpoint(ServiceEndpointDTO libraryEndpoint) {
		this.libraryEndpoint = libraryEndpoint;
	}

	@JsonIgnore
	public static SchedulerConfiguration getDefault() {
		return new SchedulerConfiguration();
	}
}
