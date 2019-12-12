package at.enfilo.def.parameterserver.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ParameterType;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.concurrent.Future;

public interface IParameterServerServiceClient extends IServiceClient {

    /**
     * Set the value for a parameter with the program id and parameter id given.
     **/
    Future<String> setParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol) throws ClientCommunicationException;

    /**
     * Creates a new parameter with the given program id, parameter id and type.
     **/
    Future<String> createParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol, ParameterType type) throws ClientCommunicationException;

    /**
     * Request a parameter with the given program id and parameter id.
     */
    Future<ResourceDTO> getParameter(String programId, String parameterId, ParameterProtocol protocol) throws ClientCommunicationException;

    /**
     * Update a stored parameter with the given program id and parameter id by adding the value in the given parameter.
     */
    Future<String> addToParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol) throws ClientCommunicationException;

    /**
     * Delete a parameter with the given program id and parameter id
     **/
    Future<String> deleteParameter(String programId, String parameterId) throws ClientCommunicationException;

    /**
     * Delete all stored parameters for the given programId
     **/
    Future<String> deleteAllParameters(String programId) throws ClientCommunicationException;
}
