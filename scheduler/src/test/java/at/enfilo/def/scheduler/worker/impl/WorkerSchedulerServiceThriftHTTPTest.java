package at.enfilo.def.scheduler.worker.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;
import at.enfilo.def.scheduler.server.Scheduler;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerResponseService;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerService;

import java.util.LinkedList;
import java.util.List;

public class WorkerSchedulerServiceThriftHTTPTest extends WorkerSchedulerServiceTest {

    @Override
    protected IServer getServer() throws Exception {
        List<ThriftProcessor> thriftProcessors = new LinkedList<>();

        ThriftProcessor<WorkerSchedulerServiceImpl> workerSchedulerServiceProcessor = new ThriftProcessor<>(
                WorkerSchedulerService.class.getName(),
                new WorkerSchedulerServiceImpl(strategy),
                WorkerSchedulerService.Processor<WorkerSchedulerService.Iface>::new
        );
        ThriftProcessor<WorkerSchedulerResponseServiceImpl> workerSchedulerResponseServiceProcessor = new ThriftProcessor<>(
                WorkerSchedulerResponseService.class.getName(),
                new WorkerSchedulerResponseServiceImpl(),
                WorkerSchedulerResponseService.Processor<WorkerSchedulerResponseService.Iface>::new
        );

        thriftProcessors.add(workerSchedulerServiceProcessor);
        thriftProcessors.add(workerSchedulerResponseServiceProcessor);

        return ThriftHTTPServer.getInstance(
                Scheduler.getInstance().getConfiguration().getServerHolderConfiguration().getThriftHTTPConfiguration(),
                thriftProcessors
        );
    }
}
