package at.enfilo.def.scheduler.impl;

import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.scheduler.api.rest.ISchedulerResponseService;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;

/**
 * Scheduler Response Service.
 */
public class SchedulerResponseServiceImpl extends ResponseService implements ISchedulerResponseService {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(SchedulerResponseServiceImpl.class);

    /**
     * Public constructor.
     */
    public SchedulerResponseServiceImpl() {
        super(LOGGER);
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<ResourceDTO> finalizeReduce(String ticketId) {
        return getResult(ticketId, List.class);
    }
}
