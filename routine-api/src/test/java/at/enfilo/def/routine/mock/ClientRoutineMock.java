package at.enfilo.def.routine.mock;

import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.routine.ClientRoutine;

public class ClientRoutineMock extends ClientRoutine {

    private static ClientRoutineMock lastInstance;

    private boolean run = false;
    private boolean shutdownIO = false;

    public ClientRoutineMock() { lastInstance = this; }

    @Override
    protected void routine(String pId, IExecLogicServiceClient client) {

    }

    @Override
    public void run() { run = true; }

    @Override
    protected void shutdownIO() {
        shutdownIO = true;
    }

    public static ClientRoutineMock getLastInstance() { return lastInstance; }

    public boolean isRun() { return run; }
}
