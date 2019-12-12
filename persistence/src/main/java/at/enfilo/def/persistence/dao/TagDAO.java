package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Tag;
import at.enfilo.def.domain.entity.Tag_;
import at.enfilo.def.persistence.api.ITagDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import org.hibernate.HibernateException;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by mase on 16.06.2017.
 */
class TagDAO extends GenericDAO<Tag, String> implements ITagDAO {

	public TagDAO() {
		super(Tag.class, String.class, Tag_.id);
	}

	@Override
	public List<Tag> findByLabelOrDescription(String searchPattern)
	throws PersistenceException {

		try {
			return findByCriteria(criteriaBuilder -> {

				CriteriaQuery<Tag> criteriaQuery = criteriaBuilder.createQuery(Tag.class);

				Root<Tag> tagRoot = criteriaQuery.from(Tag.class);

				Predicate whereLabel = criteriaBuilder.like(
					tagRoot.get(Tag_.id),
					searchPattern
				);

				Predicate whereDescription = criteriaBuilder.like(
					tagRoot.get(Tag_.description),
					searchPattern
				);

				Predicate nameOrDescription = criteriaBuilder.or(
					whereLabel,
					whereDescription
				);

				return criteriaQuery.where(nameOrDescription);
			});
		} catch (HibernateException | NoResultException e) {
			throw new PersistenceException(e);
		}
	}
}
