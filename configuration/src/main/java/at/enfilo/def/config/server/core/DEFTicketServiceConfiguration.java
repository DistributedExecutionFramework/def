package at.enfilo.def.config.server.core;

import at.enfilo.def.config.server.api.IConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DEFTicketServiceConfiguration implements IConfiguration {
	public static final String PROPERTY_ENABLED = "enabled";
	public static final String PROPERTY_THREADS = "threads";

	private int threads = 4;
	private boolean enabled = true;

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
}
