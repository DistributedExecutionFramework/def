package at.enfilo.def.domain.map.pk;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.map.PJMap;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by mase on 21.08.16.
 */
@Embeddable
public class PJMapPK implements Serializable {

    private String programId;
    private String jobId;

    public PJMapPK() {
    }

    public PJMapPK(Program program, Job job) {
        this.programId = program.getId();
        this.jobId = job.getId();
    }

    @Column(name = PJMap.PROGRAM_ID_FIELD_NAME, length = 36)
    public String getProgramId() {
        return programId;
    }

    @Column(name = PJMap.JOB_ID_FIELD_NAME, length = 36)
    public String getJobId() {
        return jobId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PJMapPK pjMapPK = (PJMapPK) o;

        if (programId != null ? !programId.equals(pjMapPK.programId) : pjMapPK.programId != null) return false;
        return jobId != null ? jobId.equals(pjMapPK.jobId) : pjMapPK.jobId == null;
    }

    @Override
    public int hashCode() {
        int result = programId != null ? programId.hashCode() : 0;
        result = 31 * result + (jobId != null ? jobId.hashCode() : 0);
        return result;
    }
}
