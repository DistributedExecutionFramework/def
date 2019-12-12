package at.enfilo.def.manager.webservice.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.transfer.dto.JobDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface IJobService extends IResource {

    /**
     * Fetches and returns all job ids of of the program with the given id
     *
     * @param pId   the id of the program all job ids should be returned from
     * @return      a list with all ids of the jobs as {@link String}s
     */
    @GET
    @Path("/programs/{pId}/jobIds")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getAllJobIds(@PathParam("pId") String pId);


    /**
     * Fetches and returns all jobs of the program with the given id
     *
     * @param pId   the id of the program all jobs should be returned from
     * @return      a list with all jobs as {@link JobDTO}s
     */
    @GET
    @Path("/programs/{pId}/jobs")
    @Produces(MediaType.APPLICATION_JSON)
    List<JobDTO> getAllJobs(@PathParam("pId") String pId);


    /**
     * Fetches all jobs of the program with the given id and returns the number of the finished jobs
     *
     * @param pId   the id of the program the number of finished jobs should be returned
     * @return      the number of finished jobs as int
     */
    @GET
    @Path("/programs/{pId}/nrOfFinishedJobs")
    @Produces(MediaType.APPLICATION_JSON)
    int getNrOfFinishedJobs(@PathParam("pId") String pId);


    /**
     * Fetches and returns the job info of the job with the given id of the program with the given id
     *
     * @param pId   the id of the program the job is part of
     * @param jId   the id of the job the info should be returned from
     * @return      the info of the specific job as {@link JobDTO}
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}")
    @Produces(MediaType.APPLICATION_JSON)
    JobDTO getJobInfo(@PathParam("pId") String pId, @PathParam("jId") String jId);


    /**
     * Deletes the job with the given id of the program with the given id
     *
     * @param pId   the id of the program the job is part of
     * @param jId   the id of the job that should be deleted
     * @return      the status of the deletion process as {@link TicketStatusDTO}
     */
    @DELETE
    @Path("/programs/{pId}/jobs/{jId}")
    @Produces(MediaType.APPLICATION_JSON)
    TicketStatusDTO deleteJob(@PathParam("pId") String pId, @PathParam("jId") String jId);


    /**
     * Aborts the running job with the given id of the program with the given id
     *
     * @param pId   the id of the program the job is part of
     * @param jId   the id of the job that should be aborted
     * @return      the status of the abortion process as {@link TicketStatusDTO}
     */
    @PUT
    @Path("/programs/{pId}/jobs/{jId}/abort")
    @Produces(MediaType.APPLICATION_JSON)
    TicketStatusDTO abortJob(@PathParam("pId") String pId, @PathParam("jId") String jId);

    /**
     * Fetches and returns the attached map routine of the job with the given id of the program with the given id
     *
     * @param pId   the id of the program the job is part of
     * @param jId   the id of the job the attached map routine should be returned of
     * @return      the id of the attached map routine of the job as {@link String}
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}/map")
    @Produces(MediaType.APPLICATION_JSON)
    String  getAttachedMapRoutine(@PathParam("pId") String pId, @PathParam("jId") String jId);

    /**
     * Fetches and returns the attached reduce routine of the job with the given id of the program with the given id
     *
     * @param pId   the id of the program the job is part of
     * @param jId   the id of the job the attached reduce routine should be returned of
     * @return      the id of the attached reduce routine of the job as {@link String}
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}/reduce")
    @Produces(MediaType.APPLICATION_JSON)
    String  getAttachedReduceRoutine(@PathParam("pId") String pId, @PathParam("jId") String jId);

    /**
     *
     * @param pId           the program the job is part of
     * @param jId           the job the task is part of
     * @param resourceId    the reduced result of which the data value should be returned
     * @return              the formatted data value of the reduced result as {@link String}
     */
    @GET
    @Path("/programs/{pId}/jobs/{jId}/reduce/{rId}")
    @Produces(MediaType.APPLICATION_JSON)
    String getDataValueOfJobReducedResult(@PathParam("pId") String pId, @PathParam("jId") String jId, @PathParam("rId") String resourceId);
}