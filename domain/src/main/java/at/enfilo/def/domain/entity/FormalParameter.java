package at.enfilo.def.domain.entity;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by mase on 12.08.2016.
 */
@Entity(name = FormalParameter.TABLE_NAME)
@Table(name = FormalParameter.TABLE_NAME)
public class FormalParameter extends AbstractEntity<String> {

    public static final String TABLE_NAME = "def_formal_parameter";
    public static final String ID_FIELD_NAME = "def_formal_parameter_id";

    public static final String NAME_FIELD_NAME = "def_formal_parameter_name";
	public static final String DESCRIPTION_FIELD_NAME = "def_formal_parameter_description";
    public static final String DATA_TYPE_ID_FIELD_NAME = "def_formal_parameter_data_type_id";

    private String id;
    private String name;
    private String description;
    private DataType dataType;

    public FormalParameter() {
        id = UUID.randomUUID().toString();
    }

	public FormalParameter(String id, String name, String description, DataType dataType) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.dataType = dataType;
	}

	@Id
    @Column(name = FormalParameter.ID_FIELD_NAME, length = 36)
    @Override
    public String getId() {
        return id;
    }

    @Column(name = FormalParameter.NAME_FIELD_NAME)
    public String getName() {
        return name;
    }

    @OneToOne
    @JoinColumn(name = FormalParameter.DATA_TYPE_ID_FIELD_NAME)
    public DataType getDataType() {
        return dataType;
    }

    @Lob
    @Column(name = FormalParameter.DESCRIPTION_FIELD_NAME)
	public String getDescription() {
		return description;
	}

	@Override
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

	public void setDescription(String description) {
		this.description = description;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FormalParameter that = (FormalParameter) o;

		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (description != null ? !description.equals(that.description) : that.description != null) return false;
		return dataType != null ? dataType.equals(that.dataType) : that.dataType == null;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
		return result;
	}
}
