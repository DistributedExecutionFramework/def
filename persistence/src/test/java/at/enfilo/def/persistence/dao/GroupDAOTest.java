package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.persistence.api.IGroupDAO;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.mock.datamaster.DataManager;
import at.enfilo.def.persistence.mock.datamaster.entity.GroupDataMaster;
import org.junit.After;
import org.junit.Before;


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

    @Override
    public IDataMaster<Group, String> getDataMaster() {
        return groupDataMaster;
    }

    @Override
    public IGenericDAO<Group, String> getDAO() {
        return groupDAO;
    }
}
