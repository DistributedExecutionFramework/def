package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.api.ITaskDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.mock.datamaster.DataManager;
import at.enfilo.def.persistence.mock.datamaster.entity.TaskDataMaster;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;


/**
 * Created by mase on 02.09.2016.
 */
public class TaskDAOTest extends GenericDAOTest<Task, String> {

    private ITaskDAO taskDAO;
    private IDataMaster<Task, String> dataMaster;

    @Before
    @Override
    public void prepare() {
        taskDAO = new TaskDAO();
        dataMaster = new TaskDataMaster();

        DataManager.export(dataMaster);
    }

    @After
    @Override
    public void cleanup() {
        DataManager.drop(dataMaster);
    }

    @Test
    public void getTaskInfoTest() {
        Task dummyTask = getDataMaster().getDummyEntityForSearch();

        String tId = dummyTask.getId();
        TaskDTO taskInfo = taskDAO.getTaskInfo(tId);

        assertNotNull(taskInfo);
    }

    @Override
    public IDataMaster<Task, String> getDataMaster() {
        return dataMaster;
    }

    @Override
    public IGenericDAO<Task, String> getDAO() {
        return taskDAO;
    }
}
