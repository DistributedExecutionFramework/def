package at.enfilo.def.node.queue;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.Set;

public class ResourceQueue extends Queue<ResourceDTO> {

    public ResourceQueue(String qId, String dtoCacheContext) {
        super(qId, dtoCacheContext, ResourceDTO.class);
    }

    @Override
    protected String getElementId(ResourceDTO element) {
        return element.getId();
    }

    @Override
    protected Set<ITuple<ContextIndicator, ?>> getLoggingContext(String eId) {
        return null;
    }
}
