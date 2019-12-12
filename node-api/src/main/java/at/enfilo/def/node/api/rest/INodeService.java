package at.enfilo.def.node.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.node.api.thrift.NodeService;
import at.enfilo.def.transfer.dto.PeriodUnit;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface INodeService extends NodeService.Iface, IResource {
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String takeControl(String clusterId);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getInfo();

    @GET
    @Path("/environment")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getEnvironment();

    @GET
    @Path("/features")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getFeatures();

    @POST
    @Path("/observer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String registerObserver(
        ServiceEndpointDTO endpointDTO,
        @QueryParam("checkPeriodically") boolean checkPeriodically,
        @QueryParam("periodDuration") long periodDuration,
        @QueryParam("periodUnit") PeriodUnit periodUnit
    );
    
    @DELETE
    @Path("/observer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String deregisterObserver(
        ServiceEndpointDTO endpointDTO
    );

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String shutdown();

    @POST
	@Path("/sharedResources")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String addSharedResource(ResourceDTO sharedResource);

    @DELETE
	@Path("/sharedResources")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String removeSharedResources(List<String> rIds);
}
