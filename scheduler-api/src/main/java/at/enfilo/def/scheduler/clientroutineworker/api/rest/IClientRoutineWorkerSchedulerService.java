package at.enfilo.def.scheduler.clientroutineworker.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.scheduler.clientroutineworker.api.thrift.ClientRoutineWorkerSchedulerService;
import at.enfilo.def.transfer.dto.ProgramDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface IClientRoutineWorkerSchedulerService extends ClientRoutineWorkerSchedulerService.Iface, IResource {

    @PUT
    @Path("/clientroutineworker/{wId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String addClientRoutineWorker(@PathParam("wId") final String wId, final ServiceEndpointDTO endpoint);

    @DELETE
    @Path("/clientroutineworker/{wId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String removeClientRoutineWorker(@PathParam("wId") final String wId);

    @PUT
    @Path("/clientroutineworker/schedule/{uId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String addUser(@PathParam("uId") final String uId);

    @DELETE
    @Path("/clientroutineworker/schedule/{uId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String removeUser(@PathParam("uId") final String uId);

    @POST
    @Path("/clientroutineworker/{wId}/abort/{pId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String abortProgram(@PathParam("wId") final String wId, @PathParam("pId") final String pId);

    @POST
    @Path("/clientroutineworker/schedule/{uId}/program")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String scheduleProgram(@PathParam("uId") final String uId, ProgramDTO program);
}
