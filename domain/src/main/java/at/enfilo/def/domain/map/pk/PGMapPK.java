package at.enfilo.def.domain.map.pk;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.map.PGMap;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by mase on 21.08.16.
 */
@Embeddable
public class PGMapPK implements Serializable {

    private String programId;
    private String groupId;

    public PGMapPK() {
    }

    public PGMapPK(Program program, Group group) {
        this.programId = program.getId();
        this.groupId = group.getId();
    }

    @Column(name = PGMap.PROGRAM_ID_FIELD_NAME, length = 36)
    public String getProgramId() {
        return programId;
    }

    @Column(name = PGMap.GROUP_ID_FIELD_NAME, length = 36)
    public String getGroupId() {
        return groupId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PGMapPK pgMapPK = (PGMapPK) o;

        if (programId != null ? !programId.equals(pgMapPK.programId) : pgMapPK.programId != null) return false;
        return groupId != null ? groupId.equals(pgMapPK.groupId) : pgMapPK.groupId == null;
    }

    @Override
    public int hashCode() {
        int result = programId != null ? programId.hashCode() : 0;
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
        return result;
    }
}
