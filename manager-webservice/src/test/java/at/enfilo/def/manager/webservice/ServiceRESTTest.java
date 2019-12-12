package at.enfilo.def.manager.webservice;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import at.enfilo.def.manager.impl.ManagerResponseServiceImpl;
import at.enfilo.def.manager.impl.ManagerServiceImpl;
import at.enfilo.def.manager.server.Manager;
import at.enfilo.def.manager.webservice.impl.JobServiceImpl;
import at.enfilo.def.manager.webservice.impl.ProgramServiceImpl;
import at.enfilo.def.manager.webservice.impl.TaskServiceImpl;
import at.enfilo.def.manager.webservice.mocks.ClusterServiceImplMock;
import at.enfilo.def.manager.webservice.mocks.JobServiceImplMock;
import at.enfilo.def.manager.webservice.mocks.ProgramServiceImplMock;
import at.enfilo.def.manager.webservice.mocks.TaskServiceImplMock;
import at.enfilo.def.manager.webservice.server.ManagerWebservice;

import java.util.LinkedList;
import java.util.List;

public abstract class ServiceRESTTest {

    public static ProgramServiceImplMock programService = new ProgramServiceImplMock();
    public static JobServiceImplMock jobService = new JobServiceImplMock();
    public static TaskServiceImplMock taskService = new TaskServiceImplMock();
    public static ClusterServiceImplMock clusterService = new ClusterServiceImplMock();
    public static DEFServerEndpointConfiguration restEndpoint = ManagerWebservice.getInstance().getConfiguration().getServerHolderConfiguration().getRESTConfiguration();

    protected static IServer getWebserviceServer() throws ServerCreationException {

        List<IResource> resourceList = new LinkedList<>();
        resourceList.add(programService);
        resourceList.add(jobService);
        resourceList.add(taskService);
        resourceList.add(clusterService);

        return RESTServer.getInstance(
                restEndpoint,
                resourceList
        );
    }
}
