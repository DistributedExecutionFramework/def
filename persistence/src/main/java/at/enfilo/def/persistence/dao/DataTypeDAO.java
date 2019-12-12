package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.DataType;
import at.enfilo.def.domain.entity.DataType_;
import at.enfilo.def.persistence.api.IDataTypeDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import org.hibernate.HibernateException;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

class DataTypeDAO extends GenericDAO<DataType, String> implements IDataTypeDAO {

	public DataTypeDAO() throws IllegalArgumentException {
		super(DataType.class, String.class, DataType_.id);
	}

	@Override
	public List<String> findIdsByNameOrDescription(String searchPattern) {

		try {
			return findByCriteria(criteriaBuilder -> {

				CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);

				Root<DataType> dataTypeRoot = criteriaQuery.from(DataType.class);

				Predicate whereName = criteriaBuilder.like(
					dataTypeRoot.get(DataType_.name),
					searchPattern
				);

				Predicate whereSchema = criteriaBuilder.like(
					dataTypeRoot.get(DataType_.schema),
					searchPattern
				);

				Predicate nameOrSchema = criteriaBuilder.or(
					whereName,
					whereSchema
				);

				criteriaQuery.select(criteriaBuilder.construct(String.class, dataTypeRoot.get(DataType_.id)));
				return criteriaQuery.where(nameOrSchema).distinct(true);
			});
		} catch (HibernateException | NoResultException e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public List<DataType> findByName(String name) throws PersistenceException {
		try {
			return findByCriteria(criteriaBuilder -> {

				CriteriaQuery<DataType> criteriaQuery = criteriaBuilder.createQuery(DataType.class);

				Root<DataType> root = criteriaQuery.from(DataType.class);

				Predicate wherePredicate = criteriaBuilder.like(
					root.get(DataType_.name),
					"%" + name + "%"
				);

				return criteriaQuery.where(wherePredicate);
			});
		} catch (HibernateException e) {
			throw new PersistenceException(e);
		}
	}
}
