package at.enfilo.def.cluster.api.rest;

import at.enfilo.def.cluster.api.thrift.ClusterResponseService;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response/cluster")
public interface IClusterResponseService extends ClusterResponseService.Iface, IResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/")
	@Override
	ClusterInfoDTO getClusterInfo(@QueryParam("ticketId") final String ticketId);

	@GET
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> getAllNodes(@QueryParam("ticketId") final String ticketId);

	@GET
	@Path("/nodes/nId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	NodeInfoDTO getNodeInfo(@QueryParam("ticketId") final String ticketId);

	@GET
	@Path("/nodes/nId/endpoint")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	ServiceEndpointDTO getNodeServiceEndpoint(@QueryParam("ticketId") final String ticketId);

	@GET
	@Path("/scheduler")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	ServiceEndpointDTO getSchedulerServiceEndpoint(@QueryParam("ticketId") final String ticketId);

	@GET
	@Path("/store-routine")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getStoreRoutine(@QueryParam("ticketId") final String ticketId);

	@GET
	@Path("/default-map-routine")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getDefaultMapRoutine(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/libraryEndpointConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	ServiceEndpointDTO getLibraryEndpointConfiguration(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/nodeEndpointConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	ServiceEndpointDTO getNodeServiceEndpointConfiguration(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/findNodesForShutdown")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> findNodesForShutdown(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/environment")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<FeatureDTO> getEnvironment(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/nodes/nId/environment")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<FeatureDTO> getNodeEnvironment(@QueryParam("ticketId") String ticketId);
}
