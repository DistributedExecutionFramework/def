package at.enfilo.def.domain.entity;

import at.enfilo.def.domain.map.PGMap;
import at.enfilo.def.domain.map.UGMap;

import javax.persistence.*;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by mase on 12.08.2016.
 */
@Entity(name = Group.TABLE_NAME)
@Table(name = Group.TABLE_NAME)
public class Group extends AbstractEntity<String> {

    public static final String TABLE_NAME = "def_group";
    public static final String ID_FIELD_NAME = "def_group_id";

    public static final String NAME_FIELD_NAME = "def_group_name";
    public static final String DESCRIPTION_FIELD_NAME = "def_group_description";

    private String id;
    private String name;
    private String description;
    private Collection<User> users;
    private Collection<Program> programs;

    public Group(){
        id = UUID.randomUUID().toString();
    }

    @Id
    @Column(name = Group.ID_FIELD_NAME, length = 36)
    @Override
    public String getId() {
        return id;
    }

    @Column(name = Group.NAME_FIELD_NAME)
    public String getName() {
        return name;
    }

    @Lob
    @Column(name = Group.DESCRIPTION_FIELD_NAME)
    public String getDescription() {
        return description;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = UGMap.TABLE_NAME,
        joinColumns = @JoinColumn(name = UGMap.GROUP_ID_FIELD_NAME, referencedColumnName = Group.ID_FIELD_NAME),
        inverseJoinColumns = @JoinColumn(name = UGMap.USER_ID_FIELD_NAME, referencedColumnName = User.ID_FIELD_NAME)
    )
    public Collection<User> getUsers() {
        return users;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = PGMap.TABLE_NAME,
        joinColumns = @JoinColumn(name = PGMap.GROUP_ID_FIELD_NAME, referencedColumnName = Group.ID_FIELD_NAME),
        inverseJoinColumns = @JoinColumn(name = PGMap.PROGRAM_ID_FIELD_NAME, referencedColumnName = Program.ID_FIELD_NAME)
    )
    public Collection<Program> getPrograms() {
        return programs;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }

    public void setPrograms(Collection<Program> programs) {
        this.programs = programs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        if (id != null ? !id.equals(group.id) : group.id != null) return false;
        if (name != null ? !name.equals(group.name) : group.name != null) return false;
        return description != null ? description.equals(group.description) : group.description == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
