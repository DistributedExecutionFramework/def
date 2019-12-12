package at.enfilo.def.cloud.communication.api.rest;

import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationService;
import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.InstanceTypeDTO;
import at.enfilo.def.communication.api.common.service.IResource;
import org.apache.thrift.TException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/cloud-communication")
public interface ICloudCommunicationService extends CloudCommunicationService.Iface, IResource {

    @POST
    @Path("/awscluster")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String createAWSCluster(AWSSpecificationDTO specification);

    @POST
    @Path("/boot/{cloudClusterId}/cluster")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String bootClusterInstance(@PathParam("cloudClusterId") String cloudClusterId);

    @POST
    @Path("/boot/{cloudClusterId}/nodes/{nrOfNodes}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String bootNodes(@PathParam("cloudClusterId") String cloudClusterId, InstanceTypeDTO instanceType, @PathParam("nrOfNodes") int nrOfNodes);

    @DELETE
    @Path("/terminate/{cloudClusterId}/nodes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String terminateNodes(@PathParam("cloudClusterId") String cloudClusterId, List<String> cloudInstanceIds);

    @GET
    @Path("/IP/{cloudClusterId}/public/{cloudInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getPublicIPAddressOfCloudInstance(@PathParam("cloudClusterId") String cloudClusterId, @PathParam("cloudInstanceId") String cloudInstanceId);

    @GET
    @Path("/IP/{cloudClusterId}/private/{cloudInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getPrivateIPAddressOfCloudInstance(@PathParam("cloudClusterId") String cloudClusterId, @PathParam("cloudInstanceId") String cloudInstanceId);

    @DELETE
    @Path("/shutdown/{cloudClusterId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String shutdownCloudCluster(@PathParam("cloudClusterId") String cloudClusterId);

    @PUT
    @Path("/mapDEFIdToCloudClusterId/{cloudClusterId}/{defId}/{cloudInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String mapDEFIdToCloudInstanceId(@PathParam("cloudClusterId") String cloudClusterId, @PathParam("defId") String defId, @PathParam("cloudInstanceId") String cloudInstanceId);
}
