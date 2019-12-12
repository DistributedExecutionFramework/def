package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.DataType;
import at.enfilo.def.persistence.api.IDataTypeDAO;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.mock.datamaster.DataManager;
import at.enfilo.def.persistence.mock.datamaster.entity.DataTypeDataMaster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Created by mase on 02.09.2016.
 */
public class DataTypeDAOTest extends GenericDAOTest<DataType, String> {

    private IDataTypeDAO dataTypeDAO;
    private IDataMaster<DataType, String> dataMaster;

    @Before
    @Override
    public void prepare() {
        dataTypeDAO = new DataTypeDAO();
        dataMaster = new DataTypeDataMaster();

        DataManager.export(dataMaster);
    }

    @After
    @Override
    public void cleanup() {
        DataManager.drop(dataMaster);
    }

    @Test
    public void findByNameTest() {
        DataType dummyType = getDataMaster().getDummyEntityForSearch();

        String name = dummyType.getName();
        List<DataType> savedType = dataTypeDAO.findByName(name);
        assertNotNull(savedType);
        assertEquals(1, savedType.size());
        assertEquals(dummyType, savedType.get(0));

        name = name.substring(0, name.length() - 2);
        savedType = dataTypeDAO.findByName(name);
        assertNotNull(savedType);
        assertEquals(1, savedType.size());
        assertEquals(dummyType, savedType.get(0));
    }

    @Override
    public IDataMaster<DataType, String> getDataMaster() {
        return dataMaster;
    }

    @Override
    public IGenericDAO<DataType, String> getDAO() {
        return dataTypeDAO;
    }
}
