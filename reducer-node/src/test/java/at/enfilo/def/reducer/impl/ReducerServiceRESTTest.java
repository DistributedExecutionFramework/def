package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.reducer.server.Reducer;

import java.util.LinkedList;
import java.util.List;

public class ReducerServiceRESTTest extends ReducerServiceTest {

    @Override
    protected IServer getServer(ReducerServiceController controller) throws Exception {
        List<IResource> resources = new LinkedList<>();
        resources.add(new ReducerServiceImpl(controller, TicketRegistry.getInstance()));
        resources.add(new ReducerResponseServiceImpl());

        return RESTServer.getInstance(
                Reducer.getInstance().getConfiguration().getServerHolderConfiguration().getRESTConfiguration(),
                resources
        );
    }
}
