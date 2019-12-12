package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.*;
import at.enfilo.def.domain.map.PGMap;
import at.enfilo.def.domain.map.PGMap_;
import at.enfilo.def.domain.map.UGMap;
import at.enfilo.def.domain.map.UGMap_;
import at.enfilo.def.domain.map.pk.PGMapPK_;
import at.enfilo.def.domain.map.pk.UGMapPK_;
import at.enfilo.def.persistence.api.IUserDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.IdDTO;
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
    public List<Program> getAllOwnedPrograms(String uId)
    throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<Program> criteriaQuery = criteriaBuilder.createQuery(Program.class);

                Root<Program> pRoot = criteriaQuery.from(Program.class);

                Predicate wherePredicate = criteriaBuilder.equal(
                    pRoot.get(Program_.owner).get(User_.id),
                    uId
                );

                return criteriaQuery.where(wherePredicate).distinct(true);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<Program> getAllGroupPrograms(String uId)
    throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<Program> criteriaQuery = criteriaBuilder.createQuery(Program.class);

                Root<User> uRoot = criteriaQuery.from(User.class);
                Join<User, Group> ugJoin = uRoot.join(User_.groups);
                Join<Group, Program> pgJoin = ugJoin.join(Group_.programs);

                Predicate wherePredicate = criteriaBuilder.equal(
                    uRoot.get(User_.id),
                    uId
                );

                criteriaQuery.select(pgJoin);
                return criteriaQuery.where(wherePredicate).distinct(true);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<Program> getAllPrograms(String uId)
    throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<Program> criteriaQuery = criteriaBuilder.createQuery(Program.class);

                Root<Program> pRoot = criteriaQuery.from(Program.class);
                Root<PGMap> pgMapRoot = criteriaQuery.from(PGMap.class);
                Root<UGMap> ugMapRoot = criteriaQuery.from(UGMap.class);

                // is owner

                Predicate isProgramOwner = criteriaBuilder.equal(
                    pRoot.get(Program_.owner).get(User_.id),
                    uId
                );

                // is program user (member of the group)

                Predicate isSameProgram = criteriaBuilder.equal(
                    pgMapRoot.get(PGMap_.id).get(PGMapPK_.programId),
                    pRoot.get(Program_.id)
                );

                Predicate isSameGroup = criteriaBuilder.equal(
                    pgMapRoot.get(PGMap_.id).get(PGMapPK_.groupId),
                    ugMapRoot.get(UGMap_.id).get(UGMapPK_.groupId)
                );

                Predicate isProgramUser = criteriaBuilder.equal(
                    ugMapRoot.get(UGMap_.id).get(UGMapPK_.userId),
                    uId
                );

                Predicate isProgramGroupUser = criteriaBuilder.and(
                    isSameProgram,
                    isSameGroup,
                    isProgramUser
                );

                criteriaQuery.select(pRoot);
                return criteriaQuery.where(criteriaBuilder.or(isProgramOwner, isProgramGroupUser)).distinct(true);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<IdDTO> getAllProgramIds(String uId)
    throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<IdDTO> criteriaQuery = criteriaBuilder.createQuery(IdDTO.class);

                Root<Program> pRoot = criteriaQuery.from(Program.class);
                Root<PGMap> pgMapRoot = criteriaQuery.from(PGMap.class);
                Root<UGMap> ugMapRoot = criteriaQuery.from(UGMap.class);

                // is owner

                Predicate isProgramOwner = criteriaBuilder.equal(
                    pRoot.get(Program_.owner).get(User_.id),
                    uId
                );

                // is program user (member of the group)

                Predicate isSameProgram = criteriaBuilder.equal(
                    pgMapRoot.get(PGMap_.id).get(PGMapPK_.programId),
                    pRoot.get(Program_.id)
                );

                Predicate isSameGroup = criteriaBuilder.equal(
                    pgMapRoot.get(PGMap_.id).get(PGMapPK_.groupId),
                    ugMapRoot.get(UGMap_.id).get(UGMapPK_.groupId)
                );

                Predicate isProgramUser = criteriaBuilder.equal(
                    ugMapRoot.get(UGMap_.id).get(UGMapPK_.userId),
                    uId
                );

                Predicate isProgramGroupUser = criteriaBuilder.and(
                    isSameProgram,
                    isSameGroup,
                    isProgramUser
                );

                criteriaQuery.select(criteriaBuilder.construct(IdDTO.class, pRoot.get(Program_.id)));
                return criteriaQuery.where(criteriaBuilder.or(isProgramOwner, isProgramGroupUser)).distinct(true);
            });
        } catch (HibernateException e) {
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
