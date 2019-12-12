package at.enfilo.def.scheduler.clientroutineworker.impl;

import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.scheduler.clientroutineworker.api.rest.IClientRoutineWorkerSchedulerResponseService;

public class ClientRoutineWorkerSchedulerResponseServiceImpl extends ResponseService implements IClientRoutineWorkerSchedulerResponseService {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ClientRoutineWorkerSchedulerResponseServiceImpl.class);

    public ClientRoutineWorkerSchedulerResponseServiceImpl() { super(LOGGER);}
}
