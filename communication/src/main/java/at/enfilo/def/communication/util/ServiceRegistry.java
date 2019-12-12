package at.enfilo.def.communication.util;

import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mase on 09.09.2016.
 */
public class ServiceRegistry {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ServiceRegistry.class);

	private final static Map<Class<?>, Closeable> SERVICE_REGISTRY = new ConcurrentHashMap<>();

    private static class ThreadSafeLazySingletonWrapper {
        private static final ServiceRegistry INSTANCE = new ServiceRegistry();
    }

    private ServiceRegistry() {
        // Should be implemented as thread safe lazy singleton
    }

    public static ServiceRegistry getInstance() {
        return ThreadSafeLazySingletonWrapper.INSTANCE;
    }

    public void registerService(Class<?> serviceClass, Closeable serviceInstance) {
        SERVICE_REGISTRY.put(serviceClass, serviceInstance);
		LOGGER.debug("Registered Service {}", serviceClass);
    }

    public void closeAll() throws IOException {
        List<Closeable> services = new LinkedList<>(SERVICE_REGISTRY.values());
        for (Closeable service : services) {
			LOGGER.debug("Try to close and deregister Service {}", service.getClass());
            service.close();
			LOGGER.info("Service {} closed", service.getClass());
			SERVICE_REGISTRY.remove(service.getClass());
        }
    }

    public int registeredServices() {
		return SERVICE_REGISTRY.size();
	}

    public List<Closeable> getAll() {
        return new LinkedList<>(SERVICE_REGISTRY.values());
    }

    public boolean hasServiceInstanceOf(Class<?> serviceClass) {
        return SERVICE_REGISTRY.containsKey(serviceClass);
    }

    public <T> T getServiceInstanceOf(Class<T> serviceClass) {
        return serviceClass.cast(SERVICE_REGISTRY.get(serviceClass));
    }
}
