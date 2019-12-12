package at.enfilo.def.node.routine.exec;

import at.enfilo.def.communication.api.ticket.rest.ITicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.library.api.rest.ILibraryResponseService;
import at.enfilo.def.library.api.rest.ILibraryService;
import at.enfilo.def.transfer.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class LibraryServiceMock {
	private class LibraryService implements ILibraryService {
		@Override
		public String getLibraryInfo() {
			//TODO
			return null;
		}

		@Override
		public String getRoutine(String rId) {
			String ticketId = UUID.randomUUID().toString();
			tickets.put(ticketId, rId);
			return ticketId;
		}

		@Override
		public String getRoutineRequiredFeatures(String rId) {
			String ticketId = UUID.randomUUID().toString();
			tickets.put(ticketId, rId);
			return ticketId;
		}

		@Override
		public String getRoutineBinary(String rbId) {
			return null;
		}

		@Override
		public String getRoutineBinaryChunk(String rbId, short chunk, int chunkSize) {
			return null;
		}
	}

	private class LibraryResponseService implements ILibraryResponseService {
		@Override
		public LibraryInfoDTO getLibraryInfo(String ticketId) {
			//TODO
			return null;
		}

		@Override
		public RoutineDTO getRoutine(String ticketId) {
			String rId = tickets.remove(ticketId);
			return routines.get(rId);
		}

		@Override
		public List<FeatureDTO> getRoutineRequiredFeatures(String ticketId) {
			String rId = tickets.remove(ticketId);
			return routines.get(rId).getRequiredFeatures();
		}

		@Override
		public RoutineBinaryDTO getRoutineBinary(String ticketId) {
			return null;
		}

		@Override
		public RoutineBinaryChunkDTO getRoutineBinaryChunk(String ticketId) {
			return null;
		}
	}

	private class TicketService implements ITicketService {
		@Override
		public TicketStatusDTO getTicketStatus(String ticketId) {
			return TicketStatusDTO.DONE;
		}

		@Override
		public TicketStatusDTO waitForTicket(String ticketId) {
			return TicketStatusDTO.DONE;
		}

		@Override
		public TicketStatusDTO cancelTicketExecution(String ticketId, boolean mayInterruptIfRunning) {
			return TicketStatusDTO.CANCELED;
		}

		@Override
		public String getFailedMessage(String ticketId) {
			return null;
		}
	}


	private LibraryService libraryService;
	private LibraryResponseService libraryResponseService;
	private TicketService ticketService;
	private Map<String, String> tickets;
	private Map<String, RoutineDTO> routines;

	public LibraryServiceMock() {
		libraryService = new LibraryService();
		libraryResponseService = new LibraryResponseService();
		ticketService = new TicketService();
		tickets = new HashMap<>();
		routines = new HashMap<>();
	}

	public LibraryService getLibraryService() {
		return libraryService;
	}

	public LibraryResponseService getLibraryResponseService() {
		return libraryResponseService;
	}

	public TicketService getTicketService() {
		return ticketService;
	}

	public void registerRoutine(RoutineDTO routine) {
		routines.put(routine.getId(), routine);
	}
}
