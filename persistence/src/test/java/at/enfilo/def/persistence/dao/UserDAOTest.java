package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.map.UGMap;
import at.enfilo.def.persistence.api.IUserDAO;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.mock.datamaster.DataManager;
import at.enfilo.def.persistence.mock.datamaster.entity.GroupDataMaster;
import at.enfilo.def.persistence.mock.datamaster.entity.UserDataMaster;
import at.enfilo.def.persistence.mock.datamaster.map.UGMapDataMaster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


/**
 * Created by mase on 02.09.2016.
 */
public class UserDAOTest extends GenericDAOTest<User, String> {

    private IUserDAO userDAO;
    private IDataMaster<User, String> dataMaster;

    @Before
    @Override
    public void prepare() {
        userDAO = new UserDAO();
        dataMaster = new UserDataMaster();

        DataManager.export(dataMaster);
    }

    @After
    @Override
    public void cleanup() {
        DataManager.drop(dataMaster);
    }

    @Test
    public void getUserByNameTest() {
        User dummyUser = getDataMaster().getDummyEntityForSearch();

        String name = dummyUser.getName();
        User savedUser = userDAO.getUserByName(name);

        assertNotNull(savedUser);
        assertEquals(dummyUser, savedUser);
    }

    @Test
    public void getAllGroupsTest() {

        // Preparing for export
        IDataMaster<Group, String> groupDataMaster = new GroupDataMaster();

        UGMapDataMaster ugMapDataMaster = new UGMapDataMaster(
            getDataMaster(),
            groupDataMaster
        );

        // Exporting extra values
        DataManager.export(groupDataMaster);
        DataManager.export(ugMapDataMaster);

        // Testing
        User dummyUser = getDataMaster().getDummyEntityForSearch();

        String uId = dummyUser.getId();
        List<Group> userGroupList = userDAO.getAllGroups(uId);

        assertNotNull(userGroupList);

        userGroupList.forEach(group ->  {
            String eId = group.getId();
            assertTrue(groupDataMaster.containsDummyEntityId(eId));
        });

        Set<String> groupIdSet = userGroupList.stream().map(Group::getId).collect(Collectors.toSet());
        Set<UGMap> ugMapSet = ugMapDataMaster.getAllDummyEntities();

        ugMapSet.stream().filter(ugMap -> ugMap.getId().getUserId().equals(uId)).forEach(ugMap -> {
            String gId = ugMap.getId().getGroupId();
            assertTrue(groupIdSet.contains(gId));
        });

        // Dropping extra values
        DataManager.drop(ugMapDataMaster);
        DataManager.drop(groupDataMaster);
    }

    @Override
    public IDataMaster<User, String> getDataMaster() {
        return dataMaster;
    }

    @Override
    public IGenericDAO<User, String> getDAO() {
        return userDAO;
    }
}
