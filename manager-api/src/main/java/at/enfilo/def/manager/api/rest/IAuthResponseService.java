package at.enfilo.def.manager.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.manager.api.thrift.AuthResponseService;
import at.enfilo.def.transfer.dto.AuthDTO;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/response/auth")
public interface IAuthResponseService extends AuthResponseService.Iface, IResource {

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	AuthDTO getToken(@QueryParam("ticketId") String ticketId);
}
