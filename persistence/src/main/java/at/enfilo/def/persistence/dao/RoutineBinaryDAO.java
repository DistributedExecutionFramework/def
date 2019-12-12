package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.RoutineBinary;
import at.enfilo.def.domain.entity.RoutineBinary_;
import at.enfilo.def.persistence.api.IRoutineBinaryDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.util.MapperException;
import org.hibernate.HibernateException;

/**
 * Created by mase on 06.04.2017.
 */
class RoutineBinaryDAO extends GenericDAO<RoutineBinary, String> implements IRoutineBinaryDAO {

	public RoutineBinaryDAO() {
		super(RoutineBinary.class, String.class, RoutineBinary_.id);
	}

	@Override
	public String getUrl(String bId)
	throws PersistenceException {
	    try {
            return findPropertyById(bId, RoutineBinary_.url);
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
	}
}
