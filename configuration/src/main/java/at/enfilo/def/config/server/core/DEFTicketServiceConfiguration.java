package at.enfilo.def.config.server.core;

import at.enfilo.def.config.server.api.IConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DEFTicketServiceConfiguration implements IConfiguration {
	public static final String PROPERTY_ENABLED = "enabled";
	public static final String PROPERTY_THREADS = "threads";
	public static final String PROPERTY_MEMORY_THRESHOLD = "memory-threshold";
	public static final String PROPERTY_MEMORY_CHECK_INTERVAL = "memory-check-interval";

	private boolean enabled = true;
	private int threads = 8;
	private double memoryThreshold = 0.9; // 90%
	private long memoryCheckInterval = 1000; // 1s

	public static DEFTicketServiceConfiguration getDefault() {
		return new DEFTicketServiceConfiguration();
	}

	@JsonProperty(PROPERTY_THREADS)
	public int getThreads() {
		return threads;
	}

	@JsonProperty(PROPERTY_THREADS)
	public void setThreads(int threads) {
		this.threads = threads;
	}

	@JsonProperty(PROPERTY_ENABLED)
	public boolean isEnabled() {
		return this.enabled;
	}

	@JsonProperty(PROPERTY_ENABLED)
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@JsonProperty(PROPERTY_MEMORY_THRESHOLD)
	public double getMemoryThreshold() {
		return memoryThreshold;
	}

	@JsonProperty(PROPERTY_MEMORY_THRESHOLD)
	public void setMemoryThreshold(double memoryThreshold) {
		this.memoryThreshold = memoryThreshold;
	}

	@JsonProperty(PROPERTY_MEMORY_CHECK_INTERVAL)
	public long getMemoryCheckInterval() {
		return memoryCheckInterval;
	}

	@JsonProperty(PROPERTY_MEMORY_CHECK_INTERVAL)
	public void setMemoryCheckInterval(long memoryCheckInterval) {
		this.memoryCheckInterval = memoryCheckInterval;
	}
}
