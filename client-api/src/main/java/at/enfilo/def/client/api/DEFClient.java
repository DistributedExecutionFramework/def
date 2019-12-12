package at.enfilo.def.client.api;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.transfer.dto.*;
import org.apache.thrift.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Future;

class DEFClient implements IDEFClient {
	private static final TDeserializer DESERIALIZER = new TDeserializer();
	private static final TSerializer SERIALIZER = new TSerializer();
	private static final String DEFAULT_KEY = "DEFAULT";

	private final IManagerServiceClient managerServiceClient;
	private final IExecLogicServiceClient execLogicServiceClient;

	public DEFClient(IManagerServiceClient managerServiceClient, IExecLogicServiceClient execLogicServiceClient) {
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
		return execLogicServiceClient.deleteProgram(pId);
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
	@SuppressWarnings("unchecked")
	public <T extends TBase> Future<String> createSharedResource(String pId, T value) throws TException, ClientCommunicationException {
		TFieldIdEnum idField = value.fieldForId((short)1); // Convention: field with id 1 is '_id'
		String dataTypeId = value.getFieldValue(idField).toString();
		ByteBuffer bb = ByteBuffer.wrap(SERIALIZER.serialize(value));
		return createSharedResource(pId, dataTypeId, bb);
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

	private <T extends TBase> T extractValueFromResource(ResourceDTO resource, Class<T> dataType) throws ExtractDataTypeException {
		try {
			T value = dataType.newInstance();
			DESERIALIZER.deserialize(value, resource.data.array());
			return value;
		} catch (IllegalAccessException | InstantiationException | TException e) {
			throw new ExtractDataTypeException(e);
		}
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
	public JobDTO waitForJob(String pId, String jId) throws ClientCommunicationException, InterruptedException {
		return execLogicServiceClient.waitForJob(pId, jId);
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
