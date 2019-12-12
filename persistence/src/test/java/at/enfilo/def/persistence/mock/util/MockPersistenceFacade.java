package at.enfilo.def.persistence.mock.util;

import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.domain.entity.DataType;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.persistence.api.*;
import at.enfilo.def.persistence.api.core.IGenericDAO;
import at.enfilo.def.persistence.mock.api.IDataMaster;
import at.enfilo.def.persistence.mock.datamaster.entity.*;
import org.mockito.Mockito;

import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Created by mase on 06.09.2016.
 */
public class MockPersistenceFacade implements IPersistenceFacade {

    private final GroupDataMaster groupDataMaster;
    private final UserDataMaster userDataMaster;
    private final RoutineDataMaster routineDataMaster;
    private final RoutineBinaryDataMaster routineBinaryDataMaster;
    private final DataTypeDataMaster datatypeDataMaster;
    private final TagDataMaster tagDataMaster;
    private final FeatureDataMaster featureMaster;

    public MockPersistenceFacade() {
        groupDataMaster = new GroupDataMaster();
        userDataMaster = new UserDataMaster();
        routineDataMaster = new RoutineDataMaster();
        routineBinaryDataMaster = new RoutineBinaryDataMaster();
        datatypeDataMaster = new DataTypeDataMaster();
        tagDataMaster = new TagDataMaster();
        featureMaster = new FeatureDataMaster();
    }

    public GroupDataMaster getGroupDataMaster() {
        return groupDataMaster;
    }

    public UserDataMaster getUserDataMaster() {
        return userDataMaster;
    }

    public RoutineDataMaster getRoutineDataMaster() {
        return routineDataMaster;
    }

    public RoutineBinaryDataMaster getRoutineBinaryDataMaster() {
        return routineBinaryDataMaster;
    }

    public DataTypeDataMaster getDatatypeDataMaster() {
        return datatypeDataMaster;
    }

    public TagDataMaster getTagDataMaster() {
        return tagDataMaster;
    }

    public FeatureDataMaster getFeatureMaster() {
        return featureMaster;
    }

    @Override
    public <T extends AbstractEntity<U>, U extends Serializable> IGenericDAO<T, U> getNewGenericDAO(
        Class<T> domainClass, Class<U> idClass, SingularAttribute<T, U> idAttribute
    ) {
        throw new RuntimeException("Not suitable for tests.");
    }

    @Override
    public IUserDAO getNewUserDAO() {

        IUserDAO mockDAO = Mockito.mock(IUserDAO.class);

        // Applying custom dao behaviour decorators.
        when(mockDAO.getUserByName(anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();

            if (args.length >= 1 && args[0].equals(UserDataMaster.DUMMY_USER_1_NAME)) {
                return userDataMaster.getDummyEntityForSearch();
            }
            return any();
        });

        when(mockDAO.getAllGroups(anyString())).thenReturn(
            new LinkedList<>(groupDataMaster.getAllDummyEntities())
        );

        // Applying generic dao behaviour decorators.
        return decorateGenericBehaviour(mockDAO, userDataMaster);
    }

    @Override
    public IGroupDAO getNewGroupDAO() {

        IGroupDAO mockDAO = Mockito.mock(IGroupDAO.class);

        // Applying generic dao behaviour decorators.
        return decorateGenericBehaviour(mockDAO, groupDataMaster);
    }

    @Override
    public IRoutineDAO getNewRoutineDAO() {

        IRoutineDAO mockDAO = Mockito.mock(IRoutineDAO.class);

        // Applying custom dao behaviour decorators.
        when(mockDAO.findByNameOrDescription(anyString())).thenReturn(
            new LinkedList<>(routineDataMaster.getAllDummyEntities())
        );

        when(mockDAO.findIdsByNameOrDescription(anyString())).thenReturn(
            routineDataMaster.getAllDummyEntities().stream().map(Routine::getId).collect(Collectors.toList())
        );

        when(mockDAO.saveNewRevision(anyString(), anyObject())).thenReturn(
            UUID.randomUUID().toString()
        );

        // Applying generic dao behaviour decorators.
        return decorateGenericBehaviour(mockDAO, routineDataMaster);
    }

	@Override
	public IDataTypeDAO getNewDataTypeDAO() {

        IDataTypeDAO mockDAO = Mockito.mock(IDataTypeDAO.class);

        // Applying custom dao behaviour decorators.
        when(mockDAO.findByName(anyString())).thenReturn(
            new LinkedList<>(datatypeDataMaster.getAllDummyEntities())
        );

        when(mockDAO.findIdsByNameOrDescription(anyString())).thenReturn(
            datatypeDataMaster.getAllDummyEntities().stream().map(DataType::getId).collect(Collectors.toList())
        );

        // Applying generic dao behaviour decorators.
        return decorateGenericBehaviour(mockDAO, datatypeDataMaster);
	}

    @Override
    public IComplexTransactionDAO getNewComplexTransactionDAO() {
        return null;
    }

    @Override
    public IRoutineBinaryDAO getNewRoutineBinaryDAO() {

        IRoutineBinaryDAO mockDAO = Mockito.mock(IRoutineBinaryDAO.class);

        // Applying custom dao behaviour decorators.
        when(mockDAO.getUrl(anyString())).thenReturn(
            routineBinaryDataMaster.getDummyEntityForSearch().getUrl()
        );

        // Applying generic dao behaviour decorators.
        return decorateGenericBehaviour(mockDAO, routineBinaryDataMaster);
    }

    @Override
    public ITagDAO getNewTagDAO() {

        ITagDAO mockDAO = Mockito.mock(ITagDAO.class);

        // Applying custom dao behaviour decorators.
        when(mockDAO.findByLabelOrDescription(anyString())).thenReturn(
            new LinkedList<>(tagDataMaster.getAllDummyEntities())
        );

        // Applying generic dao behaviour decorators.
        return decorateGenericBehaviour(mockDAO, tagDataMaster);
    }

    @Override
    public IFeatureDAO getNewFeatureDAO() {

        IFeatureDAO mockDAO = Mockito.mock(IFeatureDAO.class);

        // Applying custom dao behaviour decorators.
        when(mockDAO.findByNameOrVersion(anyString())).thenReturn(
                new LinkedList<>(featureMaster.getAllDummyEntities())
        );

        // Applying generic dao behaviour decorators.
        return decorateGenericBehaviour(mockDAO, featureMaster);
    }

    private <R extends IGenericDAO<T, U>, T extends AbstractEntity<U>, U extends Serializable> R decorateGenericBehaviour(
        R genericDAO, IDataMaster<T, U> dataMaster
    ) {

        when(genericDAO.findAll()).thenReturn(
            new LinkedList<>(dataMaster.getAllDummyEntities())
        );

        when(genericDAO.findById(any())).thenReturn(
            dataMaster.getDummyEntityForSearch()
        );

        when(genericDAO.findSingleResultByCriteria(any())).thenReturn(
            dataMaster.getDummyEntityForSearch()
        );

        when(genericDAO.findByCriteria(any())).thenReturn(
            Collections.singletonList(dataMaster.getDummyEntityForSearch())
        );

        when(genericDAO.findBySQLQuery(anyString(), anyVararg())).thenReturn(
            Collections.singletonList(dataMaster.getDummyEntityForSearch())
        );

        when(genericDAO.save(any())).thenReturn(
            dataMaster.getDummyEntityForSearch().getId()
        );

        return genericDAO;
    }
}
