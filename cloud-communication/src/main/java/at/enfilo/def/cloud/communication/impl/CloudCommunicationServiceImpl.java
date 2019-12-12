package at.enfilo.def.cloud.communication.impl;

import at.enfilo.def.cloud.communication.api.rest.ICloudCommunicationService;
import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationService;
import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.InstanceTypeDTO;
import at.enfilo.def.cloud.communication.logic.specific.aws.AWSSpecification;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;

import java.util.List;

public class CloudCommunicationServiceImpl implements ICloudCommunicationService, CloudCommunicationService.Iface {

    private final ITicketRegistry ticketRegistry;
    private final CloudCommunicationController controller;

    public CloudCommunicationServiceImpl() {
        this(TicketRegistry.getInstance(), CloudCommunicationController.getInstance());
    }

    public CloudCommunicationServiceImpl(ITicketRegistry ticketRegistry, CloudCommunicationController controller) {
        this.ticketRegistry = ticketRegistry;
        this.controller = controller;
    }

    @Override
    public String createAWSCluster(AWSSpecificationDTO specification) {
        AWSSpecification specs = new AWSSpecification(specification);

        ITicket ticket = ticketRegistry.createTicket(
                String.class,
                () -> controller.createAWSCluster(specs),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String bootClusterInstance(String cloudClusterId) {
        ITicket ticket = ticketRegistry.createTicket(
                String.class,
                () -> controller.bootClusterInstance(cloudClusterId),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String bootNodes(String cloudClusterId, InstanceTypeDTO instanceType, int nrOfNodes) {
        InstanceType type = InstanceType.getInstanceType(instanceType);

        ITicket ticket = ticketRegistry.createTicket(
                List.class,
                () -> controller.bootNodes(cloudClusterId, type, nrOfNodes),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String terminateNodes(String cloudClusterId, List<String> cloudInstanceIds) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.terminateNodes(cloudClusterId, cloudInstanceIds),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String getPublicIPAddressOfCloudInstance(String cloudClusterId, String cloudInstanceId) {
        ITicket ticket = ticketRegistry.createTicket(
                String.class,
                () -> controller.getPublicIPAddressOfCloudInstance(cloudClusterId, cloudInstanceId),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String getPrivateIPAddressOfCloudInstance(String cloudClusterId, String cloudInstanceId) {
        ITicket ticket = ticketRegistry.createTicket(
                String.class,
                () -> controller.getPrivateIPAddressOfCloudInstance(cloudClusterId, cloudInstanceId),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String shutdownCloudCluster(String cloudClusterId) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.shutdownCloudCluster(cloudClusterId),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }

    @Override
    public String mapDEFIdToCloudInstanceId(String cloudClusterId, String defId, String cloudInstanceId) {
        ITicket ticket = ticketRegistry.createTicket(
                () -> controller.mapDEFIdToCloudInstanceId(cloudClusterId, defId, cloudInstanceId),
                ITicket.SERVICE_PRIORITY
        );
        return ticket.getId().toString();
    }
}
