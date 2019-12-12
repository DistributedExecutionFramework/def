package at.enfilo.def.cluster.util.configuration;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.config.server.api.IConfiguration;
import at.enfilo.def.node.observer.api.util.NodeNotificationConfiguration;
import at.enfilo.def.transfer.dto.PeriodUnit;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodesConfiguration implements IConfiguration {
	
	private static final String PROPERTY_SIZE = "size";
	private static final String PROPERTY_IMAGE = "image";
	private static final String PROPERTY_SERVICE_ENDPOINT = "service-endpoint";
	private static final String PROPERTY_NOTIFICATION_FROM_NODE = "notification-from-node";
	private static final String PROPERTY_TIMEOUT = "timeout";
	private static final String PROPERTY_TIMEOUT_UNIT = "timeout-unit";
	private static final String PROPERTY_STORE_ROUTINE = "store-routine";

	private String size = "t1.micro";
	private String image = "image-id";
	private ServiceEndpointDTO nodeServiceEndpoint = new ServiceEndpointDTO(
		"localhost",
		9998,
		Protocol.THRIFT_TCP
	);
	private NodeNotificationConfiguration notificationFromNode = new NodeNotificationConfiguration();
	private long timeout = 30;
	private PeriodUnit timeoutUnit = PeriodUnit.SECONDS;
	private String storeRoutineId = "bae3723e-51b2-391f-9abd-b2ac750c1932";

	@JsonProperty(PROPERTY_SIZE)
	public String getSize() {
		return size;
	}

	@JsonProperty(PROPERTY_SIZE)
	public void setSize(String size) {
		this.size = size;
	}

	@JsonProperty(PROPERTY_SERVICE_ENDPOINT)
	public ServiceEndpointDTO getNodeServiceEndpoint() {
		return nodeServiceEndpoint;
	}

	@JsonProperty(PROPERTY_SERVICE_ENDPOINT)
	public void setNodeServiceEndpoint(ServiceEndpointDTO nodeServiceEndpoint) {
		this.nodeServiceEndpoint = nodeServiceEndpoint;
	}

	@JsonProperty(PROPERTY_NOTIFICATION_FROM_NODE)
	public NodeNotificationConfiguration getNotificationFromNode() {
		return notificationFromNode;
	}

	@JsonProperty(PROPERTY_NOTIFICATION_FROM_NODE)
	public void setNotificationFromNode(NodeNotificationConfiguration notificationFromNode) {
		this.notificationFromNode = notificationFromNode;
	}

	@JsonProperty(PROPERTY_TIMEOUT)
	public long getTimeout() {
		return timeout;
	}

	@JsonProperty(PROPERTY_TIMEOUT)
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@JsonProperty(PROPERTY_TIMEOUT_UNIT)
	public PeriodUnit getTimeoutUnit() {
		return timeoutUnit;
	}

	@JsonProperty(PROPERTY_TIMEOUT_UNIT)
	public void setTimeoutUnit(PeriodUnit timeoutUnit) {
		this.timeoutUnit = timeoutUnit;
	}

	@JsonProperty(PROPERTY_IMAGE)
	public String getImage() {
		return image;
	}

	@JsonProperty(PROPERTY_IMAGE)
	public void setImage(String image) {
		this.image = image;
	}

	@JsonProperty(PROPERTY_STORE_ROUTINE)
	public String getStoreRoutineId() { return storeRoutineId; }

	@JsonProperty(PROPERTY_STORE_ROUTINE)
	public void setStoreRoutineId(String storeRoutineId) {
		this.storeRoutineId = storeRoutineId;
	}
}
