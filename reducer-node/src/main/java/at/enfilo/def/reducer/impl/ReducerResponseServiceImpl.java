package at.enfilo.def.reducer.impl;

import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.NodeResponseServiceImpl;
import at.enfilo.def.reducer.api.rest.IReducerResponseService;
import at.enfilo.def.reducer.api.thrift.ReducerResponseService;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;

public class ReducerResponseServiceImpl extends NodeResponseServiceImpl implements ReducerResponseService.Iface, IReducerResponseService {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReducerResponseServiceImpl.class);

    public ReducerResponseServiceImpl() { super(LOGGER); }

    @Override
    public List<String> getQueuedJobs(String ticketId) {
        return getResult(ticketId, List.class);
    }

    @Override
    public List<ResourceDTO> fetchResults(String ticketId) {
        return getResult(ticketId, List.class);
    }

    @Override
    public String getStoreRoutine(String ticketId) {
        return getResult(ticketId, String.class);
    }
}
