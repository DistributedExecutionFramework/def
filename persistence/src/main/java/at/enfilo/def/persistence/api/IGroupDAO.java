package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.IdDTO;

import java.util.List;

/**
 * Created by mase on 18.08.2016.
 */
public interface IGroupDAO extends IGenericDAO<Group, String> {

    /**
     * Returns a list of all {@see Program} ids that are associated to a given {@see Group} (gId).
     *
     * @param gId {@see Group} (id) that will be used as a source of {@see Program} - {@see Group} associations.
     * @return a list of all {@see Program} ids that are associated to a given {@see Group} (gId).
     * @throws PersistenceException
     */
    List<IdDTO> getAllProgramIds(String gId)
    throws PersistenceException;
}
