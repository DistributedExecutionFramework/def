package at.enfilo.def.persistence.util;

import at.enfilo.def.persistence.api.*;
import at.enfilo.def.persistence.dao.PersistenceFacade;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by mase on 02.09.2016.
 */
public class PersistenceFacadeTest {
    
    private IPersistenceFacade persistenceFacade;

    @Before
    public void prepare() {
        persistenceFacade = new PersistenceFacade();
    }

    @Test
    public void getNewUserDAOTest() {
        IUserDAO dao = persistenceFacade.getNewUserDAO();
        assertNotNull(dao);
    }

    @Test
    public void getNewGroupDAOTest() {
        IGroupDAO dao = persistenceFacade.getNewGroupDAO();
        assertNotNull(dao);
    }

    @Test
    public void getNewProgramDAOTest() {
        IProgramDAO dao = persistenceFacade.getNewProgramDAO();
        assertNotNull(dao);
    }

    @Test
    public void getNewJobDAOTest() {
        IJobDAO dao = persistenceFacade.getNewJobDAO();
        assertNotNull(dao);
    }

    @Test
    public void getNewTaskDAOTest() {
        ITaskDAO dao = persistenceFacade.getNewTaskDAO();
        assertNotNull(dao);
    }
}
