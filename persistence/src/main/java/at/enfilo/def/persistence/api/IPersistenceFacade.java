package at.enfilo.def.persistence.api;

import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.persistence.api.core.IGenericDAO;

import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;

/**
 * Created by mase on 05.09.2016.
 */
public interface IPersistenceFacade {

    /**
     * Returns an implementation of the IGenericDAO interface providing
     * basic operations with domain objects.
     *
     * @param domainClass domain class that will be used as a reference for this DAO.
     * @param idClass class that represents type of the id property of the domain class.
     * @param idAttribute hibernate referencer to id property (required for complex queries).
     * @return an instance of IGenericDAO.
     */
    <T extends AbstractEntity<U>, U extends Serializable> IGenericDAO<T, U> getNewGenericDAO(
        Class<T> domainClass,
        Class<U> idClass,
        SingularAttribute<T, U> idAttribute
    );

    /**
     * Returns an implementation of the IUserDAO interface providing
     * further operations with users.
     *
     * @return an instance of IUserDAO.
     */
    IUserDAO getNewUserDAO();

    /**
     * Returns an implementation of the IGroupDAO interface providing
     * further operations with groups.
     *
     * @return an instance of IGroupDAO.
     */
    IGroupDAO getNewGroupDAO();

    /**
     * Returns an implementation of the IProgramDAO interface providing
     * further operations with programs.
     *
     * @return an instance of IProgramDAO.
     */
    IProgramDAO getNewProgramDAO();

    /**
     * Returns an implementation of the IJobDAO interface providing
     * further operations with jobs.
     *
     * @return an instance of IJobDAO.
     */
    IJobDAO getNewJobDAO();

    /**
     * Returns an implementation of the ITaskDAO interface providing
     * further operations with tasks.
     *
     * @return an instance of ITaskDAO.
     */
    ITaskDAO getNewTaskDAO();

    /**
     * Returns an implementation of the IRoutineDAO interface providing
     * further operations with routines.
     *
     * @return an instance of IRoutineDAO.
     */
    IRoutineDAO getNewRoutineDAO();

    /**
     * Returns an implementation of the IRoutineBinaryDAO interface providing
     * further operations with routine-binaries.
     *
     * @return an instance of IRoutineBinaryDAO.
     */
    IRoutineBinaryDAO getNewRoutineBinaryDAO();

    /**
     * Returns an implementation of the ITagDAO interface providing
     * further operations with tags.
     *
     * @return an instance of ITagDAO.
     */
    ITagDAO getNewTagDAO();

    /**
     * Returns an implementation of the IDataTypeDAO interface providing
     * further operations with data-types.
     *
     * @return an instance of IDataTypeDAO.
     */
    IDataTypeDAO getNewDataTypeDAO();

    /**
     * Returns an implementation of the IFeatureDAO interface providing
     * further operations with features.
     *
     * @return an instance of IFeatureDAO.
     */
    IFeatureDAO getNewFeatureDAO();

    /**
     * Returns an implementation of the IComplexTransactionDAO interface providing ability
     * to create and execute complex transactions.
     *
     * @return an instance of IComplexTransactionDAO.
     */
    IComplexTransactionDAO getNewComplexTransactionDAO();
}
