package at.enfilo.def.persistence.api.core;

import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.persistence.util.PersistenceException;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;

/**
 * Created by mase on 18.08.2016.
 */
public interface IBasicDAO<T extends AbstractEntity<U>, U extends Serializable> {

    /**
     * Returns id of the entity-proxy object without initializing it.
     *
     * @param proxy entity-proxy of type {@code T} object id of which will be returned.
     * @return {@code Serializable} id of the proxy object.
     * @throws PersistenceException
     */
    U getProxyId(T proxy)
    throws PersistenceException;

    /**
     * Returns object proxy of the given type.
     *
     * @return proxy object of the type {@code T} for given id.
     * @throws PersistenceException
     */
    T getProxy(U eId)
    throws PersistenceException;

    /**
     * Refreshes the given object to the actual state from the data store.
     *
     * All dirty (unsaved) changes may be lost.
     * @param entity the object to be refreshed.
     * @return instance of refreshed at.hibernate.domain.entity.
     * @throws PersistenceException
     */
    T refreshToActualState(T entity)
    throws PersistenceException;

    /**
     * Initializes lazy-loaded properties and collections.
     *
     * @param entity instance of the target entity of type {@code T}.
     * @param proxyFunction getterMethod for lazy property.
     * @throws PersistenceException
     */
    <R> R forceLoadLazyField(T entity, Function<T, R> proxyFunction)
    throws PersistenceException;

    /**
     * Saves the given object to the data store.
     *
     * @param entity the object to be saved.
     * @return Id of the newly saved at.hibernate.domain.entity.
     * @throws PersistenceException
     */
    U save(T entity)
    throws PersistenceException;

    /**
     * Persists updated (changed) values of the given object into the data store.
     *
     * @param entity the object to be updated.
     * @throws PersistenceException
     */
    void update(T entity)
    throws PersistenceException;

    /**
     * Saves the given object to the data store or updates the entry, if it
     * already exists.
     *
     * @param entity the object to be saved or updated.
     * @throws PersistenceException
     */
    void saveOrUpdate(T entity)
    throws PersistenceException;

    /**
     * Saves or updates a group of entities.
     *
     * @param entities entities to be saved or updated.
     * @throws PersistenceException
     */
    void batchSaveOrUpdate(T[] entities)
    throws PersistenceException;

    /**
     * Saves or updates a group of entities.
     *
     * @param entities entities to be saved or updated.
     * @throws PersistenceException
     */
    void batchSaveOrUpdate(Collection<T> entities)
    throws PersistenceException;

    /**
     * Deletes an existing object from the data store.
     *
     * @param entity the object to be deleted from the data store.
     * @throws PersistenceException
     */
    void delete(T entity)
    throws PersistenceException;

    /**
     * Deletes a group of entities.
     *
     * @param entities entities to be removed.
     * @throws PersistenceException
     */
    void batchDelete(T[] entities)
    throws PersistenceException;

    /**
     * Deletes a group of entities.
     *
     * @param entities entities to be removed.
     * @throws PersistenceException
     */
    void batchDelete(Collection<T> entities)
    throws PersistenceException;

    /**
     * Deletes an existing object by its id from the data store.
     *
     * @param eId an object representing the id of the specific at.hibernate.domain.entity.
     * @throws PersistenceException
     */
    void deleteById(U eId)
    throws PersistenceException;

    /**
     * Deletes a group of entities by their Ids.
     *
     * @param ids of the entities to be removed.
     * @throws PersistenceException
     */
    void batchDeleteById(U[] ids)
    throws PersistenceException;

    /**
     * Deletes a group of entities by their Ids.
     *
     * @param ids of the entities to be removed.
     * @throws PersistenceException
     */
    void batchDeleteById(Collection<U> ids)
    throws PersistenceException;
}
