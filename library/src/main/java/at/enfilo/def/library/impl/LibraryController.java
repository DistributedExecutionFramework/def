package at.enfilo.def.library.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
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
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public abstract class LibraryController {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(LibraryController.class);
	private static final Object INSTANCE_LOCK = new Object();

	private static LibraryController instance;
	protected final Map<String, RoutineDTO> routineRegistry;
	protected final LibraryConfiguration libraryConfiguration;
	protected final LibraryInfoDTO libraryInfoDTO;
	protected final IBinaryStoreDriver binaryStoreDriver;

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

	private static IBinaryStoreDriver getStoreDriver(LibraryConfiguration libraryConfiguration) {
		try {
			Class<? extends IBinaryStoreDriver> driverClass = Class.forName(libraryConfiguration.getStoreDriver()).asSubclass(IBinaryStoreDriver.class);
			return driverClass.newInstance();

		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			LOGGER.error("Error while loading StoreDriver {}. Using default: FSStoreDriver.", libraryConfiguration.getStoreDriver(), e);
			return new FSStoreDriver();
		}
	}

	protected LibraryController(LibraryConfiguration configuration, IBinaryStoreDriver storeDriver) {
		this.routineRegistry = new HashMap<>();

		this.libraryConfiguration = configuration;
		this.binaryStoreDriver = storeDriver;
		this.libraryInfoDTO = new LibraryInfoDTO(
				libraryConfiguration.getId(),
				libraryConfiguration.getLibraryType(),
				libraryConfiguration.getStoreDriver(),
				new HashSet<>()
		);
	}

	public LibraryInfoDTO getInfo() {
	    libraryInfoDTO.setStoredRoutines(routineRegistry.keySet());
	    return libraryInfoDTO;
	}

	// TODO: Lock based on rId
	public RoutineDTO getRoutine(String rId) throws ExecutionException {
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

	public RoutineBinaryDTO getRoutineBinary(String rbId) throws RoutineFetchException, ClientCommunicationException {
		// Search for RoutineBinary in cached routines
		for (RoutineDTO routine : routineRegistry.values()) {
			for (RoutineBinaryDTO routineBinary : routine.getRoutineBinaries()) {
				if (rbId.equals(routineBinary.getId())) {
					return routineBinary;
				}
			}
		}
		// Not found, fetch the requested RoutineBinary
		return fetchRoutineBinary(rbId);
	}

	public void setMasterLibraryEndpoint(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException {
		this.libraryConfiguration.setLibraryEndpoint(serviceEndpoint);
		updateLibraryEndpoint(serviceEndpoint);
	}

	public ServiceEndpointDTO getMasterLibraryEndpoint() {
		return this.libraryConfiguration.getLibraryEndpoint();
	}

	public List<FeatureDTO> getRoutineRequiredFeatures(String rId) throws ExecutionException {
		RoutineDTO routine = getRoutine(rId);
		if (routine == null) {
			throw new ExecutionException("Could not load Routine: " + rId, null);
		}

		return routine.getRequiredFeatures();
	}

	public RoutineBinaryChunkDTO getRoutineBinaryChunk(String rbId, short chunk, int chunkSize) throws RoutineFetchException, IOException, ClientCommunicationException {
		LOGGER.debug("RoutineBinaryChunk get request: {}", rbId);
		try {
			RoutineBinaryDTO routineBinary = getRoutineBinary(rbId);
			return binaryStoreDriver.readChunk(new URL(routineBinary.getUrl()), chunk, chunkSize);

		} catch (RoutineFetchException | IOException | ClientCommunicationException e) {
			LOGGER.error("Error while fetch RoutineBinaryChunk #{} (size: {}) from RoutineBinary {}.", chunk, chunkSize, rbId, e);
			throw e;
		}
	}

	public boolean verifyRoutineBinary(String rbId) throws RoutineFetchException, NoSuchAlgorithmException, ClientCommunicationException, IOException {
		try {
			RoutineBinaryDTO routineBinary = getRoutineBinary(rbId);
			long size = binaryStoreDriver.getSizeInBytes(rbId);
			String md5 = binaryStoreDriver.md5(rbId);
			boolean verifyResult = routineBinary.getSizeInBytes() == size && routineBinary.getMd5().equals(md5);
			if (!verifyResult) {
				LOGGER.warn(
						"Verify of RoutineBinary {} failed. Size: {}=={}, MD5: {}=={}.",
						rbId,
						routineBinary.getSizeInBytes(), size,
						routineBinary.getMd5(), md5
				);
			}
			return verifyResult;

		} catch (NoSuchAlgorithmException | IOException | ClientCommunicationException | RoutineFetchException e) {
			LOGGER.error("Error while verify RoutineBinary {}: {}.", rbId, e.getMessage(), e);
			throw e;
		}
	}

	protected abstract void init();

	protected abstract RoutineDTO fetchRoutine(String rId) throws RoutineFetchException;

	protected abstract RoutineBinaryDTO fetchRoutineBinary(String rbId) throws RoutineFetchException, ClientCommunicationException;

	protected abstract void updateLibraryEndpoint(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException;

    public abstract List<String> findRoutines(String pattern) throws ExecutionException;

    public abstract void removeRoutine(String rId) throws ExecutionException, IOException;

    public abstract String createRoutine(RoutineDTO routineDTO) throws ExecutionException;

	public abstract String updateRoutine(RoutineDTO routineDTO) throws ExecutionException;

	public abstract void removeRoutineBinary(String rId, String bId) throws ExecutionException;

    public abstract List<String> findDataTypes(String searchPattern) throws ExecutionException;

    public abstract String createDataType(String name, String schema) throws ExecutionException;

    public abstract DataTypeDTO getDataType(String dId) throws ExecutionException;

    public abstract void removeDataType(String dId) throws ExecutionException;

    public abstract List<TagDTO> findTags(String searchPattern) throws ExecutionException;

    public abstract void createTag(String label, String description) throws ExecutionException;

    public abstract void removeTag(String label) throws ExecutionException;

	public abstract String createFeature(String name, String group, String version) throws ExecutionException;

	public abstract String addExtension(String featureId, String name, String version) throws ExecutionException;

	public abstract List<FeatureDTO> getFeatures(String pattern) throws ExecutionException;

	public abstract String createRoutineBinary(String rId, String name, String md5, long sizeInBytes, boolean isPrimary) throws ExecutionException;

	public abstract void uploadRoutineBinaryChunk(String rbId, RoutineBinaryChunkDTO chunk) throws ExecutionException;

	public abstract FeatureDTO getFeatureByNameAndVersion(String name, String version) throws ExecutionException;
}
