package at.enfilo.def.domain.map.pk;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.map.UGMap;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by mase on 03.10.2016.
 */
@Embeddable
public class UGMapPK implements Serializable {

    private String userId;
    private String groupId;

    public UGMapPK() {
    }

    public UGMapPK(User user, Group group) {
        this.userId = user.getId();
        this.groupId = group.getId();
    }

    @Column(name = UGMap.USER_ID_FIELD_NAME, length = 36)
    public String getUserId() {
        return userId;
    }

    @Column(name = UGMap.GROUP_ID_FIELD_NAME, length = 36)
    public String getGroupId() {
        return groupId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UGMapPK ugMapPK = (UGMapPK) o;

        if (userId != null ? !userId.equals(ugMapPK.userId) : ugMapPK.userId != null) return false;
        return groupId != null ? groupId.equals(ugMapPK.groupId) : ugMapPK.groupId == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
        return result;
    }
}
