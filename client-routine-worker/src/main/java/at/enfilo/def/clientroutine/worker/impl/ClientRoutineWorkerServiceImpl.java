package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.api.rest.IClientRoutineWorkerService;
import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerService;
import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.node.impl.NodeServiceImpl;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.util.Collections;
import java.util.List;

public class ClientRoutineWorkerServiceImpl extends NodeServiceImpl implements IClientRoutineWorkerService, ClientRoutineWorkerService.Iface {

    private final ClientRoutineWorkerServiceController controller;
    private final ITicketRegistry ticketRegistry;

    public ClientRoutineWorkerServiceImpl() {
        this(ClientRoutineWorkerServiceController.getInstance(), TicketRegistry.getInstance());
    }

    public ClientRoutineWorkerServiceImpl(ClientRoutineWorkerServiceController controller, ITicketRegistry ticketRegistry) {
        super(controller, ticketRegistry);
        this.controller = controller;
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    public String getQueuedPrograms(String qId) {
        ITicket ticket = ticketRegistry.createTicket(
                List.class,
                () -> controller.getQueuedElements(qId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String queuePrograms(String qId, List<ProgramDTO> programs) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.queueElements(qId, programs)
        );
        return ticket.getId().toString();
    }

    @Override
    public String queueProgram(String qId, ProgramDTO program) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.queueElements(qId, Collections.singletonList(program))
        );
        return ticket.getId().toString();
    }


    @Override
    public String movePrograms(String qId, List<String> programIds, ServiceEndpointDTO targetNodeEndpoint) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.moveElements(
                        qId,
                        programIds,
                        targetNodeEndpoint
                )
        );
        return ticket.getId().toString();
    }

    @Override
    public String moveAllPrograms(ServiceEndpointDTO targetNodeEndpoint) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.moveAllElements(targetNodeEndpoint),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String fetchFinishedProgram(String pId) {
        ITicket ticket = ticketRegistry.createTicket(
                ProgramDTO.class,
                () -> controller.fetchFinishedElement(pId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String abortProgram(String pId) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.abortProgram(pId),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }
}
