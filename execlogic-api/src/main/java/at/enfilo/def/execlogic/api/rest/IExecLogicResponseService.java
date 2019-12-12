package at.enfilo.def.execlogic.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.apache.thrift.TException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response/exec-logic")
public interface IExecLogicResponseService extends ExecLogicResponseService.Iface, IResource {

	@GET
	@Path("/programs")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> getAllPrograms(@QueryParam("ticketId") String ticketId);

	@POST
	@Path("/programs")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createProgram(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	ProgramDTO getProgram(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/jobs")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> getAllJobs(@QueryParam("ticketId") String ticketId);

	@POST
	@Path("/programs/pId/jobs")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createJob(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/jobs/jId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	JobDTO getJob(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/jobs/jId/map")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAttachedMapRoutine(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/jobs/jId/reduce")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAttachedReduceRoutine(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/jobs/jId/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> getAllTasksWithState(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/jobs/jId/allTasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> getAllTasks(String ticketId) throws TException;

	@POST
	@Path("/programs/pId/jobs/jId/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createTask(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/jobs/jId/tasks/tId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	TaskDTO getTask(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/jobs/jId/tasks/tId/partial")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	TaskDTO getTaskPartial(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/resources")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<String> getAllSharedResources(@QueryParam("ticketId") String ticketId);

	@POST
	@Path("/programs/pId/resources")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createSharedResource(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/programs/pId/resources/rId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	ResourceDTO getSharedResource(@QueryParam("ticketId") String ticketId);
}
