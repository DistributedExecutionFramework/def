package at.enfilo.def.domain.map;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.domain.map.pk.JTMapPK;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by mase on 25.08.2016.
 */
@Entity(name = JTMap.TABLE_NAME)
@Table(name = JTMap.TABLE_NAME)
public class JTMap extends AbstractEntityMap<JTMapPK> {

    public static final String TABLE_NAME = "def_job_task";

    public static final String JOB_ID_FIELD_NAME = "def_jt_job_id";
    public static final String TASK_ID_FIELD_NAME = "def_jt_task_id";

    private JTMapPK id;

    public JTMap() {
    }

    public JTMap(Job job, Task task) {
        this.id = new JTMapPK(job, task);
    }

    @EmbeddedId
    @Override
    public JTMapPK getId() {
        return id;
    }

    public void setId(JTMapPK id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JTMap jtMap = (JTMap) o;

        return id != null ? id.equals(jtMap.id) : jtMap.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}