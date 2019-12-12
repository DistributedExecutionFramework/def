package at.enfilo.def.domain.map;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.map.pk.RFMapPK;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity(name = RFMap.TABLE_NAME)
@Table(name = RFMap.TABLE_NAME)
public class RFMap extends AbstractEntityMap<RFMapPK> {

	public static final String TABLE_NAME = "def_routine_feature";

	public static final String ROUTINE_ID_FIELD_NAME = "def_rf_routine_id";
	public static final String FEATURE_ID_FIELD_NAME = "def_rf_feature_id";

	private RFMapPK id;

	public RFMap() {
	}

	public RFMap(Routine routine, Feature feature) {
		this.id = new RFMapPK(routine, feature);
	}

	@EmbeddedId
	@Override
	public RFMapPK getId() {
		return id;
	}

	@Override
	public void setId(RFMapPK id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RFMap rfMap = (RFMap) o;
		return Objects.equals(id, rfMap.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}