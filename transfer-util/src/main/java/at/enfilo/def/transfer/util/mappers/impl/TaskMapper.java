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
public class TaskMapper extends AbstractMapper<TaskDTO, Task> {

    public TaskMapper() {
        super(TaskDTO.class, Task.class);
    }

    @Override
    public Task map(TaskDTO source, Task destination)
    throws IllegalArgumentException, IllegalStateException {

    	Task dest = destination;
        if (dest == null) {
            dest = new Task();
		}
        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getState, dest::setState);
            mapAttributes(source::getStartTime, dest::setStartTime, Instant::ofEpochMilli);
            mapAttributes(source::getCreateTime, dest::setCreateTime, Instant::ofEpochMilli);
            mapAttributes(source::getFinishTime, dest::setFinishTime, Instant::ofEpochMilli);
            if (source.getInParameters() != null) {
                if (dest.getInParameters() == null) {
                    dest.setInParameters(new HashMap<>());
                }
                for (Map.Entry<String, ResourceDTO> e : source.getInParameters().entrySet()) {
                    dest.getInParameters().put(
                            e.getKey(),
                            MapManager.map(e.getValue(), Resource.class)
                    );
                }
            }
            mapAttributes(
                    source::getOutParameters,
                    dest::setOutParameters,
                    resourceDTOs -> MapManager.map(resourceDTOs, Resource.class).collect(Collectors.toList())
            );
            return dest;
        }
        return null;
    }
}
