package at.enfilo.def.client.shell;

import at.enfilo.def.client.shell.formatter.ShellOutputFormatter;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.domain.entity.RoutineBinary;
import at.enfilo.def.library.api.util.RoutineBinaryFactory;
import at.enfilo.def.transfer.dto.DataTypeDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.dto.TagDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static at.enfilo.def.client.shell.Constants.*;

@Component
public class LibraryCommands implements CommandMarker {

    private static final long DEFAULT_CHUNK_SIZE_BYTES = 32L * 1024L * 1024L; // 32 MB

	@Autowired
	private DEFShellSession session;
	@Autowired
	private ObjectCommands objects;


	@CliAvailabilityIndicator({
			CMD_LIBRARY_ROUTINE_SHOW,
			CMD_LIBRARY_ROUTINE_FIND,
			CMD_LIBRARY_ROUTINE_REMOVE,
			CMD_LIBRARY_ROUTINE_CREATE,
			CMD_LIBRARY_ROUTINE_UPDATE,
			CMD_LIBRARY_ROUTINE_BINARY_UPLOAD,
			CMD_LIBRARY_ROUTINE_BINARY_REMOVE,
			CMD_LIBRARY_DATA_TYPE_FIND,
			CMD_LIBRARY_DATA_TYPE_CREATE,
			CMD_LIBRARY_DATA_TYPE_SHOW,
			CMD_LIBRARY_DATA_TYPE_REMOVE,
			CMD_LIBRARY_TAG_FIND,
			CMD_LIBRARY_TAG_CREATE,
			CMD_LIBRARY_TAG_REMOVE,
			CMD_LIBRARY_SET_DATA_ENDPOINT
	})
	public boolean isLibraryServiceActive() {
		return session.getActiveService() == Service.LIBRARY;
	}


	@CliCommand(value = CMD_LIBRARY_ROUTINE_SHOW, help = "Show Routine")
	public String getRoutine(
		@CliOption(key = OPT_ROUTINE_ID, mandatory = true, help = "Routine Id") final String rId,
		@CliOption(key = OPT_TO_OBJECT, help = "Store Routine to an object with given name") final String objectName
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<RoutineDTO> futureRoutine = session.getLibraryServiceClient().getRoutine(rId);
		RoutineDTO routine = futureRoutine.get();

		if (objectName == null) {
			return ShellOutputFormatter.format(routine);
		} else {
			objects.getObjectMap().put(objectName, routine);
			return String.format(MESSAGE_OBJECT_STORED, objectName);
		}
	}


	@CliCommand(value = CMD_LIBRARY_ROUTINE_FIND, help = "Find Routine by name or description")
	public String findRoutines(
		@CliOption(key = OPT_SEARCH_PATTERN, mandatory = true, help = "Pattern to match") final String searchPattern
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<String>> future = session.getLibraryAdminServiceClient().findRoutines(searchPattern);
		List<String> rIds = future.get();

		return ShellOutputFormatter.format(rIds);
	}

	@CliCommand(value = CMD_LIBRARY_ROUTINE_REMOVE, help = "Remove a Routine from Library")
	public String removeRoutine(
		@CliOption(key = OPT_ROUTINE_ID, mandatory = true, help = "Routine Id") final String rId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getLibraryAdminServiceClient().removeRoutine(rId).get();
		return MESSAGE_LIBRARY_ROUTINE_REMOVE;
	}

	@CliCommand(value = CMD_LIBRARY_ROUTINE_CREATE, help = "Create a Routine at Library")
	public String createRoutine(
		@CliOption(key = OPT_ROUTINE, mandatory = true, help = "Name of Routine Object") final String routineObject
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		RoutineDTO routine = objects.getObject(routineObject, RoutineDTO.class);
		Future<String> future = session.getLibraryAdminServiceClient().createRoutine(routine);
		return String.format(MESSAGE_LIBRARY_ROUTINE_CREATED, future.get());
	}

	@CliCommand(value = CMD_LIBRARY_ROUTINE_UPDATE, help = "Update Routine Metadata")
	public String updateRoutine(
		@CliOption(key = OPT_ROUTINE, mandatory = true, help = "Name of Routine Object") final String routineObject
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		RoutineDTO routine = objects.getObject(routineObject, RoutineDTO.class);
		Future<String> future = session.getLibraryAdminServiceClient().updateRoutine(routine);
		return String.format(MESSAGE_LIBRARY_ROUTINE_UPDATED, future.get());
	}

	@CliCommand(value = CMD_LIBRARY_ROUTINE_BINARY_UPLOAD, help = "Upload a RoutineBinary to Library and link it to given Routine")
	public String uploadRoutineBinary(
		@CliOption(key = OPT_ROUTINE_ID, mandatory = true, help = "Routine Id") final String rId,
		@CliOption(key = OPT_FILE_NAME, mandatory = true, help = "File to upload") final String fileName,
		@CliOption(key = OPT_PRIMARY, unspecifiedDefaultValue = "false", help = "Is File primary") final boolean isPrimary
	) throws NoSuchAlgorithmException, IOException, ClientCommunicationException, ExecutionException, InterruptedException {

		File routineBinaryFile = new File(fileName);
		if (!routineBinaryFile.exists()) {
			throw new FileNotFoundException(fileName);
		}
		RoutineBinaryFactory routineBinaryFactory = new RoutineBinaryFactory();
		RoutineBinary binary = routineBinaryFactory.createRoutineBinary(routineBinaryFile, isPrimary);
		Future<String> future = session.getLibraryAdminServiceClient().uploadRoutineBinary(
				rId,
				binary.getMd5(),
				binary.getSizeInBytes(),
				binary.isPrimary(),
				ByteBuffer.wrap(binary.getData())
		);

		return String.format(MESSAGE_LIBRARY_ROUTINE_BINARY_UPLOAD, future.get());
	}

	@CliCommand(value = CMD_LIBRARY_ROUTINE_BINARY_REMOVE, help = "Remove a RoutineBinary from Routine and Library")
	public String removeRoutineBinary(
			@CliOption(key = OPT_ROUTINE_ID, mandatory = true, help = "Routine Id") final String rId,
			@CliOption(key = OPT_ROUTINE_BINARY_ID, mandatory = true, help = "Routine Binary Id") final String bId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getLibraryAdminServiceClient().removeRoutineBinary(rId, bId).get();
		return MESSAGE_LIBRARY_ROUTINE_BINARY_REMOVE;
	}

	@CliCommand(value = CMD_LIBRARY_DATA_TYPE_FIND, help = "Find DataTypes by a pattern")
	public String findDataTypes(
		@CliOption(key = OPT_SEARCH_PATTERN, mandatory = true, help = "Pattern to match") final String searchPattern
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<String>> future = session.getLibraryAdminServiceClient().findDataTypes(searchPattern);
		return ShellOutputFormatter.format(future.get());
	}

	@CliCommand(value = CMD_LIBRARY_DATA_TYPE_CREATE, help = "Create a DataType at Library")
	public String createDataType(
		@CliOption(key = OPT_NAME, mandatory = true, help = "Name of DataType") final String name,
		@CliOption(key = OPT_FILE_NAME, mandatory = true, help = "Filename of Schema-File") final String fileName
	) throws IOException, ClientCommunicationException, ExecutionException, InterruptedException {

		File schemaFile = new File(fileName);
		if (!schemaFile.exists()) {
			throw new FileNotFoundException("File not exists: " + fileName);
		}
		StringBuilder schema = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				schema.append(line);
				schema.append('\n');
			}
		}
		Future<String> future = session.getLibraryAdminServiceClient().createDataType(name, schema.toString());
		return String.format(MESSAGE_LIBRARY_DATA_TYPE_CREATED, future.get());
	}

	@CliCommand(value = CMD_LIBRARY_DATA_TYPE_SHOW, help = "Show a given DataType")
	public String getDataType(
		@CliOption(key = OPT_DATATYPE_ID, mandatory = true, help = "Id of DataType") final String dId,
        @CliOption(key = OPT_TO_OBJECT, help = "Store DataType to an object with given name") final String objectName
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<DataTypeDTO> future = session.getLibraryAdminServiceClient().getDataType(dId);
		if (objectName == null) {
			return ShellOutputFormatter.format(future.get());
		} else {
			DataTypeDTO dataType = future.get();
			objects.getObjectMap().put(objectName, dataType);
			return String.format(MESSAGE_OBJECT_STORED, objectName);
		}
	}

	@CliCommand(value = CMD_LIBRARY_DATA_TYPE_REMOVE, help = "Remove a DataType from Library")
	public String removeDataType(
		@CliOption(key = OPT_DATATYPE_ID, mandatory = true, help = "Id of DataType") final String dId
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getLibraryAdminServiceClient().removeDataType(dId).get();
		return MESSAGE_LIBRARY_DATA_TYPE_REMOVE;
	}

	@CliCommand(value = CMD_LIBRARY_TAG_FIND, help = "Find a Tag by pattern")
	public String findTags(
		@CliOption(key = OPT_SEARCH_PATTERN, mandatory = true, help = "Pattern to match") final String searchPattern
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		Future<List<TagDTO>> future = session.getLibraryAdminServiceClient().findTags(searchPattern);
		return ShellOutputFormatter.format(future.get());
	}

	@CliCommand(value = CMD_LIBRARY_TAG_CREATE, help = "Create a Tag")
	public String createTag(
		@CliOption(key = OPT_LABEL, mandatory = true, help = "Tag label") final String label,
		@CliOption(key = OPT_DESCRIPTION, mandatory = true, help = "Tag description") final String description
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getLibraryAdminServiceClient().createTag(label, description).get();
		return String.format(MESSAGE_LIBRARY_TAG_CREATED, label);
	}

	@CliCommand(value = CMD_LIBRARY_TAG_REMOVE, help = "Remove a Tag")
	public String removeTag(
		@CliOption(key = OPT_LABEL, mandatory = true, help = "Tag label") final String label
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getLibraryAdminServiceClient().removeTag(label).get();
		return String.format(MESSAGE_LIBRARY_TAG_REMOVE, label);
	}

	@CliCommand(value = CMD_LIBRARY_SET_DATA_ENDPOINT, help = "Set the data endpoint of a library")
	public String setDataEndpoint(
			@CliOption(key = OPT_SERVICE_ENDPOINT, mandatory = true, help = "Data endpoint") final String dataEndpointName
	) throws ClientCommunicationException, ExecutionException, InterruptedException {

		session.getLibraryAdminServiceClient().setDataEndpoint(objects.getObject(dataEndpointName, ServiceEndpointDTO.class)).get();
		return MESSAGE_LIBRARY_DATA_ENDPOINT_SET;
	}

}
