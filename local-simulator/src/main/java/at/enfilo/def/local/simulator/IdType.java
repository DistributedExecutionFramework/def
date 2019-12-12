package at.enfilo.def.local.simulator;


public class IdType {
	private final String id;
	private final Type type;

	public IdType(String id, Type type) {
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		if (id == null || id.isEmpty()) {
			return type.toString();
		}
		return String.format("%s (%s)", type, id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IdType idType = (IdType) o;

		if (id != null ? !id.equals(idType.id) : idType.id != null) return false;
		return type == idType.type;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}
}
