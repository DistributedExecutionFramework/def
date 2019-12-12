package at.enfilo.def.scheduler.worker.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.scheduler.worker.api.rest.IWorkerSchedulerService;
import at.enfilo.def.scheduler.worker.api.strategy.ITaskSchedulingStrategy;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.util.Collections;

public class WorkerSchedulerServiceImpl implements IWorkerSchedulerService {

    private final ITicketRegistry ticketRegistry;
    private final ITaskSchedulingStrategy strategy;

    public WorkerSchedulerServiceImpl(ITaskSchedulingStrategy strategy) {
        this.strategy = strategy;
        this.ticketRegistry = TicketRegistry.getInstance();
    }

    @Override
    public String addWorker(String wId, ServiceEndpointDTO endpoint) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.addWorker(wId, endpoint));
        return ticket.getId().toString();
    }

    @Override
    public String removeWorker(String wId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.removeWorker(wId));
        return ticket.getId().toString();
    }

    @Override
    public String addJob(String jId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.addJob(jId));
        return ticket.getId().toString();
    }

    @Override
    public String removeJob(String jId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.removeJob(jId));
        return ticket.getId().toString();
    }

    @Override
    public String scheduleTask(String jId, TaskDTO task) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.schedule(jId, Collections.singletonList(task)));
        return ticket.getId().toString();
    }

    @Override
    public String abortTask(String wId, String tId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.abortTask(wId, tId));
        return ticket.getId().toString();
    }

    @Override
    public String markJobAsComplete(String jId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.markJobAsComplete(jId));
        return ticket.getId().toString();
    }
}
