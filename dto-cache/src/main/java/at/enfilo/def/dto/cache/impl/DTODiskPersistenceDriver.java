package at.enfilo.def.dto.cache.impl;

import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.dto.cache.IDTOPersistenceDriver;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSimpleFileTransport;
import org.apache.thrift.transport.TTransport;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DTODiskPersistenceDriver<T extends TBase> implements IDTOPersistenceDriver<T> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(DTODiskPersistenceDriver.class);
	private static final String DEFAULT_BASE_PATH = "/tmp/def/dto-cache";
	private static final int MAX_LOCKS = 100;

	private Path basePath;
	private TimeoutMap<String, Semaphore> lockMap;
	private Semaphore overallLock;

	private File getFile(String id) {
		return basePath.resolve(id).toFile();
	}

	private void acquireLockForId(String id) {
		try {
			// wait until overallLock is released
			overallLock.acquire();

			if (!lockMap.touch(id)) {
				lockMap.put(id, new Semaphore(1));
			}
			lockMap.get(id).acquire();
		} catch (InterruptedException e) {
			LOGGER.error("Error while acquiring lock. ", e);
		}
	}

	private void releaseLockForId(String id) {
		lockMap.get(id).release();
		overallLock.release();
	}

	@Override
	public T read(String id, T instance) throws IOException {
		acquireLockForId(id);
		File file = getFile(id);
		if (!file.exists()) {
			throw new IOException(String.format("Can not find DTO with id %s in persistence - File not found.", id));
		}
		try (TTransport transport = new TSimpleFileTransport(file.getAbsolutePath(), true, false)) {
			TProtocol protocol = new TCompactProtocol(transport);
			instance.read(protocol);
			return instance;
		} catch (TException e) {
			LOGGER.error("Error while read DTO with id {1}.", id, e);
			throw new IOException(e);
		} finally {
			releaseLockForId(id);
		}
	}

	@Override
	public void write(String id, T dto) throws IOException {
		acquireLockForId(id);
		basePath.toFile().mkdirs();
		File file = getFile(id);
		file.deleteOnExit();
		try (TTransport transport = new TSimpleFileTransport(file.getAbsolutePath(), false, true)) {
			TProtocol protocol = new TCompactProtocol(transport);
			dto.write(protocol);
		} catch (TException e) {
			LOGGER.error("Error while write DTO with Id {}.", id, e);
			throw new IOException(e);
		} finally {
			releaseLockForId(id);
		}
	}

	@Override
	public void init(String baseUrl, String context) {
		try {
			basePath = Paths.get(new URL(baseUrl).getPath());
		} catch (MalformedURLException e) {
			LOGGER.error("Error while init DTODiskPersistenceDriver.", e);
			basePath = Paths.get(DEFAULT_BASE_PATH);
		}
		basePath = basePath.resolve(context); // Add context to base path
		lockMap = new TimeoutMap<>(
				1,
				TimeUnit.HOURS,
				1,
				TimeUnit.HOURS
		);
		overallLock = new Semaphore(MAX_LOCKS);
		LOGGER.info("Initialized DTOCache Driver on {}", basePath.toAbsolutePath());
	}

	@Override
	public void remove(String id) {
		acquireLockForId(id);
		getFile(id).delete();
		releaseLockForId(id);
	}

	@Override
	public void remove(Collection<String> ids) {
		try {
			overallLock.acquire(MAX_LOCKS);
			for (String id : ids) {
				getFile(id).delete();
			}
		} catch (InterruptedException e) {
			LOGGER.error("Error while acquiring overallLock.", e);
		} finally {
			overallLock.release(MAX_LOCKS);
		}
	}

	@Override
	public void removeAll() {
		try {
			overallLock.acquire(MAX_LOCKS);
			basePath.toFile().delete();
		} catch (InterruptedException e) {
			LOGGER.error("Error while acquiring overallLock.", e);
		} finally {
			overallLock.release(MAX_LOCKS);
		}
	}

	@Override
	public boolean exists(String id) {
		acquireLockForId(id);
		boolean result = getFile(id).exists();
		releaseLockForId(id);
		return result;
	}
}
