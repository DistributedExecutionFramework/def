package at.enfilo.def.manager.api.rest;


import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.manager.api.thrift.ManagerService;
import at.enfilo.def.transfer.dto.NodeType;
import at.enfilo.def.transfer.dto.RoutineBinaryChunkDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;

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
	@Path("/clusters/AWS")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createAWSCluster(
			@QueryParam("nrOfWorkers") int numberOfWorkers,
			@QueryParam("nrOfReducers") int numberOfReducers,
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
	@Path("/clusters/{cId}/adjust")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String adjustNodePoolSize(@PathParam("cId") String cId, @QueryParam("newNodePoolSize") int newNodePoolSize, @QueryParam("nodeType") NodeType nodeType);

	@POST
	@Path("/library/clientRoutines")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createClientRoutine(RoutineDTO routine);

	@POST
	@Path("/library/clientRoutines/{rId}/binaries")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createClientRoutineBinary(
			@PathParam("rId") String rId,
			@QueryParam("binaryName") String binaryName,
			@QueryParam("md5") String md5,
			@QueryParam("sizeInBytes") long sizeInBytes,
			@QueryParam("isPrimary") boolean isPrimary
	);

	@POST
	@Path("/library/clientRoutines/binaries/{rbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String uploadClientRoutineBinaryChunk(@PathParam("rbId") String rbId, RoutineBinaryChunkDTO chunk);

	@DELETE
	@Path("/library/clientRoutines/{rId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String removeClientRoutine(@PathParam("rId") String rId);

	@GET
	@Path("/library/feature")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getFeatureByNameAndVersion(@QueryParam("name") String name, @QueryParam("version") String version);
}
