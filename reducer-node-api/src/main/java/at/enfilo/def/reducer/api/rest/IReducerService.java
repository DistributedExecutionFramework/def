package at.enfilo.def.reducer.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.node.api.rest.INodeService;
import at.enfilo.def.reducer.api.thrift.ReducerService;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface IReducerService extends ReducerService.Iface, INodeService, IResource {

	@GET
	@Path("/reduce/{pId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getQueuedJobs(@PathParam("pId") final String pId);

	@PUT
	@Path("/reduce")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createReduceJob(JobDTO job);

	@DELETE
	@Path("/reduce/reducejob/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String abortReduceJob(@PathParam("jId") final String jId);

	@POST
	@Path("/reduce/reducejob/{jId}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String addResourcesToReduce(@PathParam("jId") final String jId, final List<ResourceDTO> resources);

	@POST
	@Path("/reduce/reducejob/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String reduceJob(@PathParam("jId") final String jId);

	@GET
	@Path("/reduce/reducejob/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String fetchResults(@PathParam("jId") final String jId);
}
