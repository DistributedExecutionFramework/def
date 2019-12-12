package at.enfilo.def.domain.map.pk;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.domain.map.JTMap;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by mase on 21.08.16.
 */
@Embeddable
public class JTMapPK implements Serializable {

    private String jobId;
    private String taskId;

    public JTMapPK() {
    }

    public JTMapPK(Job job, Task task) {
        this.jobId = job.getId();
        this.taskId = task.getId();
    }

    @Column(name = JTMap.JOB_ID_FIELD_NAME, length = 36)
    public String getJobId() {
        return jobId;
    }

    @Column(name = JTMap.TASK_ID_FIELD_NAME, length = 36)
    public String getTaskId() {
        return taskId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JTMapPK jtMapPK = (JTMapPK) o;

        if (jobId != null ? !jobId.equals(jtMapPK.jobId) : jtMapPK.jobId != null) return false;
        return taskId != null ? taskId.equals(jtMapPK.taskId) : jtMapPK.taskId == null;
    }

    @Override
    public int hashCode() {
        int result = jobId != null ? jobId.hashCode() : 0;
        result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
        return result;
    }
}
