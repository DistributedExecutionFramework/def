package at.enfilo.def.parameterserver.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerResponseService;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/response/parameterserver")
public interface IParameterServerResponseService extends ParameterServerResponseService.Iface, IResource {

    @GET
    @Path("/set")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String setParameter(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String createParameter(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    ResourceDTO getParameter(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String addToParameter(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String deleteParameter(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String deleteAllParameters(@QueryParam("ticketId") String ticketId);
}
