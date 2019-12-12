package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.util.MapperTest;
import at.enfilo.def.transfer.util.MapManager;
import at.enfilo.def.transfer.util.UnsupportedMappingException;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class JobMapperTest extends MapperTest<Job, JobDTO> {

    private static final Object UNSUPPORTED_OBJ;
    private static final Class<?> UNSUPPORTED_CLASS;

    private static final Job JOB;
    private static final JobDTO JOB_DTO;

    static  {
        UNSUPPORTED_OBJ = "UNSUPPORTED_OBJ";
        UNSUPPORTED_CLASS = UNSUPPORTED_OBJ.getClass();

        String jId = UUID.randomUUID().toString();
        ExecutionState executionState = ExecutionState.SCHEDULED;
        Instant now = Instant.now();

        JOB = new Job();
        JOB.setId(jId);
        JOB.setState(executionState);
        JOB.setCreateTime(now);
        JOB.setFinishTime(now);

        JOB_DTO = new JobDTO();
        JOB_DTO.setId(jId);
        JOB_DTO.setState(executionState);
        JOB_DTO.setCreateTime(now.toEpochMilli());
        JOB_DTO.setFinishTime(now.toEpochMilli());
    }

    public JobMapperTest() {
        super(JOB, JOB_DTO, Job.class, JobDTO.class, UNSUPPORTED_OBJ, UNSUPPORTED_CLASS);
    }

    @Test
    public void mapEntityToDTO()
    throws UnsupportedMappingException {

        JobDTO jobDTO = MapManager.map(JOB, JobDTO.class);

        assertNotNull(jobDTO);

        assertEquals(jobDTO.getId(), JOB.getId());
        assertEquals(jobDTO.getState(), JOB.getState());
        assertEquals(jobDTO.getCreateTime(), JOB.getCreateTime().toEpochMilli());
        assertEquals(jobDTO.getFinishTime(), JOB.getFinishTime().toEpochMilli());
    }

    @Test
    public void mapDTOToEntity()
    throws UnsupportedMappingException {

        Job job = MapManager.map(JOB_DTO, Job.class);

        assertNotNull(job);

        assertEquals(job.getId(), JOB_DTO.getId());
        assertEquals(job.getState(), JOB_DTO.getState());
        assertEquals(job.getCreateTime().toEpochMilli(), JOB_DTO.getCreateTime());
        assertEquals(job.getFinishTime().toEpochMilli(), JOB_DTO.getFinishTime());
    }
}
