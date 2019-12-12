package at.enfilo.def.cluster.api.rest;

import at.enfilo.def.cluster.api.thrift.ClusterService;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.transfer.dto.NodeType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/cluster")
public interface IClusterService extends ClusterService.Iface, IResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/")
	@Override
	String getClusterInfo();

	@DELETE
	@Path("/")
	@Override
	void destroyCluster();

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String takeControl(String managerId);

	@GET
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAllNodes(@QueryParam("type") final NodeType type);

	@GET
	@Path("/nodes/{nId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getNodeInfo(@PathParam("nId") String nId);

	@PUT
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String addNode(final ServiceEndpointDTO serviceEndpoint, @QueryParam("type") final NodeType type);

	@DELETE
	@Path("/nodes/{nId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String removeNode(@PathParam("nId") final String nId);

	@GET
	@Path("/nodes/{nId}/endpoint")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getNodeServiceEndpoint(@PathParam("nId") String nId);

	@GET
	@Path("/scheduler")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getSchedulerServiceEndpoint(
		@QueryParam("nodeType") NodeType nodeType
	);

	@POST
	@Path("/scheduler")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String setSchedulerServiceEndpoint(
		@QueryParam("cluster-node-type") NodeType nodeType,
		ServiceEndpointDTO schedulerServiceEndpoint
	);

	@GET
	@Path("/store-routine")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getStoreRoutine();

	@POST
	@Path("/store-routine")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String setStoreRoutine(String routineId);

	@GET
	@Path("/default-map-routine")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getDefaultMapRoutine();

	@POST
	@Path("/default-map-routine")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String setDefaultMapRoutine(String routineId);

	@GET
	@Path("/findNodesForShutdown/{nodeType}/{nrOfNodesToShutdown}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String findNodesForShutdown(@PathParam("nodeType") NodeType nodeType, @PathParam("nrOfNodesToShutdown") int nrOfNodesToShutdown);

	@GET
	@Path("/nodeEndpointConfiguration/{nodeType}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getNodeServiceEndpointConfiguration(@PathParam("nodeType") NodeType nodeType);

	@GET
	@Path("/libraryEndpointConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getLibraryEndpointConfiguration();

	@GET
	@Path("/environment")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getEnvironment();

	@GET
	@Path("/nodes/{nId}/environment")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getNodeEnvironment(@PathParam("nId") String nId);
}
