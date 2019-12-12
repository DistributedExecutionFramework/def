package at.enfilo.def.communication.api.common.client;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.common.util.DaemonThreadFactory;
import at.enfilo.def.communication.api.common.util.ServiceReferenceResolver;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by mase on 31.08.2016.
 */
public class ThriftTCPClient<T extends TServiceClient> extends ServiceClient<T> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ThriftTCPClient.class);
	private static final long MAX_LIFE_TIME = 60L * 1000L;

	private final String serviceReference;
    private final Function<TProtocol, T> clientBuilder;
    private final Semaphore lock;
    private T client;
    private TSocket transport;
    private Instant lastUsed;

    public ThriftTCPClient(ServiceEndpointDTO serviceEndpoint, Class<?> serviceInterfaceClass, Function<TProtocol, T> clientBuilder) {
        this(serviceEndpoint, serviceInterfaceClass.getName(), clientBuilder);
    }

    public ThriftTCPClient(ServiceEndpointDTO serviceEndpoint, String serviceName, Function<TProtocol, T> clientBuilder) {
        super(serviceEndpoint);

        this.lock = new Semaphore(1);
        this.serviceReference = ServiceReferenceResolver.resolve(serviceName, serviceEndpoint);
        this.clientBuilder = clientBuilder;
    }

    private T getClient() throws TTransportException, IOException {
    	if (client == null) {
			transport = new TSocket(getServiceEndpoint().getHost(), getServiceEndpoint().getPort());
			transport.getSocket().setReuseAddress(false);
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, serviceReference);
			client = clientBuilder.apply(mp);
			ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
					r -> DaemonThreadFactory.newDaemonThread(r, "ThriftTCPClientRefresh")
			);
			scheduledExecutorService.schedule(
					this::resetConnection,
					30,
					TimeUnit.SECONDS
			);
		}
		lastUsed = Instant.now();
		return client;
	}


	private void resetConnection() {
		if (Duration.between(lastUsed, Instant.now()).toMillis() >= MAX_LIFE_TIME) {
			close();
		}
	}

	@Override
    protected void exec(IThrowingConsumer<T> proxy)
    throws ClientCommunicationException {
		try {
			lock.acquire();
			T client = getClient();
			proxy.accept(client);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ClientCommunicationException(e);
		} catch (Exception e) {
			throw new ClientCommunicationException(e);
		} finally {
			if (lock.availablePermits() <= 0) {
				lock.release();
			}
		}
    }

    @Override
    protected <R> R exec(IThrowingFunction<T, R> proxy)
    throws ClientCommunicationException {
		try {
			lock.acquire();
			T client = getClient();
			return proxy.apply(client);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ClientCommunicationException(e);
		} catch (Exception e) {
			throw new ClientCommunicationException(e);
		} finally {
			if (lock.availablePermits() <= 0) {
				lock.release();
			}
		}
	}


	@Override
	public void close() {
    	try {
			lock.acquire();
			if (transport != null) {
    			transport.close();
    			transport = null;
    			client = null;
			}
		} catch (InterruptedException e) {
    		LOGGER.error("Thread interrupted.", e);
    		Thread.currentThread().interrupt();
		} finally {
    		if (lock.availablePermits() <= 0) {
				lock.release();
			}
		}
	}
}
