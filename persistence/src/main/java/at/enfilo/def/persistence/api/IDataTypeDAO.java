package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.DataType;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;

import java.util.List;

public interface IDataTypeDAO extends IGenericDAO<DataType, String> {

    /**
     * Returns Ids of {@see DataType} instances for a given search pattern.
     *
     * @param searchPattern keyword or its part to be used for {@see DataType} search.
     * @return Ids of {@see DataType} instances for a given search pattern.
     * @throws PersistenceException
     */
	List<String> findIdsByNameOrDescription(String searchPattern);

	/**
	 * Find and returns all {@see DataType} where the given name contains.
	 *
	 * @param name - to find
	 * @return list of matching {@see DataType}
	 * @throws PersistenceException
	 */
	List<DataType> findByName(String name) throws PersistenceException;
}
