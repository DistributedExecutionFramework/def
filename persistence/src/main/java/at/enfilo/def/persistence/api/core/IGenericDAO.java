package at.enfilo.def.persistence.api.core;

import at.enfilo.def.domain.entity.AbstractEntity;

import java.io.Serializable;

/**
 * Created by mase on 18.08.2016.
 */
public interface IGenericDAO<T extends AbstractEntity<U>, U extends Serializable>
extends IBasicDAO<T, U>, ISearchDAO<T, U> {
}
