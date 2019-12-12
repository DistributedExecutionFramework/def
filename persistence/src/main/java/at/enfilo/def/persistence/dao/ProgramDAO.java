package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Job_;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.entity.Program_;
import at.enfilo.def.persistence.api.IProgramDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.IdDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.util.MapperException;
import at.enfilo.def.transfer.util.MapManager;
import at.enfilo.def.transfer.util.UnsupportedMappingException;
import org.hibernate.HibernateException;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by mase on 22.08.2016.
 */
class ProgramDAO extends GenericDAO<Program, String> implements IProgramDAO {

    public ProgramDAO() {
        super(Program.class, String.class, Program_.id);
    }

    @Override
    public ProgramDTO getProgramInfo(String pId)
    throws PersistenceException, UnsupportedMappingException {

        try {
            Program program = findById(pId);
            return MapManager.map(program, ProgramDTO.class);
        } catch (HibernateException | MapperException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<IdDTO> getAllJobIds(String pId)
    throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<IdDTO> criteriaQuery = criteriaBuilder.createQuery(IdDTO.class);

                Root<Program> pRoot = criteriaQuery.from(Program.class);
                Join<Program, Job> pjJoin = pRoot.join(Program_.jobs);

                Predicate wherePredicate = criteriaBuilder.equal(pRoot.get(Program_.id), pId);

                criteriaQuery.select(criteriaBuilder.construct(IdDTO.class, pjJoin.get(Job_.id)));
                return criteriaQuery.where(wherePredicate).distinct(true);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }
}
