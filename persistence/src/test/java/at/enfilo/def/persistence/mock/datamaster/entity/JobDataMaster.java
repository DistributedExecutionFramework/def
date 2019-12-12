package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Job_;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.transfer.dto.ExecutionState;

import java.time.Instant;

/**
 * Created by mase on 02.09.2016.
 */
public class JobDataMaster extends DataMaster<Job, String> {

    private static final Job DUMMY_JOB_1;
    private static final Job DUMMY_JOB_2;

    static {
        DUMMY_JOB_1 = new Job();
        DUMMY_JOB_1.setState(ExecutionState.SCHEDULED);
        DUMMY_JOB_1.setCreateTime(Instant.now());
        DUMMY_JOB_1.setFinishTime(Instant.now());
        DUMMY_JOB_1.setProgram(new Program());

        DUMMY_JOB_2 = new Job();
        DUMMY_JOB_2.setState(ExecutionState.RUN);
        DUMMY_JOB_2.setCreateTime(Instant.now());
        DUMMY_JOB_2.setFinishTime(Instant.now());
        DUMMY_JOB_1.setProgram(new Program());
    }

    public JobDataMaster() {
        super(Job.class, String.class, Job_.id);

        registerDummyEntity(DUMMY_JOB_1);
        registerDummyEntity(DUMMY_JOB_2);
    }

    @Override
    public Job updateEntity(Job initialEntity) {
        initialEntity.setState(ExecutionState.SUCCESS);
        initialEntity.setCreateTime(Instant.now());
        initialEntity.setFinishTime(Instant.now());

        return initialEntity;
    }

    @Override
    public Job getDummyEntityForSearch() {
        return DUMMY_JOB_1;
    }

    @Override
    public Job getDummyEntityForSave() {
        Job dummyJob = new Job();
        dummyJob.setState(ExecutionState.FAILED);
        dummyJob.setCreateTime(Instant.now());
        dummyJob.setFinishTime(Instant.now());

        return dummyJob;
    }

    @Override
    public Job getDummyEntityForUpdate() {
        return DUMMY_JOB_2;
    }

    @Override
    public Job getDummyEntityForDelete() {
        return DUMMY_JOB_1;
    }

}
