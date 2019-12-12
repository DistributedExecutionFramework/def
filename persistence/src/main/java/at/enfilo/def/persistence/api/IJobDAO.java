package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.IdDTO;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.util.UnsupportedMappingException;

import java.util.List;

/**
 * Created by mase on 22.08.2016.
 */
public interface IJobDAO extends IGenericDAO<Job, String> {

    /**
     * Returns information in DTO form about the requested {@see Job} by given {@see Job} (jId).
     *
     * @param jId {@see Job} (id) that will be used as a data source.
     * @return {@see JobDTO} object that contains information about requested {@see Job}.
     * @throws PersistenceException
     * @throws UnsupportedMappingException
     */
    JobDTO getJobInfo(String jId)
    throws PersistenceException, UnsupportedMappingException;

    /**
     * Returns a list of all {@see Task} ids that are associated to a given {@see Job} (jId).
     *
     * @param jId {@see Job} (id) that will be used as a source of {@see Job} - {@see Task} associations.
     * @return a list of all {@see Task} ids that are associated to a given {@see Job} (jId).
     * @throws PersistenceException
     */
    List<IdDTO> getAllTaskIds(String jId)
    throws PersistenceException;
}
