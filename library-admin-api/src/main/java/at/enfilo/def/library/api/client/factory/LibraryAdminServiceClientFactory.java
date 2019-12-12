package at.enfilo.def.library.api.client.factory;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.api.rest.ILibraryAdminResponseService;
import at.enfilo.def.library.api.rest.ILibraryAdminService;
import at.enfilo.def.library.api.thrift.LibraryAdminResponseService;
import at.enfilo.def.library.api.thrift.LibraryAdminService;

public class LibraryAdminServiceClientFactory extends UnifiedClientFactory<ILibraryAdminServiceClient> {
	static {
		// Registering unified IManagementResource.
		register(
			ILibraryAdminServiceClient.class,
			LibraryAdminServiceClient::new,
			ILibraryAdminService.class,
			ILibraryAdminResponseService.class,
			ILibraryAdminService.class,
			LibraryAdminService.Client::new,
			ILibraryAdminResponseService.class,
			LibraryAdminResponseService.Client::new
		);
	}

	public LibraryAdminServiceClientFactory() {
		super(ILibraryAdminServiceClient.class);
	}
}
