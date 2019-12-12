package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.map.PGMap;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.api.IGroupDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.mock.datamaster.DataManager;
import at.enfilo.def.persistence.mock.datamaster.entity.GroupDataMaster;
import at.enfilo.def.persistence.mock.datamaster.entity.ProgramDataMaster;
import at.enfilo.def.persistence.mock.datamaster.entity.UserDataMaster;
import at.enfilo.def.persistence.mock.datamaster.map.PGMapDataMaster;
import at.enfilo.def.transfer.dto.IdDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by mase on 02.09.2016.
 */
public class GroupDAOTest extends GenericDAOTest<Group, String> {

    private IGroupDAO groupDAO;
    private IDataMaster<Group, String> groupDataMaster;

    @Before
    @Override
    public void prepare() {
        groupDAO = new GroupDAO();
        groupDataMaster = new GroupDataMaster();

        DataManager.export(groupDataMaster);
    }

    @After
    @Override
    public void cleanup() {
        DataManager.drop(groupDataMaster);
    }

    @Test
    public void getAllProgramIdsTest() {

        // Preparing for export
        IDataMaster<User, String> userDataMaster = new UserDataMaster();
        IDataMaster<Program, String> programDataMaster = new ProgramDataMaster(userDataMaster);

        PGMapDataMaster PGMapDataMaster = new PGMapDataMaster(
            programDataMaster,
            getDataMaster()
        );

        // Exporting extra values
        DataManager.export(userDataMaster);
        DataManager.export(programDataMaster);
        DataManager.export(PGMapDataMaster);

        // Testing
        Group dummyGroup = getDataMaster().getDummyEntityForSearch();

        String gId = dummyGroup.getId();
        List<IdDTO> programIdList = groupDAO.getAllProgramIds(gId);

        assertNotNull(programIdList);

        programIdList.forEach(idDTO -> {
            String eId = idDTO.getId();
            assertTrue(programDataMaster.containsDummyEntityId(eId));
        });

        Set<String> programIdSet = programIdList.stream().map(IdDTO::getId).collect(Collectors.toSet());
        Set<PGMap> pgMapSet = PGMapDataMaster.getAllDummyEntities();

        pgMapSet.stream().filter(pgMap -> pgMap.getId().getGroupId().equals(gId)).forEach(pgMap -> {
            String pId = pgMap.getId().getProgramId();
            assertTrue(programIdSet.contains(pId));
        });

        // Dropping extra values
        DataManager.drop(PGMapDataMaster);
        DataManager.drop(programDataMaster);
        DataManager.drop(userDataMaster);
    }

    @Override
    public IDataMaster<Group, String> getDataMaster() {
        return groupDataMaster;
    }

    @Override
    public IGenericDAO<Group, String> getDAO() {
        return groupDAO;
    }
}
