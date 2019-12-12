package at.enfilo.def.domain.map;

import at.enfilo.def.domain.entity.FormalParameter;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.map.pk.RFPMapPK;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by mase on 23.08.2016.
 */
@Entity(name = RFPMap.TABLE_NAME)
@Table(name = RFPMap.TABLE_NAME)
public class RFPMap extends AbstractEntityMap<RFPMapPK> {

    public static final String TABLE_NAME = "def_routine_formal_parameter";

    public static final String ROUTINE_ID_FIELD_NAME = "def_rfp_routine_id";
    public static final String FORMAL_PARAMETER_ID_FIELD_NAME = "def_rfp_formal_parameter_id";

    private RFPMapPK id;

    public RFPMap() {
    }

    public RFPMap(Routine routine, FormalParameter formalParameter) {
        this.id = new RFPMapPK(routine, formalParameter);
    }

    @EmbeddedId
    @Override
    public RFPMapPK getId() {
        return id;
    }

    @Override
    public void setId(RFPMapPK id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RFPMap RFPMap = (RFPMap) o;

        return id != null ? id.equals(RFPMap.id) : RFPMap.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
