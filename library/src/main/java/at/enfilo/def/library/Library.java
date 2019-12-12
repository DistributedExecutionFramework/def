package at.enfilo.def.library;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.library.api.rest.ILibraryAdminResponseService;
import at.enfilo.def.library.api.rest.ILibraryAdminService;
import at.enfilo.def.library.api.rest.ILibraryResponseService;
import at.enfilo.def.library.api.rest.ILibraryService;
import at.enfilo.def.library.api.thrift.LibraryAdminResponseService;
import at.enfilo.def.library.api.thrift.LibraryAdminService;
import at.enfilo.def.library.api.thrift.LibraryResponseService;
import at.enfilo.def.library.api.thrift.LibraryService;
import at.enfilo.def.library.impl.LibraryResponseServiceImpl;
import at.enfilo.def.library.impl.LibraryServiceImpl;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Startup class for Library.
 */
public class Library extends ServerStartup<LibraryConfiguration> {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Library.class);
	private static final String CONFIG_FILE = "library.yml";

	private static Library instance;

	public Library() {
		super(Library.class, LibraryConfiguration.class, CONFIG_FILE, LOGGER);
	}

	/**
	 * Main entry point for LibraryServices.
	 * @param args - arguments
	 */
	public static void main(String[] args) {
		LOGGER.info("Startup Library...");

		try {
			// Start services
			getInstance().startServices();

		} catch (Exception e) {
			LOGGER.error("Library failed to start.", e);
		}
	}

	@Override
	protected List<ThriftProcessor> getThriftProcessors() {
		ThriftProcessor libraryServiceProcessor = new ThriftProcessor<>(
				ILibraryService.class.getName(),
				new LibraryServiceImpl(),
				LibraryService.Processor<LibraryService.Iface>::new
		);

		ThriftProcessor libraryServiceResponseProcessor = new ThriftProcessor<>(
				ILibraryResponseService.class.getName(),
				new LibraryResponseServiceImpl(),
				LibraryResponseService.Processor<LibraryResponseService.Iface>::new
		);

		ThriftProcessor libraryAdminServiceProcessor = new ThriftProcessor<>(
			ILibraryAdminService.class.getName(),
			new LibraryServiceImpl(),
			LibraryAdminService.Processor<LibraryAdminService.Iface>::new
		);

		ThriftProcessor libraryAdminServiceResponseProcessor = new ThriftProcessor<>(
			ILibraryAdminResponseService.class.getName(),
			new LibraryResponseServiceImpl(),
			LibraryAdminResponseService.Processor<LibraryAdminResponseService.Iface>::new
		);

		List<ThriftProcessor> thriftProcessorList = new LinkedList<>();
		thriftProcessorList.add(libraryServiceProcessor);
		thriftProcessorList.add(libraryServiceResponseProcessor);
		thriftProcessorList.add(libraryAdminServiceProcessor);
		thriftProcessorList.add(libraryAdminServiceResponseProcessor);
		return thriftProcessorList;
	}

	@Override
	protected List<IResource> getWebResources() {
		List<IResource> resourceList = new LinkedList<>();
		resourceList.add(new LibraryServiceImpl());
		resourceList.add(new LibraryResponseServiceImpl());
		return resourceList;
	}

	public static Library getInstance() {
		if (instance == null) {
			instance = new Library();
		}
		return instance;
	}
}
