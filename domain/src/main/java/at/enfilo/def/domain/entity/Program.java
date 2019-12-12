package at.enfilo.def.domain.entity;

import at.enfilo.def.domain.map.PGMap;
import at.enfilo.def.domain.map.PJMap;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownResourceException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

/**
 * Created by mase on 12.08.2016.
 */
@Entity(name = Program.TABLE_NAME)
@Table(name = Program.TABLE_NAME)
public class Program extends AbstractEntity<String> {

    public static final String TABLE_NAME = "def_program";
    public static final String ID_FIELD_NAME = "def_program_id";

    public static final String OWNER_FIELD_NAME = "def_program_owner";
    public static final String STATE_FIELD_NAME = "def_program_state";
    public static final String CREATE_TIME_FIELD_NAME = "def_program_create_time";
    public static final String FINISH_TIME_FIELD_NAME = "def_program_finish_time";
    public static final String IS_MASTER_LIBRARY_ROUTINE_FIELD_NAME = "def_program_is_master_library_routine";


	private String id;
	private User owner;
	private String name;
	private String description;
    private ExecutionState state;
    private Instant createTime;
    private Instant finishTime;
    private boolean isMasterLibraryRoutine;
    private Collection<Group> groups;
    private Map<String, Job> jobs;
    // TODO: Persistence
    private Map<String, ResourceDTO> sharedResources;

    public Program() {
        this.id = UUID.randomUUID().toString();
        this.createTime = Instant.now();
        this.groups = new HashSet<>();
        this.jobs = new HashMap<>();
        this.sharedResources = new HashMap<>();
        this.name = "";
        this.description = "";
    }

	@Id
	@Column(name = Program.ID_FIELD_NAME, length = 36)
    @Override
    public String getId() {
        return id;
    }

	@OneToOne
	@JoinColumn(name = Program.OWNER_FIELD_NAME)
    public User getOwner() {
        return owner;
    }

    @Transient
    public String getName() {
        return name;
    }

    @Transient
    public String getDescription() {
        return description;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = Program.STATE_FIELD_NAME)
    public ExecutionState getState() {
        return state;
    }

    @Column(name = Program.CREATE_TIME_FIELD_NAME)
    public Instant getCreateTime() {
        return createTime;
    }

    @Column(name = Program.FINISH_TIME_FIELD_NAME)
    public Instant getFinishTime() {
        return finishTime;
    }

    @Column(name = Program.IS_MASTER_LIBRARY_ROUTINE_FIELD_NAME, columnDefinition = "BOOLEAN")
    public boolean isMasterLibraryRoutine() {
        return isMasterLibraryRoutine;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = PGMap.TABLE_NAME,
        joinColumns = @JoinColumn(name = PGMap.PROGRAM_ID_FIELD_NAME, referencedColumnName = Program.ID_FIELD_NAME),
        inverseJoinColumns = @JoinColumn(name = PGMap.GROUP_ID_FIELD_NAME, referencedColumnName = Group.ID_FIELD_NAME)
    )
    public Collection<Group> getGroups() {
        return groups;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = PJMap.TABLE_NAME,
        joinColumns = @JoinColumn(name = PJMap.PROGRAM_ID_FIELD_NAME, referencedColumnName = Program.ID_FIELD_NAME),
        inverseJoinColumns = @JoinColumn(name = PJMap.JOB_ID_FIELD_NAME, referencedColumnName = Job.ID_FIELD_NAME)
    )
    public Collection<Job> getJobs() {
        return jobs.values();
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setState(ExecutionState state) {
        this.state = state;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public void setFinishTime(Instant finishTime) {
        this.finishTime = finishTime;
    }

    public void setMasterLibraryRoutine(boolean masterLibraryRoutine) {
        isMasterLibraryRoutine = masterLibraryRoutine;
    }

    public void setGroups(Collection<Group> groups) {
        this.groups = groups;
    }

    public void setJobs(Collection<Job> jobs) {
        this.jobs.clear();
        jobs.forEach(j -> this.jobs.put(j.getId(), j));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Program program = (Program) o;

        if (isMasterLibraryRoutine != program.isMasterLibraryRoutine) return false;
        if (id != null ? !id.equals(program.id) : program.id != null) return false;
        if (owner != null ? !owner.equals(program.owner) : program.owner != null) return false;
        if (name != null ? !name.equals(program.name) : program.name != null) return false;
        if (description != null ? !description.equals(program.description) : program.description != null) return false;
        if (state != program.state) return false;
        if (createTime != null ? !createTime.equals(program.createTime) : program.createTime != null) return false;
        return finishTime != null ? finishTime.equals(program.finishTime) : program.finishTime == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode(): 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (finishTime != null ? finishTime.hashCode() : 0);
        result = 31 * result + (isMasterLibraryRoutine ? 1 : 0);
        return result;
    }

	/**
	 * Clean up program
	 */
	public void cleanUp() {
		// Clean all Jobs
		jobs.values().forEach(Job::cleanUp);
		jobs.clear();
		// TODO: Clean all SharedResources
		//sharedResources.values().forEach(Resource::cleanUp);
		sharedResources.clear();
	}

	/**
	 * Returns true if Program is marked as finished.
	 *
	 * @return
	 */
	@Transient
	public boolean isFinished() {
		return finishTime != null;
	}

	/**
	 * Mark Program as finished.
	 */
	public void markAsFinished() {
		finishTime = Instant.now();
		if (state == ExecutionState.RUN || state == ExecutionState.SCHEDULED) {
			state = ExecutionState.SUCCESS;
		}
	}

	/**
	 * Notify about a new job state.
	 * @param job
	 * @param oldState
	 * @param newState
	 */
	public void notifyJobStateChange(Job job, ExecutionState oldState, ExecutionState newState) {
		if (state == ExecutionState.SCHEDULED && newState == ExecutionState.RUN) {
			state = ExecutionState.RUN;
		}
	}

	/**
	 * Adds a job to this program
	 * @param job - job to add
	 */
	public void addJob(Job job) {
		jobs.put(job.getId(), job);
		job.setProgram(this);
	}

	/**
	 * Return job by given id.
	 * @param jId - job id
	 * @return job object
	 * @throws UnknownJobException
	 */
	public Job getJobById(String jId) throws UnknownJobException {
		if (!jobs.containsKey(jId)) {
			throw new UnknownJobException("Job " + jId + " not exists in Program " + id);
		}
		return jobs.get(jId);
	}

	/**
	 * Delete job from program
	 * @param job
	 */
	public void deleteJob(Job job) {
		jobs.remove(job.getId());
	}

	@Transient
	public Collection<ResourceDTO> getSharedResources() {
		return sharedResources.values();
	}

	public void addSharedResource(ResourceDTO sharedResource) {
		sharedResources.put(sharedResource.getId(), sharedResource);
	}

	public ResourceDTO getSharedResourceById(String rId) throws UnknownResourceException {
		if (sharedResources.containsKey(rId)) {
			return sharedResources.get(rId);
		}
		throw new UnknownResourceException("SharedResource with id " + rId + " not known in Program " + id);
	}

	public void deleteSharedResource(String rId) {
		sharedResources.remove(rId);
	}

    public void abort() {
        if (state != ExecutionState.SUCCESS && state != ExecutionState.FAILED) {
            finishTime = Instant.now();
            state = ExecutionState.FAILED;
            jobs.values().forEach(Job::abort);
        }
    }
}
