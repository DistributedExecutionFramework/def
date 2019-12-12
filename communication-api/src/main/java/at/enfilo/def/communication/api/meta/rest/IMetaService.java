package at.enfilo.def.communication.api.meta.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.api.meta.thrift.MetaService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/meta")
public interface IMetaService extends MetaService.Iface, IResource {

	@GET
	@Path("version")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	String getVersion();

	@GET
	@Path("time")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	long getTime();
}
