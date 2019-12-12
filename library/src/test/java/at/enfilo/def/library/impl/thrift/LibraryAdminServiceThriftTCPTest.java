package at.enfilo.def.library.impl.thrift;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.library.Library;
import at.enfilo.def.library.api.rest.ILibraryAdminResponseService;
import at.enfilo.def.library.api.rest.ILibraryAdminService;
import at.enfilo.def.library.api.thrift.LibraryAdminResponseService;
import at.enfilo.def.library.api.thrift.LibraryAdminService;
import at.enfilo.def.library.impl.LibraryAdminServiceTest;
import at.enfilo.def.library.impl.LibraryResponseServiceImpl;
import at.enfilo.def.library.impl.LibraryServiceImpl;

import java.util.LinkedList;
import java.util.List;

public class LibraryAdminServiceThriftTCPTest extends LibraryAdminServiceTest {

    @Override
    protected IServer getServer() throws ServerCreationException {
        List<ThriftProcessor> thriftProcessors = new LinkedList<>();
        ThriftProcessor<LibraryServiceImpl> libraryServiceThriftProcessor = new ThriftProcessor<>(
            ILibraryAdminService.class.getName(),
            new LibraryServiceImpl(libraryController),
            LibraryAdminService.Processor<LibraryAdminService.Iface>::new
        );
        thriftProcessors.add(libraryServiceThriftProcessor);

        ThriftProcessor<LibraryResponseServiceImpl> libraryResponseServiceThriftProcessor = new ThriftProcessor<>(
            ILibraryAdminResponseService.class.getName(),
            new LibraryResponseServiceImpl(),
            LibraryAdminResponseService.Processor<LibraryAdminResponseService.Iface>::new
        );
        thriftProcessors.add(libraryResponseServiceThriftProcessor);

        return ThriftTCPServer.getInstance(
            Library.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
            thriftProcessors
        );
    }
}

