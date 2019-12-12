package at.enfilo.def.communication.thrift;

import at.enfilo.def.communication.api.common.service.IDEFProcessor;
import at.enfilo.def.communication.api.common.service.IResource;
import org.apache.thrift.TProcessor;

import java.util.function.Function;

/**
 * Created by mase on 31.08.2016.
 */
public class ThriftProcessor<T extends IResource> implements IDEFProcessor<T, TProcessor> {

    private final String name;
    private final T serviceImpl;
    private final TProcessor processor;

    public ThriftProcessor(Class<?> serviceInterfaceClass,  T serviceImpl, Function<T, TProcessor> processorBuilder) {
        this(serviceInterfaceClass.getName(), serviceImpl, processorBuilder);
    }

    public ThriftProcessor(String name, T serviceImpl, Function<T, TProcessor> processorBuilder) {
        this.name = name;
        this.serviceImpl = serviceImpl;
        this.processor = processorBuilder.apply(serviceImpl);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getImplementation() {
        return serviceImpl;
    }

    @Override
    public TProcessor getProcessor() {
        return processor;
    }
}
