package at.enfilo.def.scheduler.worker.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerService;
import at.enfilo.def.transfer.dto.TaskDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * RESTful WorkerSchedulerService interface
 */
@Path("/")
public interface IWorkerSchedulerService extends WorkerSchedulerService.Iface, IResource {

    @PUT
    @Path("/worker/{wId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String addWorker(@PathParam("wId") final String wId, final ServiceEndpointDTO endpoint);

    @DELETE
    @Path("/worker/{wId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String removeWorker(@PathParam("wId") final String wId);

    @PUT
    @Path("/worker/schedule/{jId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String addJob(@PathParam("jId") final String jId);

    @DELETE
    @Path("/worker/schedule/{jId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String removeJob(@PathParam("jId") final String jId);

    @POST
    @Path("/worker/schedule/{jId}/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String markJobAsComplete(@PathParam("jId") final String jId);

    @POST
    @Path("/worker/schedule/{jId}/task")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String scheduleTask(@PathParam("jId") final String jId, final TaskDTO task);

    @POST
    @Path("/worker/{wId}/abort/{tId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String abortTask(@PathParam("wId") final String wId, @PathParam("tId") final String tId);
}
