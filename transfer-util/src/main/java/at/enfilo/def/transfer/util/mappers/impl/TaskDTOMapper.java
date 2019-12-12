package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.Resource;
import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.transfer.util.MapManager;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by mase on 26.08.2016.
 */
public class TaskDTOMapper extends AbstractMapper<Task, TaskDTO> {

    public TaskDTOMapper() {
        super(Task.class, TaskDTO.class);
    }

    @Override
    public TaskDTO map(Task source, TaskDTO destination)
    throws IllegalArgumentException, IllegalStateException {

    	TaskDTO dest = destination;
        if (dest == null) {
            dest = new TaskDTO();
        }
        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            if (source.getJob() != null) {
                mapAttributes(source.getJob()::getId, dest::setJobId);
                if (source.getJob().getProgram() != null) {
                    mapAttributes(source.getJob().getProgram()::getId, dest::setProgramId);
                }
            }
            mapAttributes(source::getState, dest::setState);
            mapAttributes(source::getStartTime, dest::setStartTime, Instant::toEpochMilli);
            mapAttributes(source::getCreateTime, dest::setCreateTime, Instant::toEpochMilli);
            mapAttributes(source::getFinishTime, dest::setFinishTime, Instant::toEpochMilli);
            if (source.getObjectiveRoutine() != null) {
                mapAttributes(source.getObjectiveRoutine()::getId, dest::setObjectiveRoutineId);
            }
            if (source.getMapRoutine() != null) {
                mapAttributes(source.getMapRoutine()::getId, dest::setMapRoutineId);
            }
			if (source.getInParameters() != null) {
				if (dest.getInParameters() == null) {
					dest.setInParameters(new HashMap<>());
				}
				for (Map.Entry<String, Resource> e : source.getInParameters().entrySet()) {
					dest.getInParameters().put(
							e.getKey(),
							MapManager.map(e.getValue(), ResourceDTO.class)
					);
				}
			}
            mapAttributes(
                    source::getOutParameters,
                    dest::setOutParameters,
                    resources -> MapManager.map(resources, ResourceDTO.class).collect(Collectors.toList())
            );

            return dest;
        }
        return null;
    }
}
