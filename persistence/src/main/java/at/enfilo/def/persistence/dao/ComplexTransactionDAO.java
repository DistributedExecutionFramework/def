package at.enfilo.def.persistence.dao;

import at.enfilo.def.persistence.api.IComplexTransactionDAO;
import at.enfilo.def.persistence.util.ConnectionProvider;
import at.enfilo.def.persistence.util.PersistenceException;
import org.hibernate.Session;

import java.io.Serializable;

/**
 * Created by mase on 22.06.2017.
 */
class ComplexTransactionDAO implements IComplexTransactionDAO {

    private Session session;

    private Session getSession()
    throws PersistenceException {
        // This has to be implemented as lazy initializer.
        // Otherwise exception may thrown at unexpected way.
        // e.g. In case when this logic was implemented in public constructor.
        if (this.session == null) {
            this.session = ConnectionProvider.getDetachedSession();
        }
        return this.session;
    }

    @Override
    public void commit()
    throws PersistenceException {
        try {
            getSession().getTransaction().commit();
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> Serializable save(T entity)
    throws PersistenceException {
        try {
            return getSession().save(entity);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> void update(T entity)
    throws PersistenceException {
        try {
            getSession().update(entity);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T> void delete(T entity)
    throws PersistenceException {
        try {
            getSession().delete(entity);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <T, U extends Serializable> void deleteById(U eId, Class<T> entityClass)
    throws PersistenceException {
        try {
            T proxy = getSession().load(entityClass, eId);
            getSession().delete(proxy);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void rollback()
    throws PersistenceException {
        try {
            getSession().getTransaction().rollback();
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }
}
