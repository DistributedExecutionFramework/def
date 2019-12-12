package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.map.PJMap;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.api.IProgramDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.mock.datamaster.DataManager;
import at.enfilo.def.persistence.mock.datamaster.entity.JobDataMaster;
import at.enfilo.def.persistence.mock.datamaster.entity.ProgramDataMaster;
import at.enfilo.def.persistence.mock.datamaster.entity.UserDataMaster;
import at.enfilo.def.persistence.mock.datamaster.map.PJMapDataMaster;
import at.enfilo.def.transfer.dto.IdDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;
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
public class ProgramDAOTest extends GenericDAOTest<Program, String> {

    private IProgramDAO programDAO;
    private IDataMaster<User, String> userDataMaster;
    private IDataMaster<Program, String> dataMaster;

    @Before
    @Override
    public void prepare() {
        programDAO = new ProgramDAO();
        userDataMaster = new UserDataMaster();
        dataMaster = new ProgramDataMaster(userDataMaster);

        DataManager.export(userDataMaster);
        DataManager.export(dataMaster);
    }

    @After
    @Override
    public void cleanup() {
        DataManager.drop(dataMaster);
        DataManager.drop(userDataMaster);
    }

    @Test
    public void getProgramInfoTest() {
        Program dummyProgram = getDataMaster().getDummyEntityForSearch();

        String pId = dummyProgram.getId();
        ProgramDTO programInfo = programDAO.getProgramInfo(pId);

        assertNotNull(programInfo);
    }

    @Test
    public void getAllJobIdsTest() {

        // Preparing for export
        IDataMaster<Job, String> jobDataMaster = new JobDataMaster();

        PJMapDataMaster pjMapDataMaster = new PJMapDataMaster(
            getDataMaster(),
            jobDataMaster
        );

        // Exporting extra values
        DataManager.export(jobDataMaster);
        DataManager.export(pjMapDataMaster);

        // Testing
        Program dummyProgram = getDataMaster().getDummyEntityForSearch();

        String pId = dummyProgram.getId();
        List<IdDTO> jobIdList = programDAO.getAllJobIds(pId);

        assertNotNull(jobIdList);

        jobIdList.forEach(idDTO -> {
            String eId = idDTO.getId();
            assertTrue(jobDataMaster.containsDummyEntityId(eId));
        });

        Set<String> jobIdSet = jobIdList.stream().map(IdDTO::getId).collect(Collectors.toSet());
        Set<PJMap> pjMapSet = pjMapDataMaster.getAllDummyEntities();

        pjMapSet.stream().filter(pjMap -> pjMap.getId().getProgramId().equals(pId)).forEach(pjMap -> {
            String jId = pjMap.getId().getJobId();
            assertTrue(jobIdSet.contains(jId));
        });

        // Dropping extra values
        DataManager.drop(pjMapDataMaster);
        DataManager.drop(jobDataMaster);
    }

    @Override
    public IDataMaster<Program, String> getDataMaster() {
        return dataMaster;
    }

    @Override
    public IGenericDAO<Program, String> getDAO() {
        return programDAO;
    }
}
