package at.enfilo.def.routine;

import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import java.io.File;
import java.io.IOException;

//TODO: This class could probably be implemented better by using existing components instead of duplicating code
public class MatlabParameterManager {

    private String routineName;
    private DataReader in;
    private DataWriter out;
    private DataWriter ctrl;

    private final TSerializer serializer = new TSerializer();

    public MatlabParameterManager(String routineName, String inPipe, String outPipe, String ctrlPipe) {
        this.routineName = routineName;
        try {
            setupIO(inPipe, outPipe, ctrlPipe);
            log(LogLevel.DEBUG, String.format("Routine %s: IO setup done, run", routineName));
        } catch (Exception e) {
            log(LogLevel.ERROR, String.format("Error while running routine %s: %s", routineName, e.getMessage()));
            throw new RoutineException(e);
        }
    }

    public Object getParameter(String name, String type) throws Exception {
        log(LogLevel.DEBUG, String.format("Requesting Parameter %s", name));
        Order getParam = new Order(Command.GET_PARAMETER, name);
        try {
            ctrl.store(getParam);
            //TODO: As soon as we have new, user-defined datatypes which are not imported yet, this won't work anymore
            Object param = in.read((TBase) Class.forName("at.enfilo.def.datatype." + type).getConstructor().newInstance());
            log(LogLevel.DEBUG, String.format("Received: %s", param));
            return param;
        } catch (IllegalAccessException | InstantiationException | TException e) {
            throw new AccessParameterException(e);
        }
    }

    public void setResult(Object result) {
        try {
            log(LogLevel.DEBUG, String.format("Storing Result %s", result.toString()));
            byte[] data = serializer.serialize((TBase) result);
            out.store(data.length);
            out.store(data);

            Order done = new Order(Command.ROUTINE_DONE, "");
            log(LogLevel.DEBUG, "Send ROUTINE_DONE to RoutinesCommunicator.");
            ctrl.store(done);

            log(LogLevel.DEBUG, String.format("Routine %s done, shutting down IO", routineName));
            shutdownIO();
        } catch (Exception e) {
            log(LogLevel.ERROR, String.format("Error while setting result of routine %s: %s", routineName, e.getMessage()));
            throw new RoutineException(e);
        }
    }

    private void setupIO(String inPipe, String outPipe, String ctrlPipe) throws IOException {
        if (in == null) {
            in = inPipe != null ? new DataReader(new File(inPipe)) : null;
        }
        if (out == null) {
            out = outPipe != null ? new DataWriter(new File(outPipe)) : null;
        }
        if (ctrl == null) {
            ctrl = ctrlPipe != null ? new DataWriter(new File(ctrlPipe)) : null;
        }
        log(LogLevel.DEBUG, String.format("inPipe=%s, outPipe=%s, ctrlPipe=%s", inPipe, outPipe, ctrlPipe));
    }

    private void shutdownIO() {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (ctrl != null) {
            ctrl.close();
        }
    }

    public void log(LogLevel level, String msg) {
        Command cmd;
        switch (level) {
            case ERROR:
                cmd = Command.LOG_ERROR;
                break;
            case DEBUG:
                cmd = Command.LOG_DEBUG;
                break;
            case INFO:
            default:
                cmd = Command.LOG_INFO;
                break;
        }
        try {
            ctrl.store(new Order(cmd, msg));
        } catch (NullPointerException | TException e) {
            String outMsg = String.format("%s %s (Could not send log through ctrl-pipe: %s)", level, msg, e.getMessage());
            switch (level) {
                case ERROR:
                    System.err.println(outMsg);
                    break;
                case DEBUG:
                case INFO:
                default:
                    System.out.println(outMsg);
                    break;
            }
        }
    }
}
