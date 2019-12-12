package at.enfilo.def.persistence.api.core;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.persistence.util.PersistenceException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * Created by mase on 18.08.2016.
 */
public interface ISearchDAO<T extends AbstractEntity<U>, U extends Serializable> {

    /**
     * Returns all existing objects of the given type in the data store.
     *
     * @return a List of all objects of the type {@code T}.
     * @throws PersistenceException
     */
    List<T> findAll()
    throws PersistenceException;

    /**
     * Returns references in form of Id to all existing objects of the given type {@code T} in the data store.
     *
     * @return a List of all objects of the type {@code U}.
     * @throws PersistenceException
     */
    List<U> findAllIds()
    throws PersistenceException;

    /**
     * Returns a specific object property specified by {@code SingularAttribute<T, R>} identified
     * by its id from the data store.
     *
     * @param eId an object representing the id of the specific at.hibernate.domain.entity.
     * @param propertyAttribute attributes that specifies the property to be selected.
     * @return the specific object property of the type {@code R} for the given id.
     * @throws PersistenceException
     */
    <R> R findPropertyById(U eId, SingularAttribute<T, R> propertyAttribute)
    throws PersistenceException;

    /**
     * Returns a specific object property list specified by {@code SingularAttribute<T, R>} of
     * all existing objects of the given type from the data store in form of a tuple {@code ITuple<U, R>}.
     *
     * @param propertyAttribute attributes that specifies the property to be selected.
     * @return the specific object property list of the type {@code List<R>}.
     * @throws PersistenceException
     */
    <R> List<ITuple<U, R>> findAllProperties(SingularAttribute<T, R> propertyAttribute)
    throws PersistenceException;

    /**
     * Returns a specific object identified by its id from the data store.
     *
     * @param eId an object representing the id of the specific at.hibernate.domain.entity.
     * @return the at.hibernate.domain.entity with the matching id.
     * @throws PersistenceException
     */
    T findById(U eId)
    throws PersistenceException;

    /**
     * Returns a specific object from the data store, according to the specified query.
     *
     * @param queryBuilderFunction proxy function that builds query.
     * @return object of the type {@code R} that match given predicates, or null if request returned
     * no or more than one result.
     * @throws PersistenceException
     */
    <R> R findSingleResultByCriteria(Function<CriteriaBuilder, CriteriaQuery<R>> queryBuilderFunction)
    throws PersistenceException;

    /**
     * Returns all existing objects of the given type in the data store that
     * match the given query.
     *
     * @param queryBuilderFunction proxy function that builds query.
     * @return a list of all objects of the type {@code R} that match given predicates.
     * @throws PersistenceException
     */
    <R> List<R> findByCriteria(Function<CriteriaBuilder, CriteriaQuery<R>> queryBuilderFunction)
    throws PersistenceException;

    /**
     * Returns a specific object from the data store, according to the specified sql query.
     *
     * @param sql the sql query with named parameters.
     * @param propertyContainers for sql query.
     * @return object of the type {@code T} that match given predicates, or null if request returned
     * no or more than one result.
     * @throws PersistenceException
     */
    T findSingleResultBySQLQuery(String sql, ITuple<String, ?>... propertyContainers)
    throws PersistenceException;

    /**
     * Returns a list of objects that are matched by a given sql query.
     *
     * @param sql the sql query with named parameters.
     * @param propertyContainers for sql query.
     * @return the found objects.
     * @throws PersistenceException
     */
    List<T> findBySQLQuery(String sql, ITuple<String, ?>... propertyContainers)
    throws PersistenceException;

    /**
     * Returns a specific object from the data store, according to the specified hql query.
     *
     * @param hql the hql query with named parameters.
     * @param propertyContainers for hql query.
     * @return object of the type {@code T} that match given predicates, or null if request returned
     * no or more than one result.
     * @throws PersistenceException
     */
    T findSingleResultByHQL(String hql, ITuple<String, ?>... propertyContainers)
    throws PersistenceException;

    /**
     * Returns a list of objects that are matched by a given hql query.
     *
     * @param hql the hql query with named parameters.
     * @param propertyContainers for hql query.
     * @return object of the type {@code T} that match given predicates, or null if request returned
     * no or more than one result.
     * @throws PersistenceException
     */
    List<T> findByHQL(String hql, ITuple<String, ?>... propertyContainers)
    throws PersistenceException;
}
