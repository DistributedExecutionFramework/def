package at.enfilo.def.routine;

import at.enfilo.def.execlogic.api.IExecLogicServiceClient;

public class ClientTestRoutine extends ClientRoutine {

    @Override
    protected void routine(String pId, IExecLogicServiceClient client) throws RoutineException {
        client.getServiceEndpoint();
    }
}
