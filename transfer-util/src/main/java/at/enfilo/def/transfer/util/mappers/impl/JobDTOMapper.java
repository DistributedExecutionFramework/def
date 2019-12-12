package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.transfer.dto.JobDTO;

import java.time.Instant;

/**
 * Created by mase on 25.08.2016.
 */
public class JobDTOMapper extends AbstractMapper<Job, JobDTO> {

    public JobDTOMapper() {
        super(Job.class, JobDTO.class);
    }

    @Override
    public JobDTO map(Job source, JobDTO destination)
    throws IllegalArgumentException, IllegalStateException {

        JobDTO dest = destination;
        if (dest == null) {
            dest = new JobDTO();
        }

        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getState, dest::setState);
            mapAttributes(source::getCreateTime, dest::setCreateTime, Instant::toEpochMilli);
            mapAttributes(source::getStartTime, dest::setStartTime, Instant::toEpochMilli);
            mapAttributes(source::getFinishTime, dest::setFinishTime, Instant::toEpochMilli);
            if (source.getProgram() != null) {
                mapAttributes(source.getProgram()::getId, dest::setProgramId);
            }
            if (source.hasMapRoutine()) {
				mapAttributes(source.getMapRoutine()::getId, dest::setMapRoutineId);
			}
            if (source.hasReduceRoutine()) {
                mapAttributes(source.getReduceRoutine()::getId, dest::setReduceRoutineId);
            }
            mapAttributes(source.getScheduledTasks()::size, dest::setScheduledTasks);
            mapAttributes(source.getRunningTasks()::size, dest::setRunningTasks);
            mapAttributes(source.getSuccessfulTasks()::size, dest::setSuccessfulTasks);
            mapAttributes(source.getFailedTasks()::size, dest::setFailedTasks);
            mapAttributes(source::getReducedResults, dest::setReducedResults);

            return dest;
        }
        return null;
    }
}
