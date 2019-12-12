package at.enfilo.def.library.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.library.api.thrift.LibraryAdminService;
import at.enfilo.def.transfer.dto.RoutineBinaryChunkDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import org.apache.thrift.TException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/library")
public interface ILibraryAdminService extends ILibraryService, LibraryAdminService.Iface, IResource {
	@POST
	@Path("/masterLibrary")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String setMasterLibrary(ServiceEndpointDTO serviceEndPoint);

	@GET
	@Path("/masterLibrary")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String getMasterLibrary();

	@GET
	@Path("/routines")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String findRoutines(@QueryParam("searchPattern") String searchPattern);

	@DELETE
	@Path("/routines/{rId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String removeRoutine(@PathParam("rId") String rId);

	@POST
	@Path("/routines")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String createRoutine(RoutineDTO routineDTO);

	@PUT
	@Path("/routines")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String updateRoutine(RoutineDTO routineDTO);

	@POST
	@Path("/routines/{rId}/binaries")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String createRoutineBinary(
		@PathParam("rId") String rId,
		@QueryParam("name") String name,
		@QueryParam("md5") String md5,
        @QueryParam("sizeInBytes") long sizeInBytes,
		@QueryParam("isPrimary") boolean isPrimary
	);

	@POST
	@Path("/routines/binaries/{rbId}/chunks")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String uploadRoutineBinaryChunk(@PathParam("rbId") String rbId, RoutineBinaryChunkDTO chunk);

	@GET
	@Path("/routines/binaries/{rbId}/verify")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String verifyRoutineBinary(String rbId) throws TException;

	@DELETE
	@Path("/routines/{rId}/binaries/{rbId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String removeRoutineBinary(@PathParam("rId") String rId, @PathParam("rbId") String bId);

	@GET
	@Path("/datatypes")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String findDataTypes(@QueryParam("searchPattern") String searchPattern);

	@POST
	@Path("/datatypes")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createDataType(
		@QueryParam("name") String name,
		String schema
	);

	@GET
	@Path("/datatypes/{dId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getDataType(@PathParam("dId") String dId);

	@DELETE
	@Path("/datatypes/{dId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String removeDataType(@PathParam("dId") String dId);

	@GET
	@Path("/tags")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String findTags(@QueryParam("searchPattern") String searchPattern);

	@POST
	@Path("/tags")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createTag(
		@FormParam("label") String label,
		@FormParam("description") String description
	);

	@DELETE
	@Path("/tags/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String removeTag(@PathParam("label") String label);

	@POST
	@Path("/features")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createFeature(
			@FormParam("name") String name,
			@FormParam("group") String group,
			@FormParam("version") String version
	);

	@POST
	@Path("/features/extension")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String addExtension(
			@FormParam("featureId") String featureId,
			@FormParam("name") String name,
			@FormParam("version") String version
	);

	@GET
	@Path("/features")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getFeatures(@QueryParam("pattern") String pattern);

	@GET
	@Path("/features/{name}/{version}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getFeatureByNameAndVersion(@PathParam("name") String name, @PathParam("version") String version);
}
