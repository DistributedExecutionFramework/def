package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.entity.Program_;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.transfer.dto.ExecutionState;

import java.time.Instant;

/**
 * Created by mase on 02.09.2016.
 */
public class ProgramDataMaster extends DataMaster<Program, String> {

    private static final Program DUMMY_PROGRAM_1;
    private static final Program DUMMY_PROGRAM_2;

    static {
        DUMMY_PROGRAM_1 = new Program();
        DUMMY_PROGRAM_1.setState(ExecutionState.SCHEDULED);
        DUMMY_PROGRAM_1.setCreateTime(Instant.now());
        DUMMY_PROGRAM_1.setFinishTime(Instant.now());

        DUMMY_PROGRAM_2 = new Program();
        DUMMY_PROGRAM_2.setState(ExecutionState.RUN);
        DUMMY_PROGRAM_2.setCreateTime(Instant.now());
        DUMMY_PROGRAM_2.setFinishTime(Instant.now());
    }

    private final IDataMaster<User, String> userDataMaster;

    public ProgramDataMaster(IDataMaster<User, String> userDataMaster) {
        super(Program.class, String.class, Program_.id);

        this.userDataMaster = userDataMaster;

        User dummyUser = userDataMaster.getDummyEntityForSearch();

        DUMMY_PROGRAM_1.setOwner(dummyUser);
        registerDummyEntity(DUMMY_PROGRAM_1);

        DUMMY_PROGRAM_2.setOwner(dummyUser);
        registerDummyEntity(DUMMY_PROGRAM_2);
    }

    @Override
    public Program updateEntity(Program initialEntity) {
        initialEntity.setState(ExecutionState.SUCCESS);
        initialEntity.setCreateTime(Instant.now());
        initialEntity.setFinishTime(Instant.now());

        return initialEntity;
    }

    @Override
    public Program getDummyEntityForSearch() {
        return DUMMY_PROGRAM_1;
    }

    @Override
    public Program getDummyEntityForSave() {
        Program dummyProgram = new Program();
        dummyProgram.setState(ExecutionState.FAILED);
        dummyProgram.setCreateTime(Instant.now());
        dummyProgram.setFinishTime(Instant.now());

        return dummyProgram;
    }

    @Override
    public Program getDummyEntityForUpdate() {
        return DUMMY_PROGRAM_2;
    }

    @Override
    public Program getDummyEntityForDelete() {
        return DUMMY_PROGRAM_1;
    }
}
