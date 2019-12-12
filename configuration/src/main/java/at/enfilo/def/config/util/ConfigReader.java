package at.enfilo.def.config.util;

import at.enfilo.def.config.server.api.IConfiguration;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING;

/**
 * Created by mase on 09.09.2016.
 */
public class ConfigReader {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ConfigReader.class.getName());

    private ConfigReader() {
        // Hiding public constructor
    }

    /**
     * Reads configuration of type {@code T} from the config file distinguished  by given config file name and base class.
     *
     * @param configFileName name of the config file.
     * @param baseClass base class Classloader of which will be used to locate config file path.
     * @param configurationClass class that indicates required type of {@link IConfiguration} to be returned.
     * @param <T> type indicator for instance of {@link IConfiguration}.
     * @return instance of requested configuration.
     * @throws IOException
     */
    public static  <T extends IConfiguration> T readConfiguration(
        String configFileName, Class<?> baseClass, Class<T> configurationClass
    ) throws IOException {

        return readConfiguration(configFileName, baseClass, configurationClass, ConfigReader::getNewObjectMapper);
    }

    /**
     * Reads configuration of type {@code T} from the config file distinguished  by given config file name and base class.
     *
     * @param configFileName name of the config file.
     * @param baseClass base class Classloader of which will be used to locate config file path.
     * @param configurationClass class that indicates required type of {@link IConfiguration} to be returned.
     * @param objectMapperSupplier object mapper supplier.
     * @param <T> type indicator for instance of {@link IConfiguration}.
     * @return instance of requested configuration.
     * @throws IOException
     */
    public static  <T extends IConfiguration> T readConfiguration(
        String configFileName, Class<?> baseClass, Class<T> configurationClass, Supplier<ObjectMapper> objectMapperSupplier
    ) throws IOException {

        return readConfiguration(configFileName, baseClass, configurationClass, objectMapperSupplier.get());
    }

    /**
     * Reads configuration of type {@code T} from the config file distinguished  by given config file name and base class.
     *
     * @param configFileName name of the config file.
     * @param baseClass base class Classloader of which will be used to locate config file path.
     * @param configurationClass class that indicates required type of {@link IConfiguration} to be returned.
     * @param mapper instance of object mapper.
     * @param <T> type indicator for instance of {@link IConfiguration}.
     * @return instance of requested configuration.
     * @throws IOException
     */
    public static  <T extends IConfiguration> T readConfiguration(
        String configFileName, Class<?> baseClass, Class<T> configurationClass, ObjectMapper mapper
    ) throws IOException {

    	URL configFileUrl = fetchResourceURL(configFileName, baseClass);
    	LOGGER.info("Use configuration file: \"{}\"", configFileUrl);
        return mapper.readValue(configFileUrl, configurationClass);
    }

    /**
     * Fetches full resource URL by given file name and baseClass.
     *
     * @param fileName name of the file to be returned as a part of full resource URL.
     * @param baseClass base class Classloader of which will be used to distinguish start path.
     * @return full resource URL.
     * @throws FileNotFoundException
     */
    public static URL fetchResourceURL(String fileName, Class<?> baseClass) throws FileNotFoundException {
		LOGGER.debug("Try to find configuration file {}", fileName);
    	URL url = null;

		LOGGER.debug("Check for {}", Paths.get(fileName).toString());
		if (Files.exists(Paths.get(fileName))) {
			try {
				url = Paths.get(fileName).toUri().toURL();
			} catch (MalformedURLException e) {
				LOGGER.warn("Cannot fetch url for {}", Paths.get(fileName).toString(), e);
			}
		}
        if (url == null) {
			LOGGER.debug("Check for configuration file based on class {}", baseClass);
			url = baseClass.getClassLoader().getResource(fileName);
		}

        if (url == null) {
			LOGGER.error("Failed to find configuration file {}", fileName);
			throw new FileNotFoundException(fileName);
		}

		return url;
    }

    private static ObjectMapper getNewObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		// Uses Enum.toString() for serialization of an Enum
		objectMapper.enable(WRITE_ENUMS_USING_TO_STRING);
		// Uses Enum.toString() for deserialization of an Enum
		objectMapper.enable(READ_ENUMS_USING_TO_STRING);
		return objectMapper;
    }
}
