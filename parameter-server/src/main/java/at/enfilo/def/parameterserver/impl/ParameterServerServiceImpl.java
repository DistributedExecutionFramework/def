package at.enfilo.def.parameterserver.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.parameterserver.api.rest.IParameterServerService;
import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ParameterType;
import at.enfilo.def.transfer.dto.ResourceDTO;

public class ParameterServerServiceImpl implements IParameterServerService {

    private static final ITicketRegistry TICKET_REGISTRY = TicketRegistry.getInstance();
    private final ParameterServerController controller;

    public ParameterServerServiceImpl() {
        this(ParameterServerController.getInstance());
    }

    public ParameterServerServiceImpl(ParameterServerController controller) {
        this.controller = controller;
    }

    @Override
    public String setParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
                String.class,
                () -> controller.setParameter(programId, parameterId, parameter, protocol)
        );
        return ticket.getId().toString();
    }

    @Override
    public String createParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol, ParameterType type) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
                String.class,
                () -> controller.createParameter(programId, parameterId, parameter, protocol, type)
        );
        return ticket.getId().toString();
    }

    @Override
    public String getParameter(String programId, String parameterId, ParameterProtocol protocol) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
                ResourceDTO.class,
                () -> controller.getParameter(programId, parameterId, protocol)
        );
        return ticket.getId().toString();
    }

    @Override
    public String addToParameter(String programId, String parameterId, ResourceDTO parameter, ParameterProtocol protocol) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
                String.class,
                () -> controller.addToParameter(programId, parameterId, parameter, protocol)
        );
        return ticket.getId().toString();
    }

    @Override
    public String deleteParameter(String programId, String parameterId) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
                String.class,
                () -> controller.deleteParameter(programId, parameterId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String deleteAllParameters(String programId) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
                String.class,
                () -> controller.deleteAllParameters(programId)
        );
        return ticket.getId().toString();
    }
}
