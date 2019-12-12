package at.enfilo.def.library;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.config.server.core.DEFRootConfiguration;
import at.enfilo.def.library.util.store.driver.fs.FSStoreDriver;
import at.enfilo.def.transfer.dto.LibraryType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;

public class LibraryConfiguration extends DEFRootConfiguration {

	public static final String PROPERTY_LIBRARY_TYPE = "library-type";
	public static final String PROPERTY_LIBRARY_ENDPOINT = "library-endpoint";
	public static final String PROPERTY_STORE_DRIVER = "store-driver";
	public static final String PROPERTY_STORE_ENDPOINT_URL = "store-endpoint-url";

	private LibraryType libraryType = LibraryType.MASTER;
	private ServiceEndpointDTO libraryEndpoint = new ServiceEndpointDTO("localhost", 40042, Protocol.THRIFT_TCP);
	private String storeDriver = FSStoreDriver.class.getName();
    private String storeEndpointUrl = "file:/tmp/def/routine-binaries";

	/**
	 * Returns the library-type-specific section of the configuration file.
	 *
	 * @return library-type-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_LIBRARY_TYPE)
	public LibraryType getLibraryType() {
		return libraryType;
	}

	/**
	 * Sets the library-type-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_LIBRARY_TYPE)
	public void setLibraryType(LibraryType libraryType) {
		this.libraryType = libraryType;
	}

	/**
	 * Returns the data-endpoint-specific section of the configuration file.
	 *
	 * @return data-endpoint-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
	public ServiceEndpointDTO getLibraryEndpoint() {
		return libraryEndpoint;
	}

	/**
	 * Sets the data-endpoint-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
	public void setLibraryEndpoint(ServiceEndpointDTO dataEndpoint) {
		this.libraryEndpoint = dataEndpoint;
	}

	/**
	 * Returns the store-driver-specific section of the configuration file.
	 *
	 * @return store-driver-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_STORE_DRIVER)
	public String getStoreDriver() {
		return storeDriver;
	}

	/**
	 * Sets the store-driver-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_STORE_DRIVER)
	public void setStoreDriver(String storeDriver) {
		this.storeDriver = storeDriver;
	}

	/**
	 * Returns the store-endpoint-url-specific section of the configuration file.
	 *
	 * @return store-endpoint-url-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_STORE_ENDPOINT_URL)
	public URL getStoreEndpointUrl()
    throws MalformedURLException {
        return new URL(storeEndpointUrl);
	}

	/**
	 * Sets the store-endpoint-url-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_STORE_ENDPOINT_URL)
	public void setStoreEndpointUrl(String storeEndpointUrl) {
		this.storeEndpointUrl = storeEndpointUrl;
	}

    /**
     * Sets the store-endpoint-url-specific section of the configuration file.
     */
    @JsonProperty(PROPERTY_STORE_ENDPOINT_URL)
    public void setStoreEndpointUrl(URL storeEndpointUrl) {
        this.storeEndpointUrl = storeEndpointUrl.toString();
    }

	@JsonIgnore
	public static LibraryConfiguration getDefault() {
		return new LibraryConfiguration();
	}
}
