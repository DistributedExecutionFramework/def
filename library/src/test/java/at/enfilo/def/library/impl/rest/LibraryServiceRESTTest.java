package at.enfilo.def.library.impl.rest;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.library.Library;
import at.enfilo.def.library.impl.LibraryResponseServiceImpl;
import at.enfilo.def.library.impl.LibraryServiceImpl;
import at.enfilo.def.library.impl.LibraryServiceTest;

import java.util.LinkedList;
import java.util.List;

public class LibraryServiceRESTTest extends LibraryServiceTest {

    @Override
    protected IServer getServer() throws ServerCreationException {
        List<IResource> webResources = new LinkedList<>();
        webResources.add(new LibraryServiceImpl(libraryController));
        webResources.add(new LibraryResponseServiceImpl());

        return RESTServer.getInstance(
            Library.getInstance().getConfiguration().getServerHolderConfiguration().getRESTConfiguration(),
            webResources
        );
    }
}
