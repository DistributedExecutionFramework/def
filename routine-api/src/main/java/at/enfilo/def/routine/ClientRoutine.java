package at.enfilo.def.routine;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.parameterserver.api.IParameterServerServiceClient;
import at.enfilo.def.parameterserver.api.client.ParameterServerServiceClientFactory;
import at.enfilo.def.parameterserver.api.protocol.PrimitiveProtocolParser;
import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class ClientRoutine extends AbstractRoutine {

    private ProgramDTO program;
    private IParameterServerServiceClient parameterServerServiceClient;
    private Map<String, ResourceDTO> results = new HashMap<>();
    private final TSerializer serializer = new TSerializer();

    @Override
    protected void runRoutine() {
        // fetch pId and client
        try {
            program = getProgram();
            IExecLogicServiceClient client = getServiceClient();
            parameterServerServiceClient = getParameterServerClient();
            results = new HashMap<>();

            routine(program.getId(), client);
            setResult();
        } catch (TException | AccessParameterException e) {
            throw new RoutineException(e);
        }
    }

    /**
     * ClientRoutine
     * Real implementation.
     *
     * @throws RoutineException
     */
    protected abstract void routine(String pId, IExecLogicServiceClient client) throws RoutineException;

    private void setResult() throws TException {
        log(LogLevel.DEBUG, "Storing results.");
        program.setResults(results);
        byte[] data = serializer.serialize(program);
        Result result = new Result(0, "PROGRAM", null, ByteBuffer.wrap(data));
        Order order = new Order(Command.SEND_RESULT, Integer.toString(1));
        ctrl.store(order);
        ctrl.store(result);
        log(LogLevel.INFO, "Client routine results stored.");
    }

    /**
     * Entry point for ClientRoutine
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            String errMsg = "3 Arguments needed: <routineName> <inPipe> <ctrlPipe>";
            throw new RoutineException(errMsg);
        } else {
            try {
                // Instantiate routine and run
                AbstractRoutine routine = createRoutine(args[0], args[1], null, args[2]);
                routine.run();
            } catch (Exception e){
                System.err.println(String.format("%s - Error while running ClientRoutine: %s", LogLevel.ERROR, e.getMessage()));
                throw new RoutineException(e);
            }
        }
    }

    private ProgramDTO getProgram() throws AccessParameterException {
        log(LogLevel.DEBUG, String.format("Requesting program id."));
        Order getPId = new Order(Command.GET_PARAMETER, "program");
        try {
            ctrl.store(getPId);
            ProgramDTO program = in.read(ProgramDTO.class.newInstance());
            log(LogLevel.DEBUG, String.format("Received program id %s.", program.getId()));
            return program;
        } catch (IllegalAccessException | InstantiationException | TException e) {
            throw new AccessParameterException(e);
        }
    }

    private IExecLogicServiceClient getServiceClient() throws AccessParameterException {
        log(LogLevel.DEBUG, String.format("Requesting service client."));
        Order getClient = new Order(Command.GET_PARAMETER, "serviceEndpoint");
        try {
            ctrl.store(getClient);
            ServiceEndpointDTO endpoint = in.read(ServiceEndpointDTO.class.newInstance());
            IExecLogicServiceClient client = new ExecLogicServiceClientFactory().createClient(endpoint);
            return client;
        } catch (IllegalAccessException | InstantiationException | TException | ClientCreationException e) {
            throw new AccessParameterException(e);
        }
    }

    private IParameterServerServiceClient getParameterServerClient() throws AccessParameterException {
        log(LogLevel.DEBUG, "Requesting parameter server client.");
        Order getClient = new Order(Command.GET_PARAMETER, "parameterServerEndpoint");
        try {
            ctrl.store(getClient);
            ServiceEndpointDTO endpoint = in.read(ServiceEndpointDTO.class.newInstance());
            return new ParameterServerServiceClientFactory().createClient(endpoint);
        } catch (IllegalAccessException | InstantiationException | TException | ClientCreationException e) {
            throw new AccessParameterException(e);
        }
    }

    protected void addToResults(String key, ResourceDTO resource) {
        if (key != null) {
            results.put(key, resource);
        }
    }

    public float[] getFloatParameter(String name) throws AccessParameterException {
        try {
            ResourceDTO resourceDTO = parameterServerServiceClient.getParameter(program.getId(), name, ParameterProtocol.PRIMITIVE).get();
            return (float[]) new PrimitiveProtocolParser().decode(resourceDTO);
        } catch (Exception e) {
            throw new AccessParameterException(e);
        }
    }

    public String setParameter(String name, float[] data) throws AccessParameterException {
        try {
            ResourceDTO resourceDTO = new PrimitiveProtocolParser().encode(data, "float");
            return parameterServerServiceClient.setParameter(program.getId(), name, resourceDTO, ParameterProtocol.PRIMITIVE).get();
        } catch (Exception e) {
            throw new AccessParameterException(e);
        }
    }

    public String deleteParameter(String name) throws ClientCommunicationException {
        try {
            return parameterServerServiceClient.deleteParameter(program.getId(), name).get();
        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    public String deleteAllParameters() throws ClientCommunicationException {
        try {
            return parameterServerServiceClient.deleteAllParameters(program.getId()).get();
        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    public String addToParameter(String name, float[] data) throws AccessParameterException {
        try {
            ResourceDTO resourceDTO = new PrimitiveProtocolParser().encode(data, "float");
            return parameterServerServiceClient.addToParameter(program.getId(), name, resourceDTO, ParameterProtocol.PRIMITIVE).get();
        } catch (Exception e) {
            throw new AccessParameterException(e);
        }
    }
}
