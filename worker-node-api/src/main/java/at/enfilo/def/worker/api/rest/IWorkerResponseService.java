package at.enfilo.def.worker.api.rest;

import at.enfilo.def.node.api.rest.INodeResponseService;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.thrift.WorkerResponseService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response")
public interface IWorkerResponseService extends WorkerResponseService.Iface, INodeResponseService {

	/**
	 * Returns info about worker.
	 * @param ticketId
	 */
	@Override
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	NodeInfoDTO getInfo(@QueryParam("ticketId") String ticketId);

	/**
	 * Returns a list of active queues (id).
	 * @param ticketId
	 */
	@Override
	@GET
	@Path("/queues")
	@Produces(MediaType.APPLICATION_JSON)
	List<String> getQueues(@QueryParam("ticketId") String ticketId);


	/**
	 * Returns information of the requested Queue.
	 * @param ticketId
	 */
	@Override
	@GET
	@Path("/queues/qId")
	@Produces(MediaType.APPLICATION_JSON)
	QueueInfoDTO getQueueInfo(@QueryParam("ticketId") String ticketId);


	/**
	 * Returns all queued TaskIDs.
	 * @param ticketId
	 */
	@Override
	@GET
	@Path("/queues/qId/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	List<String> getQueuedTasks(@QueryParam("ticketId") String ticketId);


	/**
	 * Fetch (and remove) the requested task from this worker.
	 *
	 * @param ticket
	 */
	@Override
	@GET
	@Path("/queues/qId/tasks/tId")
	@Produces(MediaType.APPLICATION_JSON)
	TaskDTO fetchFinishedTask(@QueryParam("ticketId") String ticket);


	/**
	 * Returns current acvtive StoreRoutine Id.
	 *
	 * @param ticketId
	 */
	@Override
	@GET
	@Path("/store-routine")
	@Produces(MediaType.APPLICATION_JSON)
	String getStoreRoutine(@QueryParam("ticketId") String ticketId);
}
