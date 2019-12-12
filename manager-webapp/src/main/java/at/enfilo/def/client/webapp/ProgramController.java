package at.enfilo.def.client.webapp;

import at.enfilo.def.client.util.SessionConstant;
import at.enfilo.def.client.util.WebFace;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.transfer.dto.AuthDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;
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
public class ProgramController extends WebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramController.class);

    private transient IExecLogicServiceClient execLogicServiceClient;
    private String selectedProgramId;

    public ProgramController() {
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

    public List<String> getUserPrograms() {
        try {

            AuthDTO authDTO = AuthDTO.class.cast(
            	getSessionMap().get(SessionConstant.AUTH_DTO.getConstant())
			);

            if (authDTO != null && authDTO.getUserId() != null) {
				Future<List<String>> futurePrograms = execLogicServiceClient.getAllPrograms(authDTO.getUserId());
				return futurePrograms.get();
            }
            return Collections.emptyList();

        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error occurs while fetching all user programs.", e);
            return Collections.emptyList();
		}
	}

    public String getSelectedProgram() {
        return selectedProgramId;
    }

    public void setSelectedProgram(String pId) {
        selectedProgramId = pId;
    }

    public ProgramDTO getSelectedProgramInfo() {
        try {

            if (selectedProgramId != null) {
            	Future<ProgramDTO> futureProgram = execLogicServiceClient.getProgram(selectedProgramId);
            	return futureProgram.get();
			}
			return null;

        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error occurs while fetching Program info.", e);
            return null;
		}
	}

    public void openJobsView(ProgramDTO programDTO) {
        try {

            Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            sessionMap.put(SessionConstant.ACTIVE_PROGRAM_ID.getConstant(), programDTO.getId());

            redirect(WebFace.JOBS_FACE.getReference());

        } catch (IOException e) {
            LOGGER.error(
                "Unable to redirect to {} page.",
                WebFace.JOBS_FACE.getReference(),
                e
            );
        }
    }
}
