package at.enfilo.def.cluster.impl;

import at.enfilo.def.domain.exception.WrongRoutineTypeException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.IDTOConvertable;
import at.enfilo.def.transfer.dto.*;

import java.time.Instant;
import java.util.*;

import static at.enfilo.def.transfer.dto.ExecutionState.*;

class Job implements IDTOConvertable<JobDTO> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Job.class);

	private final Object tasksLock;
    private final String id;
	private final Program program;
    private final Instant createTime;
	private final Set<String> scheduledTasks;
	private final Set<String> runningTasks;
	private final Set<String> successfulTasks;
	private final Set<String> failedTasks;
	private ExecutionState state;
	private Instant startTime;
	private Instant finishTime;
	private RoutineDTO mapRoutine;
	private RoutineDTO reduceRoutine;
	private boolean complete;
	private boolean aborted;
	private List<ResourceDTO> reducedResults;

	public Job(Program program) {
        this.id = UUID.randomUUID().toString();
        this.program = program;
        this.program.addJob(this);
        this.scheduledTasks = new HashSet<>();
		this.runningTasks = new HashSet<>();
		this.successfulTasks = new HashSet<>();
		this.failedTasks = new HashSet<>();
        this.createTime = Instant.now();
        this.tasksLock = new Object();
    }

    public String getId() {
        return id;
    }

    public ExecutionState getState() {
        return state;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Instant getFinishTime() {
        return finishTime;
    }

    public Collection<String> getScheduledTasks() {
    	synchronized (tasksLock) {
			return new HashSet<>(scheduledTasks);
		}
    }

    public int getNumberOfScheduledTasks() {
		synchronized (tasksLock) {
			return scheduledTasks.size();
		}
	}

	public Collection<String> getRunningTasks() {
		synchronized (tasksLock) {
			return new HashSet<>(runningTasks);
		}
	}

	public int getNumberOfRunningTasks() {
		synchronized (tasksLock) {
			return runningTasks.size();
		}
	}

	public Collection<String> getFailedTasks() {
		synchronized (tasksLock) {
			return new HashSet<>(failedTasks);
		}
	}

	public int getNumberOfFailedTasks() {
		synchronized (tasksLock) {
			return failedTasks.size();
		}
	}

	public Collection<String> getSuccessfulTasks() {
		synchronized (tasksLock) {
			return new ArrayList<>(successfulTasks);
		}
	}

	public int getNumberOfSuccessfulTasks() {
		synchronized (tasksLock) {
			return successfulTasks.size();
		}
	}

	public RoutineDTO getMapRoutine() {
		return mapRoutine;
	}

	public boolean hasMapRoutine() {
		return mapRoutine != null;
	}

	public boolean hasReduceRoutine() {
		return reduceRoutine != null;
	}

	public RoutineDTO getReduceRoutine() {
		return reduceRoutine;
	}

	public boolean isComplete() {
		return complete;
	}

	public Program getProgram() {
		return program;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public List<ResourceDTO> getReducedResults() {
		return reducedResults;
	}

    public void setState(ExecutionState state) {
        this.state = state;
    }

    public void setFinishTime(Instant finishTime) {
        this.finishTime = finishTime;
    }

	public void setMapRoutine(RoutineDTO mapRoutine) throws WrongRoutineTypeException {
		if (mapRoutine.getType() != RoutineType.MAP) {
			throw new WrongRoutineTypeException(String.format("Routine Type must be %s - given: %s.", RoutineType.MAP, mapRoutine.getType()));
		}
		this.mapRoutine = mapRoutine;
	}

	public void setReduceRoutine(RoutineDTO reduceRoutine) throws WrongRoutineTypeException {
		if (reduceRoutine.getType() != RoutineType.REDUCE) {
			String msg = String.format("Routine Type must be %s - given: %s.", RoutineType.REDUCE, reduceRoutine.getType());
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

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public void cleanUp() {
		synchronized (tasksLock) {
			scheduledTasks.clear();
			runningTasks.clear();
			successfulTasks.clear();
			failedTasks.clear();
		}
    	finishTime = null;
    	startTime = null;
    	mapRoutine = null;
    	reduceRoutine = null;
    	state = null;
	}

	public void addTask(TaskDTO task) throws JobCompletedException {
		if (isComplete()) {
			throw new JobCompletedException("Job is marked as complete, no tasks can be added.");
		}
		if (task.getState() == null) {
			task.setState(SCHEDULED);
		}
		changeTaskState(task.getId(), task.getState());
		task.setJobId(id);
		task.setProgramId(program.getId());
	}

	public void markAsComplete() {
		setComplete(true);
	}


	public void abort() {
		if (!aborted && state != ExecutionState.SUCCESS && state != ExecutionState.FAILED) {
			aborted = true;
			finishTime = Instant.now();
			state = FAILED;
			synchronized (tasksLock) {
				failedTasks.addAll(scheduledTasks);
				scheduledTasks.clear();
			}
		}
	}

	public void notifyTaskChangedState(String tId, ExecutionState newState) {
		if (aborted) {
			LOGGER.info(DEFLoggerFactory.createTaskContext(program.getId(), id, tId), "Ignoring state change, job already aborted.");
			return;
		}

		changeTaskState(tId, newState);

		if (state == SCHEDULED && newState == RUN) {
    		// First task switched to run --> job is also in state run
			LOGGER.debug(DEFLoggerFactory.createTaskContext(program.getId(), id, tId), "Notify Program that Job state changed from {} to {}", state, newState);
			state = RUN;
			startTime = Instant.now();
			program.notifyJobStateChange(this, SCHEDULED, state);
			LOGGER.info(DEFLoggerFactory.createJobContext(program.getId(), id), "Changed Job state to {}.", RUN);
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

	private void changeTaskState(String tId, ExecutionState newState) {
		synchronized (tasksLock) {
			Set<String> dst;
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
				default:
					throw new IllegalStateException("Unknown task state: " + newState);
			}
			// Remove from all collections, regardless of oldState
			scheduledTasks.remove(tId);
			runningTasks.remove(tId);
			successfulTasks.remove(tId);
			failedTasks.remove(tId);
			// Add task to new collection
			dst.add(tId);
			LOGGER.debug(
					DEFLoggerFactory.createTaskContext(program.getId(), id, tId),
					"Changed task state to {}. (Scheduled={}, Running={}, Success={}, Failed={})",
					newState,
					scheduledTasks.size(),
					runningTasks.size(),
					successfulTasks.size(),
					failedTasks.size()
			);
		}
	}

	public void removeTask(String tId) {
		synchronized (tasksLock) {
			scheduledTasks.remove(tId);
			runningTasks.remove(tId);
			successfulTasks.remove(tId);
			failedTasks.remove(tId);
		}
	}

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

	public Collection<String> getAllTasks() {
		synchronized (tasksLock) {
			Set<String> taskIds = new HashSet<>();
			taskIds.addAll(scheduledTasks);
			taskIds.addAll(runningTasks);
			taskIds.addAll(successfulTasks);
			taskIds.addAll(failedTasks);
			return taskIds;
		}
	}

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

	public boolean allTasksSuccessful() {
    	synchronized (tasksLock) {
			return complete && scheduledTasks.isEmpty() && runningTasks.isEmpty() && failedTasks.isEmpty();
		}
	}

	public boolean allTasksFinished() {
		synchronized (tasksLock) {
			return complete && scheduledTasks.isEmpty() && runningTasks.isEmpty();
		}
	}

	public void setReducedResults(List<ResourceDTO> reducedResults) {
		this.reducedResults = reducedResults;
		this.state = SUCCESS;
		this.finishTime = Instant.now();
	}

	public ExecutionState getTaskState(String tId) {
		synchronized (tasksLock) {
			if (scheduledTasks.contains(tId)) {
				return SCHEDULED;
			}
			if (runningTasks.contains(tId)) {
				return RUN;
			}
			if (successfulTasks.contains(tId)) {
				return SUCCESS;
			}
			if (failedTasks.contains(tId)) {
				return FAILED;
			}
		}
		return FAILED;
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	@Override
	public JobDTO toDTO() {
		JobDTO dto = new JobDTO();
		dto.setId(id);
		dto.setProgramId(program.getId());
		dto.setState(state);
		dto.setCreateTime(createTime.toEpochMilli());
		if (startTime != null) {
			dto.setStartTime(startTime.toEpochMilli());
		}
		if (finishTime != null) {
			dto.setFinishTime(finishTime.toEpochMilli());
		}
		synchronized (tasksLock) {
			dto.setScheduledTasks(scheduledTasks.size());
			dto.setRunningTasks(runningTasks.size());
			dto.setSuccessfulTasks(successfulTasks.size());
			dto.setFailedTasks(failedTasks.size());
		}
		if (mapRoutine != null) {
			dto.setMapRoutineId(mapRoutine.getId());
		}
		if (reduceRoutine != null) {
			dto.setReduceRoutineId(reduceRoutine.getId());
			dto.setReducedResults(reducedResults);
		}
		return dto;
	}
}
