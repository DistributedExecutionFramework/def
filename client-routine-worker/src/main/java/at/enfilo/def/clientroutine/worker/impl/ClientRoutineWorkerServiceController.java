package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import at.enfilo.def.clientroutine.worker.api.IClientRoutineWorkerServiceClient;
import at.enfilo.def.clientroutine.worker.queue.ProgramQueue;
import at.enfilo.def.clientroutine.worker.server.ClientRoutineWorker;
import at.enfilo.def.clientroutine.worker.util.ClientRoutineWorkerConfiguration;
import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.QueueNotExistsException;
import at.enfilo.def.node.impl.ExecutorService;
import at.enfilo.def.node.impl.NodeServiceController;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.queue.Queue;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeType;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ClientRoutineWorkerServiceController extends NodeServiceController<ProgramDTO> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ClientRoutineWorkerServiceController.class);

    public static final String DTO_PROGRAM_CACHE_CONTEXT = "node-programs";
    private static final String ELEMENT_NAME = "Program";

    private final QueuePriorityWrapper<ProgramDTO> queuePriorityWrapper;
    protected final List<ProgramExecutorService> programExecutorServices;
    private final ClientRoutineWorkerServiceClientFactory clientRoutineWorkerClientFactory;

    /**
     * Private class to provide thread safe singleton
     */
    private static class ThreadSafeLazySingletonWrapper {
        private static final ClientRoutineWorkerServiceController INSTANCE = new ClientRoutineWorkerServiceController();

        private ThreadSafeLazySingletonWrapper() {}
    }

    /**
     * Singleton pattern
     * @return an instance of {@link ClientRoutineWorkerServiceController}
     */
    static ClientRoutineWorkerServiceController getInstance() {
        return ClientRoutineWorkerServiceController.ThreadSafeLazySingletonWrapper.INSTANCE;
    }



    /**
     * Singleton, hide constructor
     */
    private ClientRoutineWorkerServiceController() {
        this(
                new QueuePriorityWrapper<>(ClientRoutineWorker.getInstance().getConfiguration()),
                Collections.synchronizedSet(new HashSet<>()),
                new LinkedList<>(),
                new ClientRoutineWorkerServiceClientFactory(),
                ClientRoutineWorker.getInstance().getConfiguration(),
                new NodeObserverServiceClientFactory(),
                LOGGER
        );
    }

    /**
     * Private constructor for unit tests
     *
     */
    private ClientRoutineWorkerServiceController(
            QueuePriorityWrapper<ProgramDTO> queuePriorityWrapper,
            Set<String> finishedPrograms,
            List<INodeObserverServiceClient> observers,
            ClientRoutineWorkerServiceClientFactory clientRoutineWorkerClientFactory,
            ClientRoutineWorkerConfiguration configuration,
            NodeObserverServiceClientFactory nodeObserverServiceClientFactory,
            IDEFLogger logger
    ) {
        super(
                NodeType.CLIENT,
                observers,
                finishedPrograms,
                configuration,
                nodeObserverServiceClientFactory,
                DTO_PROGRAM_CACHE_CONTEXT,
                ProgramDTO.class,
                logger
        );
        this.queuePriorityWrapper = queuePriorityWrapper;
        this.programExecutorServices = new LinkedList<>();
        this.clientRoutineWorkerClientFactory = clientRoutineWorkerClientFactory;

        try {
            RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
                    new LibraryServiceClientFactory().createClient(configuration.getLibraryEndpoint()),
                    configuration
            );

            // Create ProgramExecutorService for all threads
            LOGGER.info("Start {} ProgramExecutorServices.", configuration.getExecutionThreads());
            for (int i = 0; i < configuration.getExecutionThreads(); i++) {
                ProgramExecutorService executorService = new ProgramExecutorService(
                        queuePriorityWrapper,
                        routineProcessBuilderFactory,
                        getStoreRoutineId(),
                        this
                );

                executorService.setName("ProgramExecutionThread " + i);
                executorService.setDaemon(true);
                executorService.start();
                programExecutorServices.add(executorService);
            }
        } catch (ClientCreationException e) {
            LOGGER.error("Error while creating LibraryServiceClient on startup.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Queue createQueueInstance(String qId) {
        return new ProgramQueue(qId);
    }

    @Override
    protected QueuePriorityWrapper getQueuePriorityWrapper() {
        return queuePriorityWrapper;
    }

    @Override
    protected List<? extends ExecutorService> getExecutorServices() {
        return programExecutorServices;
    }

    /**
     * Abort program: all running processes (sequence steps) will be killed
     * If program is not running it will be removed from the queue.
     * @param pId
     */
    public void abortProgram(String pId) {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Abort Program");
        try {
            ProgramDTO program = elementCache.fetch(pId);
            program.addToMessages("Aborted by user.");
            ExecutionState oldState = program.getState();

            abortElement(pId, program, oldState);
        } catch (IOException | UnknownCacheObjectException e) {
            LOGGER.error(getLogContext(pId), "Error while aborting program.");
        }
    }

    @Override
    public List<String> getQueueIds() {
        return queuePriorityWrapper.getAllQueues()
                .stream()
                .map(Queue::getQueueId)
                .collect(Collectors.toList());
    }

    @Override
    protected void throwException(String eId, String message) throws Exception {
        throw new UnknownProgramException(message);
    }

    @Override
    protected Set<ITuple<ContextIndicator, ?>> getLogContext(ProgramDTO program) {
        return DEFLoggerFactory.createProgramContext(program.getId());
    }

    @Override
    protected Set<ITuple<ContextIndicator, ?>> getLogContext(String pId) {
    	return DEFLoggerFactory.createProgramContext(pId);
    }

    @Override
    protected void removeElementFromQueues(String eId) {
        queuePriorityWrapper.getAllQueues().forEach(programQueue -> programQueue.remove(eId));
    }

    @Override
    protected void setState(ProgramDTO element, ExecutionState state) {
        element.setState(state);
    }

    @Override
    protected List<String> getElementIds(List<ProgramDTO> elements) {
        return elements.stream().map(ProgramDTO::getId).collect(Collectors.toList());
    }

    @Override
    protected List<? extends Queue> getQueues() throws QueueNotExistsException {
        return queuePriorityWrapper.getAllQueues();
    }

    @Override
    protected Future<Void> queueElements(String qId, List<ProgramDTO> elementsToQueue, ServiceEndpointDTO targetNodeEndpoint) throws ClientCreationException, ClientCommunicationException {
        IClientRoutineWorkerServiceClient targetNodeClient = clientRoutineWorkerClientFactory.createClient(targetNodeEndpoint);
        return targetNodeClient.queuePrograms(qId, elementsToQueue);
    }

    @Override
    protected void notifyObservers(String nId, List<String> eIds) {
        LOGGER.debug("Notify all observers.");
        notifyAllObservers(observerClient -> observerClient.notifyProgramsReceived(
               nId,
               eIds
        ));
    }

    @Override
    protected void finishedExecutionOfElement(String eId) {
        // do nothing
    }

    @Override
    protected String getElementName() {
        return ELEMENT_NAME;
    }

    /**
     * Helper function for unit testing
     */
    protected DTOCache<ProgramDTO> getProgramCache() {
        return elementCache;
    }
}
