package at.enfilo.def.local.simulator;

import at.enfilo.def.execlogic.impl.ExecLogicException;
import at.enfilo.def.execlogic.impl.IExecLogicController;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.UnknownResourceException;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.*;
import javafx.scene.control.TreeItem;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class SimulatorExecLogicController implements IExecLogicController {

	private final Map<String, ProgramDTO> programs;
	private final Map<String, JobDTO> jobs;
	private final Map<String, Set<String>> runningTasks;
	private final Map<String, TaskDTO> tasks;
	private final TreeItem<IdType> root;
	private final Map<String, TreeItem<IdType>> programTreeItems;
	private final Map<String, TreeItem<IdType>> jobTreeItems;
	private final Map<String, TreeItem<IdType>> taskTreeItems;
	private SimulatorConfiguration configuration;
	private ClusterInfoDTO clusterInfo;


	public SimulatorExecLogicController(TreeItem<IdType> root) {
		this.programs = new HashMap<>();
		this.jobs = new HashMap<>();
		this.tasks = new HashMap<>();
		this.runningTasks = new HashMap<>();
		this.root = root;
		this.programTreeItems = new HashMap<>();
		this.jobTreeItems = new HashMap<>();
		this.taskTreeItems = new HashMap<>();
	}

	@Override
	public List<String> getAllPrograms(String userId) throws ExecLogicException {
		return programs.keySet().stream().collect(Collectors.toList());
	}

	@Override
	public String createProgram(String cId, String uId) throws ExecLogicException {
		// Create Cluster
		clusterInfo = new ClusterInfoDTO();
		clusterInfo.setId(cId);
		clusterInfo.setStoreRoutineId(configuration.getStoreRoutine());
		clusterInfo.setDefaultMapRoutineId(configuration.getDefaultMapRoutine());

		root.setValue(new IdType(cId, Type.CLUSTER));
		root.setExpanded(true);

		// Create Program
		String pId = UUID.randomUUID().toString();
		ProgramDTO program = new ProgramDTO();
		program.setId(pId);
		programs.put(pId, program);
		TreeItem<IdType> treeItem = new TreeItem<>(new IdType(pId, Type.PROGRAM));
		treeItem.setExpanded(true);
		programTreeItems.put(pId, treeItem);
		root.getChildren().add(treeItem);
		program.setState(ExecutionState.SCHEDULED);
		program.setUserId(uId);
		program.setCreateTime(Instant.now().toEpochMilli());
		program.setName("Programm");
		return pId;
	}

	@Override
	public ProgramDTO getProgram(String pId) throws ExecLogicException, UnknownProgramException {
		return programs.get(pId);
	}

	@Override
	public void deleteProgram(String pId) throws ExecLogicException, UnknownProgramException {
		programs.remove(pId);
		TreeItem<IdType> treeItem = programTreeItems.remove(pId);
		root.getChildren().removeAll(treeItem);
	}

    @Override
    public void abortProgram(String pId) throws ExecLogicException, UnknownProgramException {
        try {
            ProgramDTO programDTO = programs.get(pId);
            if (programDTO.getState() == ExecutionState.RUN || programDTO.getState() == ExecutionState.SCHEDULED) {
                programDTO.setState(ExecutionState.FAILED);
                if (programDTO.getCreateTime() == 0) {
                	programDTO.setCreateTime(Instant.now().toEpochMilli());
				}
                programDTO.setFinishTime(Instant.now().toEpochMilli());
                List<String> jobIds = getAllJobs(pId);
                for (String jId : jobIds) {
                	abortJob(pId, jId);
                }
            }
        } catch (UnknownJobException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void updateProgramName(String pId, String name) throws ExecLogicException, UnknownProgramException {
		ProgramDTO programDTO = programs.get(pId);
		programDTO.setName(name);
	}

	@Override
	public void updateProgramDescription(String pId, String description) throws ExecLogicException, UnknownProgramException {
		ProgramDTO programDTO = programs.get(pId);
		programDTO.setDescription(description);
	}

	@Override
	public void startClientRoutine(String pId, String crId) throws ExecLogicException, UnknownProgramException {
		// TODO implement
	}

	@Override
	public void markProgramAsFinished(String pId) throws ExecLogicException, UnknownProgramException {
		ProgramDTO program = programs.get(pId);
		if (program != null) {
			program.setFinishTime(System.currentTimeMillis());
		}
	}

	@Override
	public List<String> getAllJobs(String pId) throws ExecLogicException, UnknownProgramException {
		return jobs.values().stream()
				.filter(job -> pId.equals(job.getProgramId()))
				.map(JobDTO::getId)
				.collect(Collectors.toList());
	}

	@Override
	public String createJob(String pId) throws ExecLogicException, UnknownProgramException {
		String jId = UUID.randomUUID().toString();
		JobDTO job = new JobDTO();
		job.setId(jId);
		job.setProgramId(pId);
		job.setMapRoutineId(clusterInfo.getDefaultMapRoutineId());
		job.setReduceRoutineId(UUID.randomUUID().toString());
		jobs.put(jId, job);
		runningTasks.put(jId, Collections.synchronizedSet(new HashSet<>()));
		TreeItem<IdType> treeItem = new TreeItem<>(new IdType(jId, Type.JOB));
		treeItem.setExpanded(true);
		jobTreeItems.put(jId, treeItem);
		programTreeItems.get(pId).getChildren().add(treeItem);
		job.setState(ExecutionState.SCHEDULED);
		job.setCreateTime(Instant.now().toEpochMilli());
		return jId;
	}

	@Override
	public JobDTO getJob(String pId, String jId) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		return jobs.get(jId);
	}

	@Override
	public void deleteJob(String pId, String jId) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		jobs.remove(jId);
		runningTasks.remove(jId);
		TreeItem<IdType> treeItem = jobTreeItems.remove(jId);
		programTreeItems.get(pId).getChildren().removeAll(treeItem);
	}

	@Override
	public String getAttachedMapRoutine(String pId, String jId) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		JobDTO job = jobs.get(jId);
		if (job != null) {
			return job.getMapRoutineId();
		}
		return null;
	}

	@Override
	public void attachMapRoutine(String pId, String jId, String mapRoutineId) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		JobDTO job = jobs.get(jId);
		if (job != null) {
			job.setMapRoutineId(mapRoutineId);
		}
	}

	@Override
	public String getAttachedReduceRoutine(String pId, String jId) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		JobDTO job = jobs.get(jId);
		if (job != null) {
			return job.getReduceRoutineId();
		}
		return null;
	}

	@Override
	public void attachReduceRoutine(String pId, String jId, String reduceRoutineId) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		JobDTO job = jobs.get(jId);
		if (job != null) {
			job.setReduceRoutineId(reduceRoutineId);
		}
	}

	@Override
	public List<String> getAllTasks(String pId, String jId, SortingCriterion sortingCriterion) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		return tasks.values().stream()
				.filter(task -> jId.equals(task.getJobId()))
				.sorted((t1, t2) -> {
					if (sortingCriterion != SortingCriterion.NO_SORTING) {
						return sortTasks(t1, t2, sortingCriterion);
					}
					return 0;
				})
				.map(TaskDTO::getId)
				.collect(Collectors.toList());
	}

	private int sortTasks(TaskDTO t1, TaskDTO t2, SortingCriterion sortingCriterion) {
		switch (sortingCriterion) {
			case CREATION_DATE_FROM_NEWEST:
				return sortByTime(t1.getCreateTime(), t2.getCreateTime());
			case CREATION_DATE_FROM_OLDEST:
				return sortByTime(t2.getCreateTime(), t1.getCreateTime());
			case START_DATE_FROM_NEWEST:
				return sortByTime(t1.getStartTime(), t2.getStartTime());
			case START_DATE_FROM_OLDEST:
				return sortByTime(t2.getStartTime(), t1.getStartTime());
			case FINISH_DATE_FROM_NEWEST:
				return sortByTime(t1.getFinishTime(), t2.getFinishTime());
			case FINISH_DATE_FROM_OLDEST:
				return sortByTime(t2.getFinishTime(), t1.getFinishTime());
			case RUNTIME_FROM_LONGEST:
				return sortByTime(t2.getRuntime(), t1.getRuntime());
			case RUNTIME_FROM_SHORTEST:
				return sortByTime(t1.getRuntime(), t2.getRuntime());
			default:
				return 0;
		}
	}

	private int sortByTime(long firstTime, long secondTime) {
		if (firstTime > secondTime) {
			return -1;
		} else if (firstTime == secondTime) {
			return 0;
		}
		return 1;
	}

	@Override
	public List<String> getAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		return tasks.values().stream()
				.filter(task -> jId.equals(task.getJobId()) && task.getState() == state)
				.sorted((t1, t2) -> sortTasks(t1, t2, sortingCriterion))
				.map(TaskDTO::getId)
				.collect(Collectors.toList());
	}

	@Override
	public synchronized String createTask(String pId, String jId, RoutineInstanceDTO objectiveRoutine) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		// Update job
		JobDTO job = jobs.get(jId);
		if (job == null) {
			throw new UnknownJobException("Job unknown");
		}
		job.setScheduledTasks(job.getScheduledTasks() + 1);

		// Create task
		String tId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();
		task.setId(tId);
		task.setJobId(jId);
		task.setProgramId(pId);
		task.setMapRoutineId(job.getMapRoutineId());
		task.setObjectiveRoutineId(objectiveRoutine.getRoutineId());
		task.setInParameters(objectiveRoutine.getInParameters());
		tasks.put(tId, task);
		runningTasks.get(jId).add(tId);
		task.setState(ExecutionState.SCHEDULED);
		task.setCreateTime(Instant.now().toEpochMilli());

		// Add it to tree
		TreeItem<IdType> treeItem = new TreeItem<>(new IdType(tId, Type.TASK));
		treeItem.setExpanded(true);
		taskTreeItems.put(tId, treeItem);
		jobTreeItems.get(jId).getChildren().add(treeItem);

		return tId;
	}

	@Override
	public TaskDTO getTask(String pId, String jId, String tId) throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException {
		return tasks.get(tId);
	}

	@Override
	public TaskDTO getTaskPartial(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters) throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException {
		return getTask(pId, jId, tId);
	}

	@Override
	public void markJobAsComplete(String pId, String jId) throws ExecLogicException, UnknownProgramException, UnknownJobException {

	}

	@Override
	public void abortJob(String pId, String jId) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		try {
			JobDTO jobDTO = getJob(pId, jId);
			if (jobDTO.getState() == ExecutionState.RUN || jobDTO.getState() == ExecutionState.SCHEDULED) {
				jobDTO.setState(ExecutionState.FAILED);
				if (jobDTO.getStartTime() == 0) {
					jobDTO.setStartTime(Instant.now().toEpochMilli());
				}
				jobDTO.setFinishTime(Instant.now().toEpochMilli());
				List<String> taskIds = new LinkedList<>();
				taskIds.addAll(getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING));
				taskIds.addAll(getAllTasksWithState(pId, jId, ExecutionState.RUN, SortingCriterion.NO_SORTING));
				for (String tId : taskIds) {
					abortTask(pId, jId, tId);
				}
			}
		} catch (UnknownJobException | UnknownTaskException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void abortTask(String pId, String jId, String tId) throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException {
		TaskDTO taskDTO = getTask(pId, jId, tId);
		ExecutionState oldState = taskDTO.getState();
		if (taskDTO.getState() == ExecutionState.RUN || taskDTO.getState() == ExecutionState.SCHEDULED) {
			taskDTO.setState(ExecutionState.FAILED);
			if (taskDTO.getStartTime() == 0) {
				taskDTO.setStartTime(Instant.now().toEpochMilli());
			}
			taskDTO.setFinishTime(Instant.now().toEpochMilli());
			JobDTO jobDTO = getJob(pId, jId);
			switch (oldState) {
				case RUN:
					jobDTO.setRunningTasks(jobDTO.getRunningTasks() - 1);
					break;
				case SCHEDULED:
					jobDTO.setScheduledTasks(jobDTO.getScheduledTasks() - 1);
					break;
			}
			jobDTO.setFailedTasks(jobDTO.getFailedTasks() + 1);
		}
	}

	@Override
	public void reRunTask(String pId, String jId, String tId) throws UnknownProgramException, ExecLogicException {

	}

	@Override
	public List<String> getAllSharedResources(String pId) throws ExecLogicException, UnknownProgramException {
		return null;
	}

	@Override
	public String createSharedResource(String pId, String dataTypeId, ByteBuffer data) throws ExecLogicException, UnknownProgramException {
		return null;
	}

	@Override
	public ResourceDTO getSharedResource(String pId, String rId) throws ExecLogicException, UnknownProgramException, UnknownResourceException {
		return null;
	}

	@Override
	public void deleteSharedResource(String pId, String rId) throws ExecLogicException, UnknownProgramException, UnknownResourceException {

	}

	public SimulatorConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(SimulatorConfiguration configuration) {
		this.configuration = configuration;
	}

	public ClusterInfoDTO getClusterInfo() {
		return clusterInfo;
	}

	public void notifyTaskDone(String jId, String tId) {
		runningTasks.get(jId).remove(tId);
		if (runningTasks.get(jId).isEmpty()) {
			JobDTO job = jobs.get(jId);
			job.setFinishTime(Instant.now().toEpochMilli());
			job.setState(ExecutionState.SUCCESS);
		}
	}
}
