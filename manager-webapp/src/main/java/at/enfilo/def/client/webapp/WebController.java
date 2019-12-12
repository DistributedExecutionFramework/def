package at.enfilo.def.client.webapp;

import at.enfilo.def.client.util.ContextParam;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by mase on 22.08.2016.
 */
public abstract class WebController implements Serializable {

    private ServiceEndpointDTO serviceEndpoint;

    protected ServiceEndpointDTO getServiceEndpoint() {
        if (serviceEndpoint == null) {
            serviceEndpoint = new ServiceEndpointDTO(
                    getContextParam(ContextParam.HOME_HOST),
                    getContextParam(ContextParam.HOME_PORT, Integer::parseInt).intValue(),
                    Protocol.REST);

            serviceEndpoint.setPathPrefix(getContextParam(ContextParam.HOME_ADDRESS_BASE));
        }
        return serviceEndpoint;
    }

    protected String getContextParam(ContextParam contextParam) {
        return FacesContext.getCurrentInstance().getExternalContext().getInitParameter(
            contextParam.getContextParamKey()
        );
    }

    protected <T> T getContextParam(ContextParam contextParam, Function<String, T> converter) {
        String contextParamValue = getContextParam(contextParam);
        return converter.apply(contextParamValue);
    }

    protected Map<String, Object> getSessionMap() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    }

    protected void redirect(String faceReference)
    throws IOException {
        String requestContextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        FacesContext.getCurrentInstance().getExternalContext().redirect(requestContextPath + faceReference);
    }
}