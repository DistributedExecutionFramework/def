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
}
