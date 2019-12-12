package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerResponseService;
import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerService;
import at.enfilo.def.clientroutine.worker.server.ClientRoutineWorker;
import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;

import java.util.LinkedList;
import java.util.List;

public abstract class ClientRoutineWorkerServiceThriftTest extends ClientRoutineWorkerServiceTest {

    private static final String REQUEST_SERVICE_NAME = ClientRoutineWorkerService.class.getName();
    private static final String RESPONSE_SERVICE_NAME = ClientRoutineWorkerResponseService.class.getName();

    @Override
    protected IServer getServer(ClientRoutineWorkerServiceController controller) throws Exception {

        ThriftProcessor clientRoutineWorkerServiceProcessor = new ThriftProcessor<>(
                REQUEST_SERVICE_NAME,
                new ClientRoutineWorkerServiceImpl(controller, TicketRegistry.getInstance()),
                ClientRoutineWorkerService.Processor<ClientRoutineWorkerService.Iface>::new
        );
        ThriftProcessor clientRoutineWorkerResponseServiceProcessor = new ThriftProcessor<>(
                RESPONSE_SERVICE_NAME,
                new ClientRoutineWorkerResponseServiceImpl(),
                ClientRoutineWorkerResponseService.Processor<ClientRoutineWorkerResponseService.Iface>::new
        );
        List<ThriftProcessor> processors = new LinkedList<>();
        processors.add(clientRoutineWorkerServiceProcessor);
        processors.add(clientRoutineWorkerResponseServiceProcessor);

        return getServer(processors);
    }

    public abstract IServer getServer(List<ThriftProcessor> processors) throws Exception;
}
