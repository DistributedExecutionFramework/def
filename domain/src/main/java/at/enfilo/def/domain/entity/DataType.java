package at.enfilo.def.domain.entity;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = DataType.TABLE_NAME)
@Table(name = DataType.TABLE_NAME)
public class DataType extends AbstractEntity<String> {

	public static final String TABLE_NAME = "def_data_type";
	public static final String ID_FIELD_NAME = "def_data_type_id";

	public static final String NAME_FIELD_NAME = "def_data_type_name";
	public static final String SCHEMA_FIELD_NAME = "def_data_type_schema";

	private String id;
	private String name;
	private String schema;

	public DataType() {
		id = UUID.randomUUID().toString();
	}

	@Id
	@Column(name = DataType.ID_FIELD_NAME, length = 36)
	@Override
	public String getId() {
		return this.id;
	}

	@Column(name = DataType.NAME_FIELD_NAME)
	public String getName() {
		return name;
	}

	@Lob
	@Column(name = DataType.SCHEMA_FIELD_NAME)
	public String getSchema() {
		return schema;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataType dataType = (DataType) o;

		if (!id.equals(dataType.id)) return false;
		if (!name.equals(dataType.name)) return false;
		return schema.equals(dataType.schema);

	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + schema.hashCode();
		return result;
	}
}
