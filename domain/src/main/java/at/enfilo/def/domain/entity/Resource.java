package at.enfilo.def.domain.entity;

import java.io.IOException;
import java.util.UUID;

public abstract class Resource extends AbstractEntity<String> {
	private String id;
	private String dataTypeId;
	private String key;

	public Resource() {
		this(null);
	}

	public Resource(String dataTypeId) {
		this.id = UUID.randomUUID().toString();
		this.dataTypeId = dataTypeId;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
		if (id == null) {
			this.id = UUID.randomUUID().toString();
		}
	}

	public String getDataTypeId() {
		return dataTypeId;
	}

	public void setDataTypeId(String dataTypeId) {
		this.dataTypeId = dataTypeId;
	}

	public abstract byte[] getData() throws IOException;

	public abstract void setData(byte[] data) throws IOException;

	public abstract void cleanUp();

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
