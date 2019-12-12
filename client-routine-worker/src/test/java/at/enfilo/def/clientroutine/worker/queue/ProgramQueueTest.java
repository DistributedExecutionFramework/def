package at.enfilo.def.clientroutine.worker.queue;

import at.enfilo.def.transfer.dto.ProgramDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProgramQueueTest {

    private ProgramQueue programQueue;

    @Before
    public void setUp() {
        this.programQueue = new ProgramQueue("test");
    }

    @Test
    public void queueProgram() throws Exception {
        assertEquals(0, this.programQueue.size());

        ProgramDTO p1 = new ProgramDTO();
        p1.setId("p1");
        this.programQueue.queue(p1);
        assertEquals(1, this.programQueue.size());

        ProgramDTO p2 = new ProgramDTO();
        p2.setId("p2");
        this.programQueue.queue(p2);
        assertEquals(2, this.programQueue.size());

        List<String> programIds = this.programQueue.getQueuedElements();
        assertEquals("p1", programIds.get(0));
        assertEquals("p2", programIds.get(1));
    }

    @After
    public void tearDown() {
        this.programQueue = null;
    }
}
