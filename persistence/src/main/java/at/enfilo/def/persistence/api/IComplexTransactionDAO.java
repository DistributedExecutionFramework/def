package at.enfilo.def.persistence.api;

import at.enfilo.def.persistence.util.PersistenceException;

import java.io.Serializable;

/**
 * Created by seregkaluv on 22.06.2017.
 */
public interface IComplexTransactionDAO {

    void commit()
    throws PersistenceException;

    <T> Serializable save(T entity)
    throws PersistenceException;

    <T> void update(T entity)
    throws PersistenceException;

    <T> void delete(T entity)
    throws PersistenceException;

    <T, U extends Serializable> void deleteById(U eId, Class<T> entityClass)
    throws PersistenceException;

    void rollback()
    throws PersistenceException;
}
