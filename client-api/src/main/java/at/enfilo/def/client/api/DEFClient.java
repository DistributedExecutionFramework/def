package at.enfilo.def.client.api;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.transfer.util.RoutineBinaryFactory;
import org.apache.thrift.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class DEFClient implements IDEFClient {
	private static final TDeserializer DESERIALIZER = new TDeserializer();
	private static final TSerializer SERIALIZER = new TSerializer();
	private static final String DEFAULT_KEY = "DEFAULT";
	private static final int DEFAULT_CHUNK_SIZE_BYTES = 1000 * 1000; // 1MB

	private final IManagerServiceClient managerServiceClient;
	private final IExecLogicServiceClient execLogicServiceClient;

	DEFClient(IManagerServiceClient managerServiceClient, IExecLogicServiceClient execLogicServiceClient) {
		this.managerServiceClient = managerServiceClient;
		this.execLogicServiceClient = execLogicServiceClient;
	}

	@Override
	public Future<List<String>> getAllPrograms(String userId) throws ClientCommunicationException {
		return execLogicServiceClient.getAllPrograms(userId);
	}

	@Override
	public Future<String> createProgram(String cId, String uId) throws ClientCommunicationException {
		return execLogicServiceClient.createProgram(cId, uId);
	}

	@Override
	public Future<ProgramDTO> getProgram(String pId) throws ClientCommunicationException {
		return execLogicServiceClient.getProgram(pId);
	}

	@Override
	public Future<Void> deleteProgram(String pId) throws ClientCommunicationException {
		try {
			ProgramDTO program = getProgram(pId).get();
			if (program.getClientRoutineId() != null && !program.getClientRoutineId().isEmpty()) {
				managerServiceClient.removeClientRoutine(program.getClientRoutineId()).get();
			}
			return execLogicServiceClient.deleteProgram(pId);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ClientCommunicationException("Error while deleting program", e);
		} catch (ExecutionException e) {
			throw new ClientCommunicationException("Error while deleting program", e);
		}
	}

	@Override
	public Future<Void> abortProgram(String pId) throws ClientCommunicationException {
		return execLogicServiceClient.abortProgram(pId);
	}

	@Override
	public Future<Void> updateProgramName(String pId, String name) throws ClientCommunicationException {
		return execLogicServiceClient.updateProgramName(pId, name);
	}

	@Override
	public Future<Void> updateProgramDescription(String pId, String description) throws ClientCommunicationException {
		return execLogicServiceClient.updateProgramDescription(pId, description);
	}

	@Override
	public Future<Void> startClientRoutine(String pId, String crId) throws ClientCommunicationException {
		return execLogicServiceClient.startClientRoutine(pId, crId);
	}

	@Override
	public <T extends TBase> Future<Void> attachAndStartClientRoutine(
	        String pId,
            List<Path> fileBinaries,
            List<FeatureDTO> requiredFeatures,
            List<String> arguments
        ) throws ClientCommunicationException, ExecutionException {
		try {
			// 1. Create new ClientRoutine
			String name = String.format("ClientRoutine for Program %s", pId);
			RoutineDTO routine = new RoutineDTO();
			routine.setName(name);
			routine.setRequiredFeatures(requiredFeatures);
			routine.setArguments(arguments);
			Future<String> frId = managerServiceClient.createClientRoutine(routine);
			String rId = frId.get();

			// 2. Upload Binaries
			boolean primary = true; // convention, first file is primary
			for (Path binaryFilePath : fileBinaries) {
				// 2.1. Create RoutineBinary
				File routineBinaryFile = binaryFilePath.toFile();
				RoutineBinaryDTO binary = RoutineBinaryFactory.createFromFile(routineBinaryFile, primary, routineBinaryFile.getName());
				Future<String> future = managerServiceClient.createClientRoutineBinary(
						rId,
						routineBinaryFile.getName(),
						binary.getMd5(),
						binary.getSizeInBytes(),
						binary.isPrimary()
				);
				String rbId = future.get();

				// 2.2. Upload chunks
				short chunks = RoutineBinaryFactory.calculateChunks(routineBinaryFile, DEFAULT_CHUNK_SIZE_BYTES);
				for (short c = 0; c < chunks; c++) {
					RoutineBinaryChunkDTO chunk = RoutineBinaryFactory.readChunk(routineBinaryFile, c, DEFAULT_CHUNK_SIZE_BYTES);
					Future<Void> f = managerServiceClient.uploadClientRoutineBinaryChunk(rbId, chunk);
					f.get(); // wait for upload
				}
			}

			// 3. Attach and start client routine
			return execLogicServiceClient.startClientRoutine(pId, rId);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ClientCommunicationException(e);
		} catch (IOException | ExecutionException e) {
			throw new ClientCommunicationException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new ExecutionException(e);
		}

	}

	@Override
	public Future<Void> markProgramAsFinished(String pId) throws ClientCommunicationException {
		return execLogicServiceClient.markProgramAsFinished(pId);
	}

	@Override
	public Future<List<String>> getAllJobs(String pId) throws ClientCommunicationException {
		return execLogicServiceClient.getAllJobs(pId);
	}

	@Override
	public Future<String> createJob(String pId) throws ClientCommunicationException {
		return execLogicServiceClient.createJob(pId);
	}

	@Override
	public Future<JobDTO> getJob(String pId, String jId) throws ClientCommunicationException {
		return execLogicServiceClient.getJob(pId, jId);
	}

	@Override
	public Future<Void> deleteJob(String pId, String jId) throws ClientCommunicationException {
		return execLogicServiceClient.deleteJob(pId, jId);
	}

	@Override
	public Future<String> getAttachedMapRoutine(String pId, String jId) throws ClientCommunicationException {
		return execLogicServiceClient.getAttachedMapRoutine(pId, jId);
	}

	@Override
	public Future<Void> attachMapRoutine(String pId, String jId, String mapRoutineId) throws ClientCommunicationException {
		return execLogicServiceClient.attachMapRoutine(pId, jId, mapRoutineId);
	}

	@Override
	public Future<String> getAttachedReduceRoutine(String pId, String jId) throws ClientCommunicationException {
		return execLogicServiceClient.getAttachedReduceRoutine(pId, jId);
	}

	@Override
	public Future<Void> attachReduceRoutine(String pId, String jId, String reduceRoutineId) throws ClientCommunicationException {
		return execLogicServiceClient.attachReduceRoutine(pId, jId, reduceRoutineId);
	}

	@Override
	public Future<List<String>> getAllTasks(String pId, String jId, SortingCriterion sortingCriterion) throws ClientCommunicationException {
		return execLogicServiceClient.getAllTasks(pId, jId, sortingCriterion);
	}

	@Override
	public Future<List<String>> getAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion) throws ClientCommunicationException {
		return execLogicServiceClient.getAllTasksWithState(pId, jId, state, sortingCriterion);
	}

	@Override
	public Future<String> createTask(String pId, String jId, RoutineInstanceDTO routineInstance) throws ClientCommunicationException {
		return execLogicServiceClient.createTask(pId, jId, routineInstance);
	}

	@Override
	public Future<TaskDTO> getTask(String pId, String jId, String tId) throws ClientCommunicationException {
		return execLogicServiceClient.getTask(pId, jId, tId);
	}

	@Override
	public Future<TaskDTO> getTask(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters) throws ClientCommunicationException {
		return execLogicServiceClient.getTask(pId, jId, tId, includeInParameters, includeOutParameters);
	}

	public Object extractOutParameterRaw(TaskDTO task, Class dataType, String key) throws ExtractDataTypeException {
		return extractOutParameter(task, dataType, key);
	}

	@Override
	public <T extends TBase> T extractOutParameter(TaskDTO task, Class<T> dataType) throws ExtractDataTypeException {
		return extractOutParameter(task, dataType, DEFAULT_KEY);
	}

	@Override
	public <T extends TBase> T extractOutParameter(TaskDTO task, Class<T> dataType, String key) throws ExtractDataTypeException {
		for (ResourceDTO resource : task.getOutParameters()) {
			if (key.equalsIgnoreCase(resource.getKey())) {
				return extractValueFromResource(resource, dataType);
			}
		}
		throw new ExtractDataTypeException("Parameter not found with key " + key);
	}

	@Override
	public <T extends TBase> T extractReducedResult(JobDTO job, Class<T> dataType) throws ExtractDataTypeException {
		return extractReducedResult(job, dataType, DEFAULT_KEY);
	}

	public Object extractReducedResultRaw(JobDTO job, Class dataType, String key) throws ExtractDataTypeException {
		return extractReducedResult(job, dataType, key);
	}

	@Override
	public <T extends TBase> Future<String> createSharedResource(String pId, T value) throws TException, ClientCommunicationException {
		ByteBuffer bb = ByteBuffer.wrap(SERIALIZER.serialize(value));
		return createSharedResource(pId, extractDataTypeId(value), bb);
	}

	@Override
	public <T extends TBase> T extractReducedResult(JobDTO job, Class<T> dataType, String key) throws ExtractDataTypeException {
		for (ResourceDTO resource : job.getReducedResults()) {
			if (key.equalsIgnoreCase(resource.getKey())) {
				return extractValueFromResource(resource, dataType);
			}
		}
		throw new ExtractDataTypeException("Result not found with key " + key);
	}

    @Override
    public <T extends TBase> T extractResult(ProgramDTO program, Class<T> dataType, String key) throws ExtractDataTypeException {
        for (Map.Entry<String, ResourceDTO> entry: program.getResults().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return extractValueFromResource(entry.getValue(), dataType);
            }
        }
        throw new ExtractDataTypeException(String.format("Result not found with key %s", key));
    }

    @Override
    public <T extends TBase> Map<String, T> extractResults(ProgramDTO program, Class<T> dataType) throws ExtractDataTypeException {
        Map<String, T> results = new HashMap<>();
        for (Map.Entry<String, ResourceDTO> entry: program.getResults().entrySet()) {
            results.put(entry.getKey(), extractValueFromResource(entry.getValue(), dataType));
        }
        return results;
    }

    private <T extends TBase> T extractValueFromResource(ResourceDTO resource, Class<T> dataType) throws ExtractDataTypeException {
		try {
			T value = dataType.newInstance();
			DESERIALIZER.deserialize(value, resource.data.array());
			return value;
		} catch (IllegalAccessException | InstantiationException | TException e) {
			throw new ExtractDataTypeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private String extractDataTypeId(TBase dataType) {
		TFieldIdEnum idField = dataType.fieldForId((short) 1); // Convention: field with id 1 is '_id'
		return dataType.getFieldValue(idField).toString();
	}

	@Override
	public Future<Void> markJobAsComplete(String pId, String jId) throws ClientCommunicationException {
		return execLogicServiceClient.markJobAsComplete(pId, jId);
	}

	@Override
	public Future<Void> abortJob(String pId, String jId) throws ClientCommunicationException {
		return execLogicServiceClient.abortJob(pId, jId);
	}

	@Override
	public Future<Void> abortTask(String pId, String jId, String tId) throws ClientCommunicationException {
		return execLogicServiceClient.abortTask(pId, jId, tId);
	}

	@Override
	public Future<Void> reRunTask(String pId, String jId, String tId) throws ClientCommunicationException {
		return execLogicServiceClient.reRunTask(pId, jId, tId);
	}

	@Override
	public Future<List<String>> getAllSharedResources(String pId) throws ClientCommunicationException {
		return execLogicServiceClient.getAllSharedResources(pId);
	}

	@Override
	public Future<String> createSharedResource(String pId, String dataTypeId, ByteBuffer data) throws ClientCommunicationException {
		return execLogicServiceClient.createSharedResource(pId, dataTypeId, data);
	}

	@Override
	public Future<ResourceDTO> getSharedResource(String pId, String rId) throws ClientCommunicationException {
		return execLogicServiceClient.getSharedResource(pId, rId);
	}

	@Override
	public Future<Void> deleteSharedResource(String pId, String rId) throws ClientCommunicationException {
		return execLogicServiceClient.deleteSharedResource(pId, rId);
	}

	@Override
	public Future<List<String>> getClusterIds() throws ClientCommunicationException {
		return managerServiceClient.getClusterIds();
	}

	@Override
	public Future<ClusterInfoDTO> getClusterInfo(String cId) throws ClientCommunicationException {
		return managerServiceClient.getClusterInfo(cId);
	}

	@Override
	public Future<ServiceEndpointDTO> getClusterEndpoint(String cId) throws ClientCommunicationException {
		return managerServiceClient.getClusterEndpoint(cId);
	}

	@Override
	public Future<String> createAWSCluster(int numberOfWorkers, int numberOfReducers, AWSSpecificationDTO awsSpecification) throws ClientCommunicationException {
		return managerServiceClient.createAWSCluster(numberOfWorkers, numberOfReducers, awsSpecification);
	}

	@Override
	public Future<Void> addCluster(ServiceEndpointDTO endpoint) throws ClientCommunicationException {
		return managerServiceClient.addCluster(endpoint);
	}

	@Override
	public Future<Void> deleteCluster(String cId) throws ClientCommunicationException {
		return managerServiceClient.deleteCluster(cId);
	}

	@Override
	public Future<Void> adjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType) throws ClientCommunicationException {
		return managerServiceClient.adjustNodePoolSize(cId, newNodePoolSize, nodeType);
	}

	@Override
	public Future<String> createClientRoutine(RoutineDTO routine) throws ClientCommunicationException {
		return managerServiceClient.createClientRoutine(routine);
	}

	@Override
	public Future<String> createClientRoutineBinary(String rId, String binaryName, String md5, long sizeInBytes, boolean isPrimary) throws ClientCommunicationException {
		return managerServiceClient.createClientRoutineBinary(rId, binaryName, md5, sizeInBytes, isPrimary);
	}

	@Override
	public Future<Void> uploadClientRoutineBinaryChunk(String rbId, RoutineBinaryChunkDTO chunk) throws ClientCommunicationException {
		return managerServiceClient.uploadClientRoutineBinaryChunk(rbId, chunk);
	}

	@Override
	public Future<Void> removeClientRoutine(String rId) throws ClientCommunicationException {
		return managerServiceClient.removeClientRoutine(rId);
	}

	@Override
	public Future<FeatureDTO> getFeatureByNameAndVersion(String name, String version) throws ClientCommunicationException {
		return managerServiceClient.getFeatureByNameAndVersion(name, version);
	}

	@Override
	public JobDTO waitForJob(String pId, String jId) throws ClientCommunicationException, InterruptedException {
		return execLogicServiceClient.waitForJob(pId, jId);
	}

	@Override
	public ProgramDTO waitForProgram(String pId) throws ClientCommunicationException, InterruptedException {
		return execLogicServiceClient.waitForProgram(pId);
	}

	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return managerServiceClient.getServiceEndpoint();
	}

	@Override
	public void close() {
		managerServiceClient.close();
		execLogicServiceClient.close();
	}
}
