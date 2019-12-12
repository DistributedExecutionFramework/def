package at.enfilo.def.domain.map;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.map.pk.PGMapPK;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by mase on 23.08.2016.
 */
@Entity(name = PGMap.TABLE_NAME)
@Table(name = PGMap.TABLE_NAME)
public class PGMap extends AbstractEntityMap<PGMapPK> {

    public static final String TABLE_NAME = "def_program_group";

    public static final String PROGRAM_ID_FIELD_NAME = "def_pg_program_id";
    public static final String GROUP_ID_FIELD_NAME = "def_pg_group_id";

    private PGMapPK id;

    public PGMap() {
    }

    public PGMap(Program program, Group group) {
        this.id = new PGMapPK(program, group);
    }

    @EmbeddedId
    @Override
    public PGMapPK getId() {
        return id;
    }

    @Override
    public void setId(PGMapPK id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PGMap pgMap = (PGMap) o;

        return id != null ? id.equals(pgMap.id) : pgMap.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
