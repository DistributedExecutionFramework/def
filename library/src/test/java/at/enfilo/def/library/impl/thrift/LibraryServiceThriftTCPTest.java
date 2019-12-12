package at.enfilo.def.library.impl.thrift;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.library.Library;
import at.enfilo.def.library.api.rest.ILibraryResponseService;
import at.enfilo.def.library.api.rest.ILibraryService;
import at.enfilo.def.library.api.thrift.LibraryResponseService;
import at.enfilo.def.library.api.thrift.LibraryService;
import at.enfilo.def.library.impl.LibraryResponseServiceImpl;
import at.enfilo.def.library.impl.LibraryServiceImpl;
import at.enfilo.def.library.impl.LibraryServiceTest;

import java.util.LinkedList;
import java.util.List;

public class LibraryServiceThriftTCPTest extends LibraryServiceTest {

    @Override
    protected IServer getServer() throws ServerCreationException {
        List<ThriftProcessor> thriftProcessors = new LinkedList<>();
        ThriftProcessor<LibraryServiceImpl> libraryServiceThriftProcessor = new ThriftProcessor<>(
            ILibraryService.class.getName(),
            new LibraryServiceImpl(libraryController),
            LibraryService.Processor<LibraryService.Iface>::new
        );
        thriftProcessors.add(libraryServiceThriftProcessor);

        ThriftProcessor<LibraryResponseServiceImpl> libraryResponseServiceThriftProcessor = new ThriftProcessor<>(
            ILibraryResponseService.class.getName(),
            new LibraryResponseServiceImpl(),
            LibraryResponseService.Processor<LibraryResponseService.Iface>::new
        );
        thriftProcessors.add(libraryResponseServiceThriftProcessor);

        return ThriftTCPServer.getInstance(
            Library.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
            thriftProcessors
        );
    }
}

