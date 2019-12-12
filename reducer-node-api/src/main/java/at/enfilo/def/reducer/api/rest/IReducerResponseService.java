package at.enfilo.def.reducer.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.node.api.rest.INodeResponseService;
import at.enfilo.def.reducer.api.thrift.ReducerResponseService;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response")
public interface IReducerResponseService extends ReducerResponseService.Iface, INodeResponseService, IResource {

	@GET
	@Path("/reduce/pId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> getQueuedJobs(@QueryParam("ticketId") final String ticketId);

	@GET
	@Path("/reduce/reducejob/jId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<ResourceDTO> fetchResults(@QueryParam("ticketId") final String ticketId);
}
