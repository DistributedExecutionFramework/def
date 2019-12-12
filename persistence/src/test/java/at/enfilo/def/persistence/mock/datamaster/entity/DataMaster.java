package at.enfilo.def.persistence.mock.datamaster.entity;

import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.persistence.mock.api.IDataMaster;

import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by mase on 02.09.2016.
 */
public abstract class DataMaster<T extends AbstractEntity<U>, U extends Serializable> implements IDataMaster<T, U> {

    private final Set<T> DUMMY_SET = new LinkedHashSet<>();
    private final Map<U, T> DUMMY_ID_MAP = new HashMap<>();

    private final Class<T> masterClass;
    private final Class<U> masterIdClass;
    private final SingularAttribute<T, U> idAttribute;

    protected DataMaster(Class<T> masterClass, Class<U> masterIdClass, SingularAttribute<T, U> idAttribute) {
        this.masterClass = masterClass;
        this.masterIdClass = masterIdClass;
        this.idAttribute = idAttribute;
    }

    @Override
    public boolean containsDummyEntityId(U eId) {
        return eId != null && DUMMY_ID_MAP.containsKey(eId);
    }

    @Override
    public Class<T> getMasterClass() {
        return masterClass;
    }

    @Override
    public Class<U> getMasterIdClass() {
        return masterIdClass;
    }

    @Override
    public SingularAttribute<T, U> getIdAttribute() {
        return idAttribute;
    }

    @Override
    public Set<T> getAllDummyEntities() {
        return new LinkedHashSet<>(DUMMY_SET);
    }

    @Override
    public T getDummyEntityById(U eId) {
        return eId != null ? DUMMY_ID_MAP.get(eId) : null;
    }

    protected void registerDummyEntity(T t) {
        DUMMY_SET.add(t);
        DUMMY_ID_MAP.put(t.getId(), t);
    }
}
