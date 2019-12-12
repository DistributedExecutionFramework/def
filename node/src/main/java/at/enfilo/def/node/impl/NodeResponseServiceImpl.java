package at.enfilo.def.node.impl;

import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.rest.INodeResponseService;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeEnvironmentDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import org.slf4j.Logger;

import java.util.List;

/**
 * Created by mase on 24.08.2017.
 */
public class NodeResponseServiceImpl extends ResponseService
implements INodeResponseService {
    /**
     * Public constructor.
     */
    public NodeResponseServiceImpl() {
        this(DEFLoggerFactory.getLogger(NodeResponseServiceImpl.class));
    }

    public NodeResponseServiceImpl(Logger logger) {
        super(logger);
    }

    @Override
    public NodeInfoDTO getInfo(String ticketId) {
        return getResult(ticketId, NodeInfoDTO.class);
    }

    @Override
    public String getStoreRoutine(String ticketId) {
        return getResult(ticketId, String.class);
    }

    @Override
    public List<String> getQueueIds(String ticketId) {
        return getResult(ticketId, List.class);
    }

    @Override
    public QueueInfoDTO getQueueInfo(String ticketId) {
        return getResult(ticketId, QueueInfoDTO.class);
    }

    @Override
    public NodeEnvironmentDTO getEnvironment(String ticketId) {
        return getResult(ticketId, NodeEnvironmentDTO.class);
    }

    @Override
    public List<FeatureDTO> getFeatures(String ticketId) {
        return getResult(ticketId, List.class);
    }
}
