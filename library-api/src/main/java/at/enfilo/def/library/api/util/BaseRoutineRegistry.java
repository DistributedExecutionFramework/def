package at.enfilo.def.library.api.util;

import at.enfilo.def.common.impl.AbstractBaseRegistry;
import at.enfilo.def.demo.*;
import at.enfilo.def.domain.entity.*;
import at.enfilo.def.library.api.UnknownDataTypeException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.dto.RoutineType;
import at.enfilo.def.transfer.util.MapManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Holds all BaseRoutines.
 */
public class BaseRoutineRegistry extends AbstractBaseRegistry<String, RoutineDTO> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(BaseRoutineRegistry.class);
    private static final String DEMO_ROUTINES_JAR = "libs/demo-routines.jar";
    private static Feature javaFeature;

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
            File routineBinaryFile = new File(DEMO_ROUTINES_JAR);
            if (!routineBinaryFile.exists()) {
                URL routineBinaryFileUrl = BaseRoutineRegistry.class.getClassLoader().getResource(DEMO_ROUTINES_JAR);
                if (routineBinaryFileUrl == null) {
                    LOGGER.error("{} not found. Please run 'gradle compileJava' first.", DEMO_ROUTINES_JAR);
                    throw new FileNotFoundException(DEMO_ROUTINES_JAR);
                }
                routineBinaryFile = new File(routineBinaryFileUrl.getFile());
            }

            // Packing fat jar as routine binary.
			RoutineBinaryFactory routineBinaryFactory = new RoutineBinaryFactory();
            RoutineBinary routineBinary = routineBinaryFactory.createRoutineBinary(routineBinaryFile, true);

            // Initialization of automatically discovered formatter classes.
//            DEFDetector.handleSubTypes(
//                TBase.class,
//                BASE_DATATYPE_PACKAGE,
//                (IThrowingConsumer<Class<? extends TBase>>) this::createDataType
//            );

            // Hardcoded part that should be replaced in the future.
            Routine piCalc = createPICalcRoutine(routineBinary);
            addRequiredFeatures(piCalc);
            put(piCalc.getId(), MapManager.map(piCalc, RoutineDTO.class));

            Routine mapper = createDefaultMapperRoutine(routineBinary);
			addRequiredFeatures(mapper);
            put(mapper.getId(), MapManager.map(mapper, RoutineDTO.class));

            Routine doubleMapper = createDefaultDoubleIntegerMapperRoutine(routineBinary);
			addRequiredFeatures(doubleMapper);
            put(doubleMapper.getId(), MapManager.map(doubleMapper, RoutineDTO.class));

            Routine fsStorer = createDefaultFSStorer(routineBinary);
			addRequiredFeatures(fsStorer);
            put(fsStorer.getId(), MapManager.map(fsStorer, RoutineDTO.class));

            Routine memoryStorer = createDefaultMemoryStorer(routineBinary);
			addRequiredFeatures(memoryStorer);
            put(memoryStorer.getId(), MapManager.map(memoryStorer, RoutineDTO.class));

            Routine doubleSumReducer = createDoubleSumReducer(routineBinary);
			addRequiredFeatures(doubleSumReducer);
            put(doubleSumReducer.getId(), MapManager.map(doubleSumReducer, RoutineDTO.class));

        } catch (RuntimeException | NoSuchAlgorithmException | IOException | UnknownDataTypeException e) {
            LOGGER.error(
                "Error occurred while registering automatically fetched DEF demo-routines.",
                e
            );
        }
    }

    private Routine createDoubleSumReducer(RoutineBinary binary) {
		Routine doubleSumReducer = new Routine();
		doubleSumReducer.setType(RoutineType.REDUCE);
		doubleSumReducer.setName(DoubleSumReducer.class.getSimpleName());
		doubleSumReducer.setDescription("Create a sum of all values.");
		doubleSumReducer.setId(UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString());
		doubleSumReducer.setRoutineBinaries(Collections.singletonList(binary));
		doubleSumReducer.setArguments(Collections.singletonList(DoubleSumReducer.class.getCanonicalName()));
		return doubleSumReducer;
    }


    private Routine createDefaultFSStorer(RoutineBinary binary) {
        Routine fsStorer = new Routine();
        fsStorer.setType(RoutineType.STORE);
        fsStorer.setName(FSStorer.class.getSimpleName());
        fsStorer.setDescription("Stores result to filesystem. Path can be specified via properties.");
        fsStorer.setId(UUID.nameUUIDFromBytes(FSStorer.class.getCanonicalName().getBytes()).toString());
        fsStorer.setRoutineBinaries(Collections.singletonList(binary));
        fsStorer.setArguments(Collections.singletonList(FSStorer.class.getCanonicalName()));
        return fsStorer;
    }

    private Routine createDefaultMemoryStorer(RoutineBinary binary) {
        Routine memoryStorer = new Routine();
        memoryStorer.setType(RoutineType.STORE);
        memoryStorer.setName(MemoryStorer.class.getSimpleName());
        memoryStorer.setDescription("Passes result direct from PartitionRoutine to Worker");
        memoryStorer.setId(UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString());
        memoryStorer.setRoutineBinaries(Collections.singletonList(binary));
        memoryStorer.setArguments(Collections.singletonList(MemoryStorer.class.getCanonicalName()));
        return memoryStorer;
    }

    private Routine createDefaultMapperRoutine(RoutineBinary binary) {
        Routine mapper = new Routine();
        mapper.setType(RoutineType.MAP);
        mapper.setName(DefaultMapper.class.getSimpleName());
        mapper.setDescription("DefaultMapper passes result value from ObjectiveRoutine direct to PartitionRoutine with key 'DEFAULT'");
        mapper.setId(UUID.nameUUIDFromBytes(DefaultMapper.class.getCanonicalName().getBytes()).toString());
        mapper.setRoutineBinaries(Collections.singletonList(binary));
        mapper.setArguments(Collections.singletonList(DefaultMapper.class.getCanonicalName()));
        return mapper;
    }

    private Routine createDefaultDoubleIntegerMapperRoutine(RoutineBinary binary) throws UnknownDataTypeException {
        Routine doubleMapper = new Routine();
        doubleMapper.setType(RoutineType.MAP);
        doubleMapper.setName(DefaultDoubleIntegerMapper.class.getSimpleName());
        doubleMapper.setId(UUID.nameUUIDFromBytes(DefaultDoubleIntegerMapper.class.getCanonicalName().getBytes()).toString());
        doubleMapper.setDescription("Maps DEFDouble to DEFDouble with key 'DEFAULT'");
        doubleMapper.setRoutineBinaries(Collections.singletonList(binary));
        doubleMapper.setInParameters(Collections.singletonList(
            new FormalParameter(
                UUID.nameUUIDFromBytes("DefaultDoublerMapper.in".getBytes()).toString(),
                "in",
                "Mapper Input",
                MapManager.map(BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble"), DataType.class)
            )
        ));
        doubleMapper.setOutParameter(
            new FormalParameter(
                UUID.nameUUIDFromBytes("DefaultDoublerMapper.out".getBytes()).toString(),
                "out",
                "Mapper output value (key is per default string)",
                MapManager.map(BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble"), DataType.class)
            )
        );
        doubleMapper.setArguments(Collections.singletonList(DefaultDoubleIntegerMapper.class.getCanonicalName()));
        return doubleMapper;
    }

    private Routine createPICalcRoutine(RoutineBinary binary) throws UnknownDataTypeException {
        Routine piCalc = new Routine();
        piCalc.setType(RoutineType.OBJECTIVE);
        piCalc.setId(UUID.nameUUIDFromBytes(PICalc.class.getCanonicalName().getBytes()).toString());
        piCalc.setName(PICalc.class.getSimpleName());
        piCalc.setDescription("Calculates PI distributed");
        List<FormalParameter> piCalcIn = new LinkedList<>();
        piCalcIn.add(new FormalParameter(
            UUID.nameUUIDFromBytes("PICalc.start".getBytes()).toString(),
            "start",
            "Start value (calculation range)",
            MapManager.map(BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble"), DataType.class)
        ));
        piCalcIn.add(new FormalParameter(
            UUID.nameUUIDFromBytes("PICalc.end".getBytes()).toString(),
            "end",
            "End value (calculation range)",
            MapManager.map(BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble"), DataType.class)
        ));
        piCalcIn.add(new FormalParameter(
            UUID.nameUUIDFromBytes("PICalc.stepSize".getBytes()).toString(),
            "stepSize",
            "Step size for the given range",
            MapManager.map(BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble"), DataType.class)
        ));
        piCalc.setInParameters(piCalcIn);
        piCalc.setOutParameter(new FormalParameter(
            UUID.nameUUIDFromBytes("PICalc.result".getBytes()).toString(),
            "result",
            "Part of PI result",
            MapManager.map(BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble"), DataType.class)
        ));
        piCalc.setRoutineBinaries(Collections.singletonList(binary));
        piCalc.setArguments(Collections.singletonList(PICalc.class.getCanonicalName()));
        return piCalc;
    }

    private void addRequiredFeatures(Routine routine) {
    	if (javaFeature == null) {
			javaFeature = new Feature();
			javaFeature.setName("java");
			javaFeature.setVersion(">1.8");
			javaFeature.setId(UUID.nameUUIDFromBytes("java(>1.8)".getBytes()).toString());
			javaFeature.setGroup("language");
		}
        Set<Feature> features = new HashSet<>();
        features.add(javaFeature);
    	routine.setRequiredFeatures(features);
	}
}
