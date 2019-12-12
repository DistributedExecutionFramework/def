package at.enfilo.def.library.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.library.api.rest.ILibraryAdminService;
import at.enfilo.def.library.api.rest.ILibraryService;
import at.enfilo.def.transfer.dto.DataTypeDTO;
import at.enfilo.def.transfer.dto.LibraryInfoDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;

import java.nio.ByteBuffer;
import java.util.List;

public class LibraryServiceImpl implements ILibraryService, ILibraryAdminService {

	private static final ITicketRegistry TICKET_REGISTRY = TicketRegistry.getInstance();
	private final LibraryController controller;

	public LibraryServiceImpl() {
		this(LibraryController.getInstance());
	}

	public LibraryServiceImpl(LibraryController controller) {
		this.controller = controller;
	}

	@Override
	public String getLibraryInfo() {
        ITicket ticket = TICKET_REGISTRY.createTicket(
            LibraryInfoDTO.class,
            controller::getInfo
        );

        return ticket.getId().toString();
	}

	@Override
	public String getRoutine(String rId) {
		ITicket ticket = TICKET_REGISTRY.createTicket(
            RoutineDTO.class,
            () -> controller.getRoutine(rId)
        );

		return ticket.getId().toString();
	}

	@Override
	public String getRoutineRequiredFeatures(String rId) {
		ITicket ticket = TICKET_REGISTRY.createTicket(
				List.class,
				() -> controller.getRoutineRequiredFeatures(rId)
		);

		return ticket.getId().toString();
	}

	@Override
	public String getRoutineBinary(String rbId) {
		ITicket ticket = TICKET_REGISTRY.createTicket(
				RoutineBinaryDTO.class,
				() -> controller.getRoutineBinary(rbId)
		);

		return ticket.getId().toString();
	}

	@Override
	public String setDataEndpoint(ServiceEndpointDTO dataEndpoint) {
		ITicket ticket = TICKET_REGISTRY.createTicket(() -> controller.setLibraryEndpoint(dataEndpoint));
		return ticket.getId().toString();
	}

	@Override
	public String findRoutines(String pattern) {
		ITicket ticket = TICKET_REGISTRY.createTicket(
			List.class,
			() -> controller.findRoutines(pattern)
		);
        return ticket.getId().toString();
	}

	@Override
	public String removeRoutine(String rId) {
        ITicket ticket = TICKET_REGISTRY.createTicket(() -> controller.removeRoutine(rId));
        return ticket.getId().toString();
	}

	@Override
	public String createRoutine(RoutineDTO routineDTO) {
		ITicket ticket = TICKET_REGISTRY.createTicket(
            String.class,
            () -> controller.createRoutine(routineDTO)
        );

		return ticket.getId().toString();
	}

	@Override
	public String updateRoutine(RoutineDTO routineDTO) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
            String.class,
            () -> controller.updateRoutine(routineDTO)
        );
        return ticket.getId().toString();
	}

	@Override
	public String uploadRoutineBinary(
        String rId,
        String md5,
        long sizeInBytes,
        boolean isPrimary,
        ByteBuffer data
    ) {
		ITicket ticket = TICKET_REGISTRY.createTicket(
            String.class,
            () -> controller.uploadRoutineBinary(
                rId,
                md5,
                sizeInBytes,
                isPrimary,
                data
            )
        );

        return ticket.getId().toString();
	}

	@Override
	public String removeRoutineBinary(String rId, String bId) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
        		() -> controller.removeRoutineBinary(rId, bId)
		);

        return ticket.getId().toString();
	}

	@Override
	public String findDataTypes(String searchPattern) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
            List.class,
            () -> controller.findDataTypes(searchPattern)
        );

        return ticket.getId().toString();
	}

	@Override
	public String createDataType(String name, String schema) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
            String.class,
            () -> controller.createDataType(
                name,
                schema
            )
        );

        return ticket.getId().toString();
	}

	@Override
	public String getDataType(String dId) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
            DataTypeDTO.class,
            () -> controller.getDataType(dId)
        );

        return ticket.getId().toString();
	}

	@Override
	public String removeDataType(String dId) {
        ITicket ticket = TICKET_REGISTRY.createTicket(() -> controller.removeDataType(dId));
        return ticket.getId().toString();
	}

	@Override
	public String findTags(String searchPattern) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
            List.class,
            () -> controller.findTags(searchPattern)
        );

        return ticket.getId().toString();
	}

	@Override
	public String createTag(String label, String description) {
        ITicket ticket = TICKET_REGISTRY.createTicket(
            String.class,
            () -> controller.createTag(label, description)
        );

        return ticket.getId().toString();
	}

	@Override
	public String removeTag(String name) {
        ITicket ticket = TICKET_REGISTRY.createTicket(() -> controller.removeTag(name));
        return ticket.getId().toString();
	}

	@Override
	public String createFeature(String name, String group, String version) {
		ITicket ticket = TICKET_REGISTRY.createTicket(
				String.class,
				() -> controller.createFeature(name, group, version)
		);

		return ticket.getId().toString();
	}

	@Override
	public String addExtension(String featureId, String name, String version) {
		ITicket ticket = TICKET_REGISTRY.createTicket(
				String.class,
				() -> controller.addExtension(featureId, name, version)
		);

		return ticket.getId().toString();
	}

	@Override
	public String getFeatures(String pattern) {
		ITicket ticket = TICKET_REGISTRY.createTicket(
				List.class,
				() -> controller.getFeatures(pattern)
		);

		return ticket.getId().toString();
	}
}
