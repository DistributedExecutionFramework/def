package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.node.impl.NodeServiceImpl;
import at.enfilo.def.reducer.api.rest.IReducerService;
import at.enfilo.def.reducer.api.thrift.ReducerService;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;

/**
 * Created by mase on 28.08.2017.
 *
 * Implementation of {@link ReducerService.Iface} (Thrift) and {@link IReducerService} (RESTful)
 */
public class ReducerServiceImpl extends NodeServiceImpl
implements IReducerService {

    private final ReducerServiceController controller;
    private final ITicketRegistry ticketRegistry;

    public ReducerServiceImpl() {
        this(ReducerServiceController.getInstance(), TicketRegistry.getInstance());
    }

    public ReducerServiceImpl(ReducerServiceController controller, ITicketRegistry ticketRegistry) {
        super(controller, ticketRegistry);

        this.controller = controller;
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    public String createReduceJob(String jId, String routineId) {
        ITicket ticket = ticketRegistry.createTicket(() -> controller.createReduceJob(jId, routineId));
        return ticket.getId().toString();
    }

    @Override
    public String add(String jId, List<ResourceDTO> resources) {
        ITicket ticket = ticketRegistry.createTicket(() -> controller.addResources(jId, resources));
        return ticket.getId().toString();
    }

    @Override
    public String reduce(String jId) {
		ITicket ticket = ticketRegistry.createTicket(() -> controller.reduce(jId));
		return ticket.getId().toString();
    }

    @Override
    public String fetchResult(String jId) {
		ITicket ticket = ticketRegistry.createTicket(List.class, () -> controller.fetchResult(jId));
		return ticket.getId().toString();
    }

    @Override
    public String deleteReduceJob(String jId) {
        ITicket ticket = ticketRegistry.createTicket(() -> controller.deleteReduceJob(jId));
        return ticket.getId().toString();
    }
}
