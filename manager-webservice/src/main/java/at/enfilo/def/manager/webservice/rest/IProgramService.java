package at.enfilo.def.manager.webservice.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/")
public interface IProgramService extends IResource {

    /**
     * Fetches and returns all program ids available
     *
     * @return  a list with all program ids as {@link String}
     */
    @GET
    @Path("/programIds/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getAllProgramIds(@PathParam("userId") String userId);


    /**
     * Fetches and returns all programs available
     *
     * @return  a list with all programs as {@link ProgramDTO}s
     */
    @GET
    @Path("/programs/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    List<ProgramDTO> getAllPrograms(@PathParam("userId") String userId);


    /**
     * Fetches and returns the program info of the program with the given id
     *
     * @param pId   the program ID of the program we're interested in
     * @return      the info of the specific program as {@link ProgramDTO}
     */
    @GET
    @Path("/programs/{pId}")
    @Produces(MediaType.APPLICATION_JSON)
    ProgramDTO getProgramInfo(@PathParam("pId") String pId);


    /**
     * Deletes the program with the given id
     *
     * @param pId   the program ID of the program that should be deleted
     * @return      the status of the deletion process as {@link TicketStatusDTO}
     */
    @DELETE
    @Path("/programs/{pId}")
    @Produces(MediaType.APPLICATION_JSON)
    TicketStatusDTO deleteProgram(@PathParam("pId") String pId);


    /**
     * Aborts the program with the given id
     *
     * @param pId   the program ID of the program that should be aborted
     * @return      the status of the deletion process as {@link TicketStatusDTO}
     */
    @PUT
    @Path("/programs/{pId}/abort")
    @Produces(MediaType.APPLICATION_JSON)
    TicketStatusDTO abortProgram(@PathParam("pId") String pId);


    /**
     * Updates the program name and program description of the program with the given id
     *
     * @param pId       the program ID of the program that should be updated
     * @param program   the updated program data as JSON string
     * @return          the status of the update process as {@link TicketStatusDTO}
     */
    @PUT
    @Path("/programs/{pId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    TicketStatusDTO updateProgram(@PathParam("pId") String pId, ProgramDTO program);
}