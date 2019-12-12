package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.Tag;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;

import java.util.List;

/**
 * Created by mase on 16.06.2017.
 */
public interface ITagDAO extends IGenericDAO<Tag, String> {

    /**
     * Returns an instances of a {@see Tag} for a given search pattern.
     *
     * @param searchPattern keyword or its part to be used for {@see Tag} search.
     * @return instances of a {@see Tag} for a given search pattern.
     * @throws PersistenceException
     */
    List<Tag> findByLabelOrDescription(String searchPattern)
    throws PersistenceException;
}
