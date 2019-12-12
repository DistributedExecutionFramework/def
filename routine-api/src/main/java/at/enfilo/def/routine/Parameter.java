package at.enfilo.def.routine;

import org.apache.thrift.TBase;

/**
 * Represents a Parameter with value and type.
 *
 * @param <T>
 */
public class Parameter<T extends TBase> {
	private final Class<T> type;
	private final T value;

	/**
	 * Create a parameter with type and value.
	 *
	 * @param type - class/type
	 * @param value - value
	 */
	public Parameter(Class<T> type, T value) {
		this.type = type;
		this.value = value;
	}

	public Class<T> getType() {
		return type;
	}

	public T getValue() {
		return value;
	}

	/**
	 * Get value and try to cast to given type.
	 * @param type - type to cast.
	 * @param <V> - generic type.
	 * @return value
	 */
	public <V> V getValue(Class<V> type) {
		if (type.equals(this.type)) {
			return type.cast(value);
		}
		throw new WrongTypeException("Requested Type: " + type.getName() + ", Assigned Type: " + this.type.getName());
	}
}
