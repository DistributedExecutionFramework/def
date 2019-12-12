package at.enfilo.def.scheduler.reducer.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerService;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface IReducerSchedulerService extends ReducerSchedulerService.Iface, IResource {

    @PUT
    @Path("/reducer/{rId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String addReducer(@PathParam("rId") final String rId, final ServiceEndpointDTO endpoint);

    @DELETE
    @Path("/reducer/{rId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    String removeReducer(@PathParam("rId") final String rId);

    @PUT
    @Path("/reducer/reduce")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String addReduceJob(final JobDTO job);

    @DELETE
    @Path("/reducer/reduce/{jId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String removeReduceJob(@PathParam("jId") final String jId);

    @POST
    @Path("/reducer/reduce/{jId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String scheduleResourcesToReduce(@PathParam("jId") final String jId, final List<ResourceDTO> resources);

    @GET
    @Path("/reducer/reduce/{jId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String finalizeReduce(@PathParam("jId") final String jId);

}
