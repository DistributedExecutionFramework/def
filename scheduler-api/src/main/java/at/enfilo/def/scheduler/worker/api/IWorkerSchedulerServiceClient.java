package at.enfilo.def.scheduler.worker.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.util.concurrent.Future;

public interface IWorkerSchedulerServiceClient extends IServiceClient {

    /**
     * Add a worker for scheduling process.
     *
     * @param wId - id of worker node
     * @param endpoint - service endpoint of worker node
     */
    Future<Void> addWorker(String wId, ServiceEndpointDTO endpoint) throws ClientCommunicationException;

    /**
     * Removes a worker from scheduling process.
     *
     * @param wId - worker to remove
     */
    Future<Void> removeWorker(String wId) throws ClientCommunicationException;

    /**
     * Adds a new job to scheduler.
     *
     * @param jId - job id
     */
    Future<Void> addJob(String jId) throws ClientCommunicationException;

    /**
     * Remove a finished job or abort a running job.
     * Aborting means, cancel all tasks on nodes too.
     *
     * @param jId - job id to abort
     */
    Future<Void> removeJob(String jId) throws ClientCommunicationException;

    /**
     * Mark a job as complete. This means, that all tasks are created for the given job.
     *
     * @param jId - job id
     */
    Future<Void> markJobAsComplete(String jId) throws ClientCommunicationException;

    /**
     * Schedule a task.
     *
     * @param jId - job id
     * @param task - task to schedulePrograms
     */
    Future<Void> scheduleTask(String jId, TaskDTO task) throws ClientCommunicationException;

    /**
     * Abort a task.
     *
     * @param wId - worker id the task was scheduled on
     * @param tId - task id to abort
     */
    Future<Void> abortTask(String wId, String tId) throws ClientCommunicationException;
}
