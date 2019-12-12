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
	@Path("/queue/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getQueues();

	@GET
	@Path("/queue/{qId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String getQueueInfo(@PathParam("qId") String qId);

	@PUT
	@Path("/queue/{qId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createQueue(@PathParam("qId") String qId);

	@DELETE
	@Path("/queue/{qId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String deleteQueue(@PathParam("qId") String qId);

	@POST
	@Path("/queue/{qId}/release")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String releaseQueue(@PathParam("qId") String qId);

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
	@Path("/queue/{qId}/pause")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String pauseQueue(@PathParam("qId") String qId);

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

	/**
	 * Request the current active StoreRoutine for this worker.
	 * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
	 */
	@Override
	@GET
	@Path("/store-routine")
	@Produces(MediaType.APPLICATION_JSON)
	String getStoreRoutine();

	/**
	 * Set new StoreRoutine for this worker.
	 * Returns a ticket id, state of ticket is available over TicketService interface, real result over Response interface.
	 *
	 * @param routineId
	 */
	@Override
	@POST
	@Path("/store-routine")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	String setStoreRoutine(String routineId);
}
