package at.enfilo.def.scheduler.clientroutineworker.strategy;

import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import at.enfilo.def.clientroutine.worker.api.IClientRoutineWorkerServiceClient;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.scheduler.general.impl.RoundRobinScheduler;
import at.enfilo.def.scheduler.general.util.SchedulerConfiguration;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoundRobinProgramSchedulingStrategy extends ProgramSchedulingStrategy {

    private final RoundRobinScheduler roundRobinScheduler;

    public RoundRobinProgramSchedulingStrategy(
            SchedulerConfiguration configuration
    ) {
        super(configuration);
        this.roundRobinScheduler = new RoundRobinScheduler();
    }

    RoundRobinProgramSchedulingStrategy(
            Map<String, IClientRoutineWorkerServiceClient> clientRoutineWorkers,
            Map<String, Environment> clientRoutineWorkerEnvironments,
            SchedulerConfiguration configuration,
            ClientRoutineWorkerServiceClientFactory factory,
            ILibraryServiceClient libraryServiceClient
    ) {
        super(
                Collections.synchronizedSet(new HashSet<>()),
                new ConcurrentHashMap<>(),
                clientRoutineWorkers,
                clientRoutineWorkerEnvironments,
                factory,
                libraryServiceClient,
                configuration
        );
        this.roundRobinScheduler = new RoundRobinScheduler();
    }

    @Override
    public String nextClientRoutineWorkerId() {
        return roundRobinScheduler.nextId(getClientRoutineWorkers());
    }

    @Override
    public void notifyNodeInfo(String nId, NodeInfoDTO nodeInfo) {
        // not needed, because RoundRobin is a static scheduler
    }
}
