package at.enfilo.def.library.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.library.api.thrift.LibraryService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/library")
public interface ILibraryService extends LibraryService.Iface, IResource {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getLibraryInfo();

	@GET
	@Path("/routines/{rId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getRoutine(@PathParam("rId") String rId);

	@GET
	@Path("/routines/{rId}/features")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getRoutineRequiredFeatures(@PathParam("rId") String rId);

	@GET
	@Path("/routineBinaries/{rbId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getRoutineBinary(@PathParam("rbId") String rbId);

	@POST
	@Path("/dataEndpoint")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String setDataEndpoint(ServiceEndpointDTO dataEndpoint);
}
