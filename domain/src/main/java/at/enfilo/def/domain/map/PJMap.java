package at.enfilo.def.domain.map;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.map.pk.PJMapPK;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by mase on 25.08.2016.
 */
@Entity(name = PJMap.TABLE_NAME)
@Table(name = PJMap.TABLE_NAME)
public class PJMap extends AbstractEntityMap<PJMapPK> {

    public static final String TABLE_NAME = "def_program_job";

    public static final String PROGRAM_ID_FIELD_NAME = "def_pj_program_id";
    public static final String JOB_ID_FIELD_NAME = "def_pj_job_id";

    private PJMapPK id;

    public PJMap() {
    }

    public PJMap(Program program, Job job) {
        this.id = new PJMapPK(program, job);
    }

    @EmbeddedId
    @Override
    public PJMapPK getId() {
        return id;
    }

    @Override
    public void setId(PJMapPK id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PJMap pjMap = (PJMap) o;

        return id != null ? id.equals(pjMap.id) : pjMap.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
