package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.common.util.DEFHashHelper;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.entity.User_;

/**
 * Created by mase on 02.09.2016.
 */
public class UserDataMaster extends DataMaster<User, String> {

    public static final String DUMMY_USER_1_NAME = "DUMMY USER #1";
    public static final String DUMMY_USER_1_REAL_PASSWORD = "DUMMY USER PASS #1";

    public static final String DUMMY_USER_2_NAME = "DUMMY USER #2";
    public static final String DUMMY_USER_2_REAL_PASSWORD = "DUMMY USER PASS #2";


    private static final User DUMMY_USER_1;
    private static final User DUMMY_USER_2;

    static {
        DUMMY_USER_1 = new User();

        String dummyUserSalt1 = DEFHashHelper.generateNewSalt();
        DUMMY_USER_1.setSalt(dummyUserSalt1);
        DUMMY_USER_1.setPass(DEFHashHelper.getPasswordHash(DUMMY_USER_1_REAL_PASSWORD, dummyUserSalt1));
        DUMMY_USER_1.setName(DUMMY_USER_1_NAME);

        DUMMY_USER_2 = new User();

        String dummyUserSalt2 = DEFHashHelper.generateNewSalt();
        DUMMY_USER_2.setSalt(dummyUserSalt2);
        DUMMY_USER_2.setPass(DEFHashHelper.getPasswordHash(DUMMY_USER_2_REAL_PASSWORD, dummyUserSalt2));
        DUMMY_USER_2.setName(DUMMY_USER_2_NAME);
    }

    public UserDataMaster() {
        super(User.class, String.class, User_.id);

        registerDummyEntity(DUMMY_USER_1);
        registerDummyEntity(DUMMY_USER_2);
    }

    @Override
    public User updateEntity(User initialEntity) {
        initialEntity.setSalt("DUMMY USER SALT #UPDATED");
        initialEntity.setPass("DUMMY USER PASS #UPDATED");
        initialEntity.setName("DUMMY USER #UPDATED");

        return initialEntity;
    }

    @Override
    public User getDummyEntityForSearch() {
        return DUMMY_USER_1;
    }

    @Override
    public User getDummyEntityForSave() {
        User dummyUser = new User();
        dummyUser.setSalt("DUMMY USER SALT #FOR_SAVE");
        dummyUser.setPass("DUMMY USER PASS #FOR_SAVE");
        dummyUser.setName("DUMMY USER #FOR_SAVE");

        return dummyUser;
    }

    @Override
    public User getDummyEntityForUpdate() {
        return DUMMY_USER_2;
    }

    @Override
    public User getDummyEntityForDelete() {
        return DUMMY_USER_1;
    }
}
