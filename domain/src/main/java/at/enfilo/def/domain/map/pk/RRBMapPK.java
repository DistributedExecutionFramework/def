package at.enfilo.def.domain.map.pk;

import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.entity.RoutineBinary;
import at.enfilo.def.domain.map.RRBMap;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by mase on 06.04.2017.
 */
@Embeddable
public class RRBMapPK implements Serializable {

	private String routineId;
	private String routineBinaryId;

	public RRBMapPK() {
	}

	public RRBMapPK(Routine routine, RoutineBinary routineBinary) {
		this.routineId = routine.getId();
		this.routineBinaryId = routineBinary.getId();
	}

	@Column(name = RRBMap.ROUTINE_ID_FIELD_NAME, length = 36)
	public String getRoutineId() {
		return routineId;
	}

	@Column(name = RRBMap.ROUTINE_BINARY_ID_FIELD_NAME, length = 36)
	public String getRoutineBinaryId() {
		return routineBinaryId;
	}

	public void setRoutineId(String routineId) {
		this.routineId = routineId;
	}

	public void setRoutineBinaryId(String routineBinaryId) {
		this.routineBinaryId = routineBinaryId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RRBMapPK rrbMapPK = (RRBMapPK) o;

		if (routineId != null ? !routineId.equals(rrbMapPK.routineId) : rrbMapPK.routineId != null) return false;
		return routineBinaryId != null ? routineBinaryId.equals(rrbMapPK.routineBinaryId) : rrbMapPK.routineBinaryId == null;
	}

	@Override
	public int hashCode() {
		int result = routineId != null ? routineId.hashCode() : 0;
		result = 31 * result + (routineBinaryId != null ? routineBinaryId.hashCode() : 0);
		return result;
	}
}
