package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryAdminServiceClientFactory;
import at.enfilo.def.manager.webservice.rest.ILibraryService;
import at.enfilo.def.manager.webservice.rest.ManagerWebserviceException;
import at.enfilo.def.manager.webservice.server.ManagerWebservice;
import at.enfilo.def.manager.webservice.util.ManagerWebserviceConfiguration;
import at.enfilo.def.transfer.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LibraryServiceImpl implements ILibraryService {

    /**
     * Logger for logging activities of instances of this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryServiceImpl.class);
	private static final Map<String, String> THRIFT_LANGUAGE_MAPPING;

	static {
		THRIFT_LANGUAGE_MAPPING = new HashMap<>();
		THRIFT_LANGUAGE_MAPPING.put("c#", "csharp");
		THRIFT_LANGUAGE_MAPPING.put("python", "py");
		THRIFT_LANGUAGE_MAPPING.put("python3", "py");
		THRIFT_LANGUAGE_MAPPING.put("matlab", "java");
	}

    private final ManagerWebserviceConfiguration configuration;
    private ILibraryAdminServiceClient libraryServiceClient;

    public LibraryServiceImpl() {
        configuration = ManagerWebservice.getInstance().getConfiguration();
        init();
    }

    /**
     * Initializing of all needed components
     */
    private void init() {

        // Create a library service endpoint
        try {
            libraryServiceClient = new LibraryAdminServiceClientFactory().createClient(configuration.getLibraryEndpoint());
        } catch (ClientCreationException e) {
            LOGGER.error("Error initializing ClusterServiceClient", e);
        }
    }

    @Override
    public List<RoutineDTO> findRoutines(String pattern) throws ManagerWebserviceException {
		List<RoutineDTO> routines = new LinkedList<>();
    	try {
    		// Fetch all routine ids which match the given pattern
			List<String> routineIds = libraryServiceClient.findRoutines(pattern).get();
			// Fetch RoutineDTOs for given routines
			for (String rId : routineIds) {
				routines.add(libraryServiceClient.getRoutine(rId).get());
			}
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
    		LOGGER.error("Error while find routines.", e);
    		throw new ManagerWebserviceException(e);
		}
		return routines;
    }

	@Override
	public RoutineDTO getRoutine(String rId) throws ManagerWebserviceException {
    	try {
    		RoutineDTO routine = libraryServiceClient.getRoutine(rId).get();
    		return routine;
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while get routine.", e);
			throw new ManagerWebserviceException(e);
		}
	}

	@Override
    public List<TagDTO> findTags(String pattern) {
        return null;
    }

    @Override
    public List<DataTypeDTO> findDataTypes(String pattern) throws ManagerWebserviceException {
		try {
			List<DataTypeDTO> dataTypes = new LinkedList<>();
			// Fetch all data type ids which match the given pattern
			List<String> dataTypeIds = libraryServiceClient.findDataTypes(pattern).get();
			// Fetch DataTypeDTOs for given ids
			for (String dId : dataTypeIds) {
				dataTypes.add(libraryServiceClient.getDataType(dId).get());
			}
			return dataTypes;
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while search DataTypes.", e);
			throw new ManagerWebserviceException(e);
		}
    }

	@Override
	public DataTypeDTO getDataType(String dId) throws ManagerWebserviceException {
    	try {
			DataTypeDTO dataType = libraryServiceClient.getDataType(dId).get();
			return dataType;
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while fetch DataType.", e);
			throw new ManagerWebserviceException(e);
		}
	}

	@Override
    public String createDataType(String name, String schema) throws ManagerWebserviceException {
    	try {
    		String dId = libraryServiceClient.createDataType(name, schema).get();
    		return dId;
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
    		LOGGER.error("Error while create DataType.", e);
    		throw new ManagerWebserviceException(e);
		}
	}

	@Override
	public String createRoutine(RoutineDTO routine) throws ManagerWebserviceException {
    	LOGGER.debug("Try to create Routine: id: {}, name: {}.", routine.getId(), routine.getName());
		try {
			String result = libraryServiceClient.createRoutine(routine).get();
			LOGGER.info("Routine (id: {}, name: {}) created.", routine.getId(), routine.getName());
			return result;
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while create Routine: ", e);
			throw new ManagerWebserviceException(e);
		}
	}

	@Override
	public String createRoutineBinary(String rId, String name, String md5, Boolean primary, Long sizeInBytes) throws ManagerWebserviceException {
		LOGGER.debug("Try to create RoutineBinary: RoutineId: {}, name: {}, size: {}, md5: {}.", rId, name, sizeInBytes, md5);
		try {
			String result = libraryServiceClient.createRoutineBinary(
					rId,
					name,
					md5,
					sizeInBytes,
					primary
			).get();
			LOGGER.info("RoutineBinary (id: {}, size: {}, md5: {}) created for Routine {}.", result, sizeInBytes, md5, rId);
			return result;
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while create RoutineBinary: ", e);
			throw new ManagerWebserviceException(e);
		}
	}

	@Override
	public void uploadRoutineBinaryChunk(String rbId, short chunk, short totalChunks, int chunkSize, byte[] data) throws ManagerWebserviceException {
		LOGGER.debug("Try to upload RoutineBinaryChunk. RoutineBinaryId: {}, Chunk #{}/{} size: {}.", rbId, chunk, totalChunks, chunkSize);
		try {
			RoutineBinaryChunkDTO chunkDTO = new RoutineBinaryChunkDTO(
				chunk,
				totalChunks,
				chunkSize,
				ByteBuffer.wrap(data)
			);
			Future<Void> future = libraryServiceClient.uploadRoutineBinaryChunk(rbId, chunkDTO);
			future.get(); // Wait for ticket.
			LOGGER.info("RoutineBinaryChunk (RoutineBinaryId: {}, Chunk #{}/{} size: {}) upload done.",  rbId, chunk, totalChunks, chunkSize);
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while upload RoutineBinaryChunk: ", e);
			throw new ManagerWebserviceException(e);
		}
	}

	@Override
	public byte[] generateDataTypes(String language, String[] dIds) throws ManagerWebserviceException {
    	// Update language parameter for thrift compiler
    	language = language.toLowerCase();
    	if (THRIFT_LANGUAGE_MAPPING.containsKey(language)) {
    		language = THRIFT_LANGUAGE_MAPPING.get(language);
		}

		LOGGER.debug("Try to generate DataTypes ({}) for Language {}.", Arrays.toString(dIds), language);

		try {
			Path dir = Files.createTempDirectory(language);
			LOGGER.debug("Created temp directory for generation: {}.", dir.toString());

			// Fetch all DataTypes
			List<Future<DataTypeDTO>> dataTypes = new LinkedList<>();
			for (String dId : dIds) {
				Future<DataTypeDTO> dataType = libraryServiceClient.getDataType(dId);
				dataTypes.add(dataType);
			}

			Path allSchemasFilePath = dir.resolve("allDataTypes.thrift");
			boolean firstDataType = true;
			try (FileWriter allSchemasWriter = new FileWriter(allSchemasFilePath.toFile())) {
				for (Future<DataTypeDTO> fDataType : dataTypes) {
				// Create a schema file for each DataType
					DataTypeDTO dataType = fDataType.get();
					Path schemaFilePath = dir.resolve(dataType.getName() + ".thrift");
					if (Files.exists(schemaFilePath)) {
						continue; // Avoid multiple fetching of the same DataType.
					}
					Path schemaFile = Files.createFile(schemaFilePath);
					try (FileWriter fileWriter = new FileWriter(schemaFile.toFile())) {
						fileWriter.write(dataType.getSchema());
					}

					if (!firstDataType) {
						String allContent = dataType.getSchema()
								.replaceAll("namespace (java|py) [a-zA-Z0-9._]+", "")
								.replaceAll("typedef string Id", "");
						allSchemasWriter.write(allContent);
					} else {
						allSchemasWriter.write(dataType.getSchema());
						firstDataType = false;
					}
				}
			}
			String[] cmd = new String[]{"thrift", "-o", dir.toAbsolutePath().toString(), "--gen", language, allSchemasFilePath.toAbsolutePath().toString()};
			LOGGER.debug("Thrift process: {}", Arrays.toString(cmd));
			Process thriftProcess = new ProcessBuilder()
					.command(cmd)
					.start();
			int exitValue = thriftProcess.waitFor();
			if (exitValue > 0) {
				LOGGER.warn("Thrift generation for all DataTypes failed.");
			}

			Path zipFile = Paths.get(dir.toAbsolutePath().toString() + ".zip");
			cmd = new String[] {"zip", "-r", zipFile.getFileName().toString(), dir.getFileName().toString()};
			LOGGER.debug("Zip process: {}", Arrays.toString(cmd));
			Process zipProcess = new ProcessBuilder()
					.directory(dir.getParent().toAbsolutePath().toFile())
					.command(cmd)
					.start();
			exitValue = zipProcess.waitFor();
			if (exitValue > 0) {
				LOGGER.warn("Zip process failed.");
				return new byte[0];
			}

			byte[] zipData = Files.readAllBytes(zipFile);
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
			Files.delete(zipFile);
			return zipData;

		} catch (IOException | InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while generating DataTypes for given Language.", e);
			throw new ManagerWebserviceException(e);
		}
	}

	@Override
	public List<FeatureDTO> getFeatures(String pattern) throws ManagerWebserviceException {
		try {
			return libraryServiceClient.getFeatures(pattern).get();
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while getting features.", e);
			throw new ManagerWebserviceException(e);
		}
	}

	@Override
	public String createFeature(String name, String group, String version) throws ManagerWebserviceException {
		LOGGER.debug("Trying to create feature: name: {}, group: {}, version: {}.", name, group, version);
		try {
			String result = libraryServiceClient.createFeature(name, group, version).get();
			LOGGER.info("Feature (name: {}, group: {}, version: {}) created.", name, group, version);
			return result;
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while creating Feature: ", e);
			throw new ManagerWebserviceException(e);
		}
	}

	@Override
	public void removeRoutine(String rId) throws ManagerWebserviceException {
		LOGGER.debug("Trying to remove routine: {}.", rId);
		try {
			Future<Void> f = libraryServiceClient.removeRoutine(rId);
			f.get();
			LOGGER.info("Routine {} removed successfully.", rId);
		} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
			LOGGER.error("Error while remove Routine {}.", rId, e);
			throw new ManagerWebserviceException(e);
		}
	}

	@Override
	public String addExtension(String featureId, String name, String version) throws ManagerWebserviceException {
		LOGGER.debug("Trying to add extension to feature id: {} with name: {}, version: {}.", featureId, name, version);
		try {
			String result = libraryServiceClient.addExtension(featureId, name, version).get();
			LOGGER.info("Extension (name: {}, featureId: {}, version: {}) created.", name, featureId, version);
			return result;
		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error while creating Extension: ", e);
			throw new ManagerWebserviceException(e);
		}
	}
}
