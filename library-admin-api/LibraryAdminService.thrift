include "../transfer/DTOs.thrift"
include "../library-api/LibraryService.thrift"
include "../communication-api/CommunicationDTOs.thrift"

namespace java at.enfilo.def.library.api.thrift

/**
* Library Admin Service to manage Routines, Datatypes and Tags.
*/
service LibraryAdminService extends LibraryService.LibraryService {
    /**
    * Sets the MasterLibrary endpoint of the library for pulling routines and binaries
    **/
    DTOs.TicketId setMasterLibrary(1: CommunicationDTOs.ServiceEndpointDTO serviceEndPoint)

    /**
    * Request the MasterLibrary endpoint. Returns null, if this Library is a MasterLibrary.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getMasterLibrary();

    /**
    * Request to find all registered Routine Ids including searchPattern in name or description.
    * Empty searchPattern match all.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId findRoutines(1: string searchPattern);

    /**
    * Request to remove the specified Routine from library.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId removeRoutine(1: DTOs.Id rId);

    /**
    * Creates a new Routine and returns new RoutineId.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createRoutine(1: DTOs.RoutineDTO routineDTO);

    /**
    * Update Routine "Header".
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId updateRoutine(1: DTOs.RoutineDTO routineDTO);

    /**
    * Create a binary to the specified Routine.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createRoutineBinary(
        1: DTOs.Id rId,
        2: string name,
        3: string md5,
        4: i64 sizeInBytes,
        5: bool isPrimary
    );

    /**
    * Uploads a RoutineBinaryChunk to the given RoutineBinary (rbId).
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId uploadRoutineBinaryChunk(1: DTOs.Id rbId, 2: DTOs.RoutineBinaryChunkDTO chunk);

    /**
    * Verifies the given RoutineBinary: check size and md5 sum.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId verifyRoutineBinary(1: DTOs.Id rbId);

    /**
    * Remove a binary from specified Routine.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId removeRoutineBinary(1: DTOs.Id rId, 2: DTOs.Id bId);

    /**
    * Request to find a List of all registered DataTypes (Ids) including searchPattern in name or description.
    * Empty searchPattern match all.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId findDataTypes(1: string searchPattern);

    /**
    * Creates a new DataType
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createDataType(1: string name, 2: string schema);

    /**
    * Returns the requested DataType.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getDataType(1: DTOs.Id dId);

    /**
    * Removes the specified DataType.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId removeDataType(1: DTOs.Id dId);

    /**
    * Returns a list of all tags.
    * Empty searchPattern match all.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId findTags(1: string searchPattern);

    /**
    * Creates a new Tag.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createTag(1: string label, 2: string description);

    /**
    * Removes a Tag.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId removeTag(1: string label);

    /**
    * Creates a feature.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId createFeature(1: string name, 2: string group, 3: string version)

    /**
    * Creates an extension.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId addExtension(1: string featureId, 2: string name, 3: string version)

    /**
    * Filter features.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    */
    DTOs.TicketId getFeatures(1: string pattern)

    /**
    * Fetch a feature with a given name and version.
    * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
    **/
    DTOs.TicketId getFeatureByNameAndVersion(1: string name, 2: string version)
}


/**
* Library Service Response interface.
**/
service LibraryAdminResponseService extends LibraryService.LibraryResponseService {
    /**
    * Returns the MasterLibrary endpoint if is set, otherwise null.
    **/
    CommunicationDTOs.ServiceEndpointDTO getMasterLibrary(1: DTOs.TicketId ticketId);

    /**
    * Returns all registered Routine Ids
    **/
    list<DTOs.Id> findRoutines(1: DTOs.TicketId ticketId);

    /**
    * Returns Id of new created Routine.
    */
    DTOs.Id createRoutine(1: DTOs.TicketId ticketId);

    /**
    * Returns Id of new Routine version. Every update creates a new Routine.
    */
    DTOs.Id updateRoutine(1: DTOs.TicketId ticketId);

	/**
	 * Returns Id of newly uploaded RoutineBinary.
	 */
    DTOs.Id createRoutineBinary(1: DTOs.TicketId ticketId);

    /**
    * Returns true if size and md5 sum matches.
    **/
    bool verifyRoutineBinary(1: DTOs.TicketId ticketId);

    /**
    * Returns all registered DataType Ids
    */
    list<DTOs.Id> findDataTypes(1: DTOs.TicketId ticketId);

    /**
    * Returns Id of new created Routine.
    */
    DTOs.Id createDataType(1: DTOs.TicketId ticketId);

    /**
    * Returns the requested DataType
    */
    DTOs.DataTypeDTO getDataType(1: DTOs.TicketId ticketId);

    /**
    * Returns a list of all registered Tags
    */
    list<DTOs.TagDTO> findTags(1: DTOs.TicketId ticketId);

    /**
    * Returns a feature id.
    */
    DTOs.Id createFeature(1: DTOs.TicketId ticketId)

    /**
    * Returns an extension id.
    */
    DTOs.Id addExtension(1: DTOs.TicketId ticketId)

    /**
    * Returns filtered features.
    */
    list<DTOs.FeatureDTO> getFeatures(1: DTOs.TicketId ticketId)

    /**
    * Returns feature with given name and version.
    **/
    DTOs.FeatureDTO getFeatureByNameAndVersion(1: DTOs.TicketId ticketId)
}
