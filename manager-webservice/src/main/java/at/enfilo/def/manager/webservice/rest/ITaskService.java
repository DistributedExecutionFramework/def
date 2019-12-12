package at.enfilo.def.manager.webservice.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.transfer.dto.SortingCriterion;
import at.enfilo.def.transfer.dto.TaskDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface ITaskService extends IResource {

    /**
     * Fetches and returns all tasks with a given state of the job with the given id of the program with the given id
     *
     * @param pId           the id of the program the job is part of
     * @param jId           the id of the job all tasks should be returned of
     * @param stateString   the state the returned tasks should have
     * @return              a list with all tasks as {@link TaskDTO}s
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}/tasks/state/{state}/sort/{sortingCriterion}")
    @Produces(MediaType.APPLICATION_JSON)
    List<TaskDTO> getAllTasksWithState(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("state") String stateString, @PathParam("sortingCriterion") SortingCriterion sortingCriterion);

    /**
     * Fetches and returns all tasks with ids that matches the given filters
     *
     * @param taskIdFilters     the filters used for the task ids
     * @return                  a list with the filtered tasks as {@link TaskDTO}s
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}/tasks/sort/{sortingCriterion}/filter")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<TaskDTO> getAllTasksByFilters(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("sortingCriterion") SortingCriterion sortingCriterion, @QueryParam("filters") String taskIdFilters);

    /**
     * Fetches and returns a specific number of tasks with a given state of the job with the given id of the program with the given id
     *
     * @param pId               the id of the program the job is part of
     * @param jId               the id of the job the tasks should be returned of
     * @param stateString       the state the returned tasks should have
     * @param nrOfTasksString   the maximum number of tasks to be returned
     * @return                  a list with the tasks as {@link TaskDTO}s
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}/tasks/state/{state}/sort/{sortingCriterion}/nrOfTasks/{nrOfTasksString}")
    @Produces(MediaType.APPLICATION_JSON)
    List<TaskDTO> getSpecificNumberOfTasksWithState(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("state") String stateString, @PathParam("sortingCriterion") SortingCriterion sortingCriterion, @PathParam("nrOfTasksString") String nrOfTasksString);


    /**
     * Fetches and returns the task info of the task with the given id of the job with the given id of the program with the given id
     *
     * @param pId   the program the job is part of
     * @param jId   the job the task is part of
     * @param tId   the task of which the task info should be returned
     * @return      the info of the specific task as {@link TaskDTO}
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}/tasks/{tId}")
    @Produces(MediaType.APPLICATION_JSON)
    TaskDTO getTaskInfo(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("tId") String tId);

    /**
     *
     * @param pId               the program the job is part of
     * @param jId               the job the task is part of
     * @param tId               the task the input parameter is part of
     * @param inputParamName    the name of the input parameter of which the data value should be returned as {@link String}
     * @return                  the formatted data value of the input parameter as {@link String}
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}/tasks/{tId}/inParam/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    String getDataValueOfTaskInputParameter(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("tId") String tId, @PathParam("name") String inputParamName);

    /**
     *
     * @param pId           the program the job is part of
     * @param jId           the job the task is part of
     * @param tId           the task the output parameter is part of
     * @param resourceId    the output parameter of which the data value should be returned
     * @return              the formatted data value of the output parameter as {@link String}
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}/tasks/{tId}/outParam/{rId}")
    @Produces(MediaType.APPLICATION_JSON)
    String getDataValueOfTaskOutputParameter(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("tId") String tId, @PathParam("rId") String resourceId);

    /**
     * Aborts a task with the given id of the job with the given id of the program with the given id
     *
     * @param pId   the program the job is part of
     * @param jId   the job the task is part of
     * @param tId   the task to abort
     * @return      the status of the abortion process as {@link TicketStatusDTO}
     */
    @PUT
    @Path("/programs/{pId}/jobs/{jId}/tasks/{tId}/abort")
    @Produces(MediaType.APPLICATION_JSON)
    TicketStatusDTO abortTask(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("tId") String tId);

    /**
     * Reschedule a task with the given id of the job with the given id of the program with the given id
     *
     * @param pId   the program the job is part of
     * @param jId   the job the task is part of
     * @param tId   the task to reschedule
     * @return      the status of the reschedule process as {@link TicketStatusDTO}
     */
    @PUT
    @Path("/programs/{pId}/jobs/{jId}/tasks/{tId}/reschedule")
    @Produces(MediaType.APPLICATION_JSON)
    TicketStatusDTO reRunTask(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("tId") String tId);


}
