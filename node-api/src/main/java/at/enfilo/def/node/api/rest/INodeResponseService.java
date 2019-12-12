package at.enfilo.def.node.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.node.api.thrift.NodeResponseService;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeEnvironmentDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response")
public interface INodeResponseService extends NodeResponseService.Iface, IResource {

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    NodeInfoDTO getInfo(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/environment")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    NodeEnvironmentDTO getEnvironment(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/features")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    List<FeatureDTO> getFeatures(@QueryParam("ticketId") String ticketId);
}
