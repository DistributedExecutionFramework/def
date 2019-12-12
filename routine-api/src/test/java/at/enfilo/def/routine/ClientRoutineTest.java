package at.enfilo.def.routine;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.routine.mock.ClientRoutineMock;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import at.enfilo.def.routine.util.ThreadSafePipedIOStream;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.apache.thrift.TException;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class ClientRoutineTest {

    @Test
    public void routineWorkFlow() throws Exception {
        // Setup streams and IO
        ThreadSafePipedIOStream inPipe = new ThreadSafePipedIOStream();
        DataReader inReader = new DataReader(inPipe.getInputStream());
        DataWriter inWriter = new DataWriter(inPipe.getOutputStream());

        ThreadSafePipedIOStream ctrlPipe = new ThreadSafePipedIOStream();
        DataReader ctrlReader = new DataReader(ctrlPipe.getInputStream());
        DataWriter ctrlWriter = new DataWriter(ctrlPipe.getOutputStream());

        ClientTestRoutine routine = new ClientTestRoutine();
        routine.in = inReader;
        routine.ctrl = ctrlWriter;
        Thread t = new Thread(routine);
        t.start();
        await().atMost(10, TimeUnit.SECONDS).until(t::isAlive);

        // Receiver 2 orders: request input parameters
        // 1. param: program
        handleLog(ctrlReader);
        handleLog(ctrlReader);
        handleLog(ctrlReader);
        Order o = ctrlReader.read(new Order());
        assertEquals(Command.GET_PARAMETER, o.getCommand());
        assertEquals("program", o.getValue());
        ProgramDTO program = new ProgramDTO();
        program.setId(UUID.randomUUID().toString());
        program.setResults(new HashMap<>());
        inWriter.store(program);

        // 2. param: service endpoint
        handleLog(ctrlReader);
        handleLog(ctrlReader);
        o = ctrlReader.read(new Order());
        assertEquals(Command.GET_PARAMETER, o.getCommand());
        assertEquals("serviceEndpoint", o.getValue());
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
        endpoint.setProtocol(Protocol.THRIFT_TCP);
        endpoint.setPort(40012);
        endpoint.setHost("localhost");
        inWriter.store(endpoint);

        // 3. param: parameter server endpoint
        handleLog(ctrlReader);
        o = ctrlReader.read(new Order());
        assertEquals(Command.GET_PARAMETER, o.getCommand());
        assertEquals("parameterServerEndpoint", o.getValue());
        endpoint = new ServiceEndpointDTO();
        endpoint.setProtocol(Protocol.THRIFT_TCP);
        endpoint.setPort(40092);
        endpoint.setHost("localhost");
        inWriter.store(endpoint);

        // receive routine done
        handleLog(ctrlReader);
        o = ctrlReader.read(new Order());
        assertEquals(Command.SEND_RESULT, o.getCommand());
        assertEquals(1, Integer.parseInt(o.getValue()));

        Result result = new Result();
        ctrlReader.read(result);
        assertEquals(0, result.getSeq());
        assertEquals("PROGRAM", result.getKey());
        assertNotNull(result.getData());
        assertTrue(result.getData().length > 0);

        handleLog(ctrlReader);
        handleLog(ctrlReader);
        o = ctrlReader.read(new Order());
        assertEquals(Command.ROUTINE_DONE, o.getCommand());

        // wait for thread
        t.join();

        // Close all streams
        inReader.close();
        inWriter.close();
        ctrlReader.close();
        ctrlWriter.close();
    }

    private void handleLog(DataReader ctrlReader) throws TException {
        Order o = ctrlReader.read(new Order());
        switch (o.getCommand()) {
            case LOG_DEBUG:
                System.out.println(String.format("DEBUG %s", o.getValue()));
                break;
            case LOG_INFO:
                System.out.println(String.format("INFO %s", o.getValue()));
                break;
            case LOG_ERROR:
                System.out.println(String.format("ERROR %s", o.getValue()));
                break;
            default:
                fail();
        }
    }

    @Test
    public void mainSuccess() throws Exception {
        String inPipe = UUID.randomUUID().toString();
        String ctrlPipe = UUID.randomUUID().toString();
        ClientRoutine.main(new String[]{ClientRoutineMock.class.getCanonicalName(), inPipe, ctrlPipe});
        assertNotNull(ClientRoutineMock.getLastInstance());
        assertTrue(ClientRoutineMock.getLastInstance().isRun());
    }

    @Test (expected = RoutineException.class)
    public void mainWrongArgs() throws Exception {
        ClientRoutine.main(new String[]{"", ""});
    }
}
