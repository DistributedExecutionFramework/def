package at.enfilo.def.library.api.client.factory;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.rest.ILibraryResponseService;
import at.enfilo.def.library.api.rest.ILibraryService;
import at.enfilo.def.library.api.thrift.LibraryResponseService;
import at.enfilo.def.library.api.thrift.LibraryService;

public class LibraryServiceClientFactory extends UnifiedClientFactory<ILibraryServiceClient> {
	static {
		// Registering unified (I)LibraryService interface.
		register(
			ILibraryServiceClient.class,
			LibraryServiceClient::new,
			ILibraryService.class,
			ILibraryResponseService.class,
			ILibraryService.class,
			LibraryService.Client::new,
			ILibraryResponseService.class,
			LibraryResponseService.Client::new
		);
	}

	public LibraryServiceClientFactory() {
		super(ILibraryServiceClient.class);
	}
}
