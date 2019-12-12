package at.enfilo.def.scheduler.reducer.impl;

import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.scheduler.reducer.api.rest.IReducerSchedulerResponseService;
import at.enfilo.def.transfer.dto.JobDTO;

public class ReducerSchedulerResponseServiceImpl extends ResponseService implements IReducerSchedulerResponseService {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReducerSchedulerResponseServiceImpl.class);

    public ReducerSchedulerResponseServiceImpl() { super(LOGGER);}

    @Override
    public JobDTO finalizeReduce(String ticketId) {
        return getResult(ticketId, JobDTO.class);
    }
}
