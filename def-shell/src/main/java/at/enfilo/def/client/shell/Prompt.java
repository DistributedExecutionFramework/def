package at.enfilo.def.client.shell;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

import static at.enfilo.def.client.shell.Constants.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class Prompt extends DefaultPromptProvider {

	@Autowired
	private DEFShellSession session;

	@Override
	public String getPrompt() {
		if (session.getActiveService() != null) {
			ServiceEndpointDTO endpoint = session.getActiveEndpoint();
			return String.format(
					PROMPT_CONNECTED,
					session.getActiveService().toString(),
					endpoint.getHost(),
					endpoint.getPort(),
					endpoint.getPathPrefix()
			);
		} else {
			return PROMPT_NOT_CONNECTED;
		}
	}

	@Override
	public String getProviderName() {
		return PROMPT_PROVIDER_NAME;
	}
}
