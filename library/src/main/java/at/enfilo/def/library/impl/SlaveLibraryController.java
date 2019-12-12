package at.enfilo.def.library.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.library.LibraryConfiguration;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.*;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SlaveLibraryController extends LibraryController {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(SlaveLibraryController.class);
	private static final LibraryServiceClientFactory LIBRARY_SERVICE_CLIENT_FACTORY = new LibraryServiceClientFactory();

	private final IBinaryStoreDriver storeDriver;
	private ILibraryServiceClient libraryServiceClient;

	public SlaveLibraryController(LibraryConfiguration configuration, IBinaryStoreDriver storeDriver) throws ClientCreationException {
		super(configuration, storeDriver);
		this.storeDriver = storeDriver;
		updateLibraryEndpoint(configuration.getLibraryEndpoint());
	}

	@Override
	protected void init() {

	}

	@Override
	protected RoutineDTO fetchRoutine(String rId) throws RoutineFetchException {
		LOGGER.info("Fetch RoutineDTO {} from MasterLibrary at {}.", rId, libraryServiceClient.getServiceEndpoint());
		try {
			RoutineDTO routine = libraryServiceClient.getRoutine(rId).get();
			for (RoutineBinaryDTO routineBinary : routine.getRoutineBinaries()) {
				String rbId = routineBinary.getId();

				if (!storeDriver.exists(rbId)) {
					LOGGER.info("RoutineBinary {} not exists locally, fetch from MasterLibrary at {}.", rbId, libraryServiceClient.getServiceEndpoint());
					fetchRoutineBinary(routineBinary);
				} else {
					// Force update url of RoutineBinary
					routineBinary.setUrl(storeDriver.getURL(rbId).toString());
					// Verify existing routine binary:
					// 1. Compare size of existing binary with the received binary info
					// 2. Compare MD5 sum
					// --> if 1./2. mismatch: pull the binary from master
					if (routineBinary.getSizeInBytes() == storeDriver.getSizeInBytes(rbId)) {
						String localMd5 = storeDriver.md5(rbId);
						if (!localMd5.equalsIgnoreCase(routineBinary.getMd5())) {
							LOGGER.info("RoutineBinary {} MD5 mismatch with local version ({} != {}), fetch from MasterLibrary at {}.", rbId, localMd5, routineBinary.getMd5(), libraryServiceClient.getServiceEndpoint());
							fetchRoutineBinary(routineBinary);
						}
					} else {
						LOGGER.info("RoutineBinary {} size mismatch with local version ({} != {}), fetch from MasterLibrary at {}.", rbId, storeDriver.getSizeInBytes(rbId), routineBinary.getSizeInBytes(), libraryServiceClient.getServiceEndpoint());
						fetchRoutineBinary(routineBinary);
					}
				}
			}

			return routine;

		} catch (ClientCommunicationException | ExecutionException e) {
			LOGGER.error("Error while fetch Routine from MasterStorage.", e);
			throw new RoutineFetchException(e);
		} catch (IOException | NoSuchAlgorithmException e) {
			LOGGER.error("Error while fetch, check or store RoutineBinary.", e);
			throw new RoutineFetchException(e);
		} catch (InterruptedException e) {
			LOGGER.error("Error while fetch Routine from MasterStorage.", e);
			Thread.currentThread().interrupt();
			throw new RoutineFetchException(e);
		}
	}

	@Override
	protected void updateLibraryEndpoint(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException {
		setLibraryServiceClient(LIBRARY_SERVICE_CLIENT_FACTORY.createClient(serviceEndpoint));
	}

	void setLibraryServiceClient(ILibraryServiceClient libraryServiceClient) {
		LOGGER.info("Update LibraryService. Endpoint {}.", libraryServiceClient.getServiceEndpoint());
		this.libraryServiceClient = libraryServiceClient;
	}

	private void fetchRoutineBinary(RoutineBinaryDTO origin) throws IOException, ClientCommunicationException, ExecutionException, InterruptedException {
		// RoutineBinary is not available or has the wrong checksum. Fetch and store it.
		RoutineBinaryDTO fetchedRoutineBinary = libraryServiceClient.getRoutineBinary(origin.getId()).get();
		URL url = storeDriver.store(fetchedRoutineBinary);

		// Update existing routineBinary
		origin.unsetData();
		origin.setUrl(url.toString());
	}

	@Override
	public List<String> findRoutines(String pattern) {
		final String p = pattern.toLowerCase();
		return routineRegistry.values().stream()
				.filter(routine -> findRoutineFilterMatcher(routine, p))
				.map(RoutineDTO::getId)
				.collect(Collectors.toList());
	}

	private boolean findRoutineFilterMatcher(RoutineDTO routineDTO, String pattern) {
		if (routineDTO.isSetName()) {
			if (routineDTO.getName().toLowerCase().contains(pattern)) {
				return true;
			}
		}
		if (routineDTO.isSetDescription()) {
			if (routineDTO.getDescription().toLowerCase().contains(pattern)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public void removeRoutine(String rId) throws PersistenceException {
		routineRegistry.remove(rId);
	}

	@Override
	public String createRoutine(RoutineDTO routineDTO) throws ExecutionException {
		String rId = UUID.randomUUID().toString();
		routineDTO.setId(rId);
		routineRegistry.put(rId, routineDTO);
		return rId;
	}

	@Override
	public String updateRoutine(RoutineDTO routine) throws ExecutionException {
		RoutineDTO newRoutine = routine.deepCopy();
		newRoutine.setRevision((short) (routine.getRevision() + 1));
		String newId = createRoutine(newRoutine);
		return newId;
	}

	@Override
	public String uploadRoutineBinary(String rId, String md5, long sizeInBytes, boolean isPrimary, ByteBuffer data) throws ExecutionException {
		RoutineBinaryDTO binary = new RoutineBinaryDTO();
		binary.setId(UUID.randomUUID().toString());
		binary.setMd5(md5);
		binary.setSizeInBytes(sizeInBytes);
		binary.setPrimary(isPrimary);
		binary.setData(data);
		try {
			URL url = storeDriver.store(binary);
			LOGGER.info("Stored RoutineBinary ({}) for Routine {} to URL {}.", binary.getId(), rId, url);
		} catch (IOException e) {
			LOGGER.error("Error while upload and store new RoutineBinary.", e);
			throw new ExecutionException(e);
		}
		return binary.getId();
	}

	@Override
	public void removeRoutineBinary(String rId, String bId) throws ExecutionException {
		try {
			RoutineDTO routine = routineRegistry.get(rId);
			Iterator<RoutineBinaryDTO> it = routine.getRoutineBinariesIterator();
			while (it.hasNext()) {
				RoutineBinaryDTO binary = it.next();
				if (binary.getId().equalsIgnoreCase(bId)) {
					it.remove();
					break;
				}
			}

			storeDriver.delete(bId);

		} catch (IOException e) {
			LOGGER.error("Error while delete RoutineBinary {}.", bId, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	// TODO lock based on bId
	public synchronized RoutineBinaryDTO getRoutineBinary(String bId) throws ExecutionException {
		try {
			if (storeDriver.exists(bId)) {
				LOGGER.debug("Fetch RoutineBinary {} from local storage.", bId);
				return storeDriver.read(bId);
			}

			LOGGER.info("RoutineBinary {} not found in local storage, fetch from MasterLibrary at {}.", bId, libraryServiceClient.getServiceEndpoint());
			RoutineBinaryDTO routineBinary = libraryServiceClient.getRoutineBinary(bId).get();
			URL url = storeDriver.store(routineBinary);
			routineBinary.setUrl(url.toString());
			return routineBinary;

		} catch (IOException | ClientCommunicationException | NoSuchAlgorithmException e) {
			LOGGER.error("Error while fetch RoutineBinary {}.", bId, e);
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			LOGGER.error("Error while fetch RoutineBinary {}.", bId, e);
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public List<String> findDataTypes(String searchPattern) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method findDataTypes not allowed in SlaveLibrary."));
	}

	@Override
	public String createDataType(String name, String schema) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method createDataType not allowed in SlaveLibrary."));
	}

	@Override
	public DataTypeDTO getDataType(String dId) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method getDataType not allowed in SlaveLibrary."));
	}

	@Override
	public void removeDataType(String dId) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method removeDataType not allowed in SlaveLibrary."));
	}

	@Override
	public List<TagDTO> findTags(String searchPattern) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method findTags not allowed in SlaveLibrary."));
	}

	@Override
	public String createTag(String label, String description) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method createTag not allowed in SlaveLibrary."));
	}

	@Override
	public void removeTag(String label) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method removeTag not allowed in SlaveLibrary."));
	}

	@Override
	public String createFeature(String name, String group, String version) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method createFeature not allowed in SlaveLibrary."));
	}

	@Override
	public String addExtension(String featureId, String name, String version) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method addExtension not allowed in SlaveLibrary."));
	}

	@Override
	public List<FeatureDTO> getFeatures(String pattern) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method getFeatures not allowed in SlaveLibrary."));
	}
}
