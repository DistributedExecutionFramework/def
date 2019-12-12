package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;

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
     * Returns a list of all {@see Group} that are associated to a given {@see User} (uId).
     *
     * @param uId {@see User} (id) that will be used as a source of {@see User} - {@see Group} associations.
     * @return a list of all {@see Group} that are associated to a given {@see User} (uId).
     * @throws PersistenceException
     */
    List<Group> getAllGroups(String uId)
    throws PersistenceException;
}