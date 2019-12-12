package at.enfilo.def.scheduler.worker.impl;

import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.scheduler.worker.api.rest.IWorkerSchedulerResponseService;

public class WorkerSchedulerResponseServiceImpl extends ResponseService implements IWorkerSchedulerResponseService {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(WorkerSchedulerResponseServiceImpl.class);

    public WorkerSchedulerResponseServiceImpl() {
        super(LOGGER);
    }
}
