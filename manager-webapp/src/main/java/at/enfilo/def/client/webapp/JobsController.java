package at.enfilo.def.client.webapp;

import at.enfilo.def.client.util.SessionConstant;
import at.enfilo.def.client.util.WebFace;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.transfer.dto.AuthDTO;
import at.enfilo.def.transfer.dto.JobDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by mase on 22.08.2016.
 */
@ManagedBean
@ViewScoped
public class JobsController extends WebController {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobsController.class);

	private transient IExecLogicServiceClient execLogicServiceClient;
	private String selectedJobId;

	public JobsController() {
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
			LOGGER.error("Error initialize ExecLogicServiceClient", e);
		}
	}

	public List<String> getAllJobs() {
		try {

			String pId = String.class.cast(
					getSessionMap().get(SessionConstant.ACTIVE_PROGRAM_ID.getConstant())
			);
			Future<List<String>> jIds = execLogicServiceClient.getAllJobs(pId);
			return jIds.get();

		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error occurs while fetching all program jobs.", e);
			return Collections.emptyList();
		}
	}

	public String getSelectedJob() {
		return selectedJobId;
	}

	public void setSelectedJob(String jId) {
		selectedJobId = jId;
	}

	public JobDTO getSelectedJobInfo() {
		try {

			String pId = String.class.cast(
				getSessionMap().get(SessionConstant.ACTIVE_PROGRAM_ID.getConstant())
			);

			if (selectedJobId != null) {
				Future<JobDTO> futureJob = execLogicServiceClient.getJob(pId, selectedJobId);
				return futureJob.get();
			}
			return null;

		} catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
			LOGGER.error("Error occurs while fetching Job info.", e);
			return null;
		}
	}

	public void openTasksView(JobDTO jobDTO) {
		try {

			Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
			sessionMap.put(SessionConstant.ACTIVE_JOB_ID.getConstant(), jobDTO.getId());

			redirect(WebFace.TASKS_FACE.getReference());

		} catch (IOException e) {
			LOGGER.error(
					"Unable to redirect to {} page.",
					WebFace.TASKS_FACE.getReference(),
					e
			);
		}
	}
}
