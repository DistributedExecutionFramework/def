package at.enfilo.def.communication.api.ticket.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by mase on 21.09.2016.
 */
@Path("/tickets")
public interface ITicketService extends TicketService.Iface, IResource {
    @GET
    @Path("/{ticketId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    TicketStatusDTO getTicketStatus(@PathParam("ticketId") final String ticketId);

	@GET
	@Path("/{ticketId}/wait")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	TicketStatusDTO waitForTicket(@PathParam("ticketId") final String ticketId);

	@DELETE
    @Path("/{ticketId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    TicketStatusDTO cancelTicketExecution(@PathParam("ticketId") final String ticketId, @QueryParam("mayInterrupt") final boolean mayInterruptIfRunning);

	@GET
	@Path("/{ticketId}/failedMessage")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getFailedMessage(@PathParam("ticketId") String ticketId);
}
