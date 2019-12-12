package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.server.ClientRoutineWorker;
import at.enfilo.def.clientroutine.worker.util.ClientRoutineWorkerConfiguration;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.ExecutorService;
import at.enfilo.def.node.impl.IStateChangeListener;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.routine.exec.SequenceStepsBuilder;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.dto.RoutineType;
import org.apache.thrift.TDeserializer;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Picks one by one program with a client routine from queuePriorityWrappe and executes / runs it.
 * A program with a client routine consists of one routine type: an ObjectiveRoutine
 */
public class ProgramExecutorService extends ExecutorService<ProgramDTO> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ProgramExecutorService.class);
    private static ClientRoutineWorkerConfiguration CONFIGURATION;
    private final TDeserializer deserializer;

    static {
        CONFIGURATION = ClientRoutineWorker.getInstance().getConfiguration();
    }

    private final RoutineProcessBuilderFactory routineProcessBuilderFactory;

    public ProgramExecutorService(
        QueuePriorityWrapper<ProgramDTO> queuePriorityWrapper,
        RoutineProcessBuilderFactory routineProcessBuilderFactory,
        String storeRoutineId,
        IStateChangeListener stateChangeListener
    ) {
        super(
                queuePriorityWrapper,
                storeRoutineId,
                stateChangeListener,
                ClientRoutineWorkerServiceController.DTO_PROGRAM_CACHE_CONTEXT,
                ProgramDTO.class
        );
        this.routineProcessBuilderFactory = routineProcessBuilderFactory;
        this.deserializer = new TDeserializer();
    }

    /**
     * Constructor for unit testing
     */
    protected ProgramExecutorService(
            QueuePriorityWrapper<ProgramDTO> queuePriorityWrapper,
            RoutineProcessBuilderFactory routineProcessBuilderFactory,
            String storeRoutineId,
            IStateChangeListener stateChangeListener,
            ClientRoutineWorkerConfiguration configuration,
            TDeserializer deserializer
    ) {
        super(
                queuePriorityWrapper,
                storeRoutineId,
                stateChangeListener,
                ClientRoutineWorkerServiceController.DTO_PROGRAM_CACHE_CONTEXT,
                ProgramDTO.class
        );
        this.routineProcessBuilderFactory = routineProcessBuilderFactory;
        this.deserializer = deserializer;
        CONFIGURATION = configuration;
    }

    @Override
    protected void logInfo(String message) {
        LOGGER.info(message);
    }

    @Override
    protected void logInfo(ProgramDTO program, String message) {
        LOGGER.info(DEFLoggerFactory.createProgramContext(program.getId()), message);
    }

    @Override
    protected void logError(String message, Exception e) {
        LOGGER.error(message, e);
    }

    @Override
    protected void logError(ProgramDTO program, String message, Exception e) {
        LOGGER.error(DEFLoggerFactory.createProgramContext(program.getId()), message, e);
    }

    @Override
    protected String getElementId(ProgramDTO program) {
        return program.getId();
    }

    @Override
    protected ExecutionState getElementState(ProgramDTO program) {
        return program.getState();
    }

    @Override
    protected SequenceStepsExecutor buildSequenceStepsExecutor(ProgramDTO program) {
        return new SequenceStepsBuilder(program.getId(), CONFIGURATION)
                .appendStep(program.getClientRoutineId(), RoutineType.CLIENT)
                .build(program, this.routineProcessBuilderFactory);
    }

    @Override
    protected void prepareElementForExecution(ProgramDTO program) {
        program.setState(ExecutionState.RUN);
    }

    @Override
    protected List<Result> executeElement(ProgramDTO program, SequenceStepsExecutor executor) throws Exception {
        executor.getCommunicator().addParameter("program", createResourceDTO(program));
        executor.getCommunicator().addParameter("serviceEndpoint", createResourceDTO(CONFIGURATION.getClusterEndpoint()));
        executor.getCommunicator().addParameter("parameterServerEndpoint", createResourceDTO(CONFIGURATION.getParameterServerEndpoint()));

        List<Consumer<String>> processOutputConsumers = Collections.singletonList(program::addToMessages);
        return executor.run(processOutputConsumers, processOutputConsumers);
    }

    @Override
    protected void handleSuccessfulExecutionOfElement(ProgramDTO program, List<Result> results) throws Exception {
        extractResults(program, results);
        program.setState(ExecutionState.SUCCESS);
    }

    @Override
    protected void handleFailedExecutionOfElement(ProgramDTO program, Exception e) {
        program.setState(ExecutionState.FAILED);
        program.addToMessages(e.getMessage());
    }

    protected void extractResults(ProgramDTO program, List<Result> results) throws Exception {
        if (results.size() != 1) {
            throw new IllegalArgumentException("Results list should contain exactly one result.");
        }
        deserializer.deserialize(program, results.get(0).getData());
    }

    /**
     * Method for unit testing
     *
     * @param program
     */
    protected void runProgram(ProgramDTO program) {
        super.run(program);
    }
}
