package at.enfilo.def.scheduler.clientroutineworker.strategy;

import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RoundRobinProgramSchedulingStrategyTest extends ProgramSchedulingStrategyTest {

    private RoundRobinProgramSchedulingStrategy rr;

    @Override
    protected ProgramSchedulingStrategy createStrategy() {
        rr = new RoundRobinProgramSchedulingStrategy(
                nodes,
                nodeEnvironments,
                schedulerConfiguration,
                new ClientRoutineWorkerServiceClientFactory(),
                libraryServiceClient
        );
        return rr;
    }

    @Test
    public void zeroNodes() throws Exception {
        nodes.clear();
        assertNull(rr.nextClientRoutineWorkerId());
        assertNull(rr.nextClientRoutineWorkerId());
    }

    @Test
    public void oneNode() throws Exception {
        String w1 = UUID.randomUUID().toString();
        nodes.put(w1, null);

        assertEquals(w1, rr.nextClientRoutineWorkerId());
        assertEquals(w1, rr.nextClientRoutineWorkerId());
        assertEquals(w1, rr.nextClientRoutineWorkerId());
    }

    @Test
    public void moreNodes() throws Exception {
        int nrWorkers = 5;
        for (int i = 0; i < nrWorkers; i++) {
            nodes.put(UUID.randomUUID().toString(), null);
        }

        String[] orderedWorkers = nodes.keySet().toArray(new String[nrWorkers]);

        for (int i = 0; i < 99; i++) {
            assertEquals(orderedWorkers[i % nrWorkers], rr.nextClientRoutineWorkerId());
        }
    }
}
