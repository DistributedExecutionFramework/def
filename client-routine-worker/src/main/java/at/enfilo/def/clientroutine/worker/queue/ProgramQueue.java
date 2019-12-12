package at.enfilo.def.clientroutine.worker.queue;

import at.enfilo.def.clientroutine.worker.impl.ClientRoutineWorkerServiceController;
import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.queue.Queue;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.Set;


public class ProgramQueue extends Queue<ProgramDTO> {

    /**
     * Create a ProgramQueue
     *
     * @param qId - id of this queue, should be user id
     */
    public ProgramQueue(String qId) {
        super(qId, ClientRoutineWorkerServiceController.DTO_PROGRAM_CACHE_CONTEXT, ProgramDTO.class);
    }

    @Override
    protected Set<ITuple<ContextIndicator, ?>> getLoggingContext(String eId) {
        return DEFLoggerFactory.createProgramContext(eId);
    }

    @Override
    protected String getElementId(ProgramDTO element) {
        return element.getId();
    }
}
