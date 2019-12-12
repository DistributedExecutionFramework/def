package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Group_;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.entity.User_;
import at.enfilo.def.persistence.api.IUserDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import org.hibernate.HibernateException;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by mase on 16.08.2016.
 */
class UserDAO extends GenericDAO<User, String> implements IUserDAO {

    public UserDAO() {
        super(User.class, String.class, User_.id);
    }

    @Override
    public User getUserByName(String name)
    throws PersistenceException {

        try {
            return findSingleResultByCriteria(criteriaBuilder -> {

                CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);

                Root<User> userRoot = criteriaQuery.from(User.class);

                Predicate wherePredicate = criteriaBuilder.like(
                    userRoot.get(User_.name),
                    name
                );

                return criteriaQuery.where(wherePredicate);
            });
        } catch (HibernateException | NoResultException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<Group> getAllGroups(String uId)
    throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<Group> criteriaQuery = criteriaBuilder.createQuery(Group.class);

                Root<Group> gRoot = criteriaQuery.from(Group.class);
                Join<Group, User> guJoin = gRoot.join(Group_.users);

                Predicate wherePredicate = criteriaBuilder.equal(
                    guJoin.get(User_.id),
                    uId
                );

                return criteriaQuery.where(wherePredicate);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }
}
