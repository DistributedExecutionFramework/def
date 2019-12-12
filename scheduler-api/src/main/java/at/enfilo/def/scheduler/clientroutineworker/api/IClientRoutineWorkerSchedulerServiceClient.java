package at.enfilo.def.scheduler.clientroutineworker.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.concurrent.Future;

public interface IClientRoutineWorkerSchedulerServiceClient extends IServiceClient {

    /**
     * Adds a client routine worker with the given id to scheduling strategy.
     *
     * @param wId - id of client routine worker node
     * @param endpoint - service endpoint of client routine worker node
     */
    Future<Void> addClientRoutineWorker(String wId, ServiceEndpointDTO endpoint) throws ClientCommunicationException;

    /**
     * Removes a client routine worker with the given id from scheduling strategy.
     *
     * @param wId - id of client routine worker node
     */
    Future<Void> removeClientRoutineWorker(String wId) throws ClientCommunicationException;

    /**
     * Add a new user to scheduling strategy.
     *
     * @param uId - user id
     */
    Future<Void> addUser(String uId) throws ClientCommunicationException;

    /**
     * Remove a user with given id from scheduling strategy.
     *
     * @param uId - user id
     */
    Future<Void> removeUser(String uId) throws ClientCommunicationException;

    /**
     * Abort a given program, including all reduce jobs on reducers and tasks on workers.
     *
     * @param wId - id of client routine worker node
     * @param pId - program id
     */
    Future<Void> abortProgram(String wId, String pId) throws ClientCommunicationException;

    /**
     * Schedule a given program.
     *
     * @param uId - user id
     * @param program - program to schedule
     */
    Future<Void> scheduleProgram(String uId, ProgramDTO program) throws ClientCommunicationException;
}
