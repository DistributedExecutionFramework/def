package at.enfilo.def.scheduler.clientroutineworker.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;
import at.enfilo.def.scheduler.clientroutineworker.api.thrift.ClientRoutineWorkerSchedulerResponseService;
import at.enfilo.def.scheduler.clientroutineworker.api.thrift.ClientRoutineWorkerSchedulerService;
import at.enfilo.def.scheduler.server.Scheduler;

import java.util.LinkedList;
import java.util.List;

public class ClientRoutineWorkerSchedulerServiceThriftHTTPTest extends ClientRoutineWorkerSchedulerServiceTest {

    @Override
    protected IServer getServer() throws Exception {
        List<ThriftProcessor> thriftProcessors = new LinkedList<>();

        ThriftProcessor<ClientRoutineWorkerSchedulerServiceImpl> serviceProcessor = new ThriftProcessor<>(
                ClientRoutineWorkerSchedulerService.class.getName(),
                new ClientRoutineWorkerSchedulerServiceImpl(strategy),
                ClientRoutineWorkerSchedulerService.Processor<ClientRoutineWorkerSchedulerService.Iface>::new
        );
        ThriftProcessor<ClientRoutineWorkerSchedulerResponseServiceImpl> responseServiceProcessor = new ThriftProcessor<>(
                ClientRoutineWorkerSchedulerResponseService.class.getName(),
                new ClientRoutineWorkerSchedulerResponseServiceImpl(),
                ClientRoutineWorkerSchedulerResponseService.Processor<ClientRoutineWorkerSchedulerResponseService.Iface>::new
        );

        thriftProcessors.add(serviceProcessor);
        thriftProcessors.add(responseServiceProcessor);

        return ThriftHTTPServer.getInstance(
                Scheduler.getInstance().getConfiguration().getServerHolderConfiguration().getThriftHTTPConfiguration(),
                thriftProcessors
        );
    }
}
