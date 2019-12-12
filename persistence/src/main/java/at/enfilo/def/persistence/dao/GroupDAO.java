package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Group_;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.entity.Program_;
import at.enfilo.def.persistence.api.IGroupDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.IdDTO;
import org.hibernate.HibernateException;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by mase on 16.08.2016.
 */
class GroupDAO extends GenericDAO<Group, String> implements IGroupDAO {

    public GroupDAO() {
        super(Group.class, String.class, Group_.id);
    }

    @Override
    public List<IdDTO> getAllProgramIds(String gId)
    throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<IdDTO> criteriaQuery = criteriaBuilder.createQuery(IdDTO.class);

                Root<Group> gRoot = criteriaQuery.from(Group.class);
                Join<Group, Program> gpJoin = gRoot.join(Group_.programs);

                Predicate isInProgramGroup = criteriaBuilder.equal(gRoot.get(Group_.id), gId);

                criteriaQuery.select(criteriaBuilder.construct(IdDTO.class, gpJoin.get(Program_.id)));
                return criteriaQuery.where(isInProgramGroup).distinct(true);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }
}
