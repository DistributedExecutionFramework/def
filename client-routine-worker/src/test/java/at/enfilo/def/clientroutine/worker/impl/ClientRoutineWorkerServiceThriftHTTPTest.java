package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.server.ClientRoutineWorker;
import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;

import java.util.List;

public class ClientRoutineWorkerServiceThriftHTTPTest extends ClientRoutineWorkerServiceThriftTest {

    @Override
    public IServer getServer(List<ThriftProcessor> processors) throws Exception {
        return ThriftHTTPServer.getInstance(
                ClientRoutineWorker.getInstance().getConfiguration().getServerHolderConfiguration().getThriftHTTPConfiguration(),
                processors
        );
    }
}
