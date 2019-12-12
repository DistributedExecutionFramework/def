package at.enfilo.def.client.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultHistoryFileNameProvider;
import org.springframework.stereotype.Component;

import static at.enfilo.def.client.shell.Constants.HISTORY_FILE_NAME;
import static at.enfilo.def.client.shell.Constants.HISTORY_PROVIDER_NAME;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HistoryFileNameProvider extends DefaultHistoryFileNameProvider {
	@Override
	public String getHistoryFileName() {
		return HISTORY_FILE_NAME;
	}

	@Override
	public String getProviderName() {
		return HISTORY_PROVIDER_NAME;
	}
}
