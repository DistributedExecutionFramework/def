package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.reducer.api.thrift.ReducerResponseService;
import at.enfilo.def.reducer.api.thrift.ReducerService;

import java.util.LinkedList;
import java.util.List;

public abstract class ReducerServiceThriftTest extends ReducerServiceTest {

    private static final String REQUEST_SERVICE_NAME = ReducerService.class.getName();
    private static final String RESPONSE_SERVICE_NAME = ReducerResponseService.class.getName();

    @Override
    protected IServer getServer(ReducerServiceController controller) throws Exception {
        ThriftProcessor reducerServiceProcessor = new ThriftProcessor<>(
                REQUEST_SERVICE_NAME,
                new ReducerServiceImpl(controller, TicketRegistry.getInstance()),
                ReducerService.Processor<ReducerService.Iface>::new
        );
        ThriftProcessor reducerResponseServiceProcessor = new ThriftProcessor<>(
                RESPONSE_SERVICE_NAME,
                new ReducerResponseServiceImpl(),
                ReducerResponseService.Processor<ReducerResponseService.Iface>::new
        );
        List<ThriftProcessor> processors = new LinkedList<>();
        processors.add(reducerServiceProcessor);
        processors.add(reducerResponseServiceProcessor);

        return getServer(processors);
    }

    public abstract IServer getServer(List<ThriftProcessor> processors) throws Exception;
}
