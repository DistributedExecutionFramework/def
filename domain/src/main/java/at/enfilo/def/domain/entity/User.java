package at.enfilo.def.domain.entity;

import at.enfilo.def.domain.map.UGMap;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

/**
 * Created by mase on 12.08.2016.
 */
@Entity(name = User.TABLE_NAME)
@Table(name = User.TABLE_NAME)
public class User extends AbstractEntity<String> {

    public static final String TABLE_NAME = "def_user";

    public static final String ID_FIELD_NAME = "def_user_id";
    public static final String NAME_FIELD_NAME = "def_user_name";
    public static final String PASS_FIELD_NAME = "def_user_pass";
    public static final String SALT_FIELD_NAME = "def_user_salt";

    private String id;
    private String name;
    private String pass;
    private String salt;
    private Set<Group> groups;
    private Set<Routine> routines;

    public User() {
        id = UUID.randomUUID().toString();
    }

    public User(String uId) {
        id = uId;
    }

	@Id
    @Column(name = User.ID_FIELD_NAME, length = 36)
    @Override
    public String getId() {
        return id;
    }

    @Column(name = User.NAME_FIELD_NAME)
    public String getName() {
        return name;
    }

    @Column(name = User.PASS_FIELD_NAME)
    public String getPass() {
        return pass;
    }

    @Column(name = User.SALT_FIELD_NAME)
    public String getSalt() {
        return salt;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = UGMap.TABLE_NAME,
        joinColumns = @JoinColumn(name = UGMap.USER_ID_FIELD_NAME, referencedColumnName = User.ID_FIELD_NAME),
        inverseJoinColumns = @JoinColumn(name = UGMap.GROUP_ID_FIELD_NAME, referencedColumnName = Group.ID_FIELD_NAME)
    )
    public Set<Group> getGroups() {
        return groups;
    }

    @Transient
    public Set<Routine> getRoutines() {
        //TODO set dependencies
        return routines;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public void setRoutines(Set<Routine> routines) {
        this.routines = routines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (pass != null ? !pass.equals(user.pass) : user.pass != null) return false;
        return salt != null ? salt.equals(user.salt) : user.salt == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (pass != null ? pass.hashCode() : 0);
        result = 31 * result + (salt != null ? salt.hashCode() : 0);
        return result;
    }
}
