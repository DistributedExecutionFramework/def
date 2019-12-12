package at.enfilo.def.parameterserver.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerService;
import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ParameterType;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/parameterserver")
public interface IParameterServerService extends ParameterServerService.Iface, IResource {

    @POST
    @Path("/set/{programId}/{parameterId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String setParameter(@PathParam("programId") String programId, @PathParam("parameterId") String parameterId, ResourceDTO parameter, @QueryParam("parameterId") ParameterProtocol protocol);

    @POST
    @Path("/create/{programId}/{parameterId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String createParameter(@PathParam("programId") String programId, @PathParam("parameterId") String parameterId, ResourceDTO parameter, @QueryParam("parameterId") ParameterProtocol protocol, @QueryParam("type") ParameterType type);

    @GET
    @Path("/get/{programId}/{parameterId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getParameter(@PathParam("programId") String programId, @PathParam("parameterId") String parameterId, @QueryParam("parameterId") ParameterProtocol protocol);

    @POST
    @Path("/add/{programId}/{parameterId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String addToParameter(@PathParam("programId") String programId, @PathParam("parameterId") String parameterId, ResourceDTO parameter, @QueryParam("parameterId") ParameterProtocol protocol);

    @GET
    @Path("/delete/{programId}/{parameterId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String deleteParameter(@PathParam("programId") String programId, @PathParam("parameterId") String parameterId);

    @GET
    @Path("/deleteAll/{programId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String deleteAllParameters(@PathParam("programId") String programId);
}
