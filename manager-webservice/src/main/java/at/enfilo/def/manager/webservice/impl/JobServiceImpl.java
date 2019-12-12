package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.manager.webservice.rest.IJobService;
import at.enfilo.def.manager.webservice.server.ManagerWebservice;
import at.enfilo.def.manager.webservice.util.DataConverter;
import at.enfilo.def.manager.webservice.util.ManagerWebserviceConfiguration;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JobServiceImpl implements IJobService {

    /**
     * Logger for logging activities of instances of this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramServiceImpl.class);

    private final ManagerWebserviceConfiguration configuration;
    /**
     * IExecLogicServiceClient needed for accessing the execution logic implementations regarding jobs
     */
    protected IExecLogicServiceClient serviceClient;

    protected DataConverter dataConverter;

    /**
     * Constructor of ProgramServiceImpl
     */
    public JobServiceImpl() {
        configuration = ManagerWebservice.getInstance().getConfiguration();
        dataConverter = new DataConverter();
        init();
    }

    /**
     * Initializing of all needed components
     */
    private void init() {

        // service client for execution logic is fetched
        try {
            LOGGER.info("Initialization of all needed components for managing jobs ");
            serviceClient = new ExecLogicServiceClientFactory().createClient(configuration.getExecLogicEndpoint());
        } catch (ClientCreationException e) {
            LOGGER.error("Error initializing ExecLogicServiceClient", e);
        }
    }

    @Override
    public List<String> getAllJobIds(String pId) {
        try {
            LOGGER.info("Fetching all job ids of program with id '" + pId + "'");
            Future<List<String>> future = serviceClient.getAllJobs(pId);
            return future.get();
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching all job ids of program with id '" + pId + "'", e);
            return null;
        }
    }

    @Override
    public List<JobDTO> getAllJobs(String pId) {
        try {
            LOGGER.info("Fetching all jobs of program with id '" + pId + "'");
            List<String> jobIds = getAllJobIds(pId);
            List<JobDTO> jobs = new LinkedList<>();
            if (jobIds != null) {
				for (String jobId : jobIds) {
					Future<JobDTO> jobDTOFuture = serviceClient.getJob(pId, jobId);
					jobs.add(jobDTOFuture.get());
				}
			}
            return jobs;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching all jobs of program with id '" + pId + "'", e);
            return null;
        }
    }

    @Override
    public int getNrOfFinishedJobs(String pId) {
            LOGGER.info("Fetching number of finished jobs of program with id '" + pId + "'");
            int finishedJobsCounter = 0;
            List<JobDTO> jobs = getAllJobs(pId);
            if (jobs != null) {
				for (JobDTO job : jobs) {
					ExecutionState state = job.getState();
					if (state == ExecutionState.SUCCESS || state == ExecutionState.FAILED) {
						finishedJobsCounter++;
					}
				}
				return finishedJobsCounter;
			}
			return -1;
    }

    @Override
    public JobDTO getJobInfo(String pId, String jId) {
        try {
            LOGGER.info("Fetching job info of job with id '" + jId + "' of program with id '" + pId + "'");
            Future<JobDTO> jobsFuture = serviceClient.getJob(pId, jId);
            JobDTO job = jobsFuture.get();
            return job;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching job info of job with id '" + jId + "' of program with id '" + pId + "'", e);
            return null;
        }
    }

    @Override
    public TicketStatusDTO deleteJob(String pId, String jId) {
        try {
            LOGGER.info("Deleting job with id '" + jId + "' of program with id '" + pId + "'");
            Future<Void> future = serviceClient.deleteJob(pId, jId);
            future.get();
            return TicketStatusDTO.DONE;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error deleting job with id '" + jId + "' of program with id '" + pId + "'", e);
            return TicketStatusDTO.FAILED;
        }
    }

    @Override
    public TicketStatusDTO abortJob(String pId, String jId) {
        try {
            LOGGER.info("Aborting with id '" + jId + "' of program with id '" + pId + "'");
            Future<Void> future = serviceClient.abortJob(pId, jId);
            future.get();
            return TicketStatusDTO.DONE;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error aborting job with id '" + jId + "' of program with id '" + pId + "'", e);
            return TicketStatusDTO.FAILED;
        }
    }

    @Override
    public String getAttachedMapRoutine(String pId, String jId) {
        try {
            LOGGER.info("Fetching attached map routine of job with id '" + jId + "' of program with id '" + pId + "'");
            Future<String> future = serviceClient.getAttachedMapRoutine(pId, jId);
            return future.get();
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching attached map routine of job with id '" + jId + "' of program with id '" + pId + "'", e);
            return null;
        }
    }

    @Override
    public String getAttachedReduceRoutine(String pId, String jId) {
        try {
            LOGGER.info("Fetching attached reduce routine of job with id '" + jId + "' of program with id '" + pId + "'");
            Future<String> future = serviceClient.getAttachedReduceRoutine(pId, jId);
            return future.get();
        } catch (InterruptedException | ExecutionException |ClientCommunicationException e) {
            LOGGER.error("Error fetching attached reduce routine of job with id '" + jId + "' of program with id '" + pId + "'", e);
            return null;
        }
    }

    @Override
    public String getDataValueOfJobReducedResult(String pId, String jId, String resourceId) {
        try {
            LOGGER.info("Fetching data value of reduced result with id '" + resourceId + "' of job with id '" + jId + "'");
            Future<JobDTO> future = serviceClient.getJob(pId, jId);
            JobDTO job = future.get();

            List<ResourceDTO> reducedResults = job.getReducedResults();
            ResourceDTO reducedResult = null;
            for (ResourceDTO resource : reducedResults) {
                if (resource.getId().equals(resourceId)) {
                    reducedResult = resource;
                    break;
                }
            }
            return this.dataConverter.convertResourceData(reducedResult);
        } catch (InterruptedException | ExecutionException | ClientCommunicationException | TException e) {
            LOGGER.error("Error fetching data value of reduced result.", e);
            return "";
        }
    }
}
