package at.enfilo.def.domain.entity;

import at.enfilo.def.domain.exception.WrongRoutineTypeException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.RoutineType;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

/**
 * Created by mase on 22.08.2016.
 */
@Entity(name = Task.TABLE_NAME)
@Table(name = Task.TABLE_NAME)
public class Task extends AbstractEntity<String> {

    public static final String TABLE_NAME = "def_task";
    public static final String ID_FIELD_NAME = "def_task_id";

    public static final String TYPE_FIELD_NAME = "def_task_type";
    public static final String STATE_FIELD_NAME = "def_task_state";
    public static final String CREATE_TIME_FIELD_NAME = "def_task_create_time";
    public static final String START_TIME_FIELD_NAME = "def_task_start_time";
    public static final String FINISH_TIME_FIELD_NAME = "def_task_finish_time";
	public static final String OBJECTIVE_ROUTINE_FIELD_NAME = "def_objective_routine_od";
	public static final String MAP_ROUTINE_FIELD_NAME = "def_map_routine_id";

    private String id;
    private ExecutionState state;
    private Instant createTime;
    private Instant startTime;
    private Instant finishTime;
	private Routine objectiveRoutine;
	private Routine mapRoutine;
    // TODO: Add persistence
    private Map<String, Resource> inParameters;
	private List<Resource> outParameters;
	private Job job;

    public Task() {
		this.id = UUID.randomUUID().toString();
		this.createTime = Instant.now();
		this.inParameters = new HashMap<>();
		this.outParameters = new LinkedList<>();
    }

	public Task(Routine objectiveRoutine, Routine mapRoutine, Map<String, Resource> inParameters) throws WrongRoutineTypeException {
		this();
		setObjectiveRoutine(objectiveRoutine);
		setMapRoutine(mapRoutine);
		this.objectiveRoutine = objectiveRoutine;
		this.inParameters = inParameters;
	}

	@Id
    @Column(name = Task.ID_FIELD_NAME, length = 36)
    @Override
    public String getId() {
        return id;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = Task.STATE_FIELD_NAME)
    public ExecutionState getState() {
        return state;
    }

    @Column(name = Task.CREATE_TIME_FIELD_NAME)
    public Instant getCreateTime() {
        return createTime;
    }

    @Column(name = Task.START_TIME_FIELD_NAME)
    public Instant getStartTime() {
        return startTime;
    }

    @Column(name = Task.FINISH_TIME_FIELD_NAME)
    public Instant getFinishTime() {
        return finishTime;
    }

	//@OneToOne
	//@JoinColumn(name = OBJECTIVE_ROUTINE_FIELD_NAME)
	@Transient
	public Routine getObjectiveRoutine() {
		return objectiveRoutine;
	}

	//@OneToOne
	//@JoinColumn(name = MAP_ROUTINE_FIELD_NAME)
	@Transient
	public Routine getMapRoutine() {
		return mapRoutine;
	}

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public void setState(ExecutionState newState) {
		this.state = newState;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void setFinishTime(Instant finishTime) {
        this.finishTime = finishTime;
    }


	public void setObjectiveRoutine(Routine objectiveRoutine) throws WrongRoutineTypeException {
		if (objectiveRoutine != null && objectiveRoutine.getType() != RoutineType.OBJECTIVE) {
			throw new WrongRoutineTypeException(
					String.format("Found RoutineType %s instead of %s", mapRoutine.getType(), RoutineType.OBJECTIVE)
			);
		}
    	this.objectiveRoutine = objectiveRoutine;
	}

	@Transient
	public Map<String, Resource> getInParameters() {
		return inParameters;
	}

	public void setInParameters(Map<String, Resource> inParameters) {
		this.inParameters = inParameters;
	}

	@Transient
	public List<Resource> getOutParameters() {
		return outParameters;
	}

	public void setOutParameters(List<Resource> outParameters) {
		this.outParameters = outParameters;
	}

	@Transient
	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}


	public void setMapRoutine(Routine mapRoutine) throws WrongRoutineTypeException {
		if (mapRoutine != null && mapRoutine.getType() != RoutineType.MAP) {
			throw new WrongRoutineTypeException(
					String.format("Found RoutineType %s instead of %s", mapRoutine.getType(), RoutineType.MAP)
			);
		}
		this.mapRoutine = mapRoutine;
	}

	public void cleanUp() {
    	if (inParameters != null) {
			inParameters.values().forEach(Resource::cleanUp);
			inParameters.clear();
			inParameters = null;
		}
		if (outParameters != null) {
			outParameters.forEach(Resource::cleanUp);
			outParameters.clear();
			outParameters = null;
		}
		job = null;
		mapRoutine = null;
		objectiveRoutine = null;
	}

	public void abort() {
		if (state != ExecutionState.SUCCESS && state != ExecutionState.FAILED) {
			finishTime = Instant.now();
			state = ExecutionState.FAILED;
		}
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Task task = (Task) o;

		if (id != null ? !id.equals(task.id) : task.id != null) return false;
		if (state != task.state) return false;
		if (createTime != null ? !createTime.equals(task.createTime) : task.createTime != null) return false;
		if (startTime != null ? !startTime.equals(task.startTime) : task.startTime != null) return false;
		if (finishTime != null ? !finishTime.equals(task.finishTime) : task.finishTime != null) return false;
		if (objectiveRoutine != null ? !objectiveRoutine.equals(task.objectiveRoutine) : task.objectiveRoutine != null)
			return false;
		if (inParameters != null ? !inParameters.equals(task.inParameters) : task.inParameters != null) return false;
		return outParameters != null ? outParameters.equals(task.outParameters) : task.outParameters == null;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (state != null ? state.hashCode() : 0);
		result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
		result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
		result = 31 * result + (finishTime != null ? finishTime.hashCode() : 0);
		result = 31 * result + (objectiveRoutine != null ? objectiveRoutine.hashCode() : 0);
		result = 31 * result + (inParameters != null ? inParameters.hashCode() : 0);
		result = 31 * result + (outParameters != null ? outParameters.hashCode() : 0);
		return result;
	}
}