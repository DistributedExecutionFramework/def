package at.enfilo.def.reducer.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.node.api.rest.INodeResponseService;
import at.enfilo.def.reducer.api.thrift.ReducerResponseService;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response")
public interface IReducerResponseService extends ReducerResponseService.Iface, INodeResponseService, IResource {
	@GET
	@Path("/reduce/jId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<ResourceDTO> fetchResult(@QueryParam("ticketId") final String ticketId);

}
