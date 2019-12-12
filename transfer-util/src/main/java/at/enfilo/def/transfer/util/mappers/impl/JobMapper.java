package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.transfer.dto.JobDTO;

import java.time.Instant;

/**
 * Created by mase on 25.08.2016.
 */
public class JobMapper extends AbstractMapper<JobDTO, Job> {

    public JobMapper() {
        super(JobDTO.class, Job.class);
    }

    @Override
    public Job map(JobDTO source, Job destination)
    throws IllegalArgumentException, IllegalStateException {

        Job dest = destination;
        if (dest == null) {
            dest = new Job();
        }

        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getState, dest::setState);
            mapAttributes(source::getCreateTime, dest::setCreateTime, Instant::ofEpochMilli);
            mapAttributes(source::getStartTime, dest::setStartTime, Instant::ofEpochMilli);
            mapAttributes(source::getFinishTime, dest::setFinishTime, Instant::ofEpochMilli);

            return dest;
        }
        return null;
    }
}
