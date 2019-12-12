package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.FormalParameter;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.entity.Routine_;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.RoutineType;

import java.time.Instant;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by mase on 28.04.2017.
 */
public class RoutineDataMaster extends DataMaster<Routine, String> {

    private static final Routine DUMMY_ROUTINE_1;
    private static final Routine DUMMY_ROUTINE_2;

    static {
        DUMMY_ROUTINE_1 = new Routine();
        DUMMY_ROUTINE_1.setPrivateRoutine(false);
        DUMMY_ROUTINE_1.setName(String.class.getName());
        DUMMY_ROUTINE_1.setDescription(String.class.getCanonicalName());
        DUMMY_ROUTINE_1.setRevision((short) 1);
        DUMMY_ROUTINE_1.setType(RoutineType.MASTER);
        DUMMY_ROUTINE_1.setInParameters(new LinkedList<>());
        DUMMY_ROUTINE_1.setOutParameter(new FormalParameter());

		DUMMY_ROUTINE_2 = new Routine();
		DUMMY_ROUTINE_2.setPrivateRoutine(false);
		DUMMY_ROUTINE_2.setName(Integer.class.getName());
		DUMMY_ROUTINE_2.setDescription(Integer.class.getCanonicalName());
		DUMMY_ROUTINE_2.setRevision((short) 2);
		DUMMY_ROUTINE_2.setType(RoutineType.MASTER);
        DUMMY_ROUTINE_2.setInParameters(new LinkedList<>());
        DUMMY_ROUTINE_2.setOutParameter(new FormalParameter());
    }

    public RoutineDataMaster() {
        super(Routine.class, String.class, Routine_.id);

        registerDummyEntity(DUMMY_ROUTINE_1);
        registerDummyEntity(DUMMY_ROUTINE_2);
    }

    @Override
    public Routine updateEntity(Routine initialEntity) {
        initialEntity.setId(UUID.randomUUID().toString());
		initialEntity.setPrivateRoutine(true);
		initialEntity.setRevision((short) 2);

        return initialEntity;
    }

    @Override
    public Routine getDummyEntityForSearch() {
        return DUMMY_ROUTINE_1;
    }

    @Override
    public Routine getDummyEntityForSave() {
        Routine dummyRoutine = new Routine();
		dummyRoutine.setPrivateRoutine(true);
		dummyRoutine.setName(Boolean.class.getName());
		dummyRoutine.setDescription(Boolean.class.getCanonicalName());
		dummyRoutine.setRevision((short) 1);
		dummyRoutine.setType(RoutineType.STORE);

        return dummyRoutine;
    }

    @Override
    public Routine getDummyEntityForUpdate() {
        return DUMMY_ROUTINE_2;
    }

    @Override
    public Routine getDummyEntityForDelete() {
        return DUMMY_ROUTINE_1;
    }

}
