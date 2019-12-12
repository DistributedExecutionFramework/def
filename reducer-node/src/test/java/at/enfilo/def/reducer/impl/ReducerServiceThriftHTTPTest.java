package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;
import at.enfilo.def.reducer.server.Reducer;

import java.util.List;

public class ReducerServiceThriftHTTPTest extends ReducerServiceThriftTest {

    @Override
    public IServer getServer(List<ThriftProcessor> processors) throws Exception {
        return ThriftHTTPServer.getInstance(
                Reducer.getInstance().getConfiguration().getServerHolderConfiguration().getThriftHTTPConfiguration(),
                processors
        );
    }
}
