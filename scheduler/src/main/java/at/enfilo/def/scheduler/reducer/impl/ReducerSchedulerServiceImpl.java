package at.enfilo.def.scheduler.reducer.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.scheduler.reducer.api.rest.IReducerSchedulerService;
import at.enfilo.def.scheduler.reducer.api.strategy.IReduceSchedulingStrategy;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;

public class ReducerSchedulerServiceImpl implements IReducerSchedulerService {

    private final ITicketRegistry ticketRegistry;
    private final IReduceSchedulingStrategy strategy;

    public ReducerSchedulerServiceImpl(IReduceSchedulingStrategy strategy) {
        this.strategy = strategy;
        this.ticketRegistry = TicketRegistry.getInstance();
    }

    @Override
    public String addReducer(String rId, ServiceEndpointDTO endpoint) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.addReducer(rId, endpoint));
        return ticket.getId().toString();
    }

    @Override
    public String removeReducer(String rId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.removeReducer(rId));
        return ticket.getId().toString();
    }

    @Override
    public String addReduceJob(JobDTO job) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.addJob(job));
        return ticket.getId().toString();
    }

    @Override
    public String removeReduceJob(String jId) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.deleteJob(jId));
        return ticket.getId().toString();
    }

    @Override
    public String scheduleResourcesToReduce(String jId, List<ResourceDTO> resources) {
        ITicket ticket = ticketRegistry.createTicket(() -> strategy.scheduleReduce(jId, resources));
        return ticket.getId().toString();
    }

    @Override
    public String finalizeReduce(String jId) {
        ITicket ticket = ticketRegistry.createTicket(
                JobDTO.class,
                () -> strategy.finalizeReduce(jId)
        );
        return ticket.getId().toString();
    }
}
