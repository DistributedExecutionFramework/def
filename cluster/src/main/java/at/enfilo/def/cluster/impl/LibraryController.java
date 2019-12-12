package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.configuration.ClusterConfiguration;
import at.enfilo.def.cluster.util.exception.LibraryException;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.RoutineDTO;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LibraryController {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(LibraryController.class);

    private static LibraryController instance;

    private ILibraryServiceClient libraryServiceClient;

    private static final Object INSTANCE_LOCK = new Object();

    public static LibraryController getInstance() {
        synchronized (INSTANCE_LOCK) {
            if (instance == null) {
                instance = new LibraryController();
            }
            return instance;
        }
    }

    private LibraryController() {
        this(
            null, //null means that LibraryServiceClient will be constructed (Endpoint is defined in CluterConfiguration)
            Cluster.getInstance().getConfiguration()
        );
    }

    private LibraryController(
            ILibraryServiceClient libraryServiceClient,
            ClusterConfiguration configuration
    ) {
        if (libraryServiceClient == null) {
            LibraryServiceClientFactory factory = new LibraryServiceClientFactory();
            try {
                this.libraryServiceClient = factory.createClient(configuration.getLibraryEndpoint());
            } catch (ClientCreationException e) {
                LOGGER.error("Error while creating library service client.", e);
            }
        } else {
            this.libraryServiceClient = libraryServiceClient;
        }
    }

    public RoutineDTO fetchRoutine(String routineId) throws LibraryException {
        try {
            Future<RoutineDTO> future = libraryServiceClient.getRoutine(routineId);
            return future.get();
        } catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
            LOGGER.error("Error while fetching routine with id {} from library.", routineId, e);
            throw new LibraryException(e);
        }
    }
}
