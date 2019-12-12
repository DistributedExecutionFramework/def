package at.enfilo.def.client.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.stereotype.Component;

import static at.enfilo.def.client.shell.Constants.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class Banner extends DefaultBannerProvider {
	@Override
	public String getBanner() {
		return BANNER;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getWelcomeMessage() {
		return WELCOME_MSG;
	}

	@Override
	public String getProviderName() {
		return BANNER_PROVIDER_NAME;
	}
}
