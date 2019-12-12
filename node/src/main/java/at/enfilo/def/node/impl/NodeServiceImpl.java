package at.enfilo.def.node.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.node.api.rest.INodeService;
import at.enfilo.def.transfer.dto.NodeEnvironmentDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.PeriodUnit;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;

public class NodeServiceImpl implements INodeService {

    private final NodeServiceController controller;
    private final ITicketRegistry ticketRegistry;

    public NodeServiceImpl(NodeServiceController controller) {
        this(controller, TicketRegistry.getInstance());
    }

    public NodeServiceImpl(NodeServiceController controller, ITicketRegistry ticketRegistry) {
        this.controller = controller;
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    public String takeControl(String clusterId) {
        ITicket ticket = ticketRegistry.createTicket(
            	() -> controller.takeControl(clusterId),
				ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String getInfo() {
        ITicket ticket = ticketRegistry.createTicket(NodeInfoDTO.class, controller::getInfo);
        return ticket.getId().toString();
    }

    @Override
    public String getEnvironment() {
        ITicket ticket = ticketRegistry.createTicket(NodeEnvironmentDTO.class, controller::getEnvironment);
        return ticket.getId().toString();
    }

    @Override
    public String getFeatures() {
        ITicket ticket = ticketRegistry.createTicket(List.class, controller::getFeatures);
        return ticket.getId().toString();
    }

    @Override
    public String registerObserver(
        ServiceEndpointDTO endpointDTO,
        boolean checkPeriodically,
        long periodDuration,
        PeriodUnit periodUnit
    ) {
        ITicket ticket = ticketRegistry.createTicket(
            	() -> controller.registerObserver(
                	endpointDTO,
                	checkPeriodically,
                	periodDuration,
                	periodUnit
            	),
				ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String deregisterObserver(ServiceEndpointDTO endpointDTO) {
        ITicket ticket = ticketRegistry.createTicket(
            	() -> controller.deregisterObserver(endpointDTO),
				ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

	@Override
	public String addSharedResource(ResourceDTO sharedResource) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.addSharedResource(sharedResource)
		);
		return ticket.getId().toString();
	}

	@Override
	public String removeSharedResources(List<String> rIds) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.removeSharedResources(rIds)
		);
		return ticket.getId().toString();
	}

    @Override
    public String getStoreRoutine() {
        ITicket ticket = ticketRegistry.createTicket(
                String.class,
                controller::getStoreRoutineId
        );
        return ticket.getId().toString();
    }

    @Override
    public String setStoreRoutine(String routineId) {
        ITicket ticket = ticketRegistry.createTicket(() -> controller.setStoreRoutineId(routineId));
        return ticket.getId().toString();
    }

    @Override
    public String getQueueIds() {
        ITicket ticket = ticketRegistry.createTicket(
                List.class,
                controller::getQueueIds
        );
        return ticket.getId().toString();
    }

    @Override
    public String getQueueInfo(String qId) {
        ITicket ticket = ticketRegistry.createTicket(
                QueueInfoDTO.class,
                () -> controller.getQueueInfo(qId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String createQueue(String qId) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.createQueue(qId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String deleteQueue(String qId) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.deleteQueue(qId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String pauseQueue(String qId) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.pauseQueue(qId)
        );
        return ticket.getId().toString();
    }

    @Override
    public String releaseQueue(String qId) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.releaseQueue(qId)
        );
        return ticket.getId().toString();
    }

    @Override
	public String shutdown() {
		ITicket ticket = ticketRegistry.createTicket(
				ServiceRegistry.getInstance()::closeAll,
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}
}
