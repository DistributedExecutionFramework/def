package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Job_;
import at.enfilo.def.persistence.api.IJobDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.IdDTO;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.util.MapManager;
import at.enfilo.def.transfer.util.MapperException;
import at.enfilo.def.transfer.util.UnsupportedMappingException;
import org.hibernate.HibernateException;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by mase on 22.08.2016.
 */
class JobDAO extends GenericDAO<Job, String> implements IJobDAO {

    public JobDAO() {
        super(Job.class, String.class, Job_.id);
    }

    @Override
    public JobDTO getJobInfo(String jId)
    throws PersistenceException, UnsupportedMappingException {

        try {
            Job job = findById(jId);
            return MapManager.map(job, JobDTO.class);
        } catch (HibernateException | MapperException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<IdDTO> getAllTaskIds(String jId)
    throws PersistenceException {

        try {
            return findByCriteria(criteriaBuilder -> {

                CriteriaQuery<IdDTO> criteriaQuery = criteriaBuilder.createQuery(IdDTO.class);

                Root<Job> jRoot = criteriaQuery.from(Job.class);
                //Join<Job, Task> jtJoin = jRoot.join(Job_.tasks);

                Predicate wherePredicate = criteriaBuilder.equal(jRoot.get(Job_.id), jId);

                //criteriaQuery.select(criteriaBuilder.construct(IdDTO.class, jtJoin.get(Task_.id)));
                return criteriaQuery.where(wherePredicate).distinct(true);
            });
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }
    }
}

