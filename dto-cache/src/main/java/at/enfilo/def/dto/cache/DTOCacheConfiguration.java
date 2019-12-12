package at.enfilo.def.dto.cache;

import at.enfilo.def.config.server.api.IConfiguration;
import at.enfilo.def.dto.cache.impl.DTODiskPersistenceDriver;
import at.enfilo.def.transfer.dto.PeriodUnit;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DTOCacheConfiguration implements IConfiguration {
	private static final String PROPERTY_BASE_URL = "base-url";
	private static final String DEFAULT_BASE_URL = "file:/tmp/def/dto-cache";

	private static final String PROPERTY_PERSISTENCE_DRIVER = "driver";
	private static final String DEFAULT_PERSISTENCE_DRIVER = DTODiskPersistenceDriver.class.getCanonicalName();


	private static final String PROPERTY_EXPIRATION_TIME = "expiration-time";
	private static final int DEFAULT_EXPIRATION_TIME = 10;

	private static final String PROPERTY_EXPIRATION_TIME_UNIT = "expiration-time-unit";
	private static final PeriodUnit DEFAULT_EXPIRATION_TIME_UNIT = PeriodUnit.SECONDS;

	private String baseUrl = DEFAULT_BASE_URL;
	private String driver = DEFAULT_PERSISTENCE_DRIVER;
	private int expirationTime = DEFAULT_EXPIRATION_TIME;
	private PeriodUnit expirationTimeUnit = DEFAULT_EXPIRATION_TIME_UNIT;

	@JsonProperty(PROPERTY_BASE_URL)
	public String getBaseUrl() {
		return baseUrl;
	}

	@JsonProperty(PROPERTY_BASE_URL)
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@JsonProperty(PROPERTY_EXPIRATION_TIME)
	public int getExpirationTime() {
		return expirationTime;
	}

	@JsonProperty(PROPERTY_EXPIRATION_TIME)
	public void setExpirationTime(int expirationTime) {
		this.expirationTime = expirationTime;
	}

	@JsonProperty(PROPERTY_EXPIRATION_TIME_UNIT)
	public PeriodUnit getExpirationTimeUnit() {
		return expirationTimeUnit;
	}

	@JsonProperty(PROPERTY_EXPIRATION_TIME_UNIT)
	public void setExpirationTimeUnit(PeriodUnit expirationTimeUnit) {
		this.expirationTimeUnit = expirationTimeUnit;
	}

	@JsonProperty(PROPERTY_PERSISTENCE_DRIVER)
	public String getDriver() {
		return driver;
	}

	@JsonProperty(PROPERTY_PERSISTENCE_DRIVER)
	public void setDriver(String driver) {
		this.driver = driver;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DTOCacheConfiguration that = (DTOCacheConfiguration) o;

		if (expirationTime != that.expirationTime) return false;
		if (baseUrl != null ? !baseUrl.equals(that.baseUrl) : that.baseUrl != null) return false;
		if (driver != null ? !driver.equals(that.driver) : that.driver != null) return false;
		return expirationTimeUnit == that.expirationTimeUnit;
	}

	@Override
	public int hashCode() {
		int result = baseUrl != null ? baseUrl.hashCode() : 0;
		result = 31 * result + (driver != null ? driver.hashCode() : 0);
		result = 31 * result + expirationTime;
		result = 31 * result + (expirationTimeUnit != null ? expirationTimeUnit.hashCode() : 0);
		return result;
	}
}
