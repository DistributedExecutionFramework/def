package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Group_;

/**
 * Created by mase on 02.09.2016.
 */
public class GroupDataMaster extends DataMaster<Group, String> {

    private static final Group DUMMY_GROUP_1;
    private static final Group DUMMY_GROUP_2;

    static {
        DUMMY_GROUP_1 = new Group();
        DUMMY_GROUP_1.setName("DUMMY GROUP #1");
        DUMMY_GROUP_1.setDescription("DUMMY GROUP DESCRIPTION #1");

        DUMMY_GROUP_2 = new Group();
        DUMMY_GROUP_2.setName("DUMMY GROUP #2");
        DUMMY_GROUP_2.setDescription("DUMMY GROUP DESCRIPTION #2");
    }

    public GroupDataMaster() {
        super(Group.class, String.class, Group_.id);

        registerDummyEntity(DUMMY_GROUP_1);
        registerDummyEntity(DUMMY_GROUP_2);
    }

    @Override
    public Group updateEntity(Group initialEntity) {
        initialEntity.setName("DUMMY GROUP #UPDATED");
        initialEntity.setDescription("DUMMY GROUP DESCRIPTION #UPDATED");

        return initialEntity;
    }

    @Override
    public Group getDummyEntityForSearch() {
        return DUMMY_GROUP_1;
    }

    @Override
    public Group getDummyEntityForSave() {
        Group dummyGroup = new Group();
        dummyGroup.setName("DUMMY GROUP #FOR_SAVE");
        dummyGroup.setDescription("DUMMY GROUP DESCRIPTION #FOR_SAVE");

        return dummyGroup;
    }

    @Override
    public Group getDummyEntityForUpdate() {
        return DUMMY_GROUP_2;
    }

    @Override
    public Group getDummyEntityForDelete() {
        return DUMMY_GROUP_1;
    }
}
