package at.enfilo.def.routine.process.client;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.routine.ClientTestRoutine;
import at.enfilo.def.routine.factory.NamedPipeFactory;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ClientRoutineProcessTest {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static final String IN_PIPE = "ClientRoutineTest_in";
    public static final String CTRL_PIPE = "ClientRoutineTest_ctrl";

    public static final ProgramDTO PROGRAM ;
    public static final ServiceEndpointDTO SERVICE_ENDPOINT = new ServiceEndpointDTO(
            "localhost",
            40012,
            Protocol.THRIFT_TCP
    );
    public static final ServiceEndpointDTO PARAMETER_SERVER_ENDPOINT = new ServiceEndpointDTO(
            "localhost",
            40092,
            Protocol.THRIFT_TCP
    );

    static {
        PROGRAM = new ProgramDTO(
                "program1",
                ExecutionState.SCHEDULED,
                215,
                1003,
                "user",
                "name",
                "description",
                4,
                new HashMap<>(),
                new LinkedList<>()
        );
        PROGRAM.setResults(new HashMap<>());
    }

    private String inPipe;
    private String ctrlPipe;

    @Before
    public void setUp() throws Exception {
        inPipe = NamedPipeFactory.createPipe(IN_PIPE);
        ctrlPipe = NamedPipeFactory.createPipe(CTRL_PIPE);
    }

    @Test
    public void testClientRoutine() throws IOException, InterruptedException {
        if (OS.contains("windows")) {
            System.out.println(this.getClass().getName() + " Test is disabled under windows.");
            return;
        }

        ProcessBuilder workerServicePb = new ProcessBuilder(
                "java",
                "-cp",
                "build/classes/java/test:build/libs/*",
                WorkerServiceProcess.class.getCanonicalName(),
                inPipe,
                ctrlPipe
        );
        Process workerServiceProcess = workerServicePb.start();

        ProcessBuilder routinePb = new ProcessBuilder(
                "java",
                "-cp",
                "build/classes/java/test:build/libs/*",
                ClientTestRoutine.class.getCanonicalName(),
                ClientTestRoutine.class.getCanonicalName(),
                inPipe,
                ctrlPipe
        );
        Process routineProcess = routinePb.start();

        if (!routineProcess.waitFor(30, TimeUnit.SECONDS)) {
            fail();
        }
        int exitCode = routineProcess.exitValue();
        printProcessOutputs(routineProcess);
        assertEquals(0, exitCode);

        if (!workerServiceProcess.waitFor(30, TimeUnit.SECONDS)) {
            fail();
        }
        exitCode = workerServiceProcess.exitValue();
        printProcessOutputs(workerServiceProcess);
        assertEquals(0, exitCode);
    }

    @After
    public void tearDown() throws Exception {
        assertTrue(NamedPipeFactory.deletePipe(inPipe));
        assertTrue(NamedPipeFactory.deletePipe(ctrlPipe));
    }

    private void printProcessOutputs(Process p) throws IOException {
        String buffer;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            while ((buffer = reader.readLine()) != null) {
                System.out.println(buffer);
            }
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
            while ((buffer = reader.readLine()) != null) {
                System.err.println(buffer);
            }
        }
    }
}
