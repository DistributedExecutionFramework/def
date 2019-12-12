package at.enfilo.def.manager.impl;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.manager.api.rest.IManagerService;
import at.enfilo.def.manager.api.thrift.ManagerService;
import at.enfilo.def.transfer.dto.*;

import java.util.List;

public class ManagerServiceImpl implements IManagerService, ManagerService.Iface {

	private final ITicketRegistry ticketRegistry;
	private final ManagerController controller;

	public ManagerServiceImpl() {
		this(TicketRegistry.getInstance(), ManagerController.getInstance());
	}

	 ManagerServiceImpl(ITicketRegistry ticketRegistry, ManagerController controller) {
		this.ticketRegistry = ticketRegistry;
		this.controller = controller;
	}

	@Override
	public String getClusterIds() {
		ITicket ticket = ticketRegistry.createTicket(
				List.class,
				controller::getClusterIds,
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getClusterInfo(String cId) {
		ITicket ticket = ticketRegistry.createTicket(
				ClusterInfoDTO.class,
				() -> controller.getClusterInfo(cId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getClusterEndpoint(String cId) {
		ITicket ticket = ticketRegistry.createTicket(
				ServiceEndpointDTO.class,
				() -> controller.getClusterEndpoint(cId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String createAWSCluster(int numberOfWorkers, int numberOfReducers, AWSSpecificationDTO awsSpecificationDTO) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> controller.createAWSCluster(numberOfWorkers, numberOfReducers, awsSpecificationDTO),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String destroyCluster(String cId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.destroyCluster(cId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String adjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.adjustNodePoolSize(cId, newNodePoolSize, nodeType),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}


	@Override
	public String addCluster(ServiceEndpointDTO endpoint) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.addCluster(endpoint),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String createClientRoutine(RoutineDTO routine) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> controller.createClientRoutine(routine),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String createClientRoutineBinary(String rId, String binaryName, String md5, long sizeInBytes, boolean isPrimary) {
		ITicket ticket = ticketRegistry.createTicket(
				String.class,
				() -> controller.createClientRoutineBinary(rId, binaryName, md5, sizeInBytes, isPrimary),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String uploadClientRoutineBinaryChunk(String rbId, RoutineBinaryChunkDTO chunk) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.uploadClientRoutineBinaryChunk(rbId, chunk),
				ITicket.HIGHER_THAN_NORMAL_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String removeClientRoutine(String rId) {
		ITicket ticket = ticketRegistry.createTicket(
				() -> controller.removeClientRoutine(rId),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}

	@Override
	public String getFeatureByNameAndVersion(String name, String version) {
		ITicket ticket = ticketRegistry.createTicket(
				FeatureDTO.class,
				() -> controller.getFeatureByNameAndVersion(name, version),
				ITicket.SERVICE_PRIORITY
		);
		return ticket.getId().toString();
	}
}
