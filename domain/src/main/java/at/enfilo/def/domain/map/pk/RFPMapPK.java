package at.enfilo.def.domain.map.pk;

import at.enfilo.def.domain.entity.FormalParameter;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.map.RFPMap;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by mase on 21.08.16.
 */
@Embeddable
public class RFPMapPK implements Serializable {

    private String routineId;
    private String formalParameterId;

    public RFPMapPK() {
    }

    public RFPMapPK(Routine routine, FormalParameter formalParameter) {
        this.routineId = routine.getId();
        this.formalParameterId = formalParameter.getId();
    }

    @Column(name = RFPMap.ROUTINE_ID_FIELD_NAME, length = 36)
    public String getRoutineId() {
        return routineId;
    }

    @Column(name = RFPMap.FORMAL_PARAMETER_ID_FIELD_NAME, length = 36)
    public String getFormalParameterId() {
        return formalParameterId;
    }

    public void setRoutineId(String routineId) {
        this.routineId = routineId;
    }

    public void setFormalParameterId(String formalParameterId) {
        this.formalParameterId = formalParameterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RFPMapPK rfpMapPK = (RFPMapPK) o;

        if (routineId != null ? !routineId.equals(rfpMapPK.routineId) : rfpMapPK.routineId != null) return false;
        return formalParameterId != null ? formalParameterId.equals(rfpMapPK.formalParameterId) : rfpMapPK.formalParameterId == null;
    }

    @Override
    public int hashCode() {
        int result = routineId != null ? routineId.hashCode() : 0;
        result = 31 * result + (formalParameterId != null ? formalParameterId.hashCode() : 0);
        return result;
    }
}
