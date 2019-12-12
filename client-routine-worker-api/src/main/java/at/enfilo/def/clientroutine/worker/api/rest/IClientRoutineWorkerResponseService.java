package at.enfilo.def.clientroutine.worker.api.rest;

import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerResponseService;
import at.enfilo.def.node.api.rest.INodeResponseService;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.dto.QueueInfoDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response")
public interface IClientRoutineWorkerResponseService extends INodeResponseService, ClientRoutineWorkerResponseService.Iface {

    @GET
    @Path("/queue/programs")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    List<String> getQueuedPrograms(@QueryParam("ticketId") String ticketId);

    @GET
    @Path("/programs/pId")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    ProgramDTO fetchFinishedProgram(@QueryParam("ticketId") String ticketId);
}
