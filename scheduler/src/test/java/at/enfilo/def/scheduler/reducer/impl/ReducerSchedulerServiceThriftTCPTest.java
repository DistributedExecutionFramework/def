package at.enfilo.def.scheduler.reducer.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerResponseService;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerService;
import at.enfilo.def.scheduler.server.Scheduler;

import java.util.LinkedList;
import java.util.List;

public class ReducerSchedulerServiceThriftTCPTest extends ReducerSchedulerServiceTest {

    @Override
    protected IServer getServer() throws Exception {
        List<ThriftProcessor> thriftProcessors = new LinkedList<>();

        ThriftProcessor<ReducerSchedulerServiceImpl> reducerSchedulerServiceProcessor = new ThriftProcessor<>(
            ReducerSchedulerService.class.getName(),
            new ReducerSchedulerServiceImpl(strategy),
            ReducerSchedulerService.Processor<ReducerSchedulerService.Iface>::new
        );
        ThriftProcessor<ReducerSchedulerResponseServiceImpl> reducerSchedulerResponseServiceProcessor = new ThriftProcessor<>(
                ReducerSchedulerResponseService.class.getName(),
                new ReducerSchedulerResponseServiceImpl(),
                ReducerSchedulerResponseService.Processor<ReducerSchedulerResponseService.Iface>::new
        );

        thriftProcessors.add(reducerSchedulerServiceProcessor);
        thriftProcessors.add(reducerSchedulerResponseServiceProcessor);

        return ThriftTCPServer.getInstance(
                Scheduler.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
                thriftProcessors
        );
    }
}
