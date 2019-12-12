package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.domain.entity.Feature_;

import java.util.Collections;
import java.util.UUID;

public class FeatureDataMaster extends DataMaster<Feature, String> {

    private static final Feature DUMMY_FEATURE_1;
    private static final Feature DUMMY_FEATURE_2;
    private static final Feature DUMMY_FEATURE_3;


    static {
        DUMMY_FEATURE_1 = new Feature();
        DUMMY_FEATURE_1.setId(UUID.randomUUID().toString());
        DUMMY_FEATURE_1.setGroup("language");
        DUMMY_FEATURE_1.setName("java");
        DUMMY_FEATURE_1.setVersion("1.8");

        DUMMY_FEATURE_2 = new Feature();
        DUMMY_FEATURE_2.setId(UUID.randomUUID().toString());
        DUMMY_FEATURE_2.setGroup("language");
        DUMMY_FEATURE_2.setName("python");
        DUMMY_FEATURE_2.setVersion("3.7");

        DUMMY_FEATURE_3 = new Feature();
        DUMMY_FEATURE_3.setId(UUID.randomUUID().toString());
        DUMMY_FEATURE_3.setName("numpy");
        DUMMY_FEATURE_3.setVersion("1.8");
        DUMMY_FEATURE_3.setBaseFeature(DUMMY_FEATURE_2);

        DUMMY_FEATURE_2.setSubFeatures(Collections.singletonList(DUMMY_FEATURE_3));

    }

    public FeatureDataMaster() {
        super(Feature.class, String.class, Feature_.id);

        registerDummyEntity(DUMMY_FEATURE_1);
        registerDummyEntity(DUMMY_FEATURE_2);
        registerDummyEntity(DUMMY_FEATURE_3);
    }

    @Override
    public Feature updateEntity(Feature initialEntity) {
        initialEntity.setId("new id");
        initialEntity.setName("new name");

        return initialEntity;
    }

    @Override
    public Feature getDummyEntityForSearch() {
        return DUMMY_FEATURE_3;
    }

    @Override
    public Feature getDummyEntityForSave() {
        Feature dummyFeature = new Feature();
        dummyFeature.setId("save");
        dummyFeature.setName("save name");

        return dummyFeature;
    }

    @Override
    public Feature getDummyEntityForUpdate() {
        return DUMMY_FEATURE_2;
    }

    @Override
    public Feature getDummyEntityForDelete() {
        return DUMMY_FEATURE_1;
    }

}