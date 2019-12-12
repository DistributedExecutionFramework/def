package at.enfilo.def.domain.entity;

import at.enfilo.def.domain.map.RFMap;
import at.enfilo.def.domain.map.RFPMap;
import at.enfilo.def.domain.map.RRBMap;
import at.enfilo.def.transfer.dto.Language;
import at.enfilo.def.transfer.dto.RoutineType;

import javax.persistence.*;
import java.util.*;

/**
 * Created by mase on 12.08.2016.
 */
@Entity(name = Routine.TABLE_NAME)
@Table(name = Routine.TABLE_NAME)
public class Routine extends AbstractEntity<String> {

    public static final String TABLE_NAME = "def_routine";
    public static final String ID_FIELD_NAME = "def_routine_id";

    public static final String PREDECESSOR_FIELD_NAME = "def_routine_predecessor_id";
    public static final String IS_PRIVATE_FIELD_NAME = "def_routine_is_private";
    public static final String ROUTINE_FIELD_NAME = "def_routine_name";
    public static final String DESCRIPTION_FIELD_NAME = "def_routine_description";
    public static final String REVISION_FIELD_NAME = "def_routine_revision";
    public static final String TYPE_FIELD_NAME = "def_routine_type";
    public static final String LANGUAGE_FIELD_NAME = "def_routine_language";
    public static final String OUT_PARAMETER_FIELD_NAME = "def_routine_out_parameter";
    public static final String ARGUMENTS_FIELD_NAME = "def_routine_arguments";

    private String id;
    private Routine predecessor;
    private boolean isPrivateRoutine;
    private String name;
    private String description;
    private short revision;
    private RoutineType type;
    private Language language;
    private List<FormalParameter> inParameters;
    private FormalParameter outParameter;
    private List<RoutineBinary> routineBinaries;
    private List<String> arguments;
    private Set<Feature> requiredFeatures;

    public Routine() {
        id = UUID.randomUUID().toString();
    }

    @Id
    @Column(name = Routine.ID_FIELD_NAME, length = 36)
    @Override
    public String getId() {
        return id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Routine.PREDECESSOR_FIELD_NAME)
    public Routine getPredecessor() {
        return predecessor;
    }

    @Column(name = Routine.IS_PRIVATE_FIELD_NAME, columnDefinition = "BOOLEAN")
    public boolean isPrivateRoutine() {
        return isPrivateRoutine;
    }

    @Column(name = Routine.ROUTINE_FIELD_NAME)
    public String getName() {
        return name;
    }

    @Lob
    @Column(name = Routine.DESCRIPTION_FIELD_NAME)
    public String getDescription() {
        return description;
    }

    @Column(name = Routine.REVISION_FIELD_NAME)
    public short getRevision() {
        return revision;
    }

	@Enumerated(EnumType.STRING)
	@Column(name = Routine.TYPE_FIELD_NAME)
	public RoutineType getType() {
		return type;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = Routine.LANGUAGE_FIELD_NAME)
	public Language getLanguage() {
		return language;
	}

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = RFPMap.TABLE_NAME,
        joinColumns = @JoinColumn(name = RFPMap.ROUTINE_ID_FIELD_NAME, referencedColumnName = Routine.ID_FIELD_NAME),
        inverseJoinColumns = @JoinColumn(name = RFPMap.FORMAL_PARAMETER_ID_FIELD_NAME, referencedColumnName = FormalParameter.ID_FIELD_NAME)
    )
    public List<FormalParameter> getInParameters() {
        return inParameters;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = Routine.OUT_PARAMETER_FIELD_NAME)
	public FormalParameter getOutParameter() {
		return outParameter;
	}

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(
		name = RRBMap.TABLE_NAME,
		joinColumns = @JoinColumn(name = RRBMap.ROUTINE_ID_FIELD_NAME, referencedColumnName = Routine.ID_FIELD_NAME),
		inverseJoinColumns = @JoinColumn(name = RRBMap.ROUTINE_BINARY_ID_FIELD_NAME, referencedColumnName = RoutineBinary.ID_FIELD_NAME)
	)
	public List<RoutineBinary> getRoutineBinaries() {
		return routineBinaries;
	}

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = RFMap.TABLE_NAME,
			joinColumns = @JoinColumn(name = RFMap.ROUTINE_ID_FIELD_NAME, referencedColumnName = Routine.ID_FIELD_NAME),
			inverseJoinColumns = @JoinColumn(name = RFMap.FEATURE_ID_FIELD_NAME, referencedColumnName = Feature.ID_FIELD_NAME)
	)
	public Set<Feature> getRequiredFeatures() {
		return requiredFeatures;
	}

	@ElementCollection
	@Column(name = ARGUMENTS_FIELD_NAME)
	public List<String> getArguments() {
		return arguments;
	}

	@Override
    public void setId(String id) {
        this.id = id;
    }

    public void setPredecessor(Routine predecessor) {
        this.predecessor = predecessor;
    }

    public void setPrivateRoutine(boolean privateRoutine) {
        isPrivateRoutine = privateRoutine;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRevision(short revision) {
        this.revision = revision;
    }

	public void setType(RoutineType type) {
		this.type = type;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public void setInParameters(List<FormalParameter> inParameters) {
        this.inParameters = inParameters;
    }

	public void setOutParameter(FormalParameter outParameter) {
		this.outParameter = outParameter;
	}

	public void setRoutineBinaries(List<RoutineBinary> routineBinaries) {
		this.routineBinaries = routineBinaries;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	public void setRequiredFeatures(Set<Feature> features) {
    	this.requiredFeatures = features;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Routine routine = (Routine) o;
		return isPrivateRoutine == routine.isPrivateRoutine &&
				revision == routine.revision &&
				Objects.equals(id, routine.id) &&
				Objects.equals(predecessor, routine.predecessor) &&
				Objects.equals(name, routine.name) &&
				Objects.equals(description, routine.description) &&
				type == routine.type &&
				language == routine.language &&
				Objects.equals(inParameters, routine.inParameters) &&
				Objects.equals(outParameter, routine.outParameter) &&
				Objects.equals(routineBinaries, routine.routineBinaries) &&
				Objects.equals(arguments, routine.arguments) &&
				Objects.equals(requiredFeatures, routine.requiredFeatures);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, predecessor, isPrivateRoutine, name, description, revision, type, language, inParameters, outParameter, routineBinaries, arguments, requiredFeatures);
	}
}
