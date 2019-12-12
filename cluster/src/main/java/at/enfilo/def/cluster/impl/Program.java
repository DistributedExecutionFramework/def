package at.enfilo.def.cluster.impl;

import at.enfilo.def.transfer.IDTOConvertable;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownResourceException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.time.Instant;
import java.util.*;

class Program implements IDTOConvertable<ProgramDTO> {

	private final String id;
	private final String userId;
	private String name;
	private String description;
    private ExecutionState state;
    private Instant createTime;
    private Instant finishTime;
    private Map<String, Job> jobs;
    private Map<String, ResourceDTO> results;
    private Map<String, ResourceDTO> sharedResources;
    private String clientRoutineId;
    private boolean aborted;

    public Program(String userId) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.createTime = Instant.now();
        this.jobs = new HashMap<>();
        this.results = new HashMap<>();
        this.sharedResources = new HashMap<>();
        this.name = id;
        this.description = "";
        this.clientRoutineId = null;
    }

    public String getId() {
        return id;
    }

	public String getUserId() {
		return userId;
	}

	public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public Collection<Job> getJobs() {
        return jobs.values();
    }

    public Map<String, ResourceDTO> getResults() { return results; }

    public String getClientRoutineId() { return clientRoutineId; }

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

    public void setJobs(Collection<Job> jobs) {
        this.jobs.clear();
        jobs.forEach(j -> this.jobs.put(j.getId(), j));
    }

    public void setResults(Map<String, ResourceDTO> results) {
        this.results = results;
    }

    public void setClientRoutineId(String clientRoutineId) { this.clientRoutineId = clientRoutineId; }

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
	 */
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
	void addJob(Job job) {
		jobs.put(job.getId(), job);
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
        if (!aborted && state != ExecutionState.SUCCESS && state != ExecutionState.FAILED) {
        	aborted = true;
            finishTime = Instant.now();
            state = ExecutionState.FAILED;
            jobs.values().forEach(Job::abort);
        }
    }

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	@Override
	public ProgramDTO toDTO() {
		ProgramDTO dto = new ProgramDTO();
		dto.setId(id);
		dto.setState(state);
		dto.setCreateTime(createTime.toEpochMilli());
		if (finishTime != null) {
			dto.setFinishTime(finishTime.toEpochMilli());
		}
		dto.setUserId(userId);
		dto.setName(name);
		dto.setDescription(description);
		dto.setNrOfJobs(jobs.size());
		Map<String, String> sr = new HashMap<>();
		sharedResources.forEach((name, resource) -> sr.put(name, resource.getId()));
		dto.setSharedResources(sr);
		dto.setClientRoutineId(clientRoutineId);
		dto.setResults(results);
		return dto;
	}
}
