package at.enfilo.def.scheduler.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.scheduler.api.thrift.SchedulerService;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * RESTful SchedulerService Interface.
 */
@Path("/")
public interface ISchedulerService extends SchedulerService.Iface, IResource {

	@PUT
	@Path("/schedule/{jId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String addJob(@PathParam("jId") final String jId);

	@PUT
	@Path("/reduce/{jId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String extendToReduceJob(@PathParam("jId") final String jId, final String reduceRoutineId);

	@POST
	@Path("/schedule/{jId}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String scheduleTask(@PathParam("jId") final String jId, final TaskDTO task);

	@POST
	@Path("/schedule/{jId}/complete")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String markJobAsComplete(@PathParam("jId") final String jId);

	@DELETE
	@Path("/schedule/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String removeJob(@PathParam("jId") final String jId);

	@PUT
	@Path("/workers/{nId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String addWorker(@PathParam("nId") final String nodeId, final ServiceEndpointDTO endpoint);

	@DELETE
	@Path("/workers/{nId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String removeWorker(@PathParam("nId") final String nodeId);

	@PUT
	@Path("/reducers/{nId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String addReducer(@PathParam("nId") final String nId, final ServiceEndpointDTO serviceEndpoint);

	@DELETE
	@Path("/reducers/{nId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String removeReducer(@PathParam("nId") final String nId);

	@POST
	@Path("/schedule/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String scheduleReduce(@PathParam("jId") final String jId, final List<ResourceDTO> resources);

	@GET
	@Path("/schedule/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String finalizeReduce(@PathParam("jId") final String jId);
}
