package at.enfilo.def.client.api;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface IDEFClient extends IExecLogicServiceClient, IManagerServiceClient {
	<T extends TBase> T extractOutParameter(TaskDTO task, Class<T> dataType) throws ExtractDataTypeException;
	<T extends TBase> T extractOutParameter(TaskDTO task, Class<T> dataType, String key) throws ExtractDataTypeException;
	<T extends TBase> T extractReducedResult(JobDTO job, Class<T> dataType) throws ExtractDataTypeException;
	<T extends TBase> T extractReducedResult(JobDTO job, Class<T> dataType, String key) throws ExtractDataTypeException;
	<T extends TBase> T extractResult(ProgramDTO program, Class<T> dataType, String key) throws ExtractDataTypeException;
	<T extends TBase> Map<String, T> extractResults(ProgramDTO program, Class<T> dataType) throws ExtractDataTypeException;
	<T extends TBase> Future<String> createSharedResource(String pId, T data) throws TException, ClientCommunicationException;
	<T extends TBase> Future<Void> attachAndStartClientRoutine(String pId, List<Path> fileBinaries, List<FeatureDTO> requiredFeatures, List<String> arguments) throws ClientCommunicationException, ExecutionException;
}
