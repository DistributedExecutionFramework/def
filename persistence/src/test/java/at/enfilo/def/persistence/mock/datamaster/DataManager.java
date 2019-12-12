package at.enfilo.def.persistence.mock.datamaster;

import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.persistence.api.IPersistenceFacade;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.dao.PersistenceFacade;
import at.enfilo.def.persistence.mock.api.IPersistenceMaster;

import java.io.Serializable;

/**
 * Created by mase on 05.09.2016.
 */
public class DataManager {

    private static final IPersistenceFacade PERSISTENCE_FACADE = new PersistenceFacade();

    private DataManager() {
        // Hiding public constructor
    }

    public static <T extends AbstractEntity<U>, U extends Serializable> void export(IPersistenceMaster<T, U> dataMaster) {
        if (dataMaster != null) {
            IGenericDAO<T, U> dao = PERSISTENCE_FACADE.getNewGenericDAO(
                dataMaster.getMasterClass(),
                dataMaster.getMasterIdClass(),
                dataMaster.getIdAttribute()
            );

            dataMaster.getAllDummyEntities().forEach(dao::saveOrUpdate);
        }
    }

    public static <T extends AbstractEntity<U>, U extends Serializable> void drop(IPersistenceMaster<T, U> dataMaster) {
        if (dataMaster != null) {
            IGenericDAO<T, U> dao = PERSISTENCE_FACADE.getNewGenericDAO(
                dataMaster.getMasterClass(),
                dataMaster.getMasterIdClass(),
                dataMaster.getIdAttribute()
            );

            dao.findAll().forEach(dao::delete);
        }
    }
}
