package at.enfilo.def.domain.entity;

import at.enfilo.def.transfer.dto.FeatureDTO;

import javax.persistence.*;
import java.util.*;

@Entity(name = Feature.TABLE_NAME)
@Table(name = Feature.TABLE_NAME,
uniqueConstraints = @UniqueConstraint(columnNames = {Feature.NAME_FIELD_NAME, Feature.VERSION_FIELD_NAME}))
public class Feature extends AbstractEntity<String> {
	public static final String TABLE_NAME = "def_feature";
	public static final String ID_FIELD_NAME = "def_feature_id";
	public static final String NAME_FIELD_NAME = "def_feature_name";
	public static final String VERSION_FIELD_NAME = "def_feature_version";
	public static final String GROUP_FIELD_NAME = "def_feature_group";
	public static final String BASE_FEATURE_ID_FIELD_NAME = "def_feature_base_id";

	private String id;
	private String name;
	private String version;
	private String group;
	private Feature baseFeature;
	private Collection<Feature> subFeatures;
	private String baseFeatureId;

	public Feature() {
		id = UUID.randomUUID().toString();
	}

	public Feature(String id, String name, String version, String group) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.group = group;
	}

	public Feature(FeatureDTO dto) {
		this.id = dto.getId();
		this.name = dto.getName();
		this.group = dto.getGroup();
		this.version = dto.getVersion();
		this.baseFeatureId = dto.getBaseId();
		if (dto.getExtensions() != null && dto.getExtensionsSize() > 0) {
			this.subFeatures = new LinkedList<>();
			for (FeatureDTO ext : dto.getExtensions()) {
				this.subFeatures.add(new Feature(ext));
			}
		}
	}

	@Id
	@Column(name = ID_FIELD_NAME, length = 36)
	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}


	@Column(name = NAME_FIELD_NAME, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = VERSION_FIELD_NAME)
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Column(name = GROUP_FIELD_NAME)
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = Feature.BASE_FEATURE_ID_FIELD_NAME, insertable = false, updatable = false)
	public Feature getBaseFeature() {
		return baseFeature;
	}


	public void setBaseFeature(Feature baseFeature) {
		this.baseFeature = baseFeature;
	}

	@Column(name = Feature.BASE_FEATURE_ID_FIELD_NAME)
	public String getBaseFeatureId() {
		return this.baseFeatureId;
	}

	public void setBaseFeatureId(String baseFeatureId) {
		this.baseFeatureId = baseFeatureId;
	}

	@OneToMany(mappedBy = "baseFeature", fetch = FetchType.EAGER)
	public Collection<Feature> getSubFeatures() {
		return subFeatures;
	}

	public void setSubFeatures(Collection<Feature> subFeatures) {
		this.subFeatures = subFeatures;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Feature feature = (Feature) o;
		return Objects.equals(id, feature.id) &&
				Objects.equals(name, feature.name) &&
				Objects.equals(version, feature.version) &&
				Objects.equals(group, feature.group);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, version, group);
	}

	@Override
	public String toString() {
		return name + '(' + version + ')';
	}

	public void updateId() {
		id = UUID.nameUUIDFromBytes(toString().getBytes()).toString();
	}
}
