package at.enfilo.def.scheduler.general.impl;

import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.util.List;

public class RoundRobinScheduler {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(RoundRobinScheduler.class);
    private int counter = 0;

    public String nextId(List<String> elements) {
        if (elements == null || elements.isEmpty()) {
            LOGGER.error("No elements set, returning null.");
            return null;
        }

        counter %= elements.size();
        return elements.get(counter++);
    }
}
