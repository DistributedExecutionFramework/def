package at.enfilo.def.cloud.communication.api.rest;

import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationResponseService;
import at.enfilo.def.communication.api.common.service.IResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response/cloud-communication")
public interface ICloudCommunicationResponseService extends CloudCommunicationResponseService.Iface, IResource {

    @POST
    @Path("/awscluster")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String createAWSCluster(@QueryParam("ticketId") String ticketId);

    @POST
    @Path("/boot/cloudClusterId/cluster")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String bootClusterInstance(@QueryParam("ticketId") String ticketId);

    @POST
    @Path("/boot/cloudClusterId/nodes/nrOfNodes")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    List<String> bootNodes(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/IP/cloudClusterId/public/cloudInstanceId")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getPublicIPAddressOfCloudInstance(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/IP/cloudClusterId/private/cloudInstanceId")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getPrivateIPAddressOfCloudInstance(@QueryParam("ticketId") String ticketId);
}
