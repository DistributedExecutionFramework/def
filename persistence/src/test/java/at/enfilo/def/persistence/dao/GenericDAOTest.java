package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.util.PersistenceException;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by mase on 02.09.2016.
 */
public abstract class GenericDAOTest<T extends AbstractEntity<U>, U extends Serializable> {

    protected GenericDAOTest() {
    }

    public abstract void prepare();

    public abstract void cleanup();

    public abstract IDataMaster<T, U> getDataMaster();

    public abstract IGenericDAO<T, U> getDAO();

    @Test
    public void findAllTest() {

        List<T> entityList = getDAO().findAll();

        assertNotNull(entityList);
        assertEquals(entityList.size(), getDataMaster().getAllDummyEntities().size());

        for (T entity : entityList) {
            U eId = entity.getId();
            T initialEntity = getDataMaster().getDummyEntityById(eId);

            assertEquals(entity, initialEntity);
        }
    }

    @Test
    public void findByIdTest() {

        T dummyEntity = getDataMaster().getDummyEntityForSearch();

        U eId = dummyEntity.getId();
        T entity = getDAO().findById(eId);

        assertNotNull(entity);

        T initialEntity = getDataMaster().getDummyEntityById(eId);
        assertEquals(entity, initialEntity);
    }

    @Test(expected = PersistenceException.class)
    public void findByIdNullTest() {
        T entity = getDAO().findById(null);
        assertNull(entity);
    }

    @Test
    public void saveTest() {

        T initialEntity = getDataMaster().getDummyEntityForSave();

        Serializable rId = getDAO().save(initialEntity);
        U eId = initialEntity.getId();

        assertNotNull(rId);
        assertEquals(eId, rId);

        T savedEntity = getDAO().findById(eId);

        assertNotNull(savedEntity);
        assertEquals(initialEntity, savedEntity);
    }

    @Test(expected = PersistenceException.class)
    public void saveNullTest() {
        Serializable rId = getDAO().save(null);
        assertNull(rId);
    }

    @Test
    public void updateTest() {

        T initialEntity = getDataMaster().getDummyEntityForUpdate();

        T updatedEntity = getDataMaster().updateEntity(initialEntity);
        getDAO().update(updatedEntity);

        U eId = updatedEntity.getId();
        T savedEntity = getDAO().findById(eId);

        assertNotNull(savedEntity);
        assertEquals(updatedEntity, savedEntity);
    }

    @Test(expected = PersistenceException.class)
    public void updateNullTest() {
        getDAO().update(null);
    }

    @Test
    public void saveOrUpdateSaveTest() {

        T initialEntity = getDataMaster().getDummyEntityForSave();
        getDAO().saveOrUpdate(initialEntity);

        U eId = initialEntity.getId();
        T savedEntity = getDAO().findById(eId);

        assertNotNull(savedEntity);
        assertEquals(initialEntity, savedEntity);
    }

    @Test
    public void saveOrUpdateUpdateTest() {

        T initialEntity = getDataMaster().getDummyEntityForUpdate();

        T updatedEntity = getDataMaster().updateEntity(initialEntity);
        getDAO().saveOrUpdate(updatedEntity);

        U eId = updatedEntity.getId();
        T savedEntity = getDAO().findById(eId);

        assertNotNull(savedEntity);
        assertEquals(updatedEntity, savedEntity);
    }

    @Test(expected = PersistenceException.class)
    public void saveOrUpdateNullTest() {
        getDAO().saveOrUpdate(null);
    }

    @Test
    public void deleteTest() {

        T deleteEntity = getDataMaster().getDummyEntityForDelete();
        getDAO().delete(deleteEntity);

        U eId = deleteEntity.getId();
        T savedEntity = getDAO().findById(eId);

        assertNull(savedEntity);
    }

    @Test(expected = PersistenceException.class)
    public void deleteNullTest() {
        getDAO().delete(null);
    }
}
