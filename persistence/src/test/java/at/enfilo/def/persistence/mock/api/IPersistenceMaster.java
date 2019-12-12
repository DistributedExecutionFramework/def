package at.enfilo.def.persistence.mock.api;

import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;

/**
 * Created by mase on 05.09.2016.
 */
public interface IPersistenceMaster<T, U> {

    Class<T> getMasterClass();

    Class<U> getMasterIdClass();

    SingularAttribute<T, U> getIdAttribute();

    Collection<T> getAllDummyEntities();
}
