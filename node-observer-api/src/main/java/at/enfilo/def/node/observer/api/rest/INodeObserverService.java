package at.enfilo.def.node.observer.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.node.observer.api.thrift.NodeObserverService;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import org.apache.thrift.TException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/observer")
public interface INodeObserverService extends NodeObserverService.Iface, IResource {

	@POST
	@Path("/nodes/{nId}/elements/state/{executionState}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String notifyElementsNewState(
			@PathParam("nId") String nId,
			List<String> elementIds,
			@PathParam("executionState") ExecutionState newState
	) throws TException;

	@POST
	@Path("/nodes/{nId}/tasks/received")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String notifyTasksReceived(@PathParam("nId") String nId, List<String> taskIds);

	@POST
	@Path("/nodes/{nId}/programs/received")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String notifyProgramsReceived(@PathParam("nId") String nId, List<String> programIds);

	@POST
	@Path("/nodes/{nId}/reduce/{jId}/keys/received")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String notifyReduceKeysReceived(@PathParam("nId") String nId, @PathParam("jId") String jId, List<String> reduceKeys);

	@POST
	@Path("/nodes/{nId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String notifyNodeInfo(@PathParam("nId") String nId, NodeInfoDTO nodeInfo);
}
