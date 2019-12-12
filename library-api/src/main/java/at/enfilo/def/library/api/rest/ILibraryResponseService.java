package at.enfilo.def.library.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.LibraryInfoDTO;
import at.enfilo.def.library.api.thrift.LibraryResponseService;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/response/library")
public interface ILibraryResponseService extends LibraryResponseService.Iface, IResource {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
    LibraryInfoDTO getLibraryInfo(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/routines/rId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	RoutineDTO getRoutine(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/routines/features/rId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	List<FeatureDTO> getRoutineRequiredFeatures(@QueryParam("ticketId") String ticketId);

	@GET
	@Path("/routineBinaries/rId")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	RoutineBinaryDTO getRoutineBinary(@QueryParam("ticketId") String ticketId);
}
