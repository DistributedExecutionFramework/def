package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.api.rest.IClientRoutineWorkerResponseService;
import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.NodeResponseServiceImpl;import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.List;

public class ClientRoutineWorkerResponseServiceImpl extends NodeResponseServiceImpl
implements ClientRoutineWorkerResponseService.Iface, IClientRoutineWorkerResponseService {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ClientRoutineWorkerResponseServiceImpl.class);

    public ClientRoutineWorkerResponseServiceImpl() { super(LOGGER); }

    @Override
    public List<String> getQueuedPrograms(String ticketId) {
        return getResult(ticketId, List.class);
    }

    @Override
    public ProgramDTO fetchFinishedProgram(String ticketId) {
        return getResult(ticketId, ProgramDTO.class);
    }
}
