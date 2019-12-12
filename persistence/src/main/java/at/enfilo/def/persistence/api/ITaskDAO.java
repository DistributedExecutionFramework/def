package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.transfer.util.UnsupportedMappingException;

/**
 * Created by mase on 22.08.2016.
 */
public interface ITaskDAO extends IGenericDAO<Task, String> {

    /**
     * Returns information in DTO form about the requested {@see Task} by given {@see Task} (tId).
     *
     * @param tId {@see Task} (id) that will be used as a data source.
     * @return {@see TaskDTO} object that contains information about requested {@see Task}.
     * @throws PersistenceException
     * @throws UnsupportedMappingException
     */
    TaskDTO getTaskInfo(String tId)
    throws PersistenceException, UnsupportedMappingException;
}
