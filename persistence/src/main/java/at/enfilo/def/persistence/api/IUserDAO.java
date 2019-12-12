package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.IdDTO;

import java.util.List;

/**
 * Created by mase on 18.08.2016.
 */
public interface IUserDAO extends IGenericDAO<User, String> {

    /**
     * Returns an instance of a {@see User} for a given name.
     *
     * @param name name to be used for {@see User} search.
     * @return instance of a {@see User} for a given name.
     * @throws PersistenceException
     */
    User getUserByName(String name)
    throws PersistenceException;

    /**
     * Returns all {@see Program} owned by a given {@see User} (uId).
     *
     * @param uId {@see User} (id), represents owner. Will be used as a source of {@see User} - {@see Program} associations.
     * @return a list of all {@see Program} that are associated to a given owner - {@see User} (uId).
     * @throws PersistenceException
     */
    List<Program> getAllOwnedPrograms(String uId)
    throws PersistenceException;

    /**
     * Returns all {@see Program} supplied to a given {@see User} (uId) via {@see Group}.
     *
     * @param uId {@see User} (id), represents {@see Group} consumer. Will be used as a source of {@see User} - {@see Group} - {@see Program} associations.
     * @return a list of all {@see Program} that are associated to a given {@see Group} consumer - {@see User} (uId).
     * @throws PersistenceException
     */
    List<Program> getAllGroupPrograms(String uId)
    throws PersistenceException;

    /**
     * Returns all {@see Program} that are owned by a given {@see User} and / or are supplied via {@see Group} of a given {@see User}.
     *
     * @param uId {@see User} (id), represents owner and / or {@see Group} consumer. Will be used as a source of {@see User} - {@see Group} - {@see Program} associations.
     * @return a list of all {@see Program} that are associated to a given owner and / or {@see Group} consumer - {@see User} (uId).
     * @throws PersistenceException
     */
    List<Program> getAllPrograms(String uId)
    throws PersistenceException;

    /**
     * Returns all {@see Program} ids that are owned by a given {@see User} and / or are supplied via {@see Group} of a given {@see User}.
     *
     * @param uId {@see User} (id), represents owner and / or {@see Group} consumer. Will be used as a source of {@see User} - {@see Group} - {@see Program} associations.
     * @return a list of all {@see Program} ids that are associated to a given owner and / or {@see Group} consumer - {@see User} (uId).
     * @throws PersistenceException
     */
    List<IdDTO> getAllProgramIds(String uId)
    throws PersistenceException;


    /**
     * Returns a list of all {@see Group} that are associated to a given {@see User} (uId).
     *
     * @param uId {@see User} (id) that will be used as a source of {@see User} - {@see Group} associations.
     * @return a list of all {@see Group} that are associated to a given {@see User} (uId).
     * @throws PersistenceException
     */
    List<Group> getAllGroups(String uId)
    throws PersistenceException;
}