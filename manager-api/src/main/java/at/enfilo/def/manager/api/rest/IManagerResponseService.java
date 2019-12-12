package at.enfilo.def.manager.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.manager.api.thrift.ManagerResponseService;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response/manager")
public interface IManagerResponseService extends ManagerResponseService.Iface, IResource {

	@PUT
	@Path("/clusters/AWS")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createAWSCluster(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/clusters/cId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	ClusterInfoDTO getClusterInfo(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/clusters/cId/endpoint")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	ServiceEndpointDTO getClusterEndpoint(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/clusters")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> getClusterIds(@QueryParam("ticketId") String ticketId);
}
