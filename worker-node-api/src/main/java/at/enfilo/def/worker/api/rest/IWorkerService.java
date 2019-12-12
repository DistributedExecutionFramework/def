package at.enfilo.def.worker.api.rest;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.node.api.rest.INodeService;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.thrift.WorkerService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface IWorkerService extends INodeService, WorkerService.Iface {

	@GET
	@Path("/queue/{qId}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getQueuedTasks(@PathParam("qId") String qId);

	@PUT
	@Path("/queue/{qId}/tasks")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String queueTasks(@PathParam("qId") String qId, List<TaskDTO> taskList);

	@POST
	@Path("/queue/{qId}/tasks/move")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String moveTasks(
			@PathParam("qId") String qId,
			@QueryParam("tIds") List<String> taskIds,
			ServiceEndpointDTO targetNodeEndpoint
	);

	@POST
	@Path("/queue/all/tasks/move")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String moveAllTasks(ServiceEndpointDTO targetNodeEndpoint);

	@GET
	@Path("/tasks/{tId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String fetchFinishedTask(@PathParam("tId") String tId);

	@DELETE
	@Path("/tasks/{tId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String abortTask(@PathParam("tId") String tId);
}
