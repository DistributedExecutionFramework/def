package at.enfilo.def.scheduler.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.scheduler.api.thrift.SchedulerResponseService;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * RESTful SchedulerResponseService Interface.
 */
@Path("/response")
public interface ISchedulerResponseService extends SchedulerResponseService.Iface, IResource {

	/**
	 * Returns next node service endpoint.
	 *
	 * @param ticketId ticket id.
	 * @return next node service endpoint.
	 */
	@GET
	@Path("/schedule/jId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<ResourceDTO> finalizeReduce(@QueryParam("ticketId") String ticketId);
}
