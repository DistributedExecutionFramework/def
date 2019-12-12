package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.persistence.api.*;
import at.enfilo.def.persistence.api.core.IGenericDAO;

import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;

/**
 * Created by mase on 16.08.2016.
 */
public class PersistenceFacade implements IPersistenceFacade {

    @Override
    public <T extends AbstractEntity<U>, U extends Serializable> IGenericDAO<T, U> getNewGenericDAO(
        Class<T> domainClass,
        Class<U> idClass,
        SingularAttribute<T, U> idAttribute
    ) {
        return new GenericDAO<>(domainClass, idClass, idAttribute);
    }

    @Override
    public IUserDAO getNewUserDAO() {
        return new UserDAO();
    }

    @Override
    public IGroupDAO getNewGroupDAO() {
        return new GroupDAO();
    }

    @Override
    public IProgramDAO getNewProgramDAO() {
        return new ProgramDAO();
    }

    @Override
    public IJobDAO getNewJobDAO() {
        return new JobDAO();
    }

    @Override
    public ITaskDAO getNewTaskDAO() {
        return new TaskDAO();
    }

	@Override
	public IRoutineDAO getNewRoutineDAO() {
		return new RoutineDAO();
	}

    @Override
    public IRoutineBinaryDAO getNewRoutineBinaryDAO() {
        return new RoutineBinaryDAO();
    }

    @Override
    public ITagDAO getNewTagDAO() {
        return new TagDAO();
    }

    @Override
    public IDataTypeDAO getNewDataTypeDAO() {
        return new DataTypeDAO();
    }

    @Override
    public IComplexTransactionDAO getNewComplexTransactionDAO() {
        return new ComplexTransactionDAO();
    }

    @Override
    public IFeatureDAO getNewFeatureDAO() {
        return new FeatureDAO();
    }
}
