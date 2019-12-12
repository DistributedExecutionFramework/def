package at.enfilo.def.communication.impl;

import at.enfilo.def.communication.api.common.service.IResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/dummy")
public interface IDummyService extends DummyService.Iface, IResource {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String ping();
}
