package at.enfilo.def.node.routine.exec;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.demo.DefaultMapper;
import at.enfilo.def.demo.FSStorer;
import at.enfilo.def.demo.PICalc;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.transfer.dto.*;
import org.apache.thrift.TSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class SequenceStepsExecutorTest {
	private static final String DEMO_ROUTINES_JAR = "libs/demo-routines.jar";
	private static final String DEMO_ROUTINES_PYTHON = "square_calc.py";

	private RoutineDTO piCalc;
	private RoutineDTO squareCalc;
	private RoutineDTO mapRoutine;
	private RoutineDTO partitionRoutine;
	private RoutineDTO storeRoutine;
	private String pId;
	private String jId;
	private String tIdPi;
	private String tIdSquare;
	private JobDTO job;
	private TaskDTO taskPi;
	private TaskDTO taskSquare;
	private LibraryServiceMock libraryServiceMock;
	private RoutineProcessBuilderFactory routineProcessBuilderFactory;

	@Before
	public void setUp() throws Exception {
		Set<RoutineBinaryDTO> jarBinaries = new HashSet<>();
		URL routineBinaryJarFile = SequenceStepsExecutor.class.getClassLoader().getResource(DEMO_ROUTINES_JAR);
		if (routineBinaryJarFile == null) {
			File tmp = new File(DEMO_ROUTINES_JAR);
			if (tmp.exists()) {
				routineBinaryJarFile = tmp.toURI().toURL();
			} else {
				fail(DEMO_ROUTINES_JAR + " not found. Please run 'gradle compileJava' first.");
				throw new FileNotFoundException(DEMO_ROUTINES_JAR);
			}
		}
		RoutineBinaryDTO binaryJar = new RoutineBinaryDTO();
        binaryJar.setUrl("file:" + routineBinaryJarFile.getFile());
		jarBinaries.add(binaryJar);

		Set<RoutineBinaryDTO> pyBinaries = new HashSet<>();
		URL routineBinaryPyFile = SequenceStepsExecutorTest.class.getClassLoader().getResource(DEMO_ROUTINES_PYTHON);
		if (routineBinaryPyFile == null) {
			fail(DEMO_ROUTINES_PYTHON + " not found.");
			throw new FileNotFoundException(DEMO_ROUTINES_JAR);
		}
		RoutineBinaryDTO binaryPy = new RoutineBinaryDTO();
		binaryPy.setUrl(routineBinaryPyFile.getFile());
		binaryPy.setPrimary(true);
		pyBinaries.add(binaryPy);

		libraryServiceMock = new LibraryServiceMock();
		routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
				new LibraryServiceClientFactory().createDirectClient(
					libraryServiceMock.getLibraryService(),
					libraryServiceMock.getLibraryResponseService(),
					libraryServiceMock.getTicketService(),
					ILibraryServiceClient.class
				),
				NodeConfiguration.getDefault()
		);

		FeatureDTO featureDTO = new FeatureDTO();
		featureDTO.setId(UUID.randomUUID().toString());
		featureDTO.setVersion(">1.8");
		featureDTO.setName("java");
		featureDTO.setGroup("language");

		piCalc = new RoutineDTO(
			UUID.nameUUIDFromBytes(PICalc.class.getCanonicalName().getBytes()).toString(),
			false,
			PICalc.class.getSimpleName(),
			"desc",
			(short) 1,
			RoutineType.OBJECTIVE,
			null,
			null,
			jarBinaries,
			Collections.singletonList(PICalc.class.getCanonicalName()),
				Collections.singletonList(featureDTO)
		);
		libraryServiceMock.registerRoutine(piCalc);

		squareCalc = new RoutineDTO(
				UUID.nameUUIDFromBytes("squareCalc.py".getBytes()).toString(),
				false,
				"SquareCalc",
				"desc",
				(short) 1,
				RoutineType.OBJECTIVE,
				null,
				null,
				pyBinaries,
				Collections.singletonList("SquareCalc"),
				Collections.singletonList(featureDTO)
		);
		libraryServiceMock.registerRoutine(squareCalc);

		mapRoutine = new RoutineDTO(
			UUID.nameUUIDFromBytes(DefaultMapper.class.getCanonicalName().getBytes()).toString(),
			false,
			DefaultMapper.class.getSimpleName(),
			"desc",
			(short) 1,
			RoutineType.MAP,
			null,
			null,
			jarBinaries,
			Collections.singletonList(DefaultMapper.class.getCanonicalName()),
				Collections.singletonList(featureDTO)
		);
		libraryServiceMock.registerRoutine(mapRoutine);

		storeRoutine = new RoutineDTO(
			UUID.nameUUIDFromBytes(FSStorer.class.getCanonicalName().getBytes()).toString(),
			false,
			FSStorer.class.getSimpleName(),
			"desc",
			(short) 1,
			RoutineType.STORE,
			null,
			null,
			jarBinaries,
			Collections.singletonList(FSStorer.class.getCanonicalName()),
				Collections.singletonList(featureDTO)
		);
		libraryServiceMock.registerRoutine(storeRoutine);

		pId = UUID.randomUUID().toString();
		jId = UUID.randomUUID().toString();
		tIdPi = UUID.randomUUID().toString();
		tIdSquare = UUID.randomUUID().toString();

		job = new JobDTO();
		job.setId(jId);
		job.setProgramId(pId);
		job.setMapRoutineId(mapRoutine.getId());

		taskPi = new TaskDTO();
		taskPi.setId(tIdPi);
		taskPi.setJobId(job.getId());
		taskPi.setProgramId(pId);
		taskPi.setObjectiveRoutineId(piCalc.getId());

		taskSquare = new TaskDTO();
		taskSquare.setId(tIdPi);
		taskSquare.setJobId(job.getId());
		taskSquare.setProgramId(pId);
		taskSquare.setObjectiveRoutineId(squareCalc.getId());
	}

	@Test(timeout = 60*1000)
	public void piTest() throws Exception {
		DEFDouble start = new DEFDouble(0);
		DEFDouble end = new DEFDouble(1e9);
		DEFDouble stepSize = new DEFDouble(1e-9);
		TSerializer t = new TSerializer();
		Map<String, ResourceDTO> inParameters = new HashMap<>();
		ResourceDTO paramStart = new ResourceDTO(UUID.randomUUID().toString(), null);
		paramStart.setData(t.serialize(start));
		ResourceDTO paramEnd = new ResourceDTO(UUID.randomUUID().toString(), null);
		paramEnd.setData(t.serialize(end));
		ResourceDTO paramStepSize = new ResourceDTO(UUID.randomUUID().toString(), null);
		paramStepSize.setData(t.serialize(stepSize));
		inParameters.put("start", paramStart);
		inParameters.put("end", paramEnd);
		inParameters.put("stepSize", paramStepSize);
		taskPi.setInParameters(inParameters);

		SequenceStepsExecutor exec = new SequenceStepsBuilder(taskPi.getId(), NodeConfiguration.getDefault())
				.appendStep(taskPi.getObjectiveRoutineId(), RoutineType.OBJECTIVE)
				.appendStep(job.getMapRoutineId(), RoutineType.MAP)
				.appendStep(storeRoutine.getId(), RoutineType.STORE)
				.build(taskPi, routineProcessBuilderFactory);

		List<Result> results = exec.run();
		assertNotNull(results);
		assertFalse(results.isEmpty());

		//assertEquals(ExecutionState.SUCCESS, taskPi.getState());
		//assertTrue(taskPi.getStartTime() < taskPi.getFinishTime());
		//assertEquals(1, taskPi.getOutParametersSize());

		//String url = taskPi.getOutParameters().get(0).getUrl();
		String url = results.get(0).getUrl();
		FileInputStream fin = new FileInputStream(new File(new URI(url)));
		DataReader reader = new DataReader(fin);
		DEFDouble result = new DEFDouble();
		reader.read(result);
		assertEquals(Math.PI, result.getValue(), 1e-2);
	}

	@Test
	public void cancelTaskExecution() throws Exception {
		DEFDouble start = new DEFDouble(0);
		DEFDouble end = new DEFDouble(1e9);
		DEFDouble stepSize = new DEFDouble(1e-9);
		TSerializer t = new TSerializer();
		Map<String, ResourceDTO> inParameters = new HashMap<>();
		ResourceDTO paramStart = new ResourceDTO(UUID.randomUUID().toString(), null);
		paramStart.setData(t.serialize(start));
		ResourceDTO paramEnd = new ResourceDTO(UUID.randomUUID().toString(), null);
		paramEnd.setData(t.serialize(end));
		ResourceDTO paramStepSize = new ResourceDTO(UUID.randomUUID().toString(), null);
		paramStepSize.setData(t.serialize(stepSize));
		inParameters.put("start", paramStart);
		inParameters.put("end", paramEnd);
		inParameters.put("stepSize", paramStepSize);
		taskPi.setInParameters(inParameters);

		SequenceStepsExecutor exec = new SequenceStepsBuilder(taskPi.getId(), NodeConfiguration.getDefault())
				.appendStep(taskPi.getObjectiveRoutineId(), RoutineType.OBJECTIVE)
				.appendStep(job.getMapRoutineId(), RoutineType.MAP)
				.appendStep(storeRoutine.getId(), RoutineType.STORE)
				.build(taskPi, routineProcessBuilderFactory);

		assertFalse(exec.isRunning());

		// Start and cancel task
		ExecutorService es = Executors.newSingleThreadExecutor();
        Future<List<Result>> fTask = es.submit((Callable<List<Result>>) exec::run);
		await().atMost(5, TimeUnit.SECONDS).until(exec::isRunning);
		assertTrue(exec.isRunning());
		exec.cancel();
		assertFalse(exec.isRunning());
//		assertTrue(fTask.isCancelled());

		// Check if ExecutorService Thread is still alive
		Future<String> f = es.submit(() -> "alive");
		assertEquals("alive", f.get(10, TimeUnit.SECONDS));
		assertTrue(f.isDone());

	}

/*
 * - Python Test Routine - before execute this test, install def_api first:
 *   $ cd apis/python
 *   $ pip install . --user

*/
	//@Test
	public void squareTest() throws Exception {
		DEFDouble x = new DEFDouble(9.1);
		TSerializer t = new TSerializer();
		Map<String, ResourceDTO> inParameters = new HashMap<>();
		ResourceDTO paramX = new ResourceDTO(UUID.randomUUID().toString(), null);
		paramX.setData(t.serialize(x));
		inParameters.put("x", paramX);
		taskSquare.setInParameters(inParameters);

		NodeConfiguration configuration = NodeConfiguration.getDefault();

		RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
				new LibraryServiceClientFactory().createClient(configuration.getLibraryEndpoint()),
				configuration
		);
		SequenceStepsExecutor exec = new SequenceStepsBuilder(taskSquare.getId(), NodeConfiguration.getDefault())
				.appendStep(taskSquare.getObjectiveRoutineId(), RoutineType.OBJECTIVE)
				.appendStep(job.getMapRoutineId(), RoutineType.MAP)
				.appendStep(storeRoutine.getId(), RoutineType.STORE)
				.build(taskSquare, routineProcessBuilderFactory);

		//taskSquare = exec.call();

		//assertEquals(ExecutionState.SUCCESS, taskSquare.getState());
		//assertTrue(taskSquare.getStartTime() < taskSquare.getFinishTime());
		//assertEquals(1, taskSquare.getOutParametersSize());

		String url = taskSquare.getOutParameters().get(0).getUrl();
		FileInputStream fin = new FileInputStream(new File(new URI(url)));
		DataReader reader = new DataReader(fin);
		DEFDouble result = new DEFDouble();
		reader.read(result);
		assertEquals(9.1*9.1, result.getValue(), 1e-6);

	}

	@After
	public void tearDown() {
		try {
			// Remove temporary storage and logs
			Path p = Paths.get(NodeConfiguration.getDefault().getWorkingDir(), tIdPi);
			if (p.toFile().isDirectory()) {
				Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
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
			}
		} catch (Exception e) {
			e.printStackTrace();
			// eat up
		}
	}
}
