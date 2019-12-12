package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.Tag;
import at.enfilo.def.domain.entity.Tag_;

/**
 * Created by mase on 07.07.2017.
 */
public class TagDataMaster extends DataMaster<Tag, String> {

    private static final Tag DUMMY_TAG_1;
    private static final Tag DUMMY_TAG_2;

    static {
        DUMMY_TAG_1 = new Tag();
        DUMMY_TAG_1.setId("simple");
        DUMMY_TAG_1.setDescription("simple tag");

		DUMMY_TAG_2 = new Tag();
        DUMMY_TAG_2.setId("complex");
        DUMMY_TAG_2.setDescription("complex tag");
    }

    public TagDataMaster() {
        super(Tag.class, String.class, Tag_.id);

        registerDummyEntity(DUMMY_TAG_1);
        registerDummyEntity(DUMMY_TAG_2);
    }

    @Override
    public Tag updateEntity(Tag initialEntity) {
		initialEntity.setId("new");
		initialEntity.setDescription("new tag");

        return initialEntity;
    }

    @Override
    public Tag getDummyEntityForSearch() {
        return DUMMY_TAG_1;
    }

    @Override
    public Tag getDummyEntityForSave() {
        Tag dummyTag = new Tag();
		dummyTag.setId("save");
		dummyTag.setDescription("save tag");

        return dummyTag;
    }

    @Override
    public Tag getDummyEntityForUpdate() {
        return DUMMY_TAG_2;
    }

    @Override
    public Tag getDummyEntityForDelete() {
        return DUMMY_TAG_1;
    }

}
