package at.enfilo.def.persistence.mock.api;

import at.enfilo.def.domain.entity.AbstractEntity;

import java.io.Serializable;

/**
 * Created by mase on 02.09.2016.
 */
public interface IDataMaster<T extends AbstractEntity<U>, U extends Serializable> extends IPersistenceMaster<T, U> {

    boolean containsDummyEntityId(U eId);

    T updateEntity(T initialEntity);

    T getDummyEntityById(U eId);

    T getDummyEntityForSearch();

    T getDummyEntityForSave();

    T getDummyEntityForUpdate();

    T getDummyEntityForDelete();
}
