package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.node.impl.NodeServiceImpl;
import at.enfilo.def.reducer.api.rest.IReducerService;
import at.enfilo.def.reducer.api.thrift.ReducerService;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;

public class ReducerServiceImpl extends NodeServiceImpl implements ReducerService.Iface, IReducerService {

    private final ReducerServiceController controller;
    private final ITicketRegistry ticketRegistry;

    public ReducerServiceImpl() {
        this(ReducerServiceController.getInstance(), TicketRegistry.getInstance());
    }

    public ReducerServiceImpl(
            ReducerServiceController controller,
            ITicketRegistry ticketRegistry
    ) {
        super(controller, ticketRegistry);
        this.controller = controller;
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    public String getQueuedJobs(String pId) {
        ITicket ticket = ticketRegistry.createTicket(
                List.class,
                () -> controller.getQueuedElements(pId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String createReduceJob(JobDTO job) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.createReduceJob(job)
        );
        return ticket.getId().toString();
    }

    @Override
    public String abortReduceJob(String jId) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.abortReduceJob(jId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String addResourcesToReduce(String jId, List<ResourceDTO> resources) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.addResourcesToReduce(jId, resources)
        );
        return ticket.getId().toString();
    }

    @Override
    public String reduceJob(String jId) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.reduceJob(jId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String fetchResults(String jId) {
        ITicket ticket = ticketRegistry.createTicket(
                List.class,
                () -> controller.fetchResults(jId)
        );
        return ticket.getId().toString();
    }
}
