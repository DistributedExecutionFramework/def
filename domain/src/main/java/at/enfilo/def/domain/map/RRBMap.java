package at.enfilo.def.domain.map;

import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.entity.RoutineBinary;
import at.enfilo.def.domain.map.pk.RRBMapPK;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by mase on 06.04.2017.
 */
@Entity(name = RRBMap.TABLE_NAME)
@Table(name = RRBMap.TABLE_NAME)
public class RRBMap extends AbstractEntityMap<RRBMapPK> {

	public static final String TABLE_NAME = "def_routine_routine_binary";

	public static final String ROUTINE_ID_FIELD_NAME = "def_rrb_routine_id";
	public static final String ROUTINE_BINARY_ID_FIELD_NAME = "def_rrb_routine_binary_id";

	private RRBMapPK id;

	public RRBMap() {
	}

	public RRBMap(Routine routine, RoutineBinary routineBinary) {
		this.id = new RRBMapPK(routine, routineBinary);
	}

	@EmbeddedId
	@Override
	public RRBMapPK getId() {
		return id;
	}

	@Override
	public void setId(RRBMapPK id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RRBMap rrbMap = (RRBMap) o;

		return id != null ? id.equals(rrbMap.id) : rrbMap.id == null;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}