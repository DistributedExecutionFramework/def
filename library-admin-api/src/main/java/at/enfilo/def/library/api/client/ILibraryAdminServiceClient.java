package at.enfilo.def.library.api.client;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.transfer.dto.DataTypeDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.dto.TagDTO;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Future;


/**
 * Library Administration Client Interface.
 */
public interface ILibraryAdminServiceClient extends ILibraryServiceClient, IServiceClient {

	/**
	 * Find all Routines by a given searchPattern. If searchPattern is empty, all Routine (Id's) will be returned.
	 *
	 * @param searchPattern - to find in name and description
	 * @return List of Routine Id's as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<List<String>> findRoutines(String searchPattern)
    throws ClientCommunicationException;

	/**
	 * Remove the given Routine from Library.
	 *
	 * @param rId - Routine to remove
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<Void> removeRoutine(String rId)
    throws ClientCommunicationException;

	/**
	 * Creates a new Routine.
	 *
	 * @param routineDTO - Routine to be created
	 * @return Id of Routine as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<String> createRoutine(RoutineDTO routineDTO)
	throws ClientCommunicationException;

	/**
	 * Update a specified Routine. An update creates a new Routine-Version.
	 *
	 * @param routineDTO - Routine to be updated
	 * @return Id of new Routine-Version as Future.
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<String> updateRoutine(RoutineDTO routineDTO)
    throws ClientCommunicationException;

	/**
	 * Uploads a binary to a specified Routine.
	 *
	 * @param rId - Routine
     * @param md5 - Checksum as md5
     * @param sizeInBytes - Size in bytes
     * @param isPrimary - Is binary primary? (e.g. java -jar <primary.jar>)
     * @param data - Binary data
	 * @return RoutineBinary id as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<String> uploadRoutineBinary(
        String rId,
        String md5,
        long sizeInBytes,
        boolean isPrimary,
        ByteBuffer data
    )
	throws ClientCommunicationException;

	/**
	 * Remove a specified RoutineBinary from Routine.
	 *
	 * @param rId - Routine
	 * @param bId - RoutineBinary to remove
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<Void> removeRoutineBinary(String rId, String bId)
	throws ClientCommunicationException;

	/**
	 * Find all DataTypes which match the searchPattern. Empty searchPattern means all DataTypes.
	 *
	 * @param searchPattern - Search pattern to search
	 * @return List of matching DataType Id's as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<List<String>> findDataTypes(String searchPattern)
    throws ClientCommunicationException;

	/**
	 * Create a new DataType.
	 *
	 * @param name - Name of DataType
	 * @param schema - Thrift Schema
	 * @return Id of new DataType as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<String> createDataType(String name, String schema)
    throws ClientCommunicationException;

	/**
	 * Returns requested DataType Object.
	 *
	 * @param dId - DataType Id
	 * @return DataType as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<DataTypeDTO> getDataType(String dId)
    throws ClientCommunicationException;

	/**
	 * Removes DataType from library.
	 *
	 * @param dId - DataType to remove
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<Void> removeDataType(String dId)
    throws ClientCommunicationException;

	/**
	 * Find all Tags matching given searchPattern. Empty Search pattern means all Tags.
	 *
	 * @param searchPattern - Search pattern to search
	 * @return List of all matched Tags as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<List<TagDTO>> findTags(String searchPattern)
    throws ClientCommunicationException;

	/**
	 * Creates a new Tag.
	 *
	 * @param label - Label / Name of a Tag
     * @param description - Description of a Tag
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<Void> createTag(String label, String description)
	throws ClientCommunicationException;

	/**
	 * Remove a Tag from library.
	 *
	 * @param label - Tag to remove
	 * @return TicketStatus as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<Void> removeTag(String label)
	throws ClientCommunicationException;

	/**
	 * Create a feature.
	 *
	 * @param name - feature name
	 * @param group - feature group
	 * @param version - feature version
	 * @return Feature id as Future
	 * @throws ClientCommunicationException if some error occurred while communicating with service.
	 */
	Future<String> createFeature(String name, String group, String version)
	throws ClientCommunicationException;

	/**
	 * Create an extension.
	 *
	 * @param name - extension name
	 * @param featureId - feature id
	 * @param version - extension version
	 * @return Extension id as Future
	 * @throws ClientCommunicationException if some error occurred while communicating with service.
	 */
	Future<String> addExtension(String featureId, String name, String version)
	throws ClientCommunicationException;

	/**
	 * Get features from library.
	 *
	 * @param pattern - Search pattern
	 * @return List of FeatureDTOs as Future
	 * @throws ClientCommunicationException if some error occurred while communicate with service.
	 */
	Future<List<FeatureDTO>> getFeatures(String pattern)
	throws ClientCommunicationException;
}
