package at.enfilo.def.node.routine.factory;

import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.RoutineCreationException;
import at.enfilo.def.node.api.exception.RoutineExecutionException;
import at.enfilo.def.node.routine.exec.SequenceStep;
import at.enfilo.def.node.routine.factory.impl.GenericConfigurationProcessBuilder;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.RoutineDTO;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class RoutineProcessBuilderFactory {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(RoutineProcessBuilderFactory.class);
    private final IRoutineProcessBuilderFactory processBuilderFactory;

    private final ILibraryServiceClient libraryServiceClient;
    private final NodeConfiguration nodeConfiguration;

    public RoutineProcessBuilderFactory(ILibraryServiceClient libraryServiceClient, NodeConfiguration nodeConfiguration,
                                        IRoutineProcessBuilderFactory processBuilderFactory) {
        this.libraryServiceClient = libraryServiceClient;
        this.nodeConfiguration = nodeConfiguration;
        this.processBuilderFactory = processBuilderFactory;
    }

    public RoutineProcessBuilderFactory(ILibraryServiceClient libraryServiceClient, NodeConfiguration nodeConfiguration) {
        this(libraryServiceClient, nodeConfiguration, new GenericConfigurationProcessBuilder());
    }

    public ProcessBuilder build(Path workingDir, SequenceStep step)
            throws RoutineExecutionException, RoutineCreationException {
        try {
            // Fetch routine and create process builder
            RoutineDTO routine = libraryServiceClient.getRoutine(step.getRoutineId()).get();
            return build(workingDir, routine, step);

        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error while fetch Routine {} from Library.", step.getRoutineId(), e);
            throw new RoutineExecutionException(e);
        }
    }

    public ProcessBuilder build(Path workingDir, RoutineDTO routine, SequenceStep step)
            throws RoutineExecutionException, RoutineCreationException {

        LOGGER.debug("Creating routine process: {}, {}", routine.getName(), routine.getType());

        ProcessBuilder processBuilder;
        switch (routine.getType()) {
            case STORE:
                processBuilder = processBuilderFactory.getRoutineProcessBuilder(
                        routine, step, nodeConfiguration, false);
                break;

            case OBJECTIVE:
                processBuilder = processBuilderFactory.getRoutineProcessBuilder(
                        routine, step, nodeConfiguration, true);
                break;

            case MAP:
                processBuilder = processBuilderFactory.getRoutineProcessBuilder(
                        routine, step, nodeConfiguration, true);
                break;

            case REDUCE:
                processBuilder = processBuilderFactory.getRoutineProcessBuilder(
                        routine, step, nodeConfiguration, true);
                break;

            case CLIENT:
                processBuilder = processBuilderFactory.getRoutineProcessBuilder(
                        routine, step, nodeConfiguration, false);
                break;

            default:
                String msg = String.format("RoutineType \"%s\" is not supported.", routine.getType());
                LOGGER.error(msg);
                throw new RoutineExecutionException(msg);
        }

        // Setting working directory.
        processBuilder.directory(workingDir.toFile());

        LOGGER.debug("Command: {}", processBuilder.command());
        LOGGER.debug("Working Dir: {}", processBuilder.directory());
        return processBuilder;
    }

}

