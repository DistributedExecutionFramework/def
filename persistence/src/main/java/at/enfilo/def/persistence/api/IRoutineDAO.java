package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;

import java.util.List;

/**
 * Created by mase on 06.04.2017.
 */
public interface IRoutineDAO extends IGenericDAO<Routine, String> {

    /**
     * Returns Ids of {@see Routine} instances for a given search pattern.
     *
     * @param searchPattern keyword or its part to be used for {@see Routine} search.
     * @return Ids of {@see Routine} instances for a given search pattern.
     * @throws PersistenceException
     */
    List<String> findIdsByNameOrDescription(String searchPattern)
    throws PersistenceException;

    /**
     * Returns instances of a {@see Routine} for a given search pattern.
     *
     * @param searchPattern keyword or its part to be used for {@see Routine} search.
     * @return instances of a {@see Routine} for a given search pattern.
     * @throws PersistenceException
     */
    List<Routine> findByNameOrDescription(String searchPattern)
    throws PersistenceException;

    /**
     * Creates new revision of old {@see Routine}.
     *
     * @param rId Id of the old {@see Routine} revision.
     * @param newRoutineRevision new revision itself.
     * @return id of the new {@see Routine} revision.
     */
    String saveNewRevision(String rId, Routine newRoutineRevision);
}
