package at.enfilo.def.demo;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.routine.ClientRoutine;
import at.enfilo.def.routine.RoutineException;

public class TestClientRoutine extends ClientRoutine {

    @Override
    protected void routine(String pId, IExecLogicServiceClient client) throws RoutineException {
        ServiceEndpointDTO endpoint = client.getServiceEndpoint();
        System.out.println(String.format("Client endpoint: Host %s - Port %s - Protocol %s", endpoint.getHost(), endpoint.getPort(), endpoint.getProtocol()));
    }
}
