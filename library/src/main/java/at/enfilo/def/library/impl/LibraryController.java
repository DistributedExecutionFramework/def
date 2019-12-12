package at.enfilo.def.library.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.library.Library;
import at.enfilo.def.library.LibraryConfiguration;
import at.enfilo.def.library.api.util.BaseDataTypeRegistry;
import at.enfilo.def.library.api.util.BaseRoutineRegistry;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.library.util.store.driver.fs.FSStoreDriver;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.persistence.dao.PersistenceFacade;
import at.enfilo.def.transfer.dto.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;

public abstract class LibraryController {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(LibraryController.class);
	private static final Object INSTANCE_LOCK = new Object();

	private static LibraryController instance;

	protected final Map<String, RoutineDTO> routineRegistry;
	private final LibraryConfiguration libraryConfiguration;
	private final LibraryInfoDTO libraryInfoDTO;

	protected LibraryController(LibraryConfiguration configuration, IBinaryStoreDriver storeDriver) {
		this.routineRegistry = new HashMap<>();

        this.libraryConfiguration = Library.getInstance().getConfiguration();
        this.libraryInfoDTO = new LibraryInfoDTO(
        		libraryConfiguration.getId(),
				libraryConfiguration.getLibraryType(),
				libraryConfiguration.getStoreDriver(),
				new HashSet<>()
		);
	}

	public static LibraryController getInstance() {
		synchronized (INSTANCE_LOCK) {
			if (instance == null) {
				LibraryConfiguration configuration = Library.getInstance().getConfiguration();
				IBinaryStoreDriver storeDriver = getStoreDriver(configuration);
				switch (configuration.getLibraryType()) {
					case SLAVE:
						LOGGER.info("Starting SLAVE Library with fetch-endpoint {}.", configuration.getLibraryEndpoint());
						try {
							instance = new SlaveLibraryController(configuration, storeDriver);
						} catch (ClientCreationException e) {
							LOGGER.error("Error while creating SlaveLibraryController.", e);
							throw new RuntimeException(e);
						}
						break;

					case MASTER:
						LOGGER.info("Starting MASTER Library.");
						instance = new MasterLibraryController(
								configuration,
								storeDriver,
								new PersistenceFacade(),
								BaseDataTypeRegistry.getInstance(),
								BaseRoutineRegistry.getInstance()
						);
						break;
				}
				instance.init();
			}
			return instance;
		}
	}

	protected abstract void init();

	public LibraryInfoDTO getInfo() {
	    libraryInfoDTO.setStoredRoutines(routineRegistry.keySet());
	    return libraryInfoDTO;
	}

	public boolean isRoutineStored(String rId) {
		return routineRegistry.containsKey(rId);
	}

	// TODO: Lock based on rId
	public synchronized RoutineDTO getRoutine(String rId) throws ExecutionException {
		LOGGER.debug("Fetch routine {}.", rId);

		if (!routineRegistry.containsKey(rId)) {
			LOGGER.info("Routine {} is not stored/cached locally. Fetch from MasterStorage.", rId);
			try {

				// Requesting routine information.
				RoutineDTO routine = fetchRoutine(rId);

				// Register routine locally.
				routineRegistry.put(rId, routine);

			} catch (Exception e) {
				LOGGER.error("Error while fetch Routine {} from MasterStorage.", rId, e);
				throw new ExecutionException(e);
			}
		}
		return routineRegistry.get(rId);
	}

	protected abstract RoutineDTO fetchRoutine(String rId) throws RoutineFetchException;

	public void setLibraryEndpoint(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException {
		this.libraryConfiguration.setLibraryEndpoint(serviceEndpoint);
		updateLibraryEndpoint(serviceEndpoint);
	}

	protected abstract void updateLibraryEndpoint(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException;

    public abstract List<String> findRoutines(String pattern) throws ExecutionException;

    public abstract void removeRoutine(String rId) throws ExecutionException, IOException;

    public abstract String createRoutine(RoutineDTO routineDTO) throws ExecutionException;

	public abstract String updateRoutine(RoutineDTO routineDTO) throws ExecutionException;

	public abstract RoutineBinaryDTO getRoutineBinary(String bId) throws ExecutionException;

	public List<FeatureDTO> getRoutineRequiredFeatures(String rId) throws ExecutionException {
		RoutineDTO routine = getRoutine(rId);
		if (routine == null) {
			throw new ExecutionException("Could not load Routine: " + rId, null);
		}

		return routine.getRequiredFeatures();
	}

    public abstract String uploadRoutineBinary(String rId, String md5, long sizeInBytes, boolean isPrimary, ByteBuffer data) throws ExecutionException;

    public abstract void removeRoutineBinary(String rId, String bId) throws ExecutionException;

    public abstract List<String> findDataTypes(String searchPattern) throws ExecutionException;

    public abstract String createDataType(String name, String schema) throws ExecutionException;

    public abstract DataTypeDTO getDataType(String dId) throws ExecutionException;

    public abstract void removeDataType(String dId) throws ExecutionException;

    public abstract List<TagDTO> findTags(String searchPattern) throws ExecutionException;

    public abstract String createTag(String label, String description) throws ExecutionException;

    public abstract void removeTag(String label) throws ExecutionException;

	private static IBinaryStoreDriver getStoreDriver(LibraryConfiguration libraryConfiguration) {
		try {
			Class<? extends IBinaryStoreDriver> driverClass = Class.forName(libraryConfiguration.getStoreDriver()).asSubclass(IBinaryStoreDriver.class);
			return driverClass.newInstance();

		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			LOGGER.error("Error while loading StoreDriver {}. Using default: FSStoreDriver.", libraryConfiguration.getStoreDriver(), e);
			return new FSStoreDriver();
		}
	}

	public abstract String createFeature(String name, String group, String version) throws ExecutionException;

	public abstract String addExtension(String featureId, String name, String version) throws ExecutionException;

	public abstract List<FeatureDTO> getFeatures(String pattern) throws ExecutionException;
}
