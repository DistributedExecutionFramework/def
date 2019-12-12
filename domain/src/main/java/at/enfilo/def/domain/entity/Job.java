package at.enfilo.def.domain.entity;

import at.enfilo.def.domain.exception.JobCompletedException;
import at.enfilo.def.domain.exception.WrongRoutineTypeException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.RoutineType;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static at.enfilo.def.transfer.dto.ExecutionState.*;

/**
 * Created by mase on 12.08.2016.
 */
@Entity(name = Job.TABLE_NAME)
@Table(name = Job.TABLE_NAME)
public class Job extends AbstractEntity<String> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Job.class);

    public static final String TABLE_NAME = "def_job";
    public static final String ID_FIELD_NAME = "def_job_id";

    public static final String STATE_FIELD_NAME = "def_job_state";
    public static final String CREATE_TIME_FIELD_NAME = "def_job_create_time";
    public static final String FINISH_TIME_FIELD_NAME = "def_job_finish_time";

	private transient final Object tasksLock;

    private String id;
    private ExecutionState state;
    private Instant createTime;
	private Instant startTime;
    private Instant finishTime;
	//private Map<String, Task> tasks;
    // TODO: create persistence
	private Set<String> scheduledTasks;
	private Set<String> runningTasks;
	private Set<String> successfulTasks;
	private Set<String> failedTasks;
	private Routine mapRoutine;
	private Routine reduceRoutine;
	private boolean complete;
	private Program program;
	private List<ResourceDTO> reducedResults;

	public Job() {
        this.id = UUID.randomUUID().toString();
        this.scheduledTasks = new HashSet<>();
		this.runningTasks = new HashSet<>();
		this.successfulTasks = new HashSet<>();
		this.failedTasks = new HashSet<>();
        this.createTime = Instant.now();
        this.tasksLock = new Object();
        //this.tasks = new HashMap<>();
    }

	@Id
    @Column(name = Job.ID_FIELD_NAME, length = 36)
    @Override
    public String getId() {
        return id;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = Job.STATE_FIELD_NAME)
    public ExecutionState getState() {
        return state;
    }

    @Column(name = Job.CREATE_TIME_FIELD_NAME)
    public Instant getCreateTime() {
        return createTime;
    }

    @Column(name = Job.FINISH_TIME_FIELD_NAME)
    public Instant getFinishTime() {
        return finishTime;
    }

//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinTable(
//        name = JTMap.TABLE_NAME,
//        joinColumns = @JoinColumn(name = JTMap.JOB_ID_FIELD_NAME, referencedColumnName = Job.ID_FIELD_NAME),
//        inverseJoinColumns = @JoinColumn(name = JTMap.TASK_ID_FIELD_NAME, referencedColumnName = Task.ID_FIELD_NAME)
//    )
//    public Collection<Task> getTasks() {
//    	return this.tasks.values();
//	}

	@Transient
    public Collection<String> getScheduledTasks() {
    	synchronized (tasksLock) {
			return scheduledTasks.stream().collect(Collectors.toList());
		}
    }

	@Transient
	public Collection<String> getRunningTasks() {
		synchronized (tasksLock) {
			return runningTasks.stream().collect(Collectors.toList());
		}
	}

	@Transient
	public Collection<String> getFailedTasks() {
		synchronized (tasksLock) {
			return failedTasks.stream().collect(Collectors.toList());
		}
	}

	@Transient
	public Collection<String> getSuccessfulTasks() {
		synchronized (tasksLock) {
			return successfulTasks.stream().collect(Collectors.toList());
		}
	}

	@Transient
	public Routine getMapRoutine() {
		return mapRoutine;
	}

	@Transient
	public boolean hasMapRoutine() {
		return mapRoutine != null;
	}

	@Transient
	public boolean hasReduceRoutine() {
		return reduceRoutine != null;
	}

	@Transient
	public Routine getReduceRoutine() {
		return reduceRoutine;
	}

	@Transient
	public boolean isComplete() {
		return complete;
	}

	@Transient
	public Program getProgram() {
		return program;
	}

	@Transient
	public Instant getStartTime() {
		return startTime;
	}

	@Transient
	public List<ResourceDTO> getReducedResults() {
		return reducedResults;
	}

	@Override
    public void setId(String id) {
        this.id = id;
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


	public void setMapRoutine(Routine mapRoutine) throws WrongRoutineTypeException {
		if (mapRoutine.getType() != RoutineType.MAP) {
			throw new WrongRoutineTypeException(String.format("Routine Type must be %s - given: %s.", RoutineType.MAP, mapRoutine.getType()));
		}
		this.mapRoutine = mapRoutine;
	}


	public void setReduceRoutine(Routine reduceRoutine) throws WrongRoutineTypeException {
		if (reduceRoutine.getType() != RoutineType.REDUCE) {
			String msg = String.format("Routine Type must be %s - given: %s.", RoutineType.REDUCE, mapRoutine.getType());
			LOGGER.error(DEFLoggerFactory.createJobContext(program.getId(), id), msg);
			throw new WrongRoutineTypeException(msg);
		}
		this.reduceRoutine = reduceRoutine;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
		synchronized (tasksLock) {
			if (complete && scheduledTasks.isEmpty() && runningTasks.isEmpty() && failedTasks.isEmpty()) {
				state = ExecutionState.SUCCESS;
				LOGGER.info(DEFLoggerFactory.createJobContext(program.getId(), id), "All Tasks finished successfully, Job done.");
			}
		}
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Job)) return false;

        Job job = (Job) o;

        if (getId() != null ? !getId().equals(job.getId()) : job.getId() != null) return false;
        if (getState() != job.getState()) return false;
        if (getCreateTime() != null ? !getCreateTime().equals(job.getCreateTime()) : job.getCreateTime() != null) return false;
        return getFinishTime() != null ? getFinishTime().equals(job.getFinishTime()) : job.getFinishTime() == null;
    }


    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getState() != null ? getState().hashCode() : 0);
        result = 31 * result + (getCreateTime() != null ? getCreateTime().hashCode() : 0);
        result = 31 * result + (getFinishTime() != null ? getFinishTime().hashCode() : 0);
        return result;
    }


	public void cleanUp() {
		synchronized (tasksLock) {
			scheduledTasks.clear();
			runningTasks.clear();
			successfulTasks.clear();
			failedTasks.clear();
		}
		createTime = null;
    	finishTime = null;
    	startTime = null;
    	mapRoutine = null;
    	reduceRoutine = null;
    	state = null;
    	program = null;
    	//tasks.clear();
    	//tasks = null;
	}


	public void addTask(String tId) throws JobCompletedException {
		if (isComplete()) {
			throw new JobCompletedException("Job is marked as complete, no tasks can be added.");
		}
		synchronized (tasksLock) {
			scheduledTasks.add(tId);
		}
	}


	public void markAsComplete() {
		setComplete(true);
	}


	public void abort() {
		if (state != ExecutionState.SUCCESS && state != ExecutionState.FAILED) {
			finishTime = Instant.now();
			state = FAILED;
			synchronized (tasksLock) {
				failedTasks.addAll(scheduledTasks);
				scheduledTasks.clear();
			}
		}
	}

	public void notifyTaskChangedState(String tId, ExecutionState oldState, ExecutionState newState) {
		if ((oldState == SUCCESS || oldState == FAILED) && newState == RUN) {
			// Ignoring this state change - it should be not possible (notify of run was too slow)
    		LOGGER.info(DEFLoggerFactory.createJobContext(program.getId(), id), "Ignoring state change from {} to {}. Not possible.", oldState, newState);
    		return;
		}
		LOGGER.debug(DEFLoggerFactory.createTaskContext(program.getId(), id, tId), "Notify Task changed state from {} to {}", oldState, newState);
		synchronized (tasksLock) {
			Set<String> dst = null;
			switch (newState) {
				case SCHEDULED:
					dst = scheduledTasks;
					break;
				case RUN:
					dst = runningTasks;
					break;
				case SUCCESS:
					dst = successfulTasks;
					break;
				case FAILED:
					dst = failedTasks;
					break;
			}
			if (dst != null) {
				// Remove from all collections, regardless of oldState
				scheduledTasks.remove(tId);
				runningTasks.remove(tId);
				successfulTasks.remove(tId);
				failedTasks.remove(tId);
				dst.add(tId);
			}
		}
		if (state == SCHEDULED && newState == RUN) {
    		// First task switched to run --> job is also in state run
			LOGGER.debug(DEFLoggerFactory.createTaskContext(program.getId(), id, tId), "Notify Program that Job state changed from {} to {}", state, newState);
			state = RUN;
			startTime = Instant.now();
			program.notifyJobStateChange(this, SCHEDULED, state);
			LOGGER.debug(DEFLoggerFactory.createJobContext(program.getId(), id), "Changed Job state to {}.", RUN);
		}
		if (!hasReduceRoutine() && allTasksFinished()) {
			finishTime = Instant.now();
    		if (allTasksSuccessful()) {
				LOGGER.info(DEFLoggerFactory.createJobContext(program.getId(), id), "All Tasks finished successfully, Job done.");
				state = ExecutionState.SUCCESS;
			} else {
				LOGGER.warn(DEFLoggerFactory.createJobContext(program.getId(), id), "All Tasks finished, but at least one with failed. Job done.");
				state = ExecutionState.FAILED;
			}
		}
	}

//	public void setTasks(Collection<Task> tasks) {
//		for (Task t : tasks) {
//			this.tasks.put(t.getId(), t);
//		}
//	}

	public void removeTask(String tId) {
		synchronized (tasksLock) {
			scheduledTasks.remove(tId);
			runningTasks.remove(tId);
			successfulTasks.remove(tId);
			failedTasks.remove(tId);
		}
	}

	@Transient
	public int getNumberOfTasks() {
		synchronized (tasksLock) {
			int tasks = 0;
			tasks += scheduledTasks.size();
			tasks += runningTasks.size();
			tasks += successfulTasks.size();
			tasks += failedTasks.size();
			return tasks;
		}
	}

	@Transient
	public Collection<String> getAllTasks() {
		synchronized (tasksLock) {
			List<String> taskIds = new LinkedList<>();
			taskIds.addAll(scheduledTasks);
			taskIds.addAll(runningTasks);
			taskIds.addAll(successfulTasks);
			taskIds.addAll(failedTasks);
			return taskIds;
		}
	}

	@Transient
	public boolean containsTask(String tId) {
    	synchronized (tasksLock) {
			if (scheduledTasks.contains(tId)) {
				return true;
			}
			if (runningTasks.contains(tId)) {
				return true;
			}
			if (successfulTasks.contains(tId)) {
				return true;
			}
			if (failedTasks.contains(tId)) {
				return true;
			}
		}
		return false;
	}

	@Transient
	public boolean allTasksSuccessful() {
    	synchronized (tasksLock) {
			return complete && scheduledTasks.isEmpty() && runningTasks.isEmpty() && failedTasks.isEmpty();
		}
	}

	@Transient
	public boolean allTasksFinished() {
		synchronized (tasksLock) {
			return complete && scheduledTasks.isEmpty() && runningTasks.isEmpty();
		}
	}

	@Transient
	public Object getTasksLock() {
		return tasksLock;
	}

	public void setReducedResults(List<ResourceDTO> reducedResults) {
		this.reducedResults = reducedResults;
		this.state = SUCCESS;
		this.finishTime = Instant.now();
	}
}
