package at.enfilo.def.domain.map;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.map.pk.UGMapPK;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by mase on 23.08.2016.
 */
@Entity(name = UGMap.TABLE_NAME)
@Table(name = UGMap.TABLE_NAME)
public class UGMap extends AbstractEntityMap<UGMapPK> {

    public static final String TABLE_NAME = "def_user_group";

    public static final String USER_ID_FIELD_NAME = "def_ug_user_id";
    public static final String GROUP_ID_FIELD_NAME = "def_ug_group_id";

    private UGMapPK id;

    public UGMap() {
    }

    public UGMap(User user, Group group) {
        this.id = new UGMapPK(user, group);
    }

    @EmbeddedId
    @Override
    public UGMapPK getId() {
        return id;
    }

    @Override
    public void setId(UGMapPK id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UGMap ugMap = (UGMap) o;

        return id != null ? id.equals(ugMap.id) : ugMap.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
