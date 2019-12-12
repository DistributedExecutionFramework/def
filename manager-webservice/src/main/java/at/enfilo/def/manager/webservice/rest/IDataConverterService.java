package at.enfilo.def.manager.webservice.rest;

import at.enfilo.def.communication.api.common.service.IResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface IDataConverterService extends IResource {

    @GET
    @Path("/datatype/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    String getDataTypeFromId(@PathParam("id") String dataTypeId);

}
