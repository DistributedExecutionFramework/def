package at.enfilo.def.reducer.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.node.api.rest.INodeService;
import at.enfilo.def.reducer.api.thrift.ReducerService;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface IReducerService extends ReducerService.Iface, INodeService, IResource {

	@PUT
	@Path("/reduce/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createReduceJob(@PathParam("jId") final String jId, @QueryParam("routineId") final String routineId);

	@DELETE
	@Path("/reduce/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String deleteReduceJob(@PathParam("jId") final String jId);

	@POST
	@Path("/reduce/{jId}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String add(@PathParam("jId") final String jId, final List<ResourceDTO> resources);

	@POST
	@Path("/reduce/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String reduce(@PathParam("jId") final String jId);

	@GET
	@Path("/reduce/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String fetchResult(@PathParam("jId") final String jId);
}
