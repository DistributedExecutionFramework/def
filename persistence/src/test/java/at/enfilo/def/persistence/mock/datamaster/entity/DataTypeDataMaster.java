package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.DataType;
import at.enfilo.def.domain.entity.DataType_;

/**
 * Created by mase on 02.09.2016.
 */
public class DataTypeDataMaster extends DataMaster<DataType, String> {

    public static final String DUMMY_TYPE_1_NAME = "DEFInteger";
    public static final String DUMMY_TYPE_1_SCHEMA = "struct DEFInteger {\n  1: i32 value\n}";

    public static final String DUMMY_TYPE_2_NAME = "DEFDouble";
    public static final String DUMMY_TYPE_2_SCHEMA = "struct DEFDouble {\n  1: double value\n}";


    private static final DataType DUMMY_TYPE_1;
    private static final DataType DUMMY_TYPE_2;

    static {
        DUMMY_TYPE_1 = new DataType();
        DUMMY_TYPE_1.setName(DUMMY_TYPE_1_NAME);
        DUMMY_TYPE_1.setSchema(DUMMY_TYPE_1_SCHEMA);

        DUMMY_TYPE_2 = new DataType();
        DUMMY_TYPE_2.setName(DUMMY_TYPE_2_NAME);
        DUMMY_TYPE_2.setSchema(DUMMY_TYPE_2_SCHEMA);
    }

    public DataTypeDataMaster() {
        super(DataType.class, String.class, DataType_.id);

        registerDummyEntity(DUMMY_TYPE_1);
        registerDummyEntity(DUMMY_TYPE_2);
    }

    @Override
    public DataType updateEntity(DataType initialEntity) {
        initialEntity.setName("DUMMY TYPE NAME #UPDATED");
        initialEntity.setSchema("DUMMY TYPE SCHEMA #UPDATED");

        return initialEntity;
    }

    @Override
    public DataType getDummyEntityForSearch() {
        return DUMMY_TYPE_1;
    }

    @Override
    public DataType getDummyEntityForSave() {
        DataType dummyType = new DataType();
        dummyType.setName("DUMMY TYPE #FOR_SAVE");
        dummyType.setSchema("DUMMY SCHEMA #FOR_SAVE");

        return dummyType;
    }

    @Override
    public DataType getDummyEntityForUpdate() {
        return DUMMY_TYPE_2;
    }

    @Override
    public DataType getDummyEntityForDelete() {
        return DUMMY_TYPE_1;
    }
}
