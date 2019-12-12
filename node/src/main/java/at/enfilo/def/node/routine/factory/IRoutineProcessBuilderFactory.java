package at.enfilo.def.node.routine.factory;

import at.enfilo.def.node.api.exception.RoutineCreationException;
import at.enfilo.def.node.routine.exec.SequenceStep;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.RoutineDTO;

public interface IRoutineProcessBuilderFactory {

    ProcessBuilder getRoutineProcessBuilder(RoutineDTO routineDTO, SequenceStep sequenceStep,
                                            NodeConfiguration configuration, boolean addOutPipe)
            throws RoutineCreationException;
}
