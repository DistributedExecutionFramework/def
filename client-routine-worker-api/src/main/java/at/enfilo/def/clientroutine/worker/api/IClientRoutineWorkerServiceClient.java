package at.enfilo.def.clientroutine.worker.api;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.node.api.INodeServiceClient;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.List;
import java.util.concurrent.Future;

public interface IClientRoutineWorkerServiceClient extends INodeServiceClient {

    /**
     * Requests all queued program IDS
     *
     * @return  Future with a {@link List} of program IDs as {@link String}
     */
    Future<List<String>> getQueuedPrograms(String qId) throws ClientCommunicationException;

    /**
     * Queue a program with a client routine
     *
     * @param qId       The id of the queue
     * @param program   Program as {@link ProgramDTO} to queue
     */
    Future<Void> queueProgram(String qId, ProgramDTO program) throws ClientCommunicationException;

    /**
     * Queues a list of program with a client routine
     *
     * @param qId       The id of the queue
     * @param programs  Programs as {@link ProgramDTO} to queue
     */
    Future<Void> queuePrograms(String qId, List<ProgramDTO> programs) throws ClientCommunicationException;

    /**
     * Moves a list of programs to another node.
     *
     * @param qId                   The id of the queue
     * @param programIds            Program ids as {@link List<String>} to move
     * @param targetNodeEndpoint    The node to move the programs to as {@link ServiceEndpointDTO}
     */
    Future<Void> movePrograms(String qId, List<String> programIds, ServiceEndpointDTO targetNodeEndpoint) throws ClientCommunicationException;

    /**
     * Moves all programs to another node.
     *
     * @param targetNodeEndpoint    The node to move the programs to as {@link ServiceEndpointDTO}
     */
    Future<Void> moveAllPrograms(ServiceEndpointDTO targetNodeEndpoint) throws ClientCommunicationException;

    /**
     *  Fetches and removes a finished program from this node.
     *
     * @param pId   Program ID
     * @return      Program with the given ID of type {@link ProgramDTO}
     */
    Future<ProgramDTO> fetchFinishedProgram(String pId) throws ClientCommunicationException;

    /**
     * Aborts the program with the given ID.
     *
     * @param pId   Program ID
     */
    Future<Void> abortProgram(String pId) throws ClientCommunicationException;

}
