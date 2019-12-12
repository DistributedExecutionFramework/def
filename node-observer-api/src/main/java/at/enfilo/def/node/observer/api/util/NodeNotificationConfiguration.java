package at.enfilo.def.node.observer.api.util;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.transfer.dto.PeriodUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeNotificationConfiguration {

	private static final String PROPERTY_PERIODICALLY = "periodically";
	private static final String PROPERTY_PERIOD_DURATION = "period-duration";
	private static final String PROPERTY_PERIOD_UNIT = "period-unit";
	private static final String PROPERTY_ENDPOINT = "endpoint";

	private boolean periodically = false;
	private int periodDuration = 10;
	private PeriodUnit periodUnit = PeriodUnit.SECONDS;
	private ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

	@JsonProperty(PROPERTY_PERIODICALLY)
	public boolean isPeriodically() {
		return periodically;
	}

	@JsonProperty(PROPERTY_PERIODICALLY)
	public void setPeriodically(boolean periodically) {
		this.periodically = periodically;
	}

	@JsonProperty(PROPERTY_PERIOD_DURATION)
	public int getPeriodDuration() {
		return periodDuration;
	}

	@JsonProperty(PROPERTY_PERIOD_DURATION)
	public void setPeriodDuration(int periodDuration) {
		this.periodDuration = periodDuration;
	}

	@JsonProperty(PROPERTY_PERIOD_UNIT)
	public PeriodUnit getPeriodUnit() {
		return periodUnit;
	}

	@JsonProperty(PROPERTY_PERIOD_UNIT)
	public void setPeriodUnit(PeriodUnit periodUnit) {
		this.periodUnit = periodUnit;
	}

	@JsonProperty(PROPERTY_ENDPOINT)
	public ServiceEndpointDTO getEndpoint() {
		return endpoint;
	}

	@JsonProperty(PROPERTY_ENDPOINT)
	public void setEndpoint(ServiceEndpointDTO endpoint) {
		this.endpoint = endpoint;
	}

	@JsonIgnore
	public static NodeNotificationConfiguration getDefault() {
		return new NodeNotificationConfiguration();
	}
}
