package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.reducer.server.Reducer;

import java.util.List;

public class ReducerServiceThriftTCPTest extends ReducerServiceThriftTest {

    @Override
    public IServer getServer(List<ThriftProcessor> processors) throws Exception {
        return ThriftTCPServer.getInstance(
                Reducer.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
                processors
        );
    }
}
