package at.enfilo.def.client.webapp;

import at.enfilo.def.client.util.SessionConstant;
import at.enfilo.def.client.util.WebFace;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.manager.api.AuthServiceClientFactory;
import at.enfilo.def.manager.api.IAuthServiceClient;
import at.enfilo.def.transfer.dto.AuthDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by mase on 22.08.2016.
 */
@ManagedBean
public class AuthController extends WebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private String name;
    private String pass;
    private String uId;

    private transient IAuthServiceClient authServiceClient;

    public AuthController() {
        // Bean conventions
    }

    @PostConstruct
    public void init() {
		try {
			authServiceClient = new AuthServiceClientFactory().createClient(getServiceEndpoint());
		} catch (ClientCreationException e) {
			LOGGER.error("Error initialize AuthServiceClient", e);
		}
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUId() {
        return uId;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public boolean isAuthorized() {
        AuthDTO authDTO = AuthDTO.class.cast(
                getSessionMap().get(SessionConstant.AUTH_DTO.getConstant())
        );
        return authDTO != null && authDTO.getUserId() != null && authDTO.getToken() != null;
    }

    public void authorize() {

        try {

            String receivedName = getName();
            String receivedPass = getPass();

            // Trying to login. Performing request auth resource holder.
            Future<AuthDTO> futureAuth = authServiceClient.getToken(receivedName, receivedPass);
            AuthDTO authDTO = futureAuth.get();

            if (authDTO != null && authDTO.isSetToken() && authDTO.isSetUserId()) {

                // Saving auth data into session storage.
                getSessionMap().put(SessionConstant.AUTH_DTO.getConstant(), authDTO);

                // Redirecting to the main view.
                redirect(WebFace.PROGRAMS_FACE.getReference());

            } else {

                // Redirecting to the auth view.
                redirect(WebFace.AUTH_FACE.getReference());
            }

        } catch (ClientCommunicationException | IOException | InterruptedException | ExecutionException e) {
            LOGGER.error("An error occurs while trying to authorize.", e);
        }
    }

    public void logout() {
        try {

            // Removing all data from session storage.
            getSessionMap().clear();
            redirect(WebFace.AUTH_FACE.getReference());

        } catch (IOException e) {
            LOGGER.error("Unable to redirect to {} page.", WebFace.AUTH_FACE.getReference(), e);
        }
    }
}
