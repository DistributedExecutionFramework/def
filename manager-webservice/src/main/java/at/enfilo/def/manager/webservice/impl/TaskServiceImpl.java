package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.manager.webservice.rest.ITaskService;
import at.enfilo.def.manager.webservice.server.ManagerWebservice;
import at.enfilo.def.manager.webservice.util.DataConverter;
import at.enfilo.def.manager.webservice.util.ManagerWebserviceConfiguration;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.SortingCriterion;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskServiceImpl implements ITaskService {

    /**
     * Logger for logging activities of instances of this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramServiceImpl.class);

    private final ManagerWebserviceConfiguration configuration;
    /**
     * IExecLogicServiceClient needed for accessing the execution logic implementations regarding tasks
     */
    protected IExecLogicServiceClient serviceClient;

    protected DataConverter dataConverter;

    /**
     * Constructor of ProgramServiceImpl
     */
    public TaskServiceImpl() {
        configuration = ManagerWebservice.getInstance().getConfiguration();
        init();
    }

    /**
     * Initializing of all needed components
     */
    private void init() {

        // service client for execution logic is fetched
        try {
            LOGGER.info("Initialization of all needed components for managing tasks");
            serviceClient = new ExecLogicServiceClientFactory().createClient(configuration.getExecLogicEndpoint());
            dataConverter = new DataConverter();
        } catch (ClientCreationException e) {
            LOGGER.error("Error initializing ExecLogicServiceClient", e);
        }
    }

    @Override
    public List<TaskDTO> getAllTasksWithState(String pId, String jId, String stateString, SortingCriterion sortingCriterion) {
        try {
            LOGGER.info("Fetching all tasks with state {} of job with id {} of program with id {}", stateString, jId, pId);
            ExecutionState state = ExecutionState.valueOf(stateString);
            Future<List<String>> future = serviceClient.getAllTasksWithState(pId, jId, state, sortingCriterion);
            List<String> taskIds = future.get();
            List<TaskDTO> tasks = new LinkedList<>();

            for (String id: taskIds) {
                Future<TaskDTO> taskDTOFuture = serviceClient.getTask(pId, jId, id, false, false);
                tasks.add(taskDTOFuture.get());
            }
            return tasks;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching all tasks with state {} of job with id {} of program with id {}", stateString, jId, pId, e);
            return null;
        }
    }

    @Override
    public List<TaskDTO> getAllTasksByFilters(String pId, String jId, SortingCriterion sortingCriterion, String taskIdFilters) {
        try {
            LOGGER.info("Fetching all tasks that match with the given filters.");
            String[] filters = taskIdFilters.split(",");
            Future<List<String>> future = serviceClient.getAllTasks(pId, jId, sortingCriterion);
            List<String> taskIds = future.get();

            List<String> filteredTaskIds = new LinkedList<>();
            for (String taskId : taskIds) {
                for (String filter: filters) {
                    if (taskId.contains(filter) && !filteredTaskIds.contains(taskId)) {
                        filteredTaskIds.add(taskId);
                    }
                }
            }

            List<TaskDTO> tasks = new LinkedList<>();
            for (String tId: filteredTaskIds) {
                Future<TaskDTO> taskDTOFuture = serviceClient.getTask(pId, jId, tId, false, false);
                tasks.add(taskDTOFuture.get());
            }
            return tasks;

        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching all tasks of job with id {} of program with id {}", jId, pId, e);
            return null;
        }
    }

    @Override
    public List<TaskDTO> getSpecificNumberOfTasksWithState(String pId, String jId, String stateString, SortingCriterion sortingCriterion, String nrOfTasksString) {
        int nrOfTasks = Integer.parseInt(nrOfTasksString);
        ExecutionState state = ExecutionState.valueOf(stateString);

        LOGGER.info("Fetching {} tasks with state {} of job with id {} of program with id {} sorted by {}", nrOfTasks, stateString, jId, pId, sortingCriterion);

        try {
            Future<List<String>> future = serviceClient.getAllTasksWithState(pId, jId, state, sortingCriterion);
            List<String> taskIds = future.get();

            List<TaskDTO> tasks = new LinkedList<>();
            for(int i = 0; i < taskIds.size() && i < nrOfTasks; i++) {
                Future<TaskDTO> taskDTOFuture = serviceClient.getTask(pId, jId, taskIds.get(i), false, false);
                tasks.add(taskDTOFuture.get());
            }
            return tasks;

        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching specific number of tasks of job with id {} of program with id {}", jId, pId, e);
            return null;
        }
    }

    @Override
    public TaskDTO getTaskInfo(String pId, String jId, String tId) {
        try {
            LOGGER.info("Fetching task info of task with id '" + tId + "' of job with id '" + jId + "' of program with id '" + pId + "'");
            Future<TaskDTO> future = serviceClient.getTask(pId, jId, tId);
            TaskDTO task = future.get();
            return task;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching task info of task with id '" + tId + "' of job with id '" + jId + "' of program with id '" + pId + "'", e);
            return null;
        }
    }

    @Override
    public String getDataValueOfTaskInputParameter(String pId, String jId, String tId, String inputParamName) {
        try {
            LOGGER.info("Fetching data value of input parameter with name '" + inputParamName + "' of task with id '" + tId + "'");
            Future<TaskDTO> future = serviceClient.getTask(pId, jId, tId);
            TaskDTO task = future.get();
            Map<String, ResourceDTO> inParams = task.getInParameters();
            ResourceDTO resource = inParams.get(inputParamName);
            return this.dataConverter.convertResourceData(resource);
        } catch (InterruptedException | ExecutionException | ClientCommunicationException | TException e) {
            LOGGER.error("Error fetching data value of input parameter.", e);
            return "";
        }
    }

    @Override
    public String getDataValueOfTaskOutputParameter(String pId, String jId, String tId, String resourceId) {
        try {
            LOGGER.info("Fetching data value of output parameter with id '" + resourceId + "' of task with id '" + tId + "'");
            Future<TaskDTO> future = serviceClient.getTask(pId, jId, tId);
            TaskDTO task = future.get();

            List<ResourceDTO> outParams = task.getOutParameters();
            ResourceDTO outParam = null;
            for (ResourceDTO param : outParams) {
                if (param.getId().equals(resourceId)) {
                    outParam = param;
                    break;
                }
            }
            return this.dataConverter.convertResourceData(outParam);
        } catch (InterruptedException | ExecutionException | ClientCommunicationException | TException e) {
            LOGGER.error("Error fetching data value of output parameter.", e);
            return "";
        }
    }

    @Override
    public TicketStatusDTO abortTask(String pId, String jId, String tId) {
        try {
            LOGGER.info("Aborting task with id '" + tId + "' of job with id '" + jId + "' of program with id '" + pId + "'");
            Future<Void> future = serviceClient.abortTask(pId, jId, tId);
            future.get();
            return TicketStatusDTO.DONE;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error aborting task with id '" + tId + "' of job with id '" + jId + "' of program with id '" + pId + "'", e);
            return TicketStatusDTO.FAILED;
        }
    }

	@Override
	public TicketStatusDTO reRunTask(String pId, String jId, String tId) {
		try {
			LOGGER.info("Rerun task with id '" + tId + "' of job with id '" + jId + "' of program with id '" + pId + "'");
			Future<Void> future = serviceClient.reRunTask(pId, jId, tId);
			future.get();
			return TicketStatusDTO.DONE;
		} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
			LOGGER.error("Error rerun task with id '" + tId + "' of job with id '" + jId + "' of program with id '" + pId + "'", e);
			return TicketStatusDTO.FAILED;
		}
	}
}
