package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.RoutineBinary;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;

/**
 * Created by mase on 06.04.2017.
 */
public interface IRoutineBinaryDAO extends IGenericDAO<RoutineBinary, String> {

    /**
     * Returns url associated with this BinaryRoutine.
     *
     * @param bId id of the binary routine of which url will be returned.
     * @return url of the given routine binary.
     */
    String getUrl(String bId)
    throws PersistenceException;
}
