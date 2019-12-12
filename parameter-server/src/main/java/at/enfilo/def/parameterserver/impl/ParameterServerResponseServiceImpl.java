package at.enfilo.def.parameterserver.impl;

import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.parameterserver.api.rest.IParameterServerResponseService;
import at.enfilo.def.transfer.dto.ResourceDTO;

/**
 * Created by mase on 30.03.2017.
 */
public class ParameterServerResponseServiceImpl extends ResponseService implements IParameterServerResponseService {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ParameterServerResponseServiceImpl.class);

    /**
     * Public constructor.
     */
    public ParameterServerResponseServiceImpl() {
        super(LOGGER);
    }

    @Override
    public String setParameter(String ticketId) {
        return getResult(ticketId, String.class);
    }

    @Override
    public String createParameter(String ticketId) {
        return getResult(ticketId, String.class);
    }

    @Override
    public ResourceDTO getParameter(String ticketId) {
        return getResult(ticketId, ResourceDTO.class);
    }

    @Override
    public String addToParameter(String ticketId) {
        return getResult(ticketId, String.class);
    }

    @Override
    public String deleteParameter(String ticketId) {
        return getResult(ticketId, String.class);
    }

    @Override
    public String deleteAllParameters(String ticketId) {
        return getResult(ticketId, String.class);
    }
}
