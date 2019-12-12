package at.enfilo.def.scheduler.clientroutineworker.api.strategy;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.node.api.exception.NodeCommunicationException;
import at.enfilo.def.scheduler.clientroutineworker.api.ProgramOperationException;
import at.enfilo.def.scheduler.worker.api.TaskOperationException;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.Collection;
import java.util.List;

public interface IProgramSchedulingStrategy {

    /**
     * Add (involve) node into scheduling process.
     *
     * @param wId - new client routine worker node id.
     * @param endpoint - ServiceEndpoint of new node.
     */
    void addClientRoutineWorker(String wId, ServiceEndpointDTO endpoint) throws NodeCommunicationException;

    /**
     * Removes a node from scheduling process.
     *
     * @param wId - client routine worker to remove.
     */
    void removeClientRoutineWorker(String wId);

    /**
     * Adds a user to this scheduler.
     *
     * @param uId - job to add.
     */
    void addUser(String uId) throws NodeCommunicationException;

    /**
     * Removes all user queues.
     *
     * @param uId - user id.
     * @throws NodeCommunicationException
     */
    void removeUser(String uId) throws NodeCommunicationException;

    /**
     * Schedule given programs.
     *
     * @param uId  - user id.
     * @param program - programs to schedulePrograms.
     * @throws TaskOperationException
     */
    void scheduleProgram(String uId, ProgramDTO program) throws ProgramOperationException;

    /**
     * Abort given program on given cluster routine worker.
     *
     * @param wId - cluster routine worker id program was scheduled on
     * @param pId - program id to abort
     */
    void abortProgram(String wId, String pId) throws ProgramOperationException;

    /**
     * Returns a list of active users.
     * @return
     */
    List<String> getUsers();
}
