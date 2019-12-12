package at.enfilo.def.scheduler.reducer.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;
import java.util.concurrent.Future;

public interface IReducerSchedulerServiceClient extends IServiceClient {

    /**
     * Add a reducer for scheduling process.
     *
     * @param rId - id of reducer node
     * @param endpoint - service endpoint of reducer node
     */
    Future<Void> addReducer(String rId, ServiceEndpointDTO endpoint) throws ClientCommunicationException;

    /**
     * Removes a reducer from scheduling process.
     *
     * @param rId - reducer to remove
     */
    Future<Void> removeReducer(String rId) throws ClientCommunicationException;

    /**
     * Extends a given job to a reduce job. Start a reduce job on each reducer.
     *
      * @param job - job to add
     */
    Future<Void> addReduceJob(JobDTO job) throws ClientCommunicationException;

    /**
     * Abort a running reduce job.
     *
     * @param jId - job id to abort
     */
    Future<Void> removeReduceJob(String jId) throws ClientCommunicationException;

    /**
     * Schedule a list of resources to reduce.
     *
     * @param jId - job id
     * @param resources - task to schedule
     */
    Future<Void> scheduleResourcesToReduce(String jId, List<ResourceDTO> resources) throws ClientCommunicationException;

    /**
     * Finalize a reduce.
     *
     * @param jId - job id
     */
    Future<JobDTO> finalizeReduce(String jId) throws ClientCommunicationException;
}
