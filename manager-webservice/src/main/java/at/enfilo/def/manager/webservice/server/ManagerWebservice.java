package at.enfilo.def.manager.webservice.server;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.manager.webservice.impl.*;
import at.enfilo.def.manager.webservice.util.ManagerWebserviceConfiguration;
import at.enfilo.def.communication.misc.ServerStartup;

import java.util.LinkedList;
import java.util.List;

public class ManagerWebservice extends ServerStartup<ManagerWebserviceConfiguration> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ManagerWebservice.class);
    private static final String CONFIG_FILE = "manager-webservice.yml";

    private static ManagerWebservice instance;

    public static ManagerWebservice getInstance() {
        if (instance == null) {
            instance = new ManagerWebservice();
        }
        return instance;
    }

    public static void main(String[] args) {
        LOGGER.info("Startup ManagerWebservice");

        try {
            getInstance().startServices();
        } catch (Exception e) {
            LOGGER.error("ManagerWebservice failed to start.", e);
        }
    }

    private ManagerWebservice() {
        super(ManagerWebservice.class, ManagerWebserviceConfiguration.class, CONFIG_FILE, LOGGER);
    }

    @Override
    protected List<ThriftProcessor> getThriftProcessors() {
        return null;
    }

    @Override
    protected List<IResource> getWebResources() {
        List<IResource> resourceList = new LinkedList<>();
        resourceList.add(new ProgramServiceImpl());
        resourceList.add(new JobServiceImpl());
        resourceList.add(new TaskServiceImpl());
        resourceList.add(new ClusterServiceImpl());
        resourceList.add(new DataConverterServiceImpl());
        resourceList.add(new LibraryServiceImpl());
        return resourceList;
    }
}
