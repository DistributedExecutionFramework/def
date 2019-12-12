package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.map.PGMap;
import at.enfilo.def.domain.map.UGMap;
import at.enfilo.def.domain.map.pk.UGMapPK;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.api.IUserDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.mock.datamaster.DataManager;
import at.enfilo.def.persistence.mock.datamaster.entity.GroupDataMaster;
import at.enfilo.def.persistence.mock.datamaster.entity.ProgramDataMaster;
import at.enfilo.def.persistence.mock.datamaster.entity.UserDataMaster;
import at.enfilo.def.persistence.mock.datamaster.map.PGMapDataMaster;
import at.enfilo.def.persistence.mock.datamaster.map.UGMapDataMaster;
import at.enfilo.def.transfer.dto.IdDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void getAllOwnedProgramsTest() {

        // Preparing for export
        IDataMaster<Group, String> groupDataMaster = new GroupDataMaster();
        IDataMaster<Program, String> programDataMaster = new ProgramDataMaster(getDataMaster());

        PGMapDataMaster pgMapDataMaster = new PGMapDataMaster(
            programDataMaster,
            groupDataMaster
        );

        // Exporting extra values
        DataManager.export(groupDataMaster);
        DataManager.export(programDataMaster);
        DataManager.export(pgMapDataMaster);

        // Testing
        User dummyUser = getDataMaster().getDummyEntityForSearch();

        String uId = dummyUser.getId();
        List<Program> ownedProgramList = userDAO.getAllOwnedPrograms(uId);

        assertNotNull(ownedProgramList);

        ownedProgramList.forEach(program -> {
            String eId = program.getId();
            assertTrue(programDataMaster.containsDummyEntityId(eId));
        });

        Set<String> programIdSet = ownedProgramList.stream().map(Program::getId).collect(Collectors.toSet());
        Set<PGMap> pgMapSet = pgMapDataMaster.getAllDummyEntities();

        Map<String, String> programOwnersMap = new HashMap<>();
        programDataMaster.getAllDummyEntities().forEach(p -> programOwnersMap.put(p.getId(), p.getOwner().getId()));

        pgMapSet.stream().filter(pgMap -> uId.equals(programOwnersMap.get(pgMap.getId().getProgramId()))).forEach(pgMap -> {
            String pId = pgMap.getId().getProgramId();
            assertTrue(programIdSet.contains(pId));
        });

        // Dropping extra values
        DataManager.drop(pgMapDataMaster);
        DataManager.drop(programDataMaster);
        DataManager.drop(groupDataMaster);
    }

    @Test
    public void getAllGroupProgramsTest() {

        // Preparing for export
        IDataMaster<Group, String> groupDataMaster = new GroupDataMaster();

        UGMapDataMaster ugMapDataMaster = new UGMapDataMaster(
            getDataMaster(),
            groupDataMaster
        );

        IDataMaster<Program, String> programDataMaster = new ProgramDataMaster(getDataMaster());

        PGMapDataMaster pgMapDataMaster = new PGMapDataMaster(
            programDataMaster,
            groupDataMaster
        );

        // Exporting extra values
        DataManager.export(groupDataMaster);
        DataManager.export(ugMapDataMaster);
        DataManager.export(programDataMaster);
        DataManager.export(pgMapDataMaster);

        // Testing
        User dummyUser = getDataMaster().getDummyEntityForSearch();

        String uId = dummyUser.getId();
        List<Program> groupProgramList = userDAO.getAllGroupPrograms(uId);

        assertNotNull(groupProgramList);

        groupProgramList.forEach(program -> {
            String eId = program.getId();
            assertTrue(programDataMaster.containsDummyEntityId(eId));
        });

        Set<String> programIdSet = groupProgramList.stream().map(Program::getId).collect(Collectors.toSet());

        Set<String> userGroupIdSet = ugMapDataMaster.getAllDummyEntities().stream().filter(
            ugMap -> ugMap.getId().getUserId().equals(uId)
        ).map(UGMap::getId).map(UGMapPK::getGroupId).collect(Collectors.toSet());

        Set<String> mappedIdSet = pgMapDataMaster.getAllDummyEntities().stream().filter(
            pgMap -> userGroupIdSet.contains(pgMap.getId().getGroupId())
        ).map(pogMap -> pogMap.getId().getProgramId()).collect(Collectors.toSet());

        assertEquals(programIdSet, mappedIdSet);

        // Dropping extra values
        DataManager.drop(pgMapDataMaster);
        DataManager.drop(programDataMaster);
        DataManager.drop(ugMapDataMaster);
        DataManager.drop(groupDataMaster);
    }

    @Test
    public void getAllProgramsTest() {

        // Preparing for export
        IDataMaster<Group, String> groupDataMaster = new GroupDataMaster();

        UGMapDataMaster ugMapDataMaster = new UGMapDataMaster(
            getDataMaster(),
            groupDataMaster
        );

        IDataMaster<Program, String> programDataMaster = new ProgramDataMaster(getDataMaster());

        PGMapDataMaster pgMapDataMaster = new PGMapDataMaster(
            programDataMaster,
            groupDataMaster
        );

        // Exporting extra values
        DataManager.export(groupDataMaster);
        DataManager.export(programDataMaster);
        DataManager.export(ugMapDataMaster);
        DataManager.export(pgMapDataMaster);

        // Testing
        User dummyUser = getDataMaster().getDummyEntityForSearch();

        String uId = dummyUser.getId();
        List<Program> programList = userDAO.getAllPrograms(uId);

        assertNotNull(programList);

        programList.forEach(program -> {
            String eId = program.getId();
            assertTrue(programDataMaster.containsDummyEntityId(eId));
        });

        Set<String> programIdSet = programList.stream().map(Program::getId).collect(Collectors.toSet());

        Set<String> userGroupIdSet = ugMapDataMaster.getAllDummyEntities().stream().filter(
            ugMap -> ugMap.getId().getUserId().equals(uId)
        ).map(UGMap::getId).map(UGMapPK::getGroupId).collect(Collectors.toSet());

        Map<String, String> programOwnersMap = new HashMap<>();
        programDataMaster.getAllDummyEntities().forEach(p -> programOwnersMap.put(p.getId(), p.getOwner().getId()));

        Set<String> mappedIdSet = pgMapDataMaster.getAllDummyEntities().stream().filter(
            pgMap -> uId.equals(programOwnersMap.get(pgMap.getId().getProgramId())) || userGroupIdSet.contains(pgMap.getId().getGroupId())
        ).map(pgMap -> pgMap.getId().getProgramId()).collect(Collectors.toSet());

        assertEquals(programIdSet, mappedIdSet);

        // Dropping extra values
        DataManager.drop(pgMapDataMaster);
        DataManager.drop(ugMapDataMaster);
        DataManager.drop(programDataMaster);
        DataManager.drop(groupDataMaster);
    }

    @Test
    public void getAllProgramIdsTest() {

        // Preparing for export
        IDataMaster<Group, String> groupDataMaster = new GroupDataMaster();

        UGMapDataMaster ugMapDataMaster = new UGMapDataMaster(
            getDataMaster(),
            groupDataMaster
        );

        IDataMaster<Program, String> programDataMaster = new ProgramDataMaster(getDataMaster());

        PGMapDataMaster pgMapDataMaster = new PGMapDataMaster(
            programDataMaster,
            groupDataMaster
        );

        // Exporting extra values
        DataManager.export(groupDataMaster);
        DataManager.export(programDataMaster);
        DataManager.export(ugMapDataMaster);
        DataManager.export(pgMapDataMaster);

        // Testing
        User dummyUser = getDataMaster().getDummyEntityForSearch();

        String uId = dummyUser.getId();
        List<IdDTO> programIdList = userDAO.getAllProgramIds(uId);

        assertNotNull(programIdList);

        programIdList.forEach(idDTO -> {
            String eId = idDTO.getId();
            assertTrue(programDataMaster.containsDummyEntityId(eId));
        });

        Set<String> programIdSet = programIdList.stream().map(IdDTO::getId).collect(Collectors.toSet());

        Set<String> userGroupIdSet = ugMapDataMaster.getAllDummyEntities().stream().filter(
            ugMap -> ugMap.getId().getUserId().equals(uId)
        ).map(UGMap::getId).map(UGMapPK::getGroupId).collect(Collectors.toSet());

        Map<String, String> programOwnersMap = new HashMap<>();
        programDataMaster.getAllDummyEntities().forEach(p -> programOwnersMap.put(p.getId(), p.getOwner().getId()));

        Set<String> mappedIdSet = pgMapDataMaster.getAllDummyEntities().stream().filter(
            pgMap -> uId.equals(programOwnersMap.get(pgMap.getId().getProgramId())) || userGroupIdSet.contains(pgMap.getId().getGroupId())
        ).map(pgMap -> pgMap.getId().getProgramId()).collect(Collectors.toSet());

        assertEquals(programIdSet, mappedIdSet);

        // Dropping extra values
        DataManager.drop(pgMapDataMaster);
        DataManager.drop(ugMapDataMaster);
        DataManager.drop(programDataMaster);
        DataManager.drop(groupDataMaster);
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
