package at.enfilo.def.manager.api.rest;


import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.manager.api.thrift.AuthService;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/auth")
public interface IAuthService extends AuthService.Iface, IResource {

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    String getToken(@FormParam("name") String name, @FormParam("password") String password);
}
