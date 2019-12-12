package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.util.configuration.ClusterConfiguration;
import at.enfilo.def.domain.exception.WrongRoutineTypeException;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.execlogic.impl.ExecLogicException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.UnknownResourceException;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.transfer.util.ResourceUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static at.enfilo.def.transfer.dto.ExecutionState.*;

public class DomainController {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(DomainController.class);
    private static final String UNKNOWN_PROGRAM = "Program with id %s not known by this cluster";

    public static final String DTO_TASK_CACHE_CONTEXT = "cluster-tasks";

    private final Map<String, Program> programMap;
    private final DTOCache<TaskDTO> taskCache;
    private final ClusterConfiguration configuration;

    private final Object lock;

    public DomainController(ClusterConfiguration configuration) {
        this.programMap = new HashMap<>();
        this.taskCache = DTOCache.getInstance(DTO_TASK_CACHE_CONTEXT, TaskDTO.class);
        this.configuration = configuration;
        this.lock = new Object();
    }

    private DomainController(
            ClusterConfiguration configuration,
            Map<String, Program> programMap,
            DTOCache<TaskDTO> taskCache
    ) {
        this.programMap = programMap;
        this.taskCache = taskCache;
        this.configuration = configuration;
        this.lock = new Object();
    }

    /********************************************************************************
     * PROGRAM METHODS
     *******************************************************************************/

    public List<String> getAllPrograms(String userId) {
        LOGGER.debug("Fetching all program ids with userId {}.", userId);
        synchronized (lock) {
            return programMap.values().stream()
                    .filter(program -> program.getUserId().toLowerCase().matches(userId.toLowerCase()))
                    .map(Program::getId)
                    .collect(Collectors.toList());
        }
    }

    public List<String> getAllProgramIds() {
        LOGGER.debug("Fetching all programs ids.");
        synchronized (lock) {
            return programMap.values()
                    .stream()
                    .map(Program::getId)
                    .collect(Collectors.toList());
        }
    }

    public Collection<ProgramDTO> getAllPrograms() {
        LOGGER.debug("Fetching all programs.");

        Collection<Program> programs = null;
        synchronized (lock) {
            programs = programMap.values();
        }

        return programs
                .stream()
                .map(Program::toDTO)
                .collect(Collectors.toList());
    }

    protected Program getProgramById(String pId) throws UnknownProgramException {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching program with id {}.", pId);
        synchronized (lock) {
            if(!programMap.containsKey(pId)) {
                LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Program id {} is not contained in program map.", pId);
                throw new UnknownProgramException(String.format(UNKNOWN_PROGRAM, pId));
            }
            return programMap.get(pId);
        }
    }

    public String createProgram(String uId) {
        Program program = new Program(uId);
        program.setState(ExecutionState.SCHEDULED);
        synchronized (lock) {
            programMap.put(program.getId(), program);
        }
        LOGGER.debug(DEFLoggerFactory.createProgramContext(program.getId()), "Program with id {} successfully created.", program.getId());
        return program.getId();
    }

    public ProgramDTO getProgram(String pId)
    throws UnknownProgramException {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching programDTO.");
        return getProgramById(pId).toDTO();
    }

    public void setClientRoutineId(String pId, String crId)
    throws UnknownProgramException {
        Program program = getProgramById(pId);
        program.setClientRoutineId(crId);
        synchronized (lock) {
            programMap.put(program.getId(), program);
        }
    }

    protected String getProgramOfJob(String jId)
    throws UnknownProgramException, UnknownJobException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Fetching program of job.");
        List<String> programIds = getAllProgramIds();
        for(String programId: programIds) {
            if (getAllJobs(programId).contains(jId)) {
                return programId;
            }
        }
        throw new UnknownJobException("The given job is not contained in any program.");
    }

    public void deleteProgram(String pId) throws UnknownProgramException, ExecLogicException {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Deleting program.");
        Program program = getProgramById(pId);
        List<Job> jobs = new LinkedList<>(program.getJobs());
        for (Job job : jobs) {
            try {
                deleteJob(pId, job.getId());
            } catch (UnknownJobException e) {
                LOGGER.error("Error while deleting job {} of program {}.", job.getId(), pId, e);
                throw new ExecLogicException(e);
            }
        }
        program.cleanUp();
        synchronized (lock) {
            programMap.remove(pId);
        }
    }

    /**
     * Abort program with given id.
     * @param pId - program to abort.
     * @throws UnknownProgramException if program not exists
     */
    public void abortProgram(String pId) throws UnknownProgramException {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Aborting program.");
        Program program = getProgramById(pId);
        program.abort();
    }

    public void updateProgramName(String pId, String name)
    throws UnknownProgramException {
        Program program = getProgramById(pId);
        program.setName(name);
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Updated program name.");
    }

    public void updateProgramDescription(String pId, String description)
    throws UnknownProgramException {
        Program program = getProgramById(pId);
        program.setDescription(description);
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Updated program description");
    }

    public void markProgramAsFinished(String pId)
    throws UnknownProgramException {
        Program program = getProgramById(pId);
        program.markAsFinished();
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Marked program as finished.");
    }

    public List<String> getAllSharedResources(String pId)
    throws UnknownProgramException {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching all shared resources");
        Program program = getProgramById(pId);
        return program.getSharedResources()
                .stream()
                .map(ResourceDTO::getId)
                .collect(Collectors.toList());
    }

    public ResourceDTO createSharedResource(String pId, String dataTypeId, ByteBuffer data)
    throws UnknownProgramException {
        Program program = getProgramById(pId);
        ResourceDTO sharedResource = new ResourceDTO(UUID.randomUUID().toString(), dataTypeId);
        sharedResource.setData(data);
        program.addSharedResource(sharedResource);
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Created shared resource {} in program.", sharedResource.getId());
        return sharedResource;
    }

    public ResourceDTO getSharedResource(String pId, String rId)
    throws UnknownProgramException, UnknownResourceException {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching shared resource {} from program.", rId);
        Program program = getProgramById(pId);
        return program.getSharedResourceById(rId);
    }

    public void deleteSharedResource(String pId, String rId)
    throws UnknownProgramException {
        Program program = getProgramById(pId);
        program.deleteSharedResource(rId);
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Deleted shared resource {} from program.", rId);
    }

    /********************************************************************************
     * JOB METHODS
     *******************************************************************************/

    public String createJob(String pId)
    throws UnknownProgramException, ExecLogicException {
        Program program = getProgramById(pId);
        if (program.getState() == ExecutionState.FAILED || program.getState() == ExecutionState.SUCCESS) {
            String msg = "Cannot create a job in a finished program.";
            LOGGER.warn(DEFLoggerFactory.createProgramContext(pId), msg);
            throw new ExecLogicException(msg);
        }
        Job job = new Job(program);
        job.setState(ExecutionState.SCHEDULED);
        program.addJob(job);
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, job.getId()), "Created new job in program.");
        return job.getId();
    }

    public List<String> getAllJobs(String pId)
    throws UnknownProgramException {
        LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching all jobs of program.");
        Program program = getProgramById(pId);
        return program.getJobs()
                .stream()
                .sorted(Comparator.comparing(Job::getCreateTime))
                .map(Job::getId)
                .collect(Collectors.toList());
    }

    protected Job getJobById(String pId, String jId) throws UnknownProgramException, UnknownJobException {
        return getProgramById(pId).getJobById(jId);
    }

    public JobDTO getJob(String pId, String jId)
    throws UnknownProgramException, UnknownJobException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching job.");
        Job job = getJobById(pId, jId);
        return job.toDTO();
    }

    /**
     * Delete given job.
     * @param pId - program id
     * @param jId - job id
     * @throws UnknownProgramException if program not exists
     * @throws UnknownJobException if job not exists
     */
    public void deleteJob(String pId, String jId) throws UnknownProgramException, UnknownJobException {
    	// First abort job before delete it
        abortJob(pId, jId);

        Program program = getProgramById(pId);
        Job job = program.getJobById(jId);

        for (String tId : job.getAllTasks()) {
            taskCache.remove(tId);
        }

        job.cleanUp();
        program.deleteJob(job);
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Deleted job.");
    }

    /**
     * Abort job with given id.
     * @param pId - program id
     * @param jId - job id to abort
     * @throws UnknownProgramException if program not exits
     * @throws UnknownJobException if job not exists
     */
    public void abortJob(String pId, String jId) throws UnknownProgramException, UnknownJobException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Aborting job.");
        Job job = getJobById(pId, jId);
        job.abort();
    }

    public void setReducedResultsOfJob(String pId, String jId, List<ResourceDTO> reducedResults)
    throws UnknownProgramException, UnknownJobException {
        Job job = getJobById(pId, jId);
        job.setReducedResults(reducedResults);
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Reduced results of job set.");
    }

    public String getAttachedMapRoutine(String pId, String jId)
    throws UnknownProgramException, UnknownJobException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching attached map routine of job.");
        Job job = getJobById(pId, jId);
        if (job.hasMapRoutine()) {
            return job.getMapRoutine().getId();
        }
        LOGGER.warn(DEFLoggerFactory.createJobContext(pId, jId), "Job has no map routine attached, default map routine is taken.");
        return configuration.getDefaultMapRoutineId();
    }

    public void attachMapRoutine(String pId, String jId, RoutineDTO mapRoutine)
    throws UnknownProgramException, UnknownJobException, ExecLogicException {
        try {
            Job job = getJobById(pId, jId);
            job.setMapRoutine(mapRoutine);
            LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "MapRoutine successfully attached to job.");
        } catch (WrongRoutineTypeException e) {
            LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while attaching map routine to job.", e);
            throw new ExecLogicException(e);
        }
    }

    public String getAttachedReduceRoutine(String pId, String jId)
    throws UnknownProgramException, UnknownJobException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching attached reduce routine of job.");
        Job job = getJobById(pId, jId);
        if (job.hasReduceRoutine()) {
            return job.getReduceRoutine().getId();
        }
        LOGGER.warn(DEFLoggerFactory.createJobContext(pId, jId), "Job has no reduce routine attached.");
        return null;
    }

    public void attachReduceRoutine(String pId, String jId, RoutineDTO reduceRoutine)
    throws UnknownProgramException, UnknownJobException, ExecLogicException {
        Job job = getJobById(pId, jId);
        try {
            job.setReduceRoutine(reduceRoutine);
            LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "ReduceRoutine successfully attached to job.");
        } catch (WrongRoutineTypeException e) {
            LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while attaching reduce routine to job.", e);
            throw new ExecLogicException(e);
        }
    }

    public void markJobAsComplete(String pId, String jId)
    throws UnknownProgramException, UnknownJobException {
        Job job = getJobById(pId, jId);
        job.markAsComplete();
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Marked Job as complete.");
    }

    public boolean hasJobReduceRoutine(String pId, String jId)
    throws UnknownProgramException, UnknownJobException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Checking if job has reduce routine set.");
        Job job = getJobById(pId, jId);
        return job.hasReduceRoutine();
    }

    public boolean allTasksOfJobSuccessful(String pId, String jId)
    throws UnknownProgramException, UnknownJobException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Checking if all tasks of job are successful.");
        Job job = getJobById(pId, jId);
        return job.allTasksSuccessful();
    }

    public Collection<String> getRunningTasksOfJob(String pId, String jId)
    throws UnknownProgramException, UnknownJobException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching running tasks of job.");
        Job job = getJobById(pId, jId);
        return job.getRunningTasks();
    }

    public List<ResourceDTO> getResourcesWithSpecificKeys(String jId, Collection<String> resourceKeys)
    throws UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Fetching resources with specific keys.");
        String pId = getProgramOfJob(jId);
        List<String> taskIds = getAllTasks(pId, jId, SortingCriterion.NO_SORTING);
        List<ResourceDTO> resources = new LinkedList<>();
        for (String tId: taskIds) {
            TaskDTO task = getTask(pId, jId, tId);
            for (ResourceDTO resource : task.getOutParameters()) {
                if (resourceKeys.contains(resource.getKey())) {
                    resources.add(resource);
                }
            }
        }
        return resources;
    }


    /********************************************************************************
     * TASK METHODS
     *******************************************************************************/

    public String createEmptyTask(String pId, String jId)
    throws UnknownProgramException, UnknownJobException, ExecLogicException {
        // create task and register it
        Job job = getJobById(pId, jId);
        TaskDTO task = new TaskDTO();
        task.setId(UUID.randomUUID().toString());
        task.setJobId(jId);
        task.setProgramId(pId);
        task.setCreateTime(System.currentTimeMillis());
        task.setState(ExecutionState.SCHEDULED);

        try {
            // should be very early to be sure that markAsComplete request is not performed yet
            job.addTask(task);
            taskCache.cache(task.getId(), task);
            LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), "Created new task.");
            return task.getId();
        } catch (JobCompletedException e) {
            // remove task from job
            job.removeTask(task.getId());

            LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), "Error while creating task.");
            throw new ExecLogicException(e);
        }
    }

    public TaskDTO configureTask(String pId, String jId, String tId, RoutineInstanceDTO routineInstance, RoutineDTO objectiveRoutine)
    throws UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException {
        // check input
        if (routineInstance.isSetMissingParameters() && !routineInstance.getMissingParameters().isEmpty()) {
            String msg = "Missing Parameters must be empty at routine instance.";
            LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), msg);
            throw new ExecLogicException(msg);
        }

        // check routine and routine parameters against routine instance
        for (FormalParameterDTO formalParameter : objectiveRoutine.getInParameters()) {
            // check for all needed parameters
            if (routineInstance.getInParameters().containsKey(formalParameter.getName())) {
                ResourceDTO paramResource = routineInstance.getInParameters().get(formalParameter.getName());
                String dataTypeId = getDataTypeIdOfResource(pId, paramResource);

                // check for correct data type
                checkForValidDatatype(formalParameter, dataTypeId);
            } else {
                String msg = String.format(
                        "Parameter %s needed by routine %s is missing at RoutineInstance.",
                        formalParameter.getName(),
                        objectiveRoutine.getId()
                );
                LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), msg);
                throw new ExecLogicException(msg);
            }
        }

        // fetch job and check for task
        Job job = getJobById(pId, jId);
        if (!job.containsTask(tId)) {
            String msg = "Task is not part of this job.";
            LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), msg);
            throw new UnknownTaskException(msg);
        }

        // assemble task and cache it
        try {
            TaskDTO task = taskCache.fetch(tId);
            task.setObjectiveRoutineId(objectiveRoutine.getId());
            task.setMapRoutineId(job.getMapRoutine().getId());
            task.setInParameters(routineInstance.getInParameters());
            taskCache.cache(task.getId(), task);
            LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Task (#{}) configured.", job.getNumberOfTasks());
            return task;
        } catch (IOException | UnknownCacheObjectException e) {
            throw new UnknownTaskException(String.format("Task not known: %s", tId));
        }
    }

    public List<String> getAllTasks(String pId, String jId, SortingCriterion sortingCriterion)
    throws UnknownProgramException, UnknownJobException, ExecLogicException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching all tasks with sorting criterion {}.", sortingCriterion);
        Job job = getJobById(pId, jId);
        List<String> taskIds = new LinkedList<>(job.getAllTasks());
        try {
            return sortTasks(taskIds, sortingCriterion);
        } catch (UnknownTaskException e) {
            LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while sorting fetched tasks.", e);
            throw new ExecLogicException(e);
        }
    }

    public List<String> getAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion)
    throws UnknownProgramException, UnknownJobException, ExecLogicException {
        LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching all tasks with state {} and sorting criterion {}.", state, sortingCriterion);
        Job job = getJobById(pId, jId);
        List<String> taskIds = new LinkedList<>();
        switch (state) {
            case SCHEDULED:
                taskIds.addAll(job.getScheduledTasks());
                break;
            case RUN:
                taskIds.addAll(job.getRunningTasks());
                break;
            case SUCCESS:
                taskIds.addAll(job.getSuccessfulTasks());
                break;
            case FAILED:
                taskIds.addAll(job.getFailedTasks());
                break;
        }
        try {
            return sortTasks(taskIds, sortingCriterion);
        } catch (UnknownTaskException e) {
            LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while sorting fetched tasks.", e);
            throw new ExecLogicException(e);
        }
    }

    public TaskDTO getTask(String pId, String jId, String tId) throws UnknownProgramException, UnknownJobException, UnknownTaskException {
        LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Fetching task.");
        // check if program and job ids are valid
        Job job = getJobById(pId, jId);
        if (!job.containsTask(tId)) {
            String msg = "Task is not part of this job.";
            LOGGER.error(DEFLoggerFactory.createTaskContext(job.getProgram().getId(), job.getId(), tId), msg);
            throw new UnknownTaskException(msg);
        }

        try {
            TaskDTO task = taskCache.fetch(tId);
            task.setState(job.getTaskState(tId));
            task.setRuntime(calcRuntimeForTask(task));
            return task;
        } catch (IOException | UnknownCacheObjectException e) {
            throw new UnknownTaskException(String.format("Task not known: %s", tId));
        }
    }

    TaskDTO getTask(String tId) throws UnknownTaskException, ExecLogicException {
        try {
            TaskDTO task = taskCache.fetch(tId);
            if (task != null) {
                task.setRuntime(calcRuntimeForTask(task));
            }
            return task;
        } catch (IOException e) {
            LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while fetching task from cache.");
            throw new ExecLogicException(e);
        } catch (UnknownCacheObjectException e) {
            throw new UnknownTaskException(e.getMessage());
        }
    }

    public TaskDTO getTaskPartial(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters)
    throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException {
        LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Fetching task partial.");
        TaskDTO task = getTask(pId, jId, tId);

        if (includeInParameters && includeOutParameters) {
            return task;
        }

        TaskDTO partial = task.deepCopy();
        if (!includeInParameters) {
            partial.setInParameters(null);
        }
        if (!includeOutParameters) {
            partial.setOutParameters(null);
        }

        return partial;
    }

    public void abortTask(String pId, String jId, String tId)
    throws UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException {
        LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Aborting task.");
        // check pId, jId and tId
        Job job = getJobById(pId, jId);
        if (!job.containsTask(tId)) {
            String msg = "Task is not part of this job.";
            LOGGER.error(DEFLoggerFactory.createTaskContext(job.getProgram().getId(), job.getId(), tId), msg);
            throw new UnknownTaskException(msg);
        }

        try {
            switch (job.getTaskState(tId)) {
                case SCHEDULED:
                case RUN:
                    job.notifyTaskChangedState(tId, ExecutionState.FAILED);
                    TaskDTO task = taskCache.fetch(tId);
                    task.setState(ExecutionState.FAILED);
                    task.setFinishTime(Instant.now().toEpochMilli());
                    task.setRuntime(calcRuntimeForTask(task));
                    taskCache.cache(tId, task);
                    LOGGER.debug(DEFLoggerFactory.createTaskContext(job.getProgram().getId(), job.getId(), tId), "Aborted task.");
                    break;
                case SUCCESS:
                case FAILED:
                    break;
            }
        } catch (IOException e) {
            throw new ExecLogicException(e);
        } catch (UnknownCacheObjectException e) {
            throw new UnknownTaskException(String.format("Task %s not known."));
        }
    }

    public TaskDTO fetchAndPrepareTaskForReRun(String pId, String jId, String tId)
    throws UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException {
        LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Preparing task for re-run.");
        // check pId, jId and tId
        Job job = getJobById(pId, jId);
        if (!job.containsTask(tId)) {
            String msg = "Task is not part of this job.";
            LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), msg);
            throw new UnknownTaskException(msg);
        }

        try {
            job.notifyTaskChangedState(tId, ExecutionState.SCHEDULED);
            TaskDTO task = taskCache.fetch(tId);
            task.setState(ExecutionState.SCHEDULED);
            LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Trying to rerun task.");
            taskCache.cache(task.getId(), task); // update cache
            return task;
        } catch (IOException | UnknownCacheObjectException e) {
            throw new UnknownTaskException(String.format("Task not known: %s", tId));
        }
    }

    public void notifyTasksChangedState(List<String> taskIds, ExecutionState newState) throws UnknownProgramException, UnknownJobException, UnknownTaskException {
        for (String tId : taskIds) {
        	try {
                TaskDTO task = taskCache.fetch(tId);
                task.setState(newState);
                notifyTaskChangedState(task); // cache update will be done here

            } catch (IOException | UnknownCacheObjectException e) {
        		throw new UnknownTaskException(String.format("Task not known: %s", tId));
            }
        }
    }

    public void notifyTaskChangedState(TaskDTO task) throws UnknownJobException, UnknownProgramException {
        Job job = getJobById(task.getProgramId(), task.getJobId());
        ExecutionState oldState = job.getTaskState(task.getId());
        ExecutionState newState = task.getState();
        if ((oldState == SUCCESS || oldState == FAILED) && newState == RUN) {
            LOGGER.info(
                    DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()),
                    "State change from {} to {} not possible. Ignoring request. (Could be happened if a newer ticket 'overtake' an older ticket).",
                    oldState,
                    newState
            );
            task.setState(oldState); // change back to old state
        } else {
            job.notifyTaskChangedState(task.getId(), newState);
        }

        // Correct dates if not set
        if (task.getState() == ExecutionState.RUN && !task.isSetStartTime()) {
            task.setStartTime(System.currentTimeMillis());
        }
        if (task.getState() == ExecutionState.SUCCESS && !task.isSetFinishTime()) {
            task.setFinishTime(System.currentTimeMillis());
            task.setRuntime(calcRuntimeForTask(task));
        }

        // Update cache
        taskCache.cache(task.getId(), task);
    }

    public void notifyProgramSuccess(ProgramDTO program)
    throws UnknownProgramException {
        if (program.getState() == ExecutionState.SUCCESS) {
            LOGGER.debug(
                    DEFLoggerFactory.createProgramContext(program.getId()),
                    "Program {} finished successfully, all results fetched from client routine worker.",
                    program.getId()
            );
        } else {
            LOGGER.warn(DEFLoggerFactory.createProgramContext(
                    program.getId()),
                    "Program finished with state {} instead of {}",
                    program.getState(),
                    ExecutionState.SUCCESS
            );
        }
        Program p = getProgramById(program.getId());
        p.setState(program.getState());
        p.setFinishTime(Instant.now());
        p.setResults(program.getResults());
    }

    public void notifyProgramsRun(List<String> programIds)
    throws UnknownProgramException {
        LOGGER.debug("Notify programs run.");
        for (String pId: programIds) {
            Program program = getProgramById(pId);
            program.setState(ExecutionState.RUN);
        }
    }

    protected List<String> sortTasks(List<String> taskIds, SortingCriterion sortingCriterion)
    throws UnknownTaskException, ExecLogicException {
        LOGGER.debug("Sorting all tasks with sorting criterion {}.", sortingCriterion);
        if (sortingCriterion == SortingCriterion.NO_SORTING) {
            return taskIds;
        }

        List<TaskDTO> tasks = new LinkedList<>();
        for (String tId : taskIds) {
            try {
                TaskDTO task = taskCache.fetch(tId);
                task.setRuntime(calcRuntimeForTask(task));
                tasks.add(task);
            } catch (IOException | UnknownCacheObjectException e) {
                throw new UnknownTaskException(String.format("Task not known: %s", tId));
            }
        }

        return tasks
                .stream()
                .sorted((t1, t2) -> compareTasks(t1, t2, sortingCriterion))
                .map(TaskDTO::getId)
                .collect(Collectors.toList());
    }

    protected int compareTasks(TaskDTO t1, TaskDTO t2, SortingCriterion sortingCriterion) {
        switch (sortingCriterion) {
            case CREATION_DATE_FROM_NEWEST:
                return compareByTime(t1.getCreateTime(), t2.getCreateTime());
            case CREATION_DATE_FROM_OLDEST:
                return compareByTime(t2.getCreateTime(), t1.getCreateTime());
            case START_DATE_FROM_NEWEST:
                return compareByTime(t1.getStartTime(), t2.getStartTime());
            case START_DATE_FROM_OLDEST:
                return compareByTime(t2.getStartTime(), t1.getStartTime());
            case FINISH_DATE_FROM_NEWEST:
                return compareByTime(t1.getFinishTime(), t2.getFinishTime());
            case FINISH_DATE_FROM_OLDEST:
                return compareByTime(t2.getFinishTime(), t1.getFinishTime());
            case RUNTIME_FROM_LONGEST:
                return compareByTime(t1.getRuntime(), t2.getRuntime());
            case RUNTIME_FROM_SHORTEST:
                return compareByTime(t2.getRuntime(), t1.getRuntime());
            default:
                return -1;
        }
    }

    // Returns
    //      -1 if firstTime is greater
    //      0 if firstTime is equal to
    //      1 if firstTime is smaller
    // than secondTime
    protected int compareByTime(long firstTime, long secondTime) {
        if (firstTime > secondTime) {
            return -1;
        } else if (firstTime == secondTime) {
            return 0;
        }
        return 1;
    }

    protected long calcRuntimeForTask(TaskDTO task) {
        switch (task.getState()) {
            case SCHEDULED:
                return 0;
            case RUN:
                return System.currentTimeMillis() - task.getStartTime();
            case SUCCESS:
            case FAILED:
                if (task.getFinishTime() == 0) {
                    task.setFinishTime(System.currentTimeMillis());
                    taskCache.cache(task.getId(), task);
                }
                return task.getFinishTime() - task.getStartTime();
        }
        return -1;
    }

    protected String getDataTypeIdOfResource(String pId, ResourceDTO resource)
    throws UnknownProgramException, ExecLogicException  {
        if (ResourceUtil.isSharedResource(resource)) {
            // shared resource case, replace dataTypeId with shared resource one
            try {
                return getSharedResource(pId, resource.getId()).getDataTypeId();
            } catch (UnknownResourceException e) {
                String msg = String.format("Shared resource %s is not found.", resource.getData());
                LOGGER.error(DEFLoggerFactory.createProgramContext(pId), msg);
                throw new ExecLogicException(msg);
            }
        } else {
            return resource.getDataTypeId();
        }
    }

    protected boolean checkForValidDatatype(FormalParameterDTO parameter, String dataTypeId)
    throws ExecLogicException {
        if (!parameter.getDataType().getId().equals(dataTypeId)) {
            String msg = String.format(
                    "Parameter %s has wrong data type: needed %s, given %s.",
                    parameter.getName(),
                    parameter.getDataType().getId(),
                    dataTypeId
            );
            LOGGER.error(msg);
            throw new ExecLogicException(msg);
        }
        return true;
    }

    public boolean isProgramAborted(String pId) throws UnknownProgramException {
        return getProgramById(pId).isAborted();
    }

    public boolean isJobAborted(String pId, String jId) throws UnknownProgramException, UnknownJobException {
        return getJobById(pId, jId).isAborted();
    }
}
