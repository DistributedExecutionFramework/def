package at.enfilo.def.library.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.library.api.thrift.LibraryAdminResponseService;
import at.enfilo.def.transfer.dto.DataTypeDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.TagDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response/library")
public interface ILibraryAdminResponseService extends ILibraryResponseService, LibraryAdminResponseService.Iface, IResource {

	/**
	 * Returns all registered Routine Id's.
	 *
	 * @param ticketId
	 */
	@GET
	@Path("/routines")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> findRoutines(@QueryParam("ticketId") String ticketId);

	/**
	 * Returns Id of new created Routine.
	 *
	 * @param ticketId
	 */
    @PUT
	@Path("/routines")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createRoutine(@QueryParam("ticketId") String ticketId);

	/**
	 * Returns Id of new Routine version. Every update creates a new Routine.
	 *
	 * @param ticketId
	 */
    @PUT
	@Path("/routines/rId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String updateRoutine(@QueryParam("ticketId") String ticketId);

	/**
	 * Returns Id of newly uploaded RoutineBinary.
	 *
	 * @param ticketId
	 */
    @PUT
	@Path("/routines/binary")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String uploadRoutineBinary(@QueryParam("ticketId") String ticketId);

	/**
	 * Returns all registered DataType Id's.
	 *
	 * @param ticketId
	 */
	@GET
	@Path("/datatypes")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> findDataTypes(@QueryParam("ticketId") String ticketId);

	/**
	 * Returns Id of new created DataType.
	 *
	 * @param ticketId
	 */
    @PUT
	@Path("/datatypes")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createDataType(@QueryParam("ticketId") String ticketId);

	/**
	 * Returns the requested DataType.
	 *
	 * @param ticketId
	 */
	@GET
	@Path("/datatypes/dId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	DataTypeDTO getDataType(@QueryParam("ticketId") String ticketId);

	/**
	 * Returns a list of all registered Tags.
	 *
	 * @param ticketId
	 */
	@GET
	@Path("/tags")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<TagDTO> findTags(@QueryParam("ticketId") String ticketId);

	@POST
	@Path("/features")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createFeature(@QueryParam("ticketId") String ticketId);

	@POST
	@Path("/features/extension")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String addExtension(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/features")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<FeatureDTO> getFeatures(@QueryParam("ticketId") String ticketId);
}
