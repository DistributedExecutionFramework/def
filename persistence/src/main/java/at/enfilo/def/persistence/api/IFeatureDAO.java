package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.domain.entity.Tag;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.util.PersistenceException;

import java.util.List;

/**
 * Created by mase on 16.06.2017.
 */
public interface IFeatureDAO extends IGenericDAO<Feature, String> {

    /**
     * Returns instances of {@see Feature}s for a given search pattern.
     * The search pattern can be the name, id or version of a feature.
     * This returns a list of all matching features and extensions.
     *
     *
     * @param searchPattern keyword or its part to be used for {@see Feature} search.
     * @return instances of a {@see Feature} for a given search pattern.
     * @throws PersistenceException
     */
    List<Feature> findByNameOrVersion(String searchPattern)
    throws PersistenceException;

    /**
     * Returns instance of {@link Feature} with a given name and version.
     *
     * @param name      name of the feature
     * @param version   version of the feature
     * @return          instance of {@link Feature} for given name and version.
     * @throws PersistenceException
     */
    List<Feature> findByNameAndVersion(String name, String version)
    throws PersistenceException;

    /**
     * Returns all instances of {@see Feature}s that are base features.
     * Extensions are eagerly fetched and can be accessed through the base feature.
     *
     * @return instances of a {@see Feature}
     * @throws PersistenceException
     */
    List<Feature> findAllBaseFeatures()
            throws PersistenceException;
}
