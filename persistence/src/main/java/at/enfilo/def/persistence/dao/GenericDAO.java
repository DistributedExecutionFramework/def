package at.enfilo.def.persistence.dao;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.Tuple;
import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.ConnectionProvider;
import at.enfilo.def.persistence.util.PersistenceException;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.query.NativeQuery;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by mase on 18.08.2016.
 */
class GenericDAO<T extends AbstractEntity<U>, U extends Serializable> implements IGenericDAO<T, U> {

    private static final String BATCH_ID_SET_PARAM = "idSet";
    private static final String BATCH_DELETE_HQL_FORMAT = "delete from %s entity where entity.id in :" + BATCH_ID_SET_PARAM;

    private static final int DEFAULT_BATCH_SIZE = 5;

    private final String batchDeleteHQL;
    private final Class<T> domainClass;
    private final Class<U> idClass;
    private final SingularAttribute<T, U> idAttribute;

    public GenericDAO(Class<T> domainClass, Class<U> idClass, SingularAttribute<T, U> idAttribute)
    throws IllegalArgumentException {
        if (domainClass == null) throw new IllegalArgumentException();

        String classAlias = null;
        if (domainClass.isAnnotationPresent(Entity.class)) {
            classAlias = domainClass.getAnnotation(Entity.class).name();
        } else if (domainClass.isAnnotationPresent(Table.class)) {
            classAlias = domainClass.getAnnotation(Table.class).name();
        }

        if (classAlias == null || classAlias.isEmpty()) {
            classAlias = domainClass.getSimpleName();
        }

        this.batchDeleteHQL = String.format(
            BATCH_DELETE_HQL_FORMAT,
            classAlias
        );

        this.domainClass = domainClass;
        this.idClass = idClass;
        this.idAttribute = idAttribute;
    }

    @Override
    public U getProxyId(T proxy)
    throws PersistenceException {

        try {
            if (proxy != null && proxy instanceof HibernateProxy) {

                LazyInitializer lazyInitializer = ((HibernateProxy) proxy).getHibernateLazyInitializer();
                if (lazyInitializer.isUninitialized()) {
                    return idClass.cast(lazyInitializer.getIdentifier());
                }
            }
            return null;
        } catch (ClassCastException | HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public T getProxy(U eId)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session ->
                (T) session.load(domainClass, eId)
            );
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public T refreshToActualState(T entity)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                session.refresh(entity);
                return entity;
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }


    @Override
    public <R> R forceLoadLazyField(T entity, Function<T, R> proxyFunction)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                session.update(entity);

                R fieldProxy = proxyFunction.apply(entity);
                Hibernate.initialize(fieldProxy);

                return fieldProxy;
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public U save(T entity)
    throws PersistenceException {

        try {
            Serializable id = ConnectionProvider.makeSimpleTransaction(session -> {
                return session.save(entity);
            });

            return idClass.cast(id);
        } catch (ClassCastException | HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void update(T entity)
    throws PersistenceException {

        try {
            ConnectionProvider.makeSimpleTransaction(session -> {
                session.update(entity);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void saveOrUpdate(T entity)
    throws PersistenceException {

        try {
            ConnectionProvider.makeSimpleTransaction(session -> {
                session.saveOrUpdate(entity);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void batchSaveOrUpdate(T[] entities)
    throws PersistenceException {
        batchSaveOrUpdate(Arrays.asList(entities));
    }

    @Override
    public void batchSaveOrUpdate(Collection<T> entities)
    throws PersistenceException {

        try {
            ConnectionProvider.makeSimpleTransaction(session -> {
                Integer sessionBatchSize = session.getJdbcBatchSize();
                int currentBatchSize = sessionBatchSize != null ? sessionBatchSize : DEFAULT_BATCH_SIZE;

                int entityCounter = 0;

                for (T entity : entities) {

                    if (session.contains(entity)) session.evict(entity);

                    session.persist(entity);

                    // Flush a batch and release memory.
                    if (entityCounter % currentBatchSize == 0) {
                        session.flush();
                        session.clear();
                    }
                    ++entityCounter;
                }
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void delete(T entity)
    throws PersistenceException {

        try {
            ConnectionProvider.makeSimpleTransaction(session -> {
                session.delete(entity);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void batchDelete(T[] entities)
    throws PersistenceException {
        batchDelete(Arrays.asList(entities));
    }

    @Override
    public void batchDelete(Collection<T> entities)
    throws PersistenceException {
        Set<U> idSet = entities.stream().map(T::getId).collect(Collectors.toSet());
        batchDeleteById(idSet);
    }

    @Override
    public void deleteById(U eId)
    throws PersistenceException {

        try {
            ConnectionProvider.makeSimpleTransaction(session -> {
                session.delete(getProxy(eId));
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void batchDeleteById(U[] ids)
    throws PersistenceException {
        batchDeleteById(Arrays.asList(ids));
    }

    @Override
    public void batchDeleteById(Collection<U> ids)
    throws PersistenceException {

        try {
            ConnectionProvider.makeSimpleTransaction(session -> {
                session.createQuery(batchDeleteHQL).setParameterList(
                    BATCH_ID_SET_PARAM,
                    ids
                ).executeUpdate();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<T> findAll()
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<T> criteriaQuery = builder.createQuery(domainClass);
                criteriaQuery.from(domainClass);
                criteriaQuery.distinct(true);
                
                return session.createQuery(criteriaQuery).getResultList();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<U> findAllIds()
    throws PersistenceException {
        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<U> criteriaQuery = builder.createQuery(idClass);

                Root<T> entityRoot = criteriaQuery.from(domainClass);

                criteriaQuery.select(builder.construct(
                    idClass,
                    entityRoot.get(idAttribute)
                ));
                criteriaQuery.distinct(true);

                return session.createQuery(criteriaQuery).getResultList();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <R> R findPropertyById(U eId, SingularAttribute<T, R> propertyAttribute)
    throws PersistenceException {
        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<R> criteriaQuery = builder.createQuery(propertyAttribute.getJavaType());

                Root<T> entityRoot = criteriaQuery.from(domainClass);

                criteriaQuery.select(builder.construct(
                    propertyAttribute.getJavaType(),
                    entityRoot.get(propertyAttribute)
                ));
                return session.createQuery(criteriaQuery).getSingleResult();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <R> List<ITuple<U, R>> findAllProperties(SingularAttribute<T, R> propertyAttribute)
    throws PersistenceException {
        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<javax.persistence.Tuple> criteriaQuery = builder.createQuery(javax.persistence.Tuple.class);

                Root<T> entityRoot = criteriaQuery.from(domainClass);

                criteriaQuery.select(builder.tuple(
                    entityRoot.get(idAttribute),
                    entityRoot.get(propertyAttribute)
                ));
                criteriaQuery.distinct(true);

                List<javax.persistence.Tuple> hibernateTupleList = session.createQuery(criteriaQuery).getResultList();
                return hibernateTupleList.stream().map(t -> {
                    U id = t.get(0, idClass);
                    R property = t.get(1, propertyAttribute.getJavaType());

                    return new Tuple<>(id, property);
                }).collect(Collectors.toList());
            });
        } catch (HibernateException | IllegalArgumentException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public T findById(U eId)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session ->
                    (T) session.get(domainClass, eId)
            );
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <R> R findSingleResultByCriteria(Function<CriteriaBuilder, CriteriaQuery<R>> queryBuilderFunction)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<R> criteriaQuery = queryBuilderFunction.apply(builder);

                return session.createQuery(criteriaQuery).getSingleResult();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <R> List<R> findByCriteria(Function<CriteriaBuilder, CriteriaQuery<R>> queryBuilderFunction)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<R> criteriaQuery = queryBuilderFunction.apply(builder);

                return session.createQuery(criteriaQuery).getResultList();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @SafeVarargs
    @Override
    final public T findSingleResultBySQLQuery(String sql, ITuple<String, ?>... propertyContainers)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                NativeQuery<T> query = session.createNativeQuery(sql, domainClass);
                for (ITuple<String, ?> pair : propertyContainers) {
                    query.setParameter(pair.getKey(), pair.getValue());
                }

                return query.getSingleResult();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @SafeVarargs
    @Override
    final public List<T> findBySQLQuery(String sql, ITuple<String, ?>... propertyContainers)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                NativeQuery<T> query = session.createNativeQuery(sql, domainClass);
                for (ITuple<String, ?> pair : propertyContainers) {
                    query.setParameter(pair.getKey(), pair.getValue());
                }

                return query.getResultList();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @SafeVarargs
    @Override
    final public T findSingleResultByHQL(String hql, ITuple<String, ?>... propertyContainers)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                TypedQuery<T> query = session.createQuery(hql, domainClass);
                for (ITuple<String, ?> pair : propertyContainers) {
                    query.setParameter(pair.getKey(), pair.getValue());
                }

                return query.getSingleResult();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @SafeVarargs
    @Override
    final public List<T> findByHQL(String hql, ITuple<String, ?>... propertyContainers)
    throws PersistenceException {

        try {
            return ConnectionProvider.makeSimpleTransaction(session -> {
                TypedQuery<T> query = session.createQuery(hql, domainClass);
                for (ITuple<String, ?> pair : propertyContainers) {
                    query.setParameter(pair.getKey(), pair.getValue());
                }

                return query.getResultList();
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }
}
