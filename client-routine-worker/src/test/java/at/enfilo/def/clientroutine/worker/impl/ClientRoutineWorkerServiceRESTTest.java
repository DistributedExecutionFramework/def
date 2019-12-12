package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.server.ClientRoutineWorker;
import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.rest.RESTServer;

import java.util.LinkedList;
import java.util.List;

public class ClientRoutineWorkerServiceRESTTest extends ClientRoutineWorkerServiceTest {

    @Override
    protected IServer getServer(ClientRoutineWorkerServiceController controller) throws Exception {
        List<IResource> resources = new LinkedList<>();
        resources.add(new ClientRoutineWorkerServiceImpl(controller, TicketRegistry.getInstance()));
        resources.add(new ClientRoutineWorkerResponseServiceImpl());

        return RESTServer.getInstance(
                ClientRoutineWorker.getInstance().getConfiguration().getServerHolderConfiguration().getRESTConfiguration(),
                resources
        );
    }
}
