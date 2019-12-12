package at.enfilo.def.execlogic.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.execlogic.api.thrift.ExecLogicService;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.RoutineInstanceDTO;
import at.enfilo.def.transfer.dto.SortingCriterion;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.nio.ByteBuffer;

@Path("/exec-logic")
public interface IExecLogicService extends ExecLogicService.Iface, IResource {

	@GET
	@Path("/programs")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAllPrograms(@QueryParam("userId") String userId);

	@POST
	@Path("/programs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createProgram(@QueryParam("clusterId") String clusterId, @QueryParam("userId") String userId);

	@GET
	@Path("/programs/{pId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getProgram(@PathParam("pId") String pId);

	@DELETE
	@Path("/programs/{pId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String deleteProgram(@PathParam("pId") String pId);

	@PATCH
	@Path("/programs/{pId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String abortProgram(@PathParam("pId") String pId);

	@PUT
	@Path("/programs/{pId}/name")
	@Produces(MediaType.APPLICATION_JSON)
	String updateProgramName(@PathParam("pId") String pId, @QueryParam("name") String name);

	@PUT
	@Path("/programs/{pId}/description")
	@Produces(MediaType.APPLICATION_JSON)
	String updateProgramDescription(@PathParam("pId") String pId, @QueryParam("description") String description);

	@POST
	@Path("/programs/{pId}/finished")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String markProgramAsFinished(@PathParam("pId") String pId);

	@POST
	@Path("/programs/{pId}/client-routine")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String startClientRoutine(@PathParam("pId") String pId, @QueryParam("crId") String crId);

	@GET
	@Path("/programs/{pId}/jobs")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAllJobs(@PathParam("pId") String pId);

	@POST
	@Path("/programs/{pId}/jobs")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String createJob(@PathParam("pId") String pId);

	@GET
	@Path("/programs/{pId}/jobs/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getJob(@PathParam("pId") String pId, @PathParam("jId") String jId);

	@DELETE
	@Path("/programs/{pId}/jobs/{jId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String deleteJob(@PathParam("pId") String pId, @PathParam("jId") String jId);

	@GET
	@Path("/programs/{pId}/jobs/{jId}/map")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAttachedMapRoutine(@PathParam("pId") String pId, @PathParam("jId") String jId);

	@PUT
	@Path("/programs/{pId}/jobs/{jId}/map")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String attachMapRoutine(@PathParam("pId") String pId, @PathParam("jId") String jId, String mapRoutineId);

	@GET
	@Path("/programs/{pId}/jobs/{jId}/reduce")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAttachedReduceRoutine(@PathParam("pId") String pId, @PathParam("jId") String jId);

	@PUT
	@Path("/programs/{pId}/jobs/{jId}/reduce")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String attachReduceRoutine(@PathParam("pId") String pId, @PathParam("jId") String jId, String reduceRoutineId);

	@GET
	@Path("/programs/{pId}/jobs/{jId}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAllTasksWithState(@PathParam("pId") String pId, @PathParam("jId") String jId, @QueryParam("state") ExecutionState state, @QueryParam("sortingCriterion") SortingCriterion sortingCriterion);

	@GET
	@Path("/programs/{pId}/jobs/{jId}/allTasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAllTasks(@PathParam("pId") String pId, @PathParam("jId") String jId, @QueryParam("sortingCriterion") SortingCriterion sortingCriterion);

	@POST
	@Path("/programs/{pId}/jobs/{jId}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String createTask(@PathParam("pId") String pId, @PathParam("jId") String jId, RoutineInstanceDTO objectiveRoutine);

	@GET
	@Path("/programs/{pId}/jobs/{jId}/tasks/{tId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getTask(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("tId") String tId);

	@GET
	@Path("/programs/{pId}/jobs/{jId}/tasks/{tId}/partial")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getTaskPartial(
			@PathParam("pId") String pId,
			@PathParam("jId") String jId,
			@PathParam("tId") String tId,
			@QueryParam("includeInParameters") boolean includeInParamters,
			@QueryParam("includeOutParameters") boolean includeOutParameters
	);

	@POST
	@Path("/programs/{pId}/jobs/{jId}/complete")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String markJobAsComplete(@PathParam("pId") String pId, @PathParam("jId") String jId);

	@POST
	@Path("/programs/{pId}/jobs/{jId}/abort")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String abortJob(@PathParam("pId") String pId, @PathParam("jId") String jId);

	@DELETE
	@Path("/programs/{pId}/jobs/{jId}/tasks/{tId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String abortTask(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("tId") String tId);

	@POST
	@Path("/programs/{pId}/jobs/{jId}/tasks/{tId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String reRunTask(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("tId") String tId);

	@GET
	@Path("/programs/{pId}/resources")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getAllSharedResources(@PathParam("pId") String pId);

	@POST
	@Path("/programs/{pId}/resources")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String createSharedResource(@PathParam("pId") String pId, @QueryParam("dataTypeId") String dataTypeId, ByteBuffer data);

	@GET
	@Path("/programs/{pId}/resources/{rId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getSharedResource(@PathParam("pId") String pId, @PathParam("rId") String rId);

	@DELETE
	@Path("/programs/{pId}/resources/{rId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String deleteSharedResource(@PathParam("pId") String pId, @PathParam("rId") String rId);
}
