package at.enfilo.def.client.webapp;

import at.enfilo.def.client.util.SessionConstant;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.transfer.dto.AuthDTO;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.SortingCriterion;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by mase on 22.08.2016.
 */
@ManagedBean
@ViewScoped
public class TasksController extends WebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TasksController.class);

    private transient IExecLogicServiceClient execLogicServiceClient;
    private String selectedTaskId;

    public TasksController() {
        // Bean conventions
    }

    @PostConstruct
    public void init() {
        AuthDTO authDTO = AuthDTO.class.cast(
            getSessionMap().get(SessionConstant.AUTH_DTO.getConstant())
        );

        try {
            execLogicServiceClient = new ExecLogicServiceClientFactory().createClient(getServiceEndpoint(), authDTO);
        } catch (ClientCreationException e) {
            LOGGER.error("Error initializing ExecLogicServiceClient", e);
        }
    }

    public List<String> getAllTasks(ExecutionState state) {
        try {

            String pId = String.class.cast(
                getSessionMap().get(SessionConstant.ACTIVE_PROGRAM_ID.getConstant())
            );
            String jId = String.class.cast(
                getSessionMap().get(SessionConstant.ACTIVE_JOB_ID.getConstant())
            );

            Future<List<String>> futureTasks = execLogicServiceClient.getAllTasksWithState(pId, jId, state, SortingCriterion.CREATION_DATE_FROM_NEWEST);
            return futureTasks.get();

        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error occurs while fetching all job tasks.", e);
            return Collections.emptyList();
        }
    }

    public String getSelectedTask() {
        return selectedTaskId;
    }

    public void setSelectedTask(String tId) {
        selectedTaskId = tId;
    }

    public TaskDTO getSelectedTaskInfo() {
        try {

            String pId = String.class.cast(
                getSessionMap().get(SessionConstant.ACTIVE_PROGRAM_ID.getConstant())
            );
            String jId = (String) getSessionMap().get(SessionConstant.ACTIVE_JOB_ID.getConstant());

            if (selectedTaskId != null) {
            	Future<TaskDTO> futureTask = execLogicServiceClient.getTask(pId, jId, selectedTaskId);
            	return futureTask.get();
            }
            return null;

        } catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
            LOGGER.error("Error occurs while fetching Task info.", e);
            return null;
        }
    }

    public void stopTask(TaskDTO taskDTO) {
        //TODO Discuss;
    }
}

