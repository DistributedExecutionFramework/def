package at.enfilo.def.domain.entity;

import java.io.Serializable;

/**
 * Base class for all domain entities.
 *
 * @param <T> - type of id
 */
public abstract class AbstractEntity<T extends Serializable> implements Serializable {

    public abstract T getId();

    public abstract void setId(T id);
}
