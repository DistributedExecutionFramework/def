package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.entity.Routine_;
import at.enfilo.def.persistence.api.IRoutineDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import org.hibernate.HibernateException;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by mase on 06.04.2017.
 */
class RoutineDAO extends GenericDAO<Routine, String> implements IRoutineDAO {

	public RoutineDAO() {
		super(Routine.class, String.class, Routine_.id);
	}

	@Override
	public List<String> findIdsByNameOrDescription(String searchPattern)
	throws PersistenceException {

		try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);

                Root<Routine> routineRoot = criteriaQuery.from(Routine.class);

                Predicate whereName = criteriaBuilder.like(
                    routineRoot.get(Routine_.name),
                    searchPattern
                );

                Predicate whereDescription = criteriaBuilder.like(
                    routineRoot.get(Routine_.description),
                    searchPattern
                );

                Predicate nameOrDescription = criteriaBuilder.or(
                    whereName,
                    whereDescription
                );

                criteriaQuery.select(criteriaBuilder.construct(String.class, routineRoot.get(Routine_.id)));
                return criteriaQuery.where(nameOrDescription).distinct(true);
            });
        } catch (HibernateException | NoResultException e) {
            throw new PersistenceException(e);
        }
	}

	@Override
	public List<Routine> findByNameOrDescription(String searchPattern)
	throws PersistenceException {

		try {
			return findByCriteria(criteriaBuilder -> {

				CriteriaQuery<Routine> criteriaQuery = criteriaBuilder.createQuery(Routine.class);

				Root<Routine> routineRoot = criteriaQuery.from(Routine.class);

				Predicate whereName = criteriaBuilder.like(
					routineRoot.get(Routine_.name),
					searchPattern
				);

				Predicate whereDescription = criteriaBuilder.like(
					routineRoot.get(Routine_.description),
					searchPattern
				);

				Predicate nameOrDescription = criteriaBuilder.or(
					whereName,
					whereDescription
				);

				return criteriaQuery.where(nameOrDescription).distinct(true);
			});
		} catch (HibernateException | NoResultException e) {
			throw new PersistenceException(e);
		}
	}

    @Override
    public String saveNewRevision(String rId, Routine newRoutineRevision)
    throws PersistenceException {

        try {
            short oldRevision = findPropertyById(rId, Routine_.revision);

            // Setting predecessor and updating revision for new revision.
            newRoutineRevision.setPredecessor(getProxy(rId));
            newRoutineRevision.setRevision(++oldRevision);

            // Saving new revision.
            return save(newRoutineRevision);
        } catch (HibernateException | NoResultException e) {
            throw new PersistenceException(e);
        }
    }
}
