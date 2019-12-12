package at.enfilo.def.library.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.library.LibraryConfiguration;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryAdminServiceClientFactory;
import at.enfilo.def.transfer.util.RoutineBinaryFactory;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SlaveLibraryController extends LibraryController {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(SlaveLibraryController.class);
	static final int CHUNK_SIZE = 1000 * 1000; // 1MB
	private static final LibraryAdminServiceClientFactory LIBRARY_SERVICE_CLIENT_FACTORY = new LibraryAdminServiceClientFactory();

	private final Object routineBinaryLock;
	private ILibraryAdminServiceClient masterLibrary;

	public SlaveLibraryController(LibraryConfiguration configuration, IBinaryStoreDriver binaryStoreDriver) throws ClientCreationException {
		super(configuration, binaryStoreDriver);
		this.routineBinaryLock = new Object();
		updateLibraryEndpoint(configuration.getLibraryEndpoint());
	}

	@Override
	protected void init() {

	}

	@Override
	protected RoutineDTO fetchRoutine(String rId) throws RoutineFetchException {
		LOGGER.info("Fetch RoutineDTO {} from MasterLibrary at {}.", rId, masterLibrary.getServiceEndpoint());
		try {
			RoutineDTO routine = masterLibrary.getRoutine(rId).get();
			synchronized (routineBinaryLock) {
				for (RoutineBinaryDTO routineBinary : routine.getRoutineBinaries()) {
					String rbId = routineBinary.getId();

					if (!binaryStoreDriver.exists(rbId)) {
						LOGGER.info("RoutineBinary {} not exists locally, fetch from MasterLibrary at {}.", rbId, masterLibrary.getServiceEndpoint());
						binaryStoreDriver.create(rId, routineBinary);
						fetchRoutineBinaryChunks(routineBinary);
					} else {
						// Verify existing routine binary:
						// 1. Compare size of existing binary with the received binary info
						// 2. Compare MD5 sum
						// --> if 1./2. mismatch: pull the binary (chunks) from master
						if (routineBinary.getSizeInBytes() == binaryStoreDriver.getSizeInBytes(rbId)) {
							String localMd5 = binaryStoreDriver.md5(rbId);
							if (!localMd5.equalsIgnoreCase(routineBinary.getMd5())) {
								LOGGER.info("RoutineBinary {} MD5 mismatch with local version ({} != {}), fetch from MasterLibrary at {}.", rbId, localMd5, routineBinary.getMd5(), masterLibrary.getServiceEndpoint());
								fetchRoutineBinaryChunks(routineBinary);
							}
						} else {
							LOGGER.info("RoutineBinary {} size mismatch with local version ({} != {}), fetch from MasterLibrary at {}.", rbId, binaryStoreDriver.getSizeInBytes(rbId), routineBinary.getSizeInBytes(), masterLibrary.getServiceEndpoint());
							fetchRoutineBinaryChunks(routineBinary);
						}
					}
					// Force update url's of RoutineBinary
					routineBinary.setUrl(binaryStoreDriver.getFileURL(rbId).toString());
					routineBinary.setExecutionUrl(binaryStoreDriver.getExecutionURL(rId, rbId, routineBinary.getName()).toString());
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
	protected RoutineBinaryDTO fetchRoutineBinary(String rbId) throws RoutineFetchException {
		try {
			LOGGER.info("Fetch RoutineBinary {} from MasterLibrary at {}.", rbId, masterLibrary.getServiceEndpoint());
			RoutineBinaryDTO routineBinary = masterLibrary.getRoutineBinary(rbId).get();
			fetchRoutineBinaryChunks(routineBinary);
			return routineBinary;

		} catch (InterruptedException e) {
			LOGGER.error("Interrupted while fetch RoutineBinary {} from MasterLibrary at {}.", rbId, masterLibrary, e);
			Thread.currentThread().interrupt();
			throw new RoutineFetchException(e);
		} catch (ExecutionException | IOException | ClientCommunicationException e) {
			LOGGER.error("Error while fetch RoutineBinary {} from MasterLibrary at {}.", rbId, masterLibrary, e);
			throw new RoutineFetchException(e);
		}
	}

	@Override
	protected void updateLibraryEndpoint(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException {
		setMasterLibrary(LIBRARY_SERVICE_CLIENT_FACTORY.createClient(serviceEndpoint));
	}

	void setMasterLibrary(ILibraryAdminServiceClient masterLibrary) {
		LOGGER.info("Update LibraryService. Endpoint {}.", masterLibrary.getServiceEndpoint());
		this.masterLibrary = masterLibrary;
	}

	private void fetchRoutineBinaryChunks(RoutineBinaryDTO routineBinary) throws IOException, ExecutionException, InterruptedException, ClientCommunicationException {
		// RoutineBinary is not available or has the wrong checksum. Fetch and store it.
		// Fetch data (chunks)
		short chunks = RoutineBinaryFactory.calculateChunks(routineBinary, CHUNK_SIZE);
		for (short c = 0; c < chunks; c++) {
			LOGGER.debug("Fetch Chunk #{} ({} Bytes) for RoutineBinary {} from MasterLibrary at {}.", c, CHUNK_SIZE, routineBinary.getId(), masterLibrary.getServiceEndpoint());
			RoutineBinaryChunkDTO rbc = masterLibrary.getRoutineBinaryChunk(routineBinary.getId(), c, CHUNK_SIZE).get();
			binaryStoreDriver.storeChunk(routineBinary.getId(), rbc);
		}
		LOGGER.debug("Successfully fetched all Chunks for RoutineBinary {}.", routineBinary.getId());
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
		return routineDTO.isSetDescription() && routineDTO.getDescription().toLowerCase().contains(pattern);
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
		return createRoutine(newRoutine);
	}

	@Override
	public void removeRoutineBinary(String rId, String rbId) throws ExecutionException {
		try {
			RoutineDTO routine = routineRegistry.get(rId);
			Iterator<RoutineBinaryDTO> it = routine.getRoutineBinariesIterator();
			while (it.hasNext()) {
				RoutineBinaryDTO binary = it.next();
				if (binary.getId().equalsIgnoreCase(rbId)) {
					it.remove();
					binaryStoreDriver.delete(rId, rbId, binary.getName());
					break;
				}
			}


		} catch (IOException e) {
			LOGGER.error("Error while delete RoutineBinary {}.", rbId, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public String createRoutineBinary(String rId, String name, String md5, long sizeInBytes, boolean isPrimary) throws ExecutionException {
		try {
			LOGGER.debug("Delegate createRoutineBinary() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			return masterLibrary.createRoutineBinary(rId, name, md5, sizeInBytes, isPrimary).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public void uploadRoutineBinaryChunk(String rbId, RoutineBinaryChunkDTO chunk) throws ExecutionException {
		try {
			LOGGER.debug("Delegate uploadRoutineBinaryChunk() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			masterLibrary.uploadRoutineBinaryChunk(rbId, chunk).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public List<String> findDataTypes(String searchPattern) throws ExecutionException {
		try {
			LOGGER.debug("Delegate findDataTypes() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			return masterLibrary.findDataTypes(searchPattern).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public String createDataType(String name, String schema) throws ExecutionException {
		try {
			LOGGER.debug("Delegate createDataType() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			return masterLibrary.createDataType(name, schema).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public DataTypeDTO getDataType(String dId) throws ExecutionException {
		try {
			LOGGER.debug("Delegate getDataType() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			return masterLibrary.getDataType(dId).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public void removeDataType(String dId) throws ExecutionException {
		try {
			LOGGER.debug("Delegate removeDataType() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			masterLibrary.removeDataType(dId).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public List<TagDTO> findTags(String searchPattern) throws ExecutionException {
		try {
			LOGGER.debug("Delegate findTags() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			return masterLibrary.findTags(searchPattern).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public void createTag(String label, String description) throws ExecutionException {
		try {
			LOGGER.debug("Delegate createTag() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			masterLibrary.createTag(label, description).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public void removeTag(String label) throws ExecutionException {
		try {
			LOGGER.debug("Delegate removeTag() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			masterLibrary.removeTag(label).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public String createFeature(String name, String group, String version) throws ExecutionException {
		try {
			LOGGER.debug("Delegate createFeature() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			return masterLibrary.createFeature(name, group, version).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public String addExtension(String featureId, String name, String version) throws ExecutionException {
		try {
			LOGGER.debug("Delegate addExtension() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			return masterLibrary.addExtension(featureId, name, version).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public List<FeatureDTO> getFeatures(String pattern) throws ExecutionException {
		try {
			LOGGER.debug("Delegate getFeatures() to MasterLibrary at {}.", masterLibrary.getServiceEndpoint().getHost());
			return masterLibrary.getFeatures(pattern).get();

		} catch (ClientCommunicationException e) {
			throw new ExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(e);
		}
	}

	@Override
	public FeatureDTO getFeatureByNameAndVersion(String name, String version) throws ExecutionException {
		throw new ExecutionException(new IllegalAccessException("Method getFeatureByNameAndVersion not allowed in SlaveLibrary."));
	}
}
