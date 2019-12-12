package at.enfilo.def.reducer.queue;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.queue.Queue;
import at.enfilo.def.reducer.impl.ReducerServiceController;
import at.enfilo.def.transfer.dto.ReduceJobDTO;

import java.util.Set;

public class ReduceJobQueue extends Queue<ReduceJobDTO> {

    public ReduceJobQueue(String qId) {
        super(qId, ReducerServiceController.DTO_JOB_CACHE_CONTEXT, ReduceJobDTO.class);
    }

    @Override
    protected String getElementId(ReduceJobDTO element) {
        return element.getJobId();
    }

    @Override
    protected Set<ITuple<ContextIndicator, ?>> getLoggingContext(String eId) {
        return DEFLoggerFactory.createJobContext(eId);
    }
}
