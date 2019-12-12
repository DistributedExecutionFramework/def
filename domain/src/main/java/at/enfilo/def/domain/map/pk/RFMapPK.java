package at.enfilo.def.domain.map.pk;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.map.RFMap;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RFMapPK implements Serializable {

	private String routineId;
	private String featureId;

	public RFMapPK() {
	}

	public RFMapPK(Routine routine, Feature feature) {
		this.routineId = routine.getId();
		this.featureId = feature.getId();
	}

	@Column(name = RFMap.ROUTINE_ID_FIELD_NAME, length = 36)
	public String getRoutineId() {
		return routineId;
	}

	@Column(name = RFMap.FEATURE_ID_FIELD_NAME, length = 36)
	public String getFeatureId() {
		return featureId;
	}

	public void setRoutineId(String routineId) {
		this.routineId = routineId;
	}

	public void setFeatureId(String featureId) {
		this.featureId = featureId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RFMapPK rfMapPK = (RFMapPK) o;
		return Objects.equals(routineId, rfMapPK.routineId) &&
				Objects.equals(featureId, rfMapPK.featureId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(routineId, featureId);
	}
}
