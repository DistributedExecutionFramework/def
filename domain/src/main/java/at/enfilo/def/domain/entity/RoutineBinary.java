package at.enfilo.def.domain.entity;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by mase on 06.04.2017.
 */
@Entity(name = RoutineBinary.TABLE_NAME)
@Table(name = RoutineBinary.TABLE_NAME)
public class RoutineBinary extends AbstractEntity<String> {

	public static final String TABLE_NAME = "def_routine_binary";
	public static final String ID_FIELD_NAME = "def_routine_binary_id";

	public static final String NAME_FIELD_NAME = "def_routine_binary_name";
	public static final String MD5_FIELD_NAME = "def_routine_binary_md5";
	public static final String SIZE_BYTES_FIELD_NAME = "def_routine_binary_size_bytes";
	public static final String IS_PRIMARY_FIELD_NAME = "def_routine_binary_is_primary";
	public static final String URL_FIELD_NAME = "def_routine_binary_url";

	private String id;
	private String name;
	private String md5;
	private long sizeInBytes;
	private boolean isPrimary;
	private String url;
	private String executionUrl;

	public RoutineBinary() {
		id = UUID.randomUUID().toString();
	}

	public RoutineBinary(String id, String name, String md5, long sizeInBytes, boolean isPrimary, String url, String executionUrl) {
		this.id = id;
		this.name = name;
		this.md5 = md5;
		this.sizeInBytes = sizeInBytes;
		this.isPrimary = isPrimary;
		this.url = url;
		this.executionUrl = executionUrl;
	}

	@Id
	@Column(name = RoutineBinary.ID_FIELD_NAME, length = 36)
	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Column(name = RoutineBinary.NAME_FIELD_NAME)
	public String getName() {
		return name;
	}

	@Column(name = RoutineBinary.MD5_FIELD_NAME)
	public String getMd5() {
		return md5;
	}

	@Column(name = RoutineBinary.SIZE_BYTES_FIELD_NAME)
	public long getSizeInBytes() {
		return sizeInBytes;
	}

	@Column(name = RoutineBinary.IS_PRIMARY_FIELD_NAME, columnDefinition = "BOOLEAN")
	public boolean isPrimary() {
		return isPrimary;
	}

	@Lob
	@Column(name = RoutineBinary.URL_FIELD_NAME)
	public String getUrl() {
		return url;
	}

	@Transient
	public String getExecutionUrl() { return executionUrl; }

	public void setName(String name) {
		this.name = name;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public void setSizeInBytes(long sizeInBytes) {
		this.sizeInBytes = sizeInBytes;
	}

	public void setPrimary(boolean primary) {
		isPrimary = primary;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setExecutionUrl(String executionUrl) { this.executionUrl = executionUrl; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RoutineBinary that = (RoutineBinary) o;

		if (name != that.name) return false;
		if (sizeInBytes != that.sizeInBytes) return false;
		if (isPrimary != that.isPrimary) return false;
		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		if (md5 != null ? !md5.equals(that.md5) : that.md5 != null) return false;
		if (url != null ? !url.equals(that.url) : that.url != null) return false;
		return executionUrl != null ? executionUrl.equals(that.executionUrl) : that.executionUrl == null;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (md5 != null ? md5.hashCode() : 0);
		result = 31 * result + (int) (sizeInBytes ^ (sizeInBytes >>> 32));
		result = 31 * result + (isPrimary ? 1 : 0);
		result = 31 * result + (url != null ? url.hashCode() : 0);
		result = 31 * result + (executionUrl != null ? executionUrl.hashCode() : 0);
		return result;
	}
}