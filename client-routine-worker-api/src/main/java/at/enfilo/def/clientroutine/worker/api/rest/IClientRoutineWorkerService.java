package at.enfilo.def.clientroutine.worker.api.rest;

import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerService;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.node.api.rest.INodeService;
import at.enfilo.def.transfer.dto.ProgramDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface IClientRoutineWorkerService extends INodeService, ClientRoutineWorkerService.Iface {

    @GET
    @Path("/queue/{qId}/programs")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getQueuedPrograms(@PathParam("qId") String qId);

    @PUT
    @Path("/queue/{qId}/program")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String queueProgram(@PathParam("qId") String qId, ProgramDTO program);

    @PUT
    @Path("/queue/{qId}/programs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String queuePrograms(@PathParam("qId") String qId, List<ProgramDTO> programs);

    @POST
    @Path("/queue/{qId}/programs/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String movePrograms(
            @PathParam("qId") String qId,
            @QueryParam("pIds") List<String> programIds,
            ServiceEndpointDTO targetNodeEndpoint
    );

    @POST
    @Path("/queue/all/programs/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String moveAllPrograms(ServiceEndpointDTO targetNodeEndpoint);

    @GET
    @Path("/programs/{pId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String fetchFinishedProgram(@PathParam("pId") String pId);

    @DELETE
    @Path("/programs/{pId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String abortProgram(@PathParam("pId") String pId);
}
