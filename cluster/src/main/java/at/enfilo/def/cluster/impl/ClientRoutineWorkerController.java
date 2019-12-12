package at.enfilo.def.cluster.impl;

import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import at.enfilo.def.clientroutine.worker.api.IClientRoutineWorkerServiceClient;
import at.enfilo.def.cluster.api.NodeCreationException;
import at.enfilo.def.cluster.api.NodeExecutionException;
import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.configuration.ClientRoutineWorkersConfiguration;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.scheduler.clientroutineworker.api.IClientRoutineWorkerSchedulerServiceClient;
import at.enfilo.def.transfer.dto.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ClientRoutineWorkerController extends NodeController<IClientRoutineWorkerServiceClient, ClientRoutineWorkerServiceClientFactory> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ClientRoutineWorkerController.class);

    private static ClientRoutineWorkerController instance;

    private final Map<String, Set<String>> workerProgramAssignment;
    private final IClientRoutineWorkerSchedulerServiceClient schedulerServiceClient;

    private static final Object INSTANCE_LOCK = new Object();
    private final Object assignmentLock;

    public static ClientRoutineWorkerController getInstance() {
        synchronized (INSTANCE_LOCK) {
            if (instance == null) {
                instance = new ClientRoutineWorkerController();
            }
            return instance;
        }
    }

    private ClientRoutineWorkerController() {
        this (
                new ClientRoutineWorkerServiceClientFactory(),
                new LinkedList<>(),
                new HashMap<>(),
                new HashMap<>(),
                null, // null means a TimeoutMap will be created,
                new HashMap<>(),
                Cluster.getInstance().getConfiguration().getClientRoutineWorkersConfiguration(),
                new HashMap<>()
        );
    }

    /**
     * Internal or constructor for unit tests only
     */
    protected ClientRoutineWorkerController(
            ClientRoutineWorkerServiceClientFactory clientFactory,
            List<String> clientRoutineWorkers,
            Map<String, String> clientRoutineWorkerInstanceMap,
            Map<String, IClientRoutineWorkerServiceClient> clientRoutineWorkerConnectionMap,
            Map<String, NodeInfoDTO> clientRoutineWorkerInfoMap,
            Map<String, List<FeatureDTO>> clientRoutineWorkerFeatureMap,
            ClientRoutineWorkersConfiguration configuration,
            Map<String, Set<String>> workerProgramAssignment
    ) {
        super(
                NodeType.CLIENT,
                clientFactory,
                clientRoutineWorkers,
                clientRoutineWorkerInstanceMap,
                clientRoutineWorkerConnectionMap,
                clientRoutineWorkerInfoMap,
                clientRoutineWorkerFeatureMap,
                configuration,
                configuration.getStoreRoutineId()
        );
        this.assignmentLock = new Object();
        this.workerProgramAssignment = workerProgramAssignment;
        this.schedulerServiceClient = ClusterResource.getInstance().getClientRoutineWorkerSchedulerSerivceClient();
    }

    public String addClientRoutineWorker(ServiceEndpointDTO serviceEndpoint) throws NodeCreationException {
        return super.addNode(serviceEndpoint);
    }

    public String getStoreRoutineId() { return super.getStoreRoutineId(); }

    public void setStoreRoutineId(String storeRoutineId) { super.setStoreRoutineId(storeRoutineId);}

    public void distributeSharedResource(ResourceDTO sharedResource) { super.distributeSharedResource(sharedResource);}

    public void removeSharedResources(List<String> sharedResources) { super.removeSharedResources(sharedResources);}

    protected void removeNodeAssignments(String nId) throws UnknownNodeException {
        // Re-schedule program if needed
        Set<String> programIds = new HashSet<>();
        synchronized (assignmentLock) {
            if (workerProgramAssignment.containsKey(nId)) {
                programIds.addAll(workerProgramAssignment.get(nId));
                workerProgramAssignment.remove(nId);
            }
        }
        if (!programIds.isEmpty()) {
            ClusterExecLogicController.getInstance().reSchedulePrograms(programIds);
        }
    }

    public void removeNode(String nId) throws UnknownNodeException {
        super.removeNode(nId, true);
    }

    /**
     * Notification from a client-routine-worker about newly received programs.
     *
     * @param wId - client-routine-worker id
     * @param programIds - list of program ids
     * @throws UnknownNodeException
     */
    public void notifyProgramsReceived(String wId, List<String> programIds) throws UnknownNodeException {
        setupWorkerProgramAssignment(wId);
        synchronized (assignmentLock) {
            programIds.forEach(workerProgramAssignment.get(wId)::add);
        }
    }

    private void setupWorkerProgramAssignment(String wId) throws UnknownNodeException {
        synchronized (nodeLock) {
            if (!nodeInfoMap.containsKey(wId)) {
                LOGGER.error(String.format(UNKNOWN_NODE, wId));
                throw new UnknownNodeException(String.format(UNKNOWN_NODE, wId));
            }
        }
        synchronized (assignmentLock) {
            if (!workerProgramAssignment.containsKey(wId)) {
                workerProgramAssignment.put(wId, new HashSet<>());
            }
        }
    }

    public void notifyProgramsNewState(String wId, List<String> programIds, ExecutionState newState) throws UnknownNodeException {
        LOGGER.debug("Notify programs have new state {} from client-routine-worker with id {}.", newState, wId);
        switch (newState) {
            case SUCCESS:
            case FAILED:
                setupWorkerProgramAssignment(wId);
                synchronized (assignmentLock) {
                    programIds.forEach(workerProgramAssignment.get(wId)::remove);
                }
                break;

            case SCHEDULED:
            case RUN:
            default:
                // Ignoring this states
                break;
        }
        LOGGER.debug("Notify programs new state successful.");
    }

    public void abortProgram(String pId) throws NodeExecutionException {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Aborting program on client routine worker.");
        String wId = null;
        synchronized (assignmentLock) {
            for (Map.Entry<String, Set<String>> e : workerProgramAssignment.entrySet()) {
                if (e.getValue().contains(pId)) {
                    wId = e.getKey();
                    break;
                }
            }
        }

        if (wId != null) {
            synchronized (nodeLock) {
                try {
                    Future<Void> future = schedulerServiceClient.abortProgram(wId, pId);
                    future.get(); // Wait for done.
                } catch (ExecutionException | ClientCommunicationException e) {
                    LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Error while sending abort program to client routine worker {}", wId, e);
                    throw new NodeExecutionException(e);
                } catch (InterruptedException e) {
                	Thread.currentThread().interrupt();
                    LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Error while sending abort program to client routine worker {}. Interrupted.", wId, e);
                    throw new NodeExecutionException(e);
                }
            }
        }
    }

    public void runProgram(ProgramDTO program) throws NodeExecutionException {
        if (program != null) {
            LOGGER.debug(DEFLoggerFactory.createProgramContext(program.getId()), "Scheduling program to client routine workers.");
            try {
                Future<Void> future = schedulerServiceClient.scheduleProgram(program.getUserId(), program);
                future.get();
            } catch (ExecutionException | ClientCommunicationException e) {
                LOGGER.error(DEFLoggerFactory.createProgramContext(program.getId()), "Error while scheduling program.", e);
                throw new NodeExecutionException(e);
            } catch (InterruptedException e) {
            	Thread.currentThread().interrupt();
                LOGGER.error(DEFLoggerFactory.createProgramContext(program.getId()), "Error while scheduling program. Interrupted.", e);
                throw new NodeExecutionException(e);
            }
        } else {
            String msg = "Cannot run/schedule Program. Program is null";
            LOGGER.error(msg);
            throw new NodeExecutionException(msg);
        }
    }

    public void addUser(String uId) throws NodeExecutionException {
        LOGGER.debug("Adding user {} to client routine workers.", uId);
        try {
            Future<Void> future = schedulerServiceClient.addUser(uId);
            future.get();
        } catch (ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error while adding new user {}.", uId, e);
            throw new NodeExecutionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error while adding new user {}. Interrupted.", uId, e);
            throw new NodeExecutionException(e);
        }
    }

    public void removeUser(String uId, Collection<String> runningPrograms) throws NodeExecutionException {
        LOGGER.debug("Removing user {} from client routine workers.", uId);
        try {
            Future<Void> future = schedulerServiceClient.removeUser(uId);
            future.get(); // Wait for ticket

            for (String pId : runningPrograms) {
                abortProgram(pId);
            }
        } catch (ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error while removing user {}.", uId, e);
            throw new NodeExecutionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error while removing user {}. Interrupted.", uId, e);
            throw new NodeExecutionException(e);
        }
    }

    public ProgramDTO fetchFinishedProgram(String wId, String pId) throws UnknownNodeException, NodeExecutionException {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching finished program.");
        try {
            return getServiceClient(wId).fetchFinishedProgram(pId).get();
        } catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
            LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Error while fetching finished program from client routine worker.", e);
            throw new NodeExecutionException(e);
        }
    }
}
