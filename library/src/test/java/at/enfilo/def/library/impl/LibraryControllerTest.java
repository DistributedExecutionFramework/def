package at.enfilo.def.library.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.library.LibraryConfiguration;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.transfer.dto.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class LibraryControllerTest {
	private class TestLibraryController extends LibraryController {
		private final LibraryController mock;

		public TestLibraryController(LibraryController mock, LibraryConfiguration configuration, IBinaryStoreDriver storeDriver) {
			super(configuration, storeDriver);
			this.mock = mock;
		}

		@Override
		protected void init() {
			mock.init();
		}

		@Override
		protected RoutineDTO fetchRoutine(String rId) throws RoutineFetchException {
			return mock.fetchRoutine(rId);
		}

		@Override
		protected RoutineBinaryDTO fetchRoutineBinary(String rbId) throws RoutineFetchException, ClientCommunicationException {
			return mock.fetchRoutineBinary(rbId);
		}

		@Override
		protected void updateLibraryEndpoint(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException {
			mock.updateLibraryEndpoint(serviceEndpoint);
		}

		@Override
		public List<String> findRoutines(String pattern) throws ExecutionException {
			return mock.findRoutines(pattern);
		}

		@Override
		public void removeRoutine(String rId) throws ExecutionException, IOException {
			mock.removeRoutine(rId);
		}

		@Override
		public String createRoutine(RoutineDTO routineDTO) throws ExecutionException {
			return mock.createRoutine(routineDTO);
		}

		@Override
		public String updateRoutine(RoutineDTO routineDTO) throws ExecutionException {
			return mock.updateRoutine(routineDTO);
		}

		@Override
		public void removeRoutineBinary(String rId, String bId) throws ExecutionException {
			mock.removeRoutineBinary(rId, bId);
		}

		@Override
		public List<String> findDataTypes(String searchPattern) throws ExecutionException {
			return mock.findDataTypes(searchPattern);
		}

		@Override
		public String createDataType(String name, String schema) throws ExecutionException {
			return mock.createDataType(name, schema);
		}

		@Override
		public DataTypeDTO getDataType(String dId) throws ExecutionException {
			return mock.getDataType(dId);
		}

		@Override
		public void removeDataType(String dId) throws ExecutionException {
			mock.removeDataType(dId);
		}

		@Override
		public List<TagDTO> findTags(String searchPattern) throws ExecutionException {
			return mock.findTags(searchPattern);
		}

		@Override
		public void createTag(String label, String description) throws ExecutionException {
			mock.createTag(label, description);
		}

		@Override
		public void removeTag(String label) throws ExecutionException {
			mock.removeTag(label);
		}

		@Override
		public String createFeature(String name, String group, String version) throws ExecutionException {
			return mock.createFeature(name, group, version);
		}

		@Override
		public String addExtension(String featureId, String name, String version) throws ExecutionException {
			return mock.addExtension(featureId, name, version);
		}

		@Override
		public List<FeatureDTO> getFeatures(String pattern) throws ExecutionException {
			return mock.getFeatures(pattern);
		}

		@Override
		public String createRoutineBinary(String rId, String name, String md5, long sizeInBytes, boolean isPrimary) throws ExecutionException {
			return mock.createRoutineBinary(rId, name, md5, sizeInBytes, isPrimary);
		}

		@Override
		public FeatureDTO getFeatureByNameAndVersion(String name, String version) throws ExecutionException {
			return getFeatureByNameAndVersion(name, version);
		}

		@Override
		public void uploadRoutineBinaryChunk(String rbId, RoutineBinaryChunkDTO chunk) throws ExecutionException {
			mock.uploadRoutineBinaryChunk(rbId, chunk);
		}
	}
}
