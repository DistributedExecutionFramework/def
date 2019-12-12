package at.enfilo.def.scheduler.clientroutineworker.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.scheduler.server.Scheduler;

import java.util.LinkedList;
import java.util.List;

public class ClientRoutineWorkerSchedulerServiceRESTTest extends ClientRoutineWorkerSchedulerServiceTest {

    @Override
    protected IServer getServer() throws Exception {
        List<IResource> webResources = new LinkedList<>();
        webResources.add(new ClientRoutineWorkerSchedulerServiceImpl(strategy));
        webResources.add(new ClientRoutineWorkerSchedulerResponseServiceImpl());

        return RESTServer.getInstance(
                Scheduler.getInstance().getConfiguration().getServerHolderConfiguration().getRESTConfiguration(),
                webResources
        );
    }
}
