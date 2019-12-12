package at.enfilo.def.persistence.mock.datamaster.map;

import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.persistence.mock.api.IMapMaster;

import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by mase on 02.09.2016.
 */
public abstract class MapDataMaster<T extends AbstractEntity<U>, U extends Serializable> implements IMapMaster<T, U> {

    private final Set<T> DUMMY_SET = new LinkedHashSet<>();

    private final Class<T> masterClass;
    private final Class<U> masterIdClass;
    private final SingularAttribute<T, U> masterIdAttribute;


    protected MapDataMaster(Class<T> masterClass, Class<U> masterIdClass, SingularAttribute<T, U> masterIdAttribute) {
        this.masterClass = masterClass;
        this.masterIdClass = masterIdClass;
        this.masterIdAttribute = masterIdAttribute;
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
        return masterIdAttribute;
    }

    @Override
    public Set<T> getAllDummyEntities() {
        return new LinkedHashSet<>(DUMMY_SET);
    }

    protected void registerDummyEntity(T t) {
        DUMMY_SET.add(t);
    }
}
