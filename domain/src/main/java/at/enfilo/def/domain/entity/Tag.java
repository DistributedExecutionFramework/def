package at.enfilo.def.domain.entity;

import javax.persistence.*;

/**
 * Created by mase on 12.08.2016.
 */
@Entity(name = Tag.TABLE_NAME)
@Table(name = Tag.TABLE_NAME)
public class Tag extends AbstractEntity<String> {

    public static final String TABLE_NAME = "def_tag";
    public static final String ID_FIELD_NAME = "def_tag_label";

    public static final String DESCRIPTION_FIELD_NAME = "def_tag_description";

    private String label;
    private String description;

    public Tag() {
    }

    @Id
    @Column(name = Tag.ID_FIELD_NAME, length = 36)
    @Override
    public String getId() {
        return label;
    }

    @Lob
    @Column(name = Tag.DESCRIPTION_FIELD_NAME)
    public String getDescription() {
        return description;
    }

    @Override
    public void setId(String label) {
        this.label = label;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;

        Tag tag = (Tag) o;

        if (label != null ? !label.equals(tag.label) : tag.label != null) return false;
        return getDescription() != null ? getDescription().equals(tag.getDescription()) : tag.getDescription() == null;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        return result;
    }
}
