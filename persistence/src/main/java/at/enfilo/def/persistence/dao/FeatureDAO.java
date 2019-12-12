package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.domain.entity.Feature_;
import at.enfilo.def.persistence.api.IFeatureDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import org.hibernate.HibernateException;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

class FeatureDAO extends GenericDAO<Feature, String> implements IFeatureDAO {

    public FeatureDAO() {
        super(Feature.class, String.class, Feature_.id);
    }

    @Override
    public List<Feature> findAllBaseFeatures()
            throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<Feature> criteriaQuery = criteriaBuilder.createQuery(Feature.class);

                Root<Feature> featureRoot = criteriaQuery.from(Feature.class);

                Predicate whereBaseId = criteriaBuilder.isNull(
                        featureRoot.get(Feature_.baseFeature)
                );

                return criteriaQuery.where(whereBaseId);
            });
        } catch (HibernateException | NoResultException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<Feature> findByNameOrVersion(String searchPattern)
            throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<Feature> criteriaQuery = criteriaBuilder.createQuery(Feature.class);

                Root<Feature> featureRoot = criteriaQuery.from(Feature.class);

                Predicate whereLabel = criteriaBuilder.like(
                        featureRoot.get(Feature_.id),
                        searchPattern
                );

                Predicate whereName = criteriaBuilder.like(
                        featureRoot.get(Feature_.name),
                        searchPattern
                );

                Predicate whereVersion = criteriaBuilder.like(
                        featureRoot.get(Feature_.version),
                        searchPattern
                );

                Predicate nameOrVersion = criteriaBuilder.or(
                        whereLabel,
                        whereName,
                        whereVersion
                );

                return criteriaQuery.where(nameOrVersion);
            });
        } catch (HibernateException | NoResultException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<Feature> findByNameAndVersion(String name, String version) throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<Feature> criteriaQuery = criteriaBuilder.createQuery(Feature.class);

                Root<Feature> featureRoot = criteriaQuery.from(Feature.class);

                Predicate whereName = criteriaBuilder.like(
                      featureRoot.get(Feature_.name),
                      name
                );

                Predicate whereVersion = criteriaBuilder.like(
                    featureRoot.get(Feature_.version),
                    version
                );

                Predicate nameOrVersion = criteriaBuilder.and(
                        whereName,
                        whereVersion
                );

                return criteriaQuery.where(nameOrVersion);
            });
        } catch (HibernateException | NoResultException e) {
            throw new PersistenceException(e);
        }
    }
}