package at.enfilo.def.library.api.util;

import at.enfilo.def.common.impl.AbstractBaseRegistry;
import at.enfilo.def.demo.*;
import at.enfilo.def.library.api.UnknownDataTypeException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.transfer.util.RoutineBinaryFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Holds all BaseRoutines.
 */
public class BaseRoutineRegistry extends AbstractBaseRegistry<String, RoutineDTO> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(BaseRoutineRegistry.class);
    private static final String DEMO_ROUTINES_JAR = "demo-routines.jar";
    private static final String DEMO_ROUTINES_JAR_PATH = "libs/" + DEMO_ROUTINES_JAR;
    private static FeatureDTO javaFeature;

    /**
     * Private class to provide thread safe singleton.
     */
    private static class ThreadSafeLazySingletonWrapper {
        private static final BaseRoutineRegistry INSTANCE = new BaseRoutineRegistry();
        private ThreadSafeLazySingletonWrapper() {}
    }

    /**
     * Hide constructor, use static methods.
     */
    private BaseRoutineRegistry() {
        super();
    }

    /**
     * Singleton pattern.
     * @return an instance of BaseRoutineRegistry
     */
    public static BaseRoutineRegistry getInstance() {
        return BaseRoutineRegistry.ThreadSafeLazySingletonWrapper.INSTANCE;
    }

    @Override
    public void refresh()
    throws RuntimeException {
        try {

            // Try to find fat 'demo-routines.jar':
            //   1. outside resource structure
            //   2. inside resource structure
            File routineBinaryFile = new File(DEMO_ROUTINES_JAR_PATH);
            if (!routineBinaryFile.exists()) {
                URL routineBinaryFileUrl = BaseRoutineRegistry.class.getClassLoader().getResource(DEMO_ROUTINES_JAR_PATH);
                if (routineBinaryFileUrl == null) {
                    LOGGER.error("{} not found. Please run 'gradle compileJava' first.", DEMO_ROUTINES_JAR_PATH);
                    throw new FileNotFoundException(DEMO_ROUTINES_JAR_PATH);
                }
                routineBinaryFile = new File(routineBinaryFileUrl.getFile());
            }

            // Packing fat jar as routine binary.
            String rbId = UUID.nameUUIDFromBytes(routineBinaryFile.getName().getBytes()).toString();
            RoutineBinaryDTO routineBinary = RoutineBinaryFactory.createFromFile(routineBinaryFile, true, DEMO_ROUTINES_JAR, rbId);

            // Initialization of automatically discovered formatter classes.
//            DEFDetector.handleSubTypes(
//                TBase.class,
//                BASE_DATATYPE_PACKAGE,
//                (IThrowingConsumer<Class<? extends TBase>>) this::createDataType
//            );

            // Hardcoded part that should be replaced in the future.
            RoutineDTO piCalc = createPICalcRoutine(routineBinary);
            addRequiredFeatures(piCalc);
            put(piCalc.getId(), piCalc);

            RoutineDTO mapper = createDefaultMapperRoutine(routineBinary);
			addRequiredFeatures(mapper);
            put(mapper.getId(), mapper);

            RoutineDTO doubleMapper = createDefaultDoubleIntegerMapperRoutine(routineBinary);
			addRequiredFeatures(doubleMapper);
            put(doubleMapper.getId(), doubleMapper);

            RoutineDTO fsStorer = createDefaultFSStorer(routineBinary);
			addRequiredFeatures(fsStorer);
            put(fsStorer.getId(), fsStorer);

            RoutineDTO memoryStorer = createDefaultMemoryStorer(routineBinary);
			addRequiredFeatures(memoryStorer);
            put(memoryStorer.getId(), memoryStorer);

            RoutineDTO doubleSumReducer = createDoubleSumReducer(routineBinary);
			addRequiredFeatures(doubleSumReducer);
            put(doubleSumReducer.getId(), doubleSumReducer);

            RoutineDTO piCalcClientRoutine = createPiCalcClientRoutine(routineBinary);
            addRequiredFeatures(piCalcClientRoutine);
            put(piCalcClientRoutine.getId(), piCalcClientRoutine);

            RoutineDTO piCalcWithReduceClientRoutine = createPiCalcWithReduceClientRoutine(routineBinary);
            addRequiredFeatures(piCalcWithReduceClientRoutine);
            put(piCalcWithReduceClientRoutine.getId(), piCalcWithReduceClientRoutine);

            RoutineDTO testClientRoutine = createTestClientRoutine(routineBinary);
            addRequiredFeatures(testClientRoutine);
            put(testClientRoutine.getId(), testClientRoutine);

        } catch (RuntimeException | NoSuchAlgorithmException | IOException | UnknownDataTypeException e) {
            LOGGER.error(
                "Error occurred while registering automatically fetched DEF demo-routines.",
                e
            );
        }
    }

    private RoutineDTO createDoubleSumReducer(RoutineBinaryDTO binary) {
		RoutineDTO doubleSumReducer = new RoutineDTO();
		doubleSumReducer.setType(RoutineType.REDUCE);
		doubleSumReducer.setName(DoubleSumReducer.class.getSimpleName());
		doubleSumReducer.setDescription("Create a sum of all values.");
		doubleSumReducer.setId(UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString());
		doubleSumReducer.setRoutineBinaries(Collections.singleton(binary));
		doubleSumReducer.setArguments(Collections.singletonList(DoubleSumReducer.class.getCanonicalName()));
		return doubleSumReducer;
    }


    private RoutineDTO createDefaultFSStorer(RoutineBinaryDTO binary) {
        RoutineDTO fsStorer = new RoutineDTO();
        fsStorer.setType(RoutineType.STORE);
        fsStorer.setName(FSStorer.class.getSimpleName());
        fsStorer.setDescription("Stores result to filesystem. Path can be specified via properties.");
        fsStorer.setId(UUID.nameUUIDFromBytes(FSStorer.class.getCanonicalName().getBytes()).toString());
        fsStorer.setRoutineBinaries(Collections.singleton(binary));
        fsStorer.setArguments(Collections.singletonList(FSStorer.class.getCanonicalName()));
        return fsStorer;
    }

    private RoutineDTO createDefaultMemoryStorer(RoutineBinaryDTO binary) {
        RoutineDTO memoryStorer = new RoutineDTO();
        memoryStorer.setType(RoutineType.STORE);
        memoryStorer.setName(MemoryStorer.class.getSimpleName());
        memoryStorer.setDescription("Passes result direct from PartitionRoutineDTO to Worker");
        memoryStorer.setId(UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString());
        memoryStorer.setRoutineBinaries(Collections.singleton(binary));
        memoryStorer.setArguments(Collections.singletonList(MemoryStorer.class.getCanonicalName()));
        return memoryStorer;
    }

    private RoutineDTO createDefaultMapperRoutine(RoutineBinaryDTO binary) {
        RoutineDTO mapper = new RoutineDTO();
        mapper.setType(RoutineType.MAP);
        mapper.setName(DefaultMapper.class.getSimpleName());
        mapper.setDescription("DefaultMapper passes result value from ObjectiveRoutineDTO direct to PartitionRoutineDTO with key 'DEFAULT'");
        mapper.setId(UUID.nameUUIDFromBytes(DefaultMapper.class.getCanonicalName().getBytes()).toString());
        mapper.setRoutineBinaries(Collections.singleton(binary));
        mapper.setArguments(Collections.singletonList(DefaultMapper.class.getCanonicalName()));
        return mapper;
    }

    private RoutineDTO createDefaultDoubleIntegerMapperRoutine(RoutineBinaryDTO binary) throws UnknownDataTypeException {
        RoutineDTO doubleMapper = new RoutineDTO();
        doubleMapper.setType(RoutineType.MAP);
        doubleMapper.setName(DefaultDoubleIntegerMapper.class.getSimpleName());
        doubleMapper.setId(UUID.nameUUIDFromBytes(DefaultDoubleIntegerMapper.class.getCanonicalName().getBytes()).toString());
        doubleMapper.setDescription("Maps DEFDouble to DEFDouble with key 'DEFAULT'");
        doubleMapper.setRoutineBinaries(Collections.singleton(binary));
        doubleMapper.setInParameters(Collections.singletonList(
            new FormalParameterDTO(
                UUID.nameUUIDFromBytes("DefaultDoublerMapper.in".getBytes()).toString(),
                "in",
                "Mapper Input",
                BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble")
            )
        ));
        doubleMapper.setOutParameter(
            new FormalParameterDTO(
                UUID.nameUUIDFromBytes("DefaultDoublerMapper.out".getBytes()).toString(),
                "out",
                "Mapper output value (key is per default string)",
                BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble")
            )
        );
        doubleMapper.setArguments(Collections.singletonList(DefaultDoubleIntegerMapper.class.getCanonicalName()));
        return doubleMapper;
    }

    private RoutineDTO createPICalcRoutine(RoutineBinaryDTO binary) throws UnknownDataTypeException {
        RoutineDTO piCalc = new RoutineDTO();
        piCalc.setType(RoutineType.OBJECTIVE);
        piCalc.setId(UUID.nameUUIDFromBytes(PICalc.class.getCanonicalName().getBytes()).toString());
        piCalc.setName(PICalc.class.getSimpleName());
        piCalc.setDescription("Calculates PI distributed");
        List<FormalParameterDTO> piCalcIn = new LinkedList<>();
        piCalcIn.add(new FormalParameterDTO(
            UUID.nameUUIDFromBytes("PICalc.start".getBytes()).toString(),
            "start",
            "Start value (calculation range)",
            BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble")
        ));
        piCalcIn.add(new FormalParameterDTO(
            UUID.nameUUIDFromBytes("PICalc.end".getBytes()).toString(),
            "end",
            "End value (calculation range)",
            BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble")
        ));
        piCalcIn.add(new FormalParameterDTO(
            UUID.nameUUIDFromBytes("PICalc.stepSize".getBytes()).toString(),
            "stepSize",
            "Step size for the given range",
            BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble")
        ));
        piCalc.setInParameters(piCalcIn);
        piCalc.setOutParameter(new FormalParameterDTO(
            UUID.nameUUIDFromBytes("PICalc.result".getBytes()).toString(),
            "result",
            "Part of PI result",
            BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble")
        ));
        piCalc.setRoutineBinaries(Collections.singleton(binary));
        piCalc.setArguments(Collections.singletonList(PICalc.class.getCanonicalName()));
        return piCalc;
    }

    private RoutineDTO createPiCalcClientRoutine(RoutineBinaryDTO binary) throws UnknownDataTypeException {
        RoutineDTO piCalcClientRoutine = new RoutineDTO();
        piCalcClientRoutine.setType(RoutineType.CLIENT);
        piCalcClientRoutine.setId(UUID.nameUUIDFromBytes(PiCalcClientRoutine.class.getCanonicalName().getBytes()).toString());
        piCalcClientRoutine.setName(PiCalcClientRoutine.class.getSimpleName());
        piCalcClientRoutine.setDescription("Client routine which uses the pi calc routine");
        piCalcClientRoutine.setRoutineBinaries(Collections.singleton(binary));
        piCalcClientRoutine.setArguments(Collections.singletonList(PiCalcClientRoutine.class.getCanonicalName()));
        return piCalcClientRoutine;
    }

    private RoutineDTO createPiCalcWithReduceClientRoutine(RoutineBinaryDTO binary) throws UnknownDataTypeException {
        RoutineDTO piCalcWithReduceClientRoutine = new RoutineDTO();
        piCalcWithReduceClientRoutine.setType(RoutineType.CLIENT);
        piCalcWithReduceClientRoutine.setId(UUID.nameUUIDFromBytes(PiCalcWithReduceClientRoutine.class.getCanonicalName().getBytes()).toString());
        piCalcWithReduceClientRoutine.setName(PiCalcWithReduceClientRoutine.class.getSimpleName());
        piCalcWithReduceClientRoutine.setDescription("Client routine which uses the pi calc routine with reduce functionality");
        piCalcWithReduceClientRoutine.setRoutineBinaries(Collections.singleton(binary));
        piCalcWithReduceClientRoutine.setArguments(Collections.singletonList(PiCalcWithReduceClientRoutine.class.getCanonicalName()));
        return piCalcWithReduceClientRoutine;
    }

    private RoutineDTO createTestClientRoutine(RoutineBinaryDTO binary) {
        RoutineDTO testClientRoutine = new RoutineDTO();
        testClientRoutine.setType(RoutineType.CLIENT);
        testClientRoutine.setId(UUID.nameUUIDFromBytes(TestClientRoutine.class.getCanonicalName().getBytes()).toString());
        testClientRoutine.setName(TestClientRoutine.class.getSimpleName());
        testClientRoutine.setDescription("Test client routine without cluster interaction");
        testClientRoutine.setRoutineBinaries(Collections.singleton(binary));
        testClientRoutine.setArguments(Collections.singletonList(TestClientRoutine.class.getCanonicalName()));
        return testClientRoutine;
    }

    private void addRequiredFeatures(RoutineDTO routine) {
    	if (javaFeature == null) {
			javaFeature = new FeatureDTO();
			javaFeature.setName("java");
			javaFeature.setVersion(">1.8");
			javaFeature.setId(UUID.nameUUIDFromBytes("java(>1.8)".getBytes()).toString());
			javaFeature.setGroup("language");
		}
        List<FeatureDTO> features = new LinkedList<>();
        features.add(javaFeature);
    	routine.setRequiredFeatures(features);
	}
}
