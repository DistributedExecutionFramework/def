package at.enfilo.def.persistence.util;

import at.enfilo.def.config.server.api.IConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DatabaseConfiguration implements IConfiguration {
	private static final String PROPERTY_DRIVER = "driver";
	private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

	private static final String PROPERTY_DIALECT = "dialect";
	private static final String DEFAULT_DIALECT = "org.hibernate.dialect.MySQLDialect";

	private static final String PROPERTY_URL = "url";
	private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/def";

	private static final String PROPERTY_USER = "user";
	private static final String DEFAULT_USER = "def";

	private static final String PROPERTY_PASSWORD = "password";
	private static final String DEFAULT_PASSWORD = "def";

	@NotNull @Valid private String driver = DEFAULT_DRIVER;
	@NotNull @Valid private String dialect = DEFAULT_DIALECT;
	@NotNull @Valid private String url = DEFAULT_URL;
	@NotNull @Valid private String user = DEFAULT_USER;
	@NotNull @Valid private String password = DEFAULT_PASSWORD;

	@JsonProperty(PROPERTY_DRIVER)
	public String getDriver() {
		return driver;
	}

	@JsonProperty(PROPERTY_DRIVER)
	public void setDriver(String driver) {
		this.driver = driver;
	}

	@JsonProperty(PROPERTY_DIALECT)
	public String getDialect() {
		return dialect;
	}

	@JsonProperty(PROPERTY_DIALECT)
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	@JsonProperty(PROPERTY_URL)
	public String getUrl() {
		return url;
	}

	@JsonProperty(PROPERTY_URL)
	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty(PROPERTY_USER)
	public String getUser() {
		return user;
	}

	@JsonProperty(PROPERTY_USER)
	public void setUser(String user) {
		this.user = user;
	}

	@JsonProperty(PROPERTY_PASSWORD)
	public String getPassword() {
		return password;
	}

	@JsonProperty(PROPERTY_PASSWORD)
	public void setPassword(String password) {
		this.password = password;
	}
}
