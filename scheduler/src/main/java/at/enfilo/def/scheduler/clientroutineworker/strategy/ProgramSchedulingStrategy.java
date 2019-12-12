package at.enfilo.def.scheduler.clientroutineworker.strategy;

import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import at.enfilo.def.clientroutine.worker.api.IClientRoutineWorkerServiceClient;
import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.NodeCommunicationException;
import at.enfilo.def.scheduler.clientroutineworker.api.ProgramOperationException;
import at.enfilo.def.scheduler.clientroutineworker.api.strategy.IProgramSchedulingStrategy;
import at.enfilo.def.scheduler.general.strategy.SchedulingStrategy;
import at.enfilo.def.scheduler.general.util.SchedulerConfiguration;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Abstract base class for every program scheduler implementation
 */
public abstract class ProgramSchedulingStrategy extends SchedulingStrategy<IClientRoutineWorkerServiceClient, ClientRoutineWorkerServiceClientFactory>
implements IProgramSchedulingStrategy {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ProgramSchedulingStrategy.class);

    private final Set<String> users;
    private final Map<String, ProgramDTO> programs;

    /**
     * Constructor for implementation
     */
    protected ProgramSchedulingStrategy(SchedulerConfiguration schedulerConfiguration) {
        this(
                Collections.synchronizedSet(new HashSet<>()),
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>(),
                new ClientRoutineWorkerServiceClientFactory(),
                null,
                schedulerConfiguration
        );
    }

    /**
     * Constructor for unit test
     */
    protected ProgramSchedulingStrategy(
        Set<String> users,
        Map<String, ProgramDTO> programs,
        Map<String, IClientRoutineWorkerServiceClient> clientRoutineWorkers,
        Map<String, Environment> clientRoutineWorkerEnvironments,
        ClientRoutineWorkerServiceClientFactory factory,
        ILibraryServiceClient libraryServiceClient,
        SchedulerConfiguration schedulerConfiguration
    ) {
        super(
                clientRoutineWorkers,
                clientRoutineWorkerEnvironments,
                factory,
                libraryServiceClient,
                schedulerConfiguration);
        this.users = users;
        this.programs = programs;
    }

    /**
     * Adds a new client routine worker to this scheduler
     *
     * @param wId - new client routine worker node id.
     * @param endpoint - ServiceEndpoint of new node.
     * @throws NodeCommunicationException
     */
    @Override
    public void addClientRoutineWorker(String wId, ServiceEndpointDTO endpoint)
        throws NodeCommunicationException {

        LOGGER.debug(
                "Trying to add new client routine worker with id {} and endpoint ot scheduler.",
                wId,
                endpoint
        );
        addNode(wId, endpoint);

        try {
            IClientRoutineWorkerServiceClient serviceClient = getNodeClient(wId);
            List<Future<Void>> futures = new LinkedList<>();

            // Create all queues on node
            LOGGER.debug("Creating all queues on client routine worker {}.", wId);
            for (String uId: users) {
                Future<Void> futureCreateQueue = serviceClient.createQueue(uId);
                futures.add(futureCreateQueue);
            }

            // Wait for all futures
            for (Future<Void> future: futures) {
            	future.get();
            }
            LOGGER.info("Client routine worker {} was added.", wId);

        } catch (ExecutionException | ClientCommunicationException | UnknownNodeException e) {
            LOGGER.error("Error while adding client routine worker {} to scheduler.", wId, e);
            throw new NodeCommunicationException(e);
        } catch (InterruptedException e) {
            LOGGER.error("Error while adding client routine worker {} to scheduler. Interrupted.", wId, e);
            Thread.currentThread().interrupt();
            throw new NodeCommunicationException(e);
        }
    }

    @Override
    public void removeClientRoutineWorker(String wId) {
        removeNode(wId);
    }

    @Override
    public final void addUser(String uId) throws NodeCommunicationException {
        if (!users.contains(uId)) {
            users.add(uId);

            LOGGER.info("Added user {} to scheduler.", uId);

            try {
                // Create queues for new user on every node
                LOGGER.debug("Create queue for user {} on all client routine workers.", uId);

                List<Future<Void>> futures = new LinkedList<>();
                for (String nId: getNodes()) {
                    IClientRoutineWorkerServiceClient serviceClient = getNodeClient(nId);
                    futures.add(serviceClient.createQueue(uId));
                }
                LOGGER.debug("Create queue for user {} on all client routine workers successfully requested.", uId);

                for (Future<Void> futureCreateQueue: futures) {
                	futureCreateQueue.get();
                }
                LOGGER.info("Successfully created queue {} on all client routine workers.", uId);

            } catch (ClientCommunicationException | UnknownNodeException | ExecutionException e) {
                String msg = String.format("Error while creating queue {} on client routine workers.", uId);
                LOGGER.error(msg, e);
                throw new NodeCommunicationException(msg, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();;
                String msg = String.format("Error while creating queue {} on client routine workers.", uId);
                LOGGER.error(msg, e);
                throw new NodeCommunicationException(msg, e);
            }
        } else {
            LOGGER.warn("User {} already known by this scheduler.", uId);
        }
    }

    @Override
    public final void removeUser(String uId) throws NodeCommunicationException {
        LOGGER.debug("Trying to remove user {}.", uId);
        if (!users.contains(uId)) {
            throw new NodeCommunicationException(String.format("User with id %s not known.", uId));
        }

        users.remove(uId);

        try {
            LOGGER.debug("Try to delete all queues belonging to given user from client routine workers.");
            List<Future<Void>> futures = new LinkedList<>();

            for (String nId: getNodes()) {
                IClientRoutineWorkerServiceClient serviceClient = getNodeClient(nId);
                Future<Void> future = serviceClient.deleteQueue(uId);
                futures.add(future);
            }

            for (Future<Void> futureCreateQueue: futures) {
            	futureCreateQueue.get();
            }

            LOGGER.info("Removed queue {} on all client routine workers.", uId);
        } catch (ExecutionException | UnknownNodeException | ClientCommunicationException e) {
            String msg = String.format("Error while removing queue {} on client routine workers.", uId);
            LOGGER.error(msg, e);
            throw new NodeCommunicationException(msg, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String msg = String.format("Error while removing queue {} on client routine workers.", uId);
            LOGGER.error(msg, e);
            throw new NodeCommunicationException(msg, e);
        }
    }

    @Override
    public final void scheduleProgram(String uId, ProgramDTO program)
    throws ProgramOperationException {

        LOGGER.debug(
                "User: {}. Received program {} for scheduling.",
                uId,
                program.getId()
        );

        if (!users.contains(uId)) {
            String msg = String.format("User {} not known, call addUser() first.", uId);
            LOGGER.error(msg);
            throw new ProgramOperationException(new UnknownProgramException(msg));
        }

        LOGGER.debug("Scheduling program to client routine workers.");

        String wId = nextClientRoutineWorkerId();
        scheduleProgramToClientRoutineWorker(wId, uId, program);
        LOGGER.info("Scheduled program {} successfully to client routine worker {}.", program.getId(), wId);
    }

    /**
     * Helper method that actually schedules a program to a node.
     *
     * @param nId - node id for scheduling
     * @param qId - queue id for scheduling
     * @param program - program to schedulePrograms
     * @throws ProgramOperationException
     */
    protected void scheduleProgramToClientRoutineWorker(String nId, String qId, ProgramDTO program)
    throws ProgramOperationException {
        LOGGER.debug("Try to schedulePrograms program {} on client routine worker {}.", program.getId(), nId);
        try {
            IClientRoutineWorkerServiceClient client = getNodeClient(nId);
            Future<Void> future = client.queueProgram(qId, program);
            future.get(); // Wait for ticket.
            LOGGER.debug("Scheduling program {} successfully executed on node {}.", program.getId(), nId);
        } catch (ExecutionException | UnknownNodeException | ClientCommunicationException e) {
            LOGGER.error("Error during scheduling program {} on client routine worker {}.", program.getId(), nId, e);
            throw new ProgramOperationException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error during scheduling program {} on client routine worker {}.", program.getId(), nId, e);
            throw new ProgramOperationException(e);
        }
    }

    @Override
    public void abortProgram(String wId, String pId) throws ProgramOperationException {
        try {
            IClientRoutineWorkerServiceClient serviceClient = getNodeClient(wId);
            Future<Void> future = serviceClient.abortProgram(pId);
            future.get();
            LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Aborting program successfully executed on client routine worker {}.", wId);
        } catch (ExecutionException | UnknownNodeException | ClientCommunicationException e) {
            LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Error while aborting program {} on client routine worker {}.", pId, wId, e);
            throw new ProgramOperationException(e);
        } catch (InterruptedException e) {
            LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Error while aborting program {} on client routine worker {}. Interrupted.", pId, wId, e);
            Thread.currentThread().interrupt();
            throw new ProgramOperationException(e);
        }
    }

    /**
     * Get list of registered node ids.
     *
     * @return list of node ids.
     */
    protected List<String> getClientRoutineWorkers() { return getNodes(); }

    /**
     * Returns a list of active users.
     *
     * @return
     */
    @Override
    public List<String> getUsers() { return new LinkedList<>(users); }

    /**
     * Returns node id for scheduling next program.
     *
     * @return - most suited node id for next program.
     */
    public abstract String nextClientRoutineWorkerId();
}
