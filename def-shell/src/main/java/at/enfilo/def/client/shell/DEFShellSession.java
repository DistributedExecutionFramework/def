package at.enfilo.def.client.shell;

import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryAdminServiceClientFactory;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.manager.api.ManagerServiceClientFactory;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import org.springframework.stereotype.Component;

@Component
public class DEFShellSession {

	private Service activeService;
	private ServiceEndpointDTO activeEndpoint;
	private ILibraryServiceClient libraryServiceClient;
	private ILibraryAdminServiceClient libraryAdminServiceClient;
	private IManagerServiceClient managerServiceClient;
	private IClusterServiceClient clusterServiceClient;
	private IExecLogicServiceClient execLogicServiceClient;
	private IWorkerServiceClient workerServiceClient;

	public Service getActiveService() {
		return activeService;
	}

	public ServiceEndpointDTO getActiveEndpoint() {
		return activeEndpoint;
	}

    public ILibraryServiceClient getLibraryServiceClient() {
        return libraryServiceClient;
    }

	public ILibraryAdminServiceClient getLibraryAdminServiceClient() {
		return libraryAdminServiceClient;
	}

	public IManagerServiceClient getManagerServiceClient() {
		return managerServiceClient;
	}

	public IClusterServiceClient getClusterServiceClient() {
		return clusterServiceClient;
	}

	public IExecLogicServiceClient getExecLogicServiceClient() {
		return execLogicServiceClient;
	}

	public IWorkerServiceClient getWorkerServiceClient() {
		return workerServiceClient;
	}


	void setActiveService(Service activeService) {
		this.activeService = activeService;
	}

    void setLibraryServiceClient(ILibraryServiceClient libraryServiceClient) {
        this.libraryServiceClient = libraryServiceClient;
    }

	void setLibraryAdminServiceClient(ILibraryAdminServiceClient libraryAdminServiceClient) {
		this.libraryAdminServiceClient = libraryAdminServiceClient;
	}

	void setManagerServiceClient(IManagerServiceClient managerServiceClient) {
		this.managerServiceClient = managerServiceClient;
	}

	void setClusterServiceClient(IClusterServiceClient clusterServiceClient) {
		this.clusterServiceClient = clusterServiceClient;
	}

	void setExecLogicServiceClient(IExecLogicServiceClient execLogicServiceClient) {
		this.execLogicServiceClient = execLogicServiceClient;
	}

	void setWorkerServiceClient(IWorkerServiceClient workerServiceClient) {
		this.workerServiceClient = workerServiceClient;
	}

	void switchToService(Service service, ServiceEndpointDTO endpoint) throws ClientCreationException {
		switch (service) {
			case WORKER:
				workerServiceClient = new WorkerServiceClientFactory().createClient(endpoint);
				break;
			case CLUSTER:
				clusterServiceClient = new ClusterServiceClientFactory().createClient(endpoint);
				break;
			case EXEC_LOGIC:
				execLogicServiceClient = new ExecLogicServiceClientFactory().createClient(endpoint);
				break;
			case MANAGER:
				managerServiceClient = new ManagerServiceClientFactory().createClient(endpoint);
				break;
			case LIBRARY:
				libraryServiceClient = new LibraryServiceClientFactory().createClient(endpoint);
				libraryAdminServiceClient = new LibraryAdminServiceClientFactory().createClient(endpoint);
				break;
			case SCHEDULER:
				// TODO
			default:
				break;
		}
		this.activeService = service;
		this.activeEndpoint = endpoint;
	}
}
