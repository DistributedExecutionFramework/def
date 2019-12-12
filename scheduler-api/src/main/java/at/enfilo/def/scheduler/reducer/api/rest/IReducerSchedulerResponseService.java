package at.enfilo.def.scheduler.reducer.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerResponseService;
import at.enfilo.def.transfer.dto.JobDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/response")
public interface IReducerSchedulerResponseService extends ReducerSchedulerResponseService.Iface, IResource {

    @GET
    @Path("/reduce/jId")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    JobDTO finalizeReduce(@QueryParam("ticketId") String ticketId);
}
