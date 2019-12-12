package at.enfilo.def.library.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.library.api.rest.ILibraryAdminResponseService;
import at.enfilo.def.library.api.rest.ILibraryResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.*;

import java.util.List;

public class LibraryResponseServiceImpl extends ResponseService implements ILibraryResponseService, ILibraryAdminResponseService {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(LibraryResponseServiceImpl.class);

	/**
	 * Public constructor.
	 */
	public LibraryResponseServiceImpl() {
		super(LOGGER);
	}

	@Override
	public LibraryInfoDTO getLibraryInfo(String ticketId) {
		return getResult(ticketId, LibraryInfoDTO.class);
	}

	@Override
	public RoutineDTO getRoutine(String ticketId) {
		return getResult(ticketId, RoutineDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FeatureDTO> getRoutineRequiredFeatures(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public RoutineBinaryDTO getRoutineBinary(String ticketId) {
		return getResult(ticketId, RoutineBinaryDTO.class);
	}

	@Override
	public RoutineBinaryChunkDTO getRoutineBinaryChunk(String ticketId) {
		return getResult(ticketId, RoutineBinaryChunkDTO.class);
	}

	@Override
	public ServiceEndpointDTO getMasterLibrary(String ticketId) {
		return getResult(ticketId, ServiceEndpointDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findRoutines(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public String createRoutine(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public String updateRoutine(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public String createRoutineBinary(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public boolean verifyRoutineBinary(String ticketId) {
		return getResult(ticketId, Boolean.class);
	}

	@SuppressWarnings("unchecked")
    @Override
	public List<String> findDataTypes(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public String createDataType(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public DataTypeDTO getDataType(String ticketId) {
		return getResult(ticketId, DataTypeDTO.class);
	}

    @SuppressWarnings("unchecked")
	@Override
	public List<TagDTO> findTags(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public String createFeature(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@Override
	public String addExtension(String ticketId) {
		return getResult(ticketId, String.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FeatureDTO> getFeatures(String ticketId) {
		return getResult(ticketId, List.class);
	}

	@Override
	public FeatureDTO getFeatureByNameAndVersion(String ticketId) {
		return getResult(ticketId, FeatureDTO.class);
	}
}
