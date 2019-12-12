package at.enfilo.def.manager.webservice.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.transfer.dto.DataTypeDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.dto.TagDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/library")
public interface ILibraryService extends IResource {

	@GET
	@Path("/routines")
	@Produces(MediaType.APPLICATION_JSON)
	List<RoutineDTO> findRoutines(@QueryParam("pattern") final String pattern) throws ManagerWebserviceException;

	@GET
	@Path("/routines/{rId}")
	@Produces(MediaType.APPLICATION_JSON)
	RoutineDTO getRoutine(@PathParam("rId") final String rId) throws ManagerWebserviceException;

	@POST
	@Path("/routines")
	@Consumes(MediaType.APPLICATION_JSON)
	String createRoutine(final RoutineDTO routine) throws ManagerWebserviceException;

	@DELETE
	@Path("/routines/{rId}")
	@Consumes(MediaType.APPLICATION_JSON)
	void removeRoutine(@PathParam("rId") final String rId) throws ManagerWebserviceException;

	@POST
	@Path("/routines/{rId}/binary")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	String createRoutineBinary(@PathParam("rId") final String rId,
							   @QueryParam("name") final String name,
							   @QueryParam("md5") final String md5,
							   @QueryParam("primary") final Boolean primary,
							   @QueryParam("sizeInBytes") final Long sizeInBytes)
			throws ManagerWebserviceException;

	@POST
	@Path("/routines/binaries/{rbId}/chunks")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	void uploadRoutineBinaryChunk(
			@PathParam("rbId") final String rbId,
			@QueryParam("chunk") final short chunk,
			@QueryParam("totalChunks") final short totalChunks,
			@QueryParam("chunkSize") final int chunkSize,
			byte[] data
	) throws ManagerWebserviceException;

	@GET
	@Path("/tags")
	@Produces(MediaType.APPLICATION_JSON)
	List<TagDTO> findTags(@QueryParam("pattern") final String pattern) throws ManagerWebserviceException;

	@GET
	@Path("/datatypes")
	@Produces(MediaType.APPLICATION_JSON)
	List<DataTypeDTO> findDataTypes(@QueryParam("pattern") final String pattern) throws ManagerWebserviceException;

	@GET
	@Path("/datatypes/{dId}")
	@Produces(MediaType.APPLICATION_JSON)
	DataTypeDTO getDataType(@PathParam("dId") final String dId) throws ManagerWebserviceException;

	@POST
	@Path("/datatypes")
	@Consumes(MediaType.TEXT_PLAIN)
	String createDataType(@QueryParam("name") final String name, final String schema) throws ManagerWebserviceException;

	@POST
	@Path("/datatypes/generate")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] generateDataTypes(@QueryParam("language") final String language, final String[] dIds) throws ManagerWebserviceException;

	@GET
	@Path("/features")
	@Produces(MediaType.APPLICATION_JSON)
	List<FeatureDTO> getFeatures(@QueryParam("pattern") final String pattern) throws ManagerWebserviceException;

	@POST
	@Path("/features")
	@Consumes(MediaType.TEXT_PLAIN)
	String createFeature(@QueryParam("name") final String name, @QueryParam("group") final String group, @QueryParam("version") final String version) throws ManagerWebserviceException;

	@POST
	@Path("/features/{featureId}/extensions")
	@Consumes(MediaType.TEXT_PLAIN)
	String addExtension(@PathParam("featureId") final String featureId, @QueryParam("name") final String name, @QueryParam("version") final String version) throws ManagerWebserviceException;

}
