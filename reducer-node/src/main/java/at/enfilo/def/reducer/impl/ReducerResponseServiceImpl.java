package at.enfilo.def.reducer.impl;

import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.NodeResponseServiceImpl;
import at.enfilo.def.reducer.api.rest.IReducerResponseService;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;

/**
 * Reducer Response Service.
 */
public class ReducerResponseServiceImpl extends NodeResponseServiceImpl implements IReducerResponseService {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReducerResponseServiceImpl.class);

    /**
     * Public constructor.
     */
    public ReducerResponseServiceImpl() {
        super(LOGGER);
    }

    @SuppressWarnings("unchecked")
	@Override
	public List<ResourceDTO> fetchResult(String ticketId) {
		return getResult(ticketId, List.class);
	}
}
