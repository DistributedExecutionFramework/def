package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.RoutineBinary;
import at.enfilo.def.domain.entity.RoutineBinary_;

/**
 * Created by mase on 07.07.2017.
 */
public class RoutineBinaryDataMaster extends DataMaster<RoutineBinary, String> {

    private static final RoutineBinary DUMMY_ROUTINE_BINARY_1;
    private static final RoutineBinary DUMMY_ROUTINE_BINARY_2;

    static {
        DUMMY_ROUTINE_BINARY_1 = new RoutineBinary();
        DUMMY_ROUTINE_BINARY_1.setMd5("1");
        DUMMY_ROUTINE_BINARY_1.setSizeInBytes(1);
        DUMMY_ROUTINE_BINARY_1.setPrimary(true);
        DUMMY_ROUTINE_BINARY_1.setUrl("1");

		DUMMY_ROUTINE_BINARY_2 = new RoutineBinary();
        DUMMY_ROUTINE_BINARY_2.setMd5("2");
        DUMMY_ROUTINE_BINARY_2.setSizeInBytes(2);
        DUMMY_ROUTINE_BINARY_2.setPrimary(false);
        DUMMY_ROUTINE_BINARY_2.setUrl("2");
    }

    public RoutineBinaryDataMaster() {
        super(RoutineBinary.class, String.class, RoutineBinary_.id);

        registerDummyEntity(DUMMY_ROUTINE_BINARY_1);
        registerDummyEntity(DUMMY_ROUTINE_BINARY_2);
    }

    @Override
    public RoutineBinary updateEntity(RoutineBinary initialEntity) {
        long updatedValue = initialEntity.getSizeInBytes() + 1;
        String updatedStringValue = Long.toString(updatedValue);

        initialEntity.setMd5(updatedStringValue);
        initialEntity.setSizeInBytes(updatedValue);
        initialEntity.setUrl(updatedStringValue);

        return initialEntity;
    }

    @Override
    public RoutineBinary getDummyEntityForSearch() {
        return DUMMY_ROUTINE_BINARY_1;
    }

    @Override
    public RoutineBinary getDummyEntityForSave() {
        RoutineBinary dummyRoutineBinary = new RoutineBinary();
        dummyRoutineBinary.setMd5("0");
        dummyRoutineBinary.setSizeInBytes(0);
        dummyRoutineBinary.setPrimary(false);
        dummyRoutineBinary.setUrl("0");

        return dummyRoutineBinary;
    }

    @Override
    public RoutineBinary getDummyEntityForUpdate() {
        return DUMMY_ROUTINE_BINARY_2;
    }

    @Override
    public RoutineBinary getDummyEntityForDelete() {
        return DUMMY_ROUTINE_BINARY_1;
    }

}
