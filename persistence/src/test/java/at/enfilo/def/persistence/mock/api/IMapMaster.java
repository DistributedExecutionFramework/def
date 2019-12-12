package at.enfilo.def.persistence.mock.api;

import at.enfilo.def.domain.entity.AbstractEntity;

import java.io.Serializable;

/**
 * Created by mase on 05.09.2016.
 */
public interface IMapMaster<T extends AbstractEntity<U>, U extends Serializable> extends IPersistenceMaster<T, U> {
}
