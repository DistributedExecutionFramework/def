package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.persistence.api.IJobDAO;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.mock.datamaster.DataManager;
import at.enfilo.def.persistence.mock.datamaster.entity.JobDataMaster;
import at.enfilo.def.transfer.dto.JobDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;


/**
 * Created by mase on 02.09.2016.
 */
public class JobDAOTest extends GenericDAOTest<Job, String> {

    private IJobDAO jobDAO;
    private IDataMaster<Job, String> dataMaster;

    @Before
    @Override
    public void prepare() {
        jobDAO = new JobDAO();
        dataMaster = new JobDataMaster();

        DataManager.export(dataMaster);
    }

    @After
    @Override
    public void cleanup() {
        DataManager.drop(dataMaster);
    }

    @Test
    public void getJobInfoTest() {
        Job dummyJob = getDataMaster().getDummyEntityForSearch();

        String jId = dummyJob.getId();
        JobDTO jobInfo = jobDAO.getJobInfo(jId);

        assertNotNull(jobInfo);
    }

    /*
    @Test
    public void getAllTaskIdsTest() {

        // Preparing for export
        IDataMaster<Task, String> taskDataMaster = new TaskDataMaster();

        JTMapDataMaster jtMapDataMaster = new JTMapDataMaster(
            getDataMaster(),
            taskDataMaster
        );

        // Exporting extra values
        DataManager.export(taskDataMaster);
        DataManager.export(jtMapDataMaster);

        // Testing
        Job dummyJob = getDataMaster().getDummyEntityForSearch();

        String jId = dummyJob.getId();
        List<IdDTO> taskIdList = jobDAO.getAllTaskIds(jId);

        assertNotNull(taskIdList);

        taskIdList.forEach(idDTO ->  {
            String eId = idDTO.getId();
            assertTrue(taskDataMaster.containsDummyEntityId(eId));
        });

        Set<String> taskIdSet = taskIdList.stream().map(IdDTO::getId).collect(Collectors.toSet());
        Set<JTMap> jtMapSet = jtMapDataMaster.getAllDummyEntities();

        jtMapSet.stream().filter(jtMap -> jtMap.getId().getJobId().equals(jId)).forEach(jtMap -> {
            String tId = jtMap.getId().getTaskId();
            assertTrue(taskIdSet.contains(tId));
        });

        // Dropping extra values
        DataManager.drop(jtMapDataMaster);
        DataManager.drop(taskDataMaster);
    }
    */

    @Override
    public IDataMaster<Job, String> getDataMaster() {
        return dataMaster;
    }

    @Override
    public IGenericDAO<Job, String> getDAO() {
        return jobDAO;
    }
}
