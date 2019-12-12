package at.enfilo.def.dto.cache;

import at.enfilo.def.common.api.ITimeoutMap;
import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.config.util.ConfigReader;
import at.enfilo.def.dto.cache.impl.DTODiskPersistenceDriver;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import org.apache.thrift.TBase;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class DTOCache<T extends TBase> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(DTOCache.class);
	private static final String CONFIG_FILE = "dto-cache.yml";
	private static final Map<String, DTOCache> INSTANCES = new HashMap<>();

	/**
	 * Singleton pattern.
	 * @return an instance of TaskCache for the given context.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends TBase> DTOCache<T> getInstance(String context, Class<T> cls) {
		if (!INSTANCES.containsKey(context)) {
			INSTANCES.put(context, new DTOCache<>(context, cls));
		}
		return INSTANCES.get(context);
	}

	private final Class<T> cls;
	private final ITimeoutMap<String, T> cache;
	private IDTOPersistenceDriver<T> driver;

	@SuppressWarnings("unchecked")
	private DTOCache(String context, Class<T> cls) {
		this.cls = cls;
		DTOCacheConfiguration configuration;
		try {
			configuration = ConfigReader.readConfiguration(CONFIG_FILE, DTOCache.class, DTOCacheConfiguration.class);
		} catch (IOException e) {
			LOGGER.warn("Could not read configuration {}. Create default configuration.", CONFIG_FILE, e);
			configuration = new DTOCacheConfiguration();
		}
		cache = new TimeoutMap<>(
				configuration.getExpirationTime(),
				TimeUnit.valueOf(configuration.getExpirationTimeUnit().name()),
				configuration.getExpirationTime(),
				TimeUnit.valueOf(configuration.getExpirationTimeUnit().name()),
				this::notifyOnTimeout
		);
		try {
			driver = (IDTOPersistenceDriver) Class.forName(configuration.getDriver()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			LOGGER.warn("Error while load TaskPersistenceDriver {}. Init default driver.", configuration.getDriver(), e);
			driver = new DTODiskPersistenceDriver<>();
		}
		driver.init(configuration.getBaseUrl(), context);

		// Register a shutdown hook to clean up persistence.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// clean up all on shutdown.
			driver.removeAll();
		}));
	}


	/**
	 * Cache the given DTO.
	 * @param id - id of DTO
	 * @param t - DTO to cache
	 */
	public void cache(String id, T t) {
		LOGGER.debug("Cache object with id {}.", id);
		cache.put(id, t);
	}

	/**
	 * Fetch task from cache. If task is not available in memory it will be loaded from persistence.
	 * @param id - task to fetch.
	 * @return task instance.
	 * @throws IOException - if read from persistence failed
	 */
	public T fetch(String id) throws IOException, UnknownCacheObjectException {
		LOGGER.debug("Fetch object with id {}.", id);
		if (cache.touch(id)) {
			LOGGER.debug("Touched object with id {}.", id);
			// DTO found in memory
		} else {
			// Fetch task from disk and store it in memory.
			try {
				LOGGER.debug("Fetch task from disk and store it in memory.");
				if (!driver.exists(id)) {
					String msg = String.format("Cache object (id=%s) not found in persistence.", id);
					LOGGER.error(msg);
					throw new UnknownCacheObjectException(msg);
				}
				T t = driver.read(id, cls.newInstance());
				cache.put(id, t);
			} catch (IllegalAccessException | InstantiationException e) {
				LOGGER.error("Error while create a new cached DTO instance.", e);
			}
		}
		return cache.get(id);
	}

	/**
	 * Returns true if a entry is available.
	 * @param id - id of cached object
	 * @return true if DTO exists in cache.
	 */
	public boolean exists(String id) {
		if (cache.containsKey(id)) {
			return true;
		}
		return driver.exists(id);
	}

	/**
	 * Remove given DTO id from cache (including persistence).
	 * @param id - DTO id to remove.
	 */
	public void remove(String id) {
		LOGGER.debug("Remove object with id {} from cache.", id);
		cache.remove(id);
		driver.remove(id);
	}

	/**
	 * Remove a batch of DTOs from cache (including persistence).
	 * @param ids - DTOs to remove
	 */
	public void remove(Collection<String> ids) {
		LOGGER.debug("Remove {} objects with from cache.", ids.size());
		ids.forEach(cache::remove);
		driver.remove(ids);
	}

	/**
	 * Write/persist DTO on timeout.
	 * @param id - if of DTO
	 * @param t - DTO
	 */
	private void notifyOnTimeout(String id, T t) {
		LOGGER.debug("Try to persist DTO with id {}. (Remove from memory)", id);
		try {
			driver.write(id, t);
		} catch (IOException e) {
			LOGGER.error("Could not persist DTO with id {}.", id, e);
		}
	}
}
