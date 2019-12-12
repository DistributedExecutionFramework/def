package at.enfilo.def.scheduler.clientroutineworker.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.scheduler.clientroutineworker.api.rest.IClientRoutineWorkerSchedulerService;
import at.enfilo.def.scheduler.clientroutineworker.api.strategy.IProgramSchedulingStrategy;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.apache.thrift.TException;

import java.util.Collection;

public class ClientRoutineWorkerSchedulerServiceImpl implements IClientRoutineWorkerSchedulerService {

    private final ITicketRegistry ticketRegistry;
    private final IProgramSchedulingStrategy strategy;

    public ClientRoutineWorkerSchedulerServiceImpl(IProgramSchedulingStrategy strategy) {
        this.strategy = strategy;
        this.ticketRegistry = TicketRegistry.getInstance();
    }

    @Override
    public String addClientRoutineWorker(String wId, ServiceEndpointDTO endpoint) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.addClientRoutineWorker(wId, endpoint));
        return ticket.getId().toString();
    }

    @Override
    public String removeClientRoutineWorker(String wId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.removeClientRoutineWorker(wId));
        return ticket.getId().toString();
    }

    @Override
    public String addUser(String uId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.addUser(uId));
        return ticket.getId().toString();
    }

    @Override
    public String removeUser(String uId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.removeUser(uId));
        return ticket.getId().toString();
    }

    @Override
    public String abortProgram(String wId, String pId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.abortProgram(wId, pId));
        return ticket.getId().toString();
    }

    @Override
    public String scheduleProgram(String uId, ProgramDTO program) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.scheduleProgram(uId, program));
        return ticket.getId().toString();
    }
}
