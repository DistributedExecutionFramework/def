package at.enfilo.def.worker.util;

import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.PeriodUnit;
import com.fasterxml.jackson.annotation.JsonProperty;


public class WorkerConfiguration extends NodeConfiguration {
	public static final String PROPERTY_STORE_ROUTINE_ID = "store-routine";
	public static final String DEFAULT_STORE_ROUTINE_ID = "2a2fa500-fb5b-340b-8b80-2c4fae4921b3";

	public static final String PROPERTY_QUEUE_LIFE_TIME = "queue-life-time";
	public static final int DEFAULT_QUEUE_LIFE_TIME = 48;

	public static final String PROPERTY_QUEUE_LIFE_TIME_UNIT = "queue-life-time-unit";
	public static final PeriodUnit DEFAULT_QUEUE_LIFE_TIME_UNIT = PeriodUnit.HOURS;

	private String storeRoutineId = DEFAULT_STORE_ROUTINE_ID;
	private int queueLifeTime = DEFAULT_QUEUE_LIFE_TIME;
	private PeriodUnit queueLifeTimeUnit = PeriodUnit.HOURS;

	@JsonProperty(PROPERTY_STORE_ROUTINE_ID)
	public String getStoreRoutineId() {
		return storeRoutineId;
	}

	@JsonProperty(PROPERTY_STORE_ROUTINE_ID)
	public void setStoreRoutineId(String storeRoutineId) {
		this.storeRoutineId = storeRoutineId;
	}

	@JsonProperty(PROPERTY_QUEUE_LIFE_TIME)
	public int getQueueLifeTime() {
		return queueLifeTime;
	}

	@JsonProperty(PROPERTY_QUEUE_LIFE_TIME)
	public void setQueueLifeTime(int queueLifeTime) {
		this.queueLifeTime = queueLifeTime;
	}

	@JsonProperty(PROPERTY_QUEUE_LIFE_TIME_UNIT)
	public PeriodUnit getQueueLifeTimeUnit() {
		return queueLifeTimeUnit;
	}

	@JsonProperty(PROPERTY_QUEUE_LIFE_TIME_UNIT)
	public void setQueueLifeTimeUnit(PeriodUnit queueLifeTimeUnit) {
		this.queueLifeTimeUnit = queueLifeTimeUnit;
	}
}
