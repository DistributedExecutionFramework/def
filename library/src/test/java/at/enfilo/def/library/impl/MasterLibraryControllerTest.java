package at.enfilo.def.library.impl;

import at.enfilo.def.domain.entity.*;
import at.enfilo.def.library.LibraryConfiguration;
import at.enfilo.def.library.api.util.BaseDataTypeRegistry;
import at.enfilo.def.library.api.util.BaseRoutineRegistry;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.persistence.api.*;
import at.enfilo.def.persistence.dao.PersistenceFacade;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.transfer.util.MapManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MasterLibraryControllerTest {
	private MasterLibraryController masterLibraryController;
	private IBinaryStoreDriver storeDriver;
	private PersistenceFacade persistenceFacade;
	private BaseDataTypeRegistry baseDataTypeRegistry;
	private BaseRoutineRegistry baseRoutineRegistry;

	private String rId;
	private Routine routine;
	private RoutineDTO routineDTO;
	private String rbId;
	private String rbName;
	private RoutineBinary routineBinary;
	private RoutineBinaryDTO routineBinaryDTO;
	private RoutineBinaryChunkDTO routineBinaryChunk;
	private IDataTypeDAO dataTypeDAO;
	private IRoutineDAO routineDAO;
	private IRoutineBinaryDAO routineBinaryDAO;
	private ITagDAO tagDAO;
	private IFeatureDAO featureDAO;

	@Before
	public void setUp() throws Exception {
		storeDriver = Mockito.mock(IBinaryStoreDriver.class);
		persistenceFacade = Mockito.mock(PersistenceFacade.class);
		baseDataTypeRegistry = Mockito.mock(BaseDataTypeRegistry.class);
		baseRoutineRegistry = Mockito.mock(BaseRoutineRegistry.class);
		masterLibraryController = new MasterLibraryController(
				LibraryConfiguration.getDefault(),
				storeDriver,
				persistenceFacade,
				baseDataTypeRegistry,
				baseRoutineRegistry
		);

		rId = UUID.randomUUID().toString();
		routine = new Routine();
		routine.setId(rId);
		routineDTO = new RoutineDTO();
		routineDTO.setId(rId);

		routineBinaryChunk = new RoutineBinaryChunkDTO();
		routineBinaryChunk.setChunk((short)1);
		routineBinaryChunk.setTotalChunks((short)1);
		routineBinaryChunk.setChunkSize(1000);
		routineBinaryChunk.setData(new byte[]{0x00, 0x01, 0x02, 0x03});

		rbId = UUID.randomUUID().toString();
		rbName = "name";
		routineBinary = new RoutineBinary();
		routineBinary.setId(rbId);
		routineBinary.setName(rbName);
		routineBinary.setSizeInBytes(4);
		routine.setRoutineBinaries(new HashSet<>());
		routine.getRoutineBinaries().add(routineBinary);
		routine.setRequiredFeatures(new HashSet<>());
		Feature python = new Feature();
		python.setId(UUID.randomUUID().toString());
		python.setVersion("3.7");
		python.setName("python");
		python.setGroup("language");

		Feature numpy = new Feature();
		numpy.setId(UUID.randomUUID().toString());
		numpy.setVersion("1.15");
		numpy.setName("numpy");
		numpy.setBaseFeature(python);

		Feature opencv = new Feature();
		opencv.setId(UUID.randomUUID().toString());
		opencv.setVersion("3");
		opencv.setName("opencv");
		opencv.setBaseFeature(python);

		python.setSubFeatures(Arrays.asList(numpy, opencv));

		Feature cuda = new Feature();
		cuda.setId(UUID.randomUUID().toString());
		cuda.setVersion("9.6");
		cuda.setName("cuda");

		routine.getRequiredFeatures().addAll(Arrays.asList(python, numpy, cuda));

		routineBinaryDTO = new RoutineBinaryDTO();
		routineBinaryDTO.setId(rbId);
		routineBinaryDTO.setName(rbName);
		routineBinaryDTO.setSizeInBytes(4);
		routineDTO.addToRoutineBinaries(routineBinaryDTO);

		FeatureDTO pythonDTO = new FeatureDTO();
		pythonDTO.setId(python.getId());
		pythonDTO.setName("python");

		FeatureDTO numpyDTO = new FeatureDTO();
		numpyDTO.setId(numpy.getId());
		numpyDTO.setName("numpy");
		numpyDTO.setBaseId(pythonDTO.getId());

		pythonDTO.setExtensions(Collections.singletonList(numpyDTO));

		FeatureDTO cudaDTO = new FeatureDTO();
		cudaDTO.setId(cuda.getId());

		routineDTO.setRequiredFeatures(Arrays.asList(pythonDTO, cudaDTO));

		dataTypeDAO = Mockito.mock(IDataTypeDAO.class);
		routineDAO = Mockito.mock(IRoutineDAO.class);
		routineBinaryDAO = Mockito.mock(IRoutineBinaryDAO.class);
		tagDAO = Mockito.mock(ITagDAO.class);
		featureDAO = Mockito.mock(IFeatureDAO.class);
		when(persistenceFacade.getNewDataTypeDAO()).thenReturn(dataTypeDAO);
		when(persistenceFacade.getNewRoutineDAO()).thenReturn(routineDAO);
		when(persistenceFacade.getNewRoutineBinaryDAO()).thenReturn(routineBinaryDAO);
		when(persistenceFacade.getNewTagDAO()).thenReturn(tagDAO);
		when(persistenceFacade.getNewFeatureDAO()).thenReturn(featureDAO);
		when(featureDAO.getProxy(python.getId())).thenReturn(python);
		when(featureDAO.getProxy(numpy.getId())).thenReturn(numpy);
		when(featureDAO.getProxy(opencv.getId())).thenReturn(opencv);
		when(featureDAO.getProxy(cuda.getId())).thenReturn(cuda);
	}

	@Test
	public void init() {
		List<DataTypeDTO> dataTypes = new LinkedList<>();
		dataTypes.add(new DataTypeDTO());
		dataTypes.add(new DataTypeDTO());
		when(baseDataTypeRegistry.getAll()).thenReturn(dataTypes);

		List<RoutineDTO> routines = new LinkedList<>();
		routines.add(new RoutineDTO());
		routines.add(new RoutineDTO());
		routines.add(new RoutineDTO());
		when(baseRoutineRegistry.getAll()).thenReturn(routines);

		masterLibraryController.init();

		verify(dataTypeDAO, times(dataTypes.size())).saveOrUpdate(any());
		verify(routineDAO, times(routines.size())).saveOrUpdate(any());
	}

	@Test
	public void getRoutine() throws Exception {
		when(routineDAO.findById(rId)).thenReturn(routine);
		when(storeDriver.getExecutionURL(rId, rbId, rbName)).thenReturn(new URL("file:/path/to/routine/binary"));

		// First time getRoutine --> fetch from master library
		RoutineDTO r = masterLibraryController.getRoutine(rId);
		assertEquals(routineDTO.getId(), r.getId());
		assertEquals(2, r.getRequiredFeatures().size());
		for (FeatureDTO featureDTO : r.getRequiredFeatures()) {
			if (featureDTO.getName().equals("python")) {
				assertEquals(1, featureDTO.getExtensions().size());
			} else {
				assertEquals(0, featureDTO.getExtensions().size());
			}
		}

		// Second time getRoutine --> should be cached
		r = masterLibraryController.getRoutine(rId);
		assertEquals(routineDTO.getId(), r.getId());
		assertEquals(2, r.getRequiredFeatures().size());
		for (FeatureDTO featureDTO : r.getRequiredFeatures()) {
			if (featureDTO.getName().equals("python")) {
				assertEquals(1, featureDTO.getExtensions().size());
			} else {
				assertEquals(0, featureDTO.getExtensions().size());
			}
		}

		verify(routineDAO, times(1)).findById(rId);
	}

	@Test(expected = ExecutionException.class)
	public void getRoutineWrongId() throws ExecutionException {
		when(routineDAO.findById(anyString())).thenThrow(new PersistenceException(""));

		masterLibraryController.getRoutine(UUID.randomUUID().toString());
	}

	@Test
	public void findRoutines() {
		String pattern = null;
		String expectedPattern = "%";
		masterLibraryController.findRoutines(pattern);
		verify(routineDAO, times(1)).findIdsByNameOrDescription(expectedPattern);

		pattern = "search*pattern";
		expectedPattern = "search%pattern";
		masterLibraryController.findRoutines(pattern);
		verify(routineDAO, times(1)).findIdsByNameOrDescription(expectedPattern);
	}

	@Test
	public void removeRoutine() {
		masterLibraryController.routineRegistry.put(rId, routineDTO);
		masterLibraryController.routineRegistry.put(UUID.randomUUID().toString(), new RoutineDTO());

		masterLibraryController.removeRoutine(rId);
		assertFalse(masterLibraryController.routineRegistry.containsKey(rId));
		assertFalse(masterLibraryController.routineRegistry.isEmpty());
		verify(routineDAO, times(1)).deleteById(rId);
	}

	@Test
	public void createRoutine() throws ExecutionException {
		assertTrue(masterLibraryController.routineRegistry.isEmpty());

		when(routineDAO.save(any())).thenReturn(rId);

		String newId = masterLibraryController.createRoutine(routineDTO);
		assertEquals(rId, newId);
		assertTrue(masterLibraryController.routineRegistry.containsKey(rId));
		RoutineDTO r = masterLibraryController.routineRegistry.get(rId);
		assertEquals(2, r.getRequiredFeatures().size());
		for (FeatureDTO featureDTO : r.getRequiredFeatures()) {
			if (featureDTO.getName() != null && featureDTO.getName().equals("python")) {
				assertEquals(1, featureDTO.getExtensions().size());
			} else {
				assertNull(featureDTO.getExtensions());
			}
		}
	}

	@Test
	public void updateRoutine() throws ExecutionException {
		masterLibraryController.routineRegistry.put(rId, routineDTO);

		String updateId = UUID.randomUUID().toString();
		when(routineDAO.saveNewRevision(eq(rId), any())).thenReturn(updateId);

		String id = masterLibraryController.updateRoutine(routineDTO);
		assertEquals(updateId, id);
	}

	@Test
	public void uploadRoutineBinaryChunk() throws ExecutionException, IOException {
		Random rnd = new Random();
		byte[] data = new byte[1024];
		rnd.nextBytes(data);
		RoutineBinaryChunkDTO chunk = new RoutineBinaryChunkDTO((short)1, (short)1, 1024, ByteBuffer.wrap(data));

		when(routineDAO.findById(rId)).thenReturn(routine);
		when(storeDriver.exists(rbId)).thenReturn(true);

		masterLibraryController.uploadRoutineBinaryChunk(rbId, chunk);

		verify(storeDriver, times(1)).storeChunk(rbId, chunk);
	}

	@Test
	public void removeRoutineBinary() throws ExecutionException, IOException {
		masterLibraryController.routineRegistry.put(rId, routineDTO);

		when(routineDAO.findById(rId)).thenReturn(routine);

		masterLibraryController.removeRoutineBinary(rId, rbId);
		assertTrue(routine.getRoutineBinaries().isEmpty());
		assertTrue(masterLibraryController.routineRegistry.containsKey(rId));
		assertTrue(masterLibraryController.routineRegistry.get(rId).getRoutineBinaries().isEmpty());
	}

	@Test
	public void getCachedRoutineBinary() throws Exception {
		when(storeDriver.exists(rbId)).thenReturn(true);
		when(storeDriver.read(rbId, rbName)).thenReturn(routineBinaryDTO);
		masterLibraryController.routineRegistry.put(rId, routineDTO);

		RoutineBinaryDTO b = masterLibraryController.getRoutineBinary(rbId);
		assertEquals(routineBinaryDTO, b);
	}

	@Test
	public void getNonCachedRoutineBinary() throws Exception {
		URL url = new URL("file:/" + rbId);
		routineBinary.setUrl(url.toString());
		routineBinaryDTO.setUrl(url.toString());
		when(storeDriver.exists(rbId)).thenReturn(false);
		when(routineBinaryDAO.findById(rbId)).thenReturn(routineBinary);
		when(storeDriver.read(rbId, rbName, url)).thenReturn(routineBinaryDTO);

		RoutineBinaryDTO b = masterLibraryController.getRoutineBinary(rbId);
		assertEquals(routineBinaryDTO, b);
	}

	@Test
	public void getNonExistsRoutineBinary() throws Exception {
		when(routineBinaryDAO.findById(rbId)).thenReturn(null);
		RoutineBinaryDTO rb = masterLibraryController.getRoutineBinary(rbId);
		assertNull(rb);
	}


	@Test
	public void findDataTypes() throws ExecutionException {
		List<String> dataTypes = new LinkedList<>();
		dataTypes.add(UUID.randomUUID().toString());

		when(dataTypeDAO.findAllIds()).thenReturn(dataTypes);
		List<String> result = masterLibraryController.findDataTypes(null);
		assertEquals(dataTypes, result);

		String pattern = "search*pattern";
		when(dataTypeDAO.findIdsByNameOrDescription(pattern.replace('*', '%'))).thenReturn(dataTypes);
		result = masterLibraryController.findDataTypes(pattern);
		assertEquals(dataTypes, result);
	}

	@Test
	public void createDataType() throws ExecutionException {
		String schema = "...\n" +
				        "_id = \"\",\n" +
						"...\n";

		when(dataTypeDAO.save(any())).then(
				invocation -> {
					DataType dt = invocation.getArgumentAt(0, DataType.class);
					String id = dt.getId();
					assertTrue(dt.getSchema().contains("_id = \"" + id + "\""));
					return id;
				}
		);
		String id = masterLibraryController.createDataType("name", schema);
		assertNotNull(id);
		assertTrue(!id.isEmpty());
	}

	@Test
	public void getDataType() throws ExecutionException {
		DataType dataType = new DataType();
		String dId  = dataType.getId();
		dataType.setName("name_" + dId);
		dataType.setSchema("schema_" + dId);
		DataTypeDTO dataTypeDTO = MapManager.map(dataType, DataTypeDTO.class);

		when(dataTypeDAO.findById(dId)).thenReturn(dataType);

		DataTypeDTO requestedDataType = masterLibraryController.getDataType(dId);
		assertEquals(dataTypeDTO, requestedDataType);
	}


	@Test
	public void removeDataType() throws ExecutionException {
		String dId = UUID.randomUUID().toString();
		masterLibraryController.removeDataType(dId);

		verify(dataTypeDAO, times(1)).deleteById(dId);
	}

	@Test
	public void findTags() throws ExecutionException {
		List<Tag> tags = new LinkedList<>();
		List<TagDTO> tagDTOs = new LinkedList<>();
		Tag tag1 = new Tag();
		tag1.setId(UUID.randomUUID().toString());
		tag1.setDescription("desc1");
		tags.add(tag1);
		tagDTOs.add(MapManager.map(tag1, TagDTO.class));
		Tag tag2 = new Tag();
		tag2.setId(UUID.randomUUID().toString());
		tag2.setDescription("desc2");
		tags.add(tag2);
		tagDTOs.add(MapManager.map(tag2, TagDTO.class));
		String pattern = "*";

		when(tagDAO.findByLabelOrDescription(pattern)).thenReturn(tags);

		List<TagDTO> result = masterLibraryController.findTags(pattern);
		assertEquals(tagDTOs, result);
	}

	@Test
	public void createTag() throws ExecutionException {
		String label = UUID.randomUUID().toString();
		String desc = "description";

		Tag tag = new Tag();
		tag.setId(label);
		tag.setDescription(desc);

		when(tagDAO.save(tag)).thenReturn(label);

		masterLibraryController.createTag(label, desc);

		verify(tagDAO).save(tag);
	}

	@Test
	public void removeTag() throws ExecutionException {
		String label = UUID.randomUUID().toString();

		masterLibraryController.removeTag(label);

		verify(tagDAO, times(1)).deleteById(label);
	}

	@Test
	public void createFeature() throws ExecutionException {
		String fId = UUID.randomUUID().toString();
		Feature feature = new Feature();
		feature.setName("java");
		feature.setVersion("1.8");
		feature.setGroup("language");

		when(featureDAO.save(anyObject())).thenReturn(fId);

		String result = masterLibraryController.createFeature(feature.getName(), feature.getGroup(), feature.getVersion());
		assertEquals(fId, result);
	}

	@Test
	public void addExtension() throws ExecutionException {
		String eId = UUID.randomUUID().toString();

		Feature feature = new Feature();
		feature.setId(UUID.randomUUID().toString());
		feature.setName("python");
		feature.setVersion("3.7");
		feature.setGroup("language");

		Feature extension = new Feature();
		extension.setName("numpy");
		extension.setVersion("1.8");
		extension.setBaseFeature(feature);

		when(featureDAO.save(anyObject())).thenReturn(eId);
		when(featureDAO.findById(feature.getId())).thenReturn(feature);

		String result = masterLibraryController.addExtension(feature.getId(), extension.getName(), extension.getVersion());
		assertEquals(eId, result);
	}

	@Test
	public void getFeatures() throws ExecutionException {
		Feature feature1 = new Feature();
		feature1.setId(UUID.randomUUID().toString());
		feature1.setName("python");
		feature1.setVersion("2.7");
		feature1.setGroup("language");
		Feature feature2 = new Feature();
		feature2.setId(UUID.randomUUID().toString());
		feature2.setBaseFeature(feature1);
		feature2.setName("numpy");
		feature2.setVersion("1.15.1");
		Feature feature3 = new Feature();
		feature3.setId(UUID.randomUUID().toString());
		feature3.setBaseFeature(feature1);
		feature3.setName("opencv");
		feature3.setVersion("3.0");

		feature1.setSubFeatures(Arrays.asList(feature2, feature3));

		Feature feature4 = new Feature();
		feature4.setId(UUID.randomUUID().toString());
		feature4.setName("python");
		feature4.setVersion("3.7");
		feature4.setGroup("language");
		Feature feature5 = new Feature();
		feature5.setId(UUID.randomUUID().toString());
		feature5.setBaseFeature(feature4);
		feature5.setName("numpy");
		feature5.setVersion("1.16");

		feature4.setSubFeatures(Collections.singletonList(feature5));

		Feature feature6 = new Feature();
		feature6.setId(UUID.randomUUID().toString());
		feature6.setName("java");
		feature6.setVersion("1.8");
		feature6.setGroup("language");

		List<Feature> findAllBase = new ArrayList<>();
		findAllBase.add(feature1);
		findAllBase.add(feature4);
		findAllBase.add(feature6);

		List<Feature> findFiltered = new ArrayList<>();
		findFiltered.add(feature3);
		findFiltered.add(feature5);

		List<Feature> findAll = new ArrayList<>(findFiltered);
		findAll.add(feature1);
		findAll.add(feature2);
		findAll.add(feature3);
		findAll.add(feature4);
		findAll.add(feature5);
		findAll.add(feature6);

		when(featureDAO.findAll()).thenReturn(findAll);
		when(featureDAO.findAllBaseFeatures()).thenReturn(findAllBase);
		when(featureDAO.findByNameOrVersion("*")).thenReturn(findAll);
		when(featureDAO.findByNameOrVersion("testFiltering")).thenReturn(findFiltered);

		List<FeatureDTO> result1 = masterLibraryController.getFeatures(null);
		List<FeatureDTO> result2 = masterLibraryController.getFeatures("");
		List<FeatureDTO> result3 = masterLibraryController.getFeatures("*");
		List<FeatureDTO> result4 = masterLibraryController.getFeatures("testFiltering");

		assertEquals(3, result1.size());
		assertEquals(3, result2.size());
		assertEquals(3, result3.size());
		assertEquals(2, result4.size());
	}
}
