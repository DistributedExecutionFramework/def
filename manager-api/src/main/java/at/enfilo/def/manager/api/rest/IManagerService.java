package at.enfilo.def.manager.api.rest;


import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.SupportedCloudEnvironment;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.manager.api.thrift.ManagerService;
import at.enfilo.def.transfer.dto.NodeType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/manager")
public interface IManagerService extends ManagerService.Iface, IResource {

	@GET
	@Path("/clusters")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getClusterIds();

	@GET
	@Path("/clusters/{cId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getClusterInfo(@PathParam("cId") String cId);

	@GET
	@Path("/clusters/{cId}/endpoint")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getClusterEndpoint(@PathParam("cId") String cId);

	@PUT
	@Path("/clusters/AWS/workers/{nrOfWorkers}/reducers/{nrOfReducers}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createAWSCluster(
			@PathParam("nrOfWorkers") int numberOfWorkers,
			@PathParam("nrOfReducers") int numberOfReducers,
			AWSSpecificationDTO awsSpecificationDTO);

	@POST
	@Path("/clusters")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String addCluster(ServiceEndpointDTO endpoint);

	@DELETE
	@Path("/clusters/{cId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String destroyCluster(@PathParam("cId") String cId);

	@POST
	@Path("/clusters/{cId}/adjust/{nodeType}/{newNodePoolSize}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String adjustNodePoolSize(@PathParam("cId") String cId, @PathParam("newNodePoolSize") int newNodePoolSize, @PathParam("nodeType") NodeType nodeType);

}
