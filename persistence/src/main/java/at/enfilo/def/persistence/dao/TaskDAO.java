package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.domain.entity.Task_;
import at.enfilo.def.persistence.api.ITaskDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.transfer.util.MapperException;
import at.enfilo.def.transfer.util.MapManager;
import at.enfilo.def.transfer.util.UnsupportedMappingException;
import org.hibernate.HibernateException;

/**
 * Created by mase on 22.08.2016.
 */
class TaskDAO extends GenericDAO<Task, String> implements ITaskDAO {

    public TaskDAO() {
        super(Task.class, String.class, Task_.id);
    }

    @Override
    public TaskDTO getTaskInfo(String tId)
    throws PersistenceException, UnsupportedMappingException {

        try {
            Task task = findById(tId);
            return MapManager.map(task, TaskDTO.class);
        } catch (HibernateException | MapperException e) {
            throw new PersistenceException(e);
        }
    }
}
