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
	@Path("/nodes/{nId}/tasks/state/{executionState}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String notifyTasksNewState(
			@PathParam("nId") String nId,
			List<String> taskIds,
			@PathParam("executionState") ExecutionState newState
	) throws TException;

	@POST
	@Path("/nodes/{nId}/tasks/received")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String notifyTasksReceived(@PathParam("nId") String nId, List<String> taskIds);

	@POST
	@Path("/nodes/{nId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	String notifyNodeInfo(@PathParam("nId") String nId, NodeInfoDTO nodeInfo);
}
