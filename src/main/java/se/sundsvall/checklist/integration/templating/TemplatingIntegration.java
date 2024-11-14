package se.sundsvall.checklist.integration.templating;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.templating.RenderRequest;
import generated.se.sundsvall.templating.RenderResponse;

@Component
public class TemplatingIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplatingIntegration.class);
	private final TemplatingClient templatingClient;

	public TemplatingIntegration(final TemplatingClient templatingClient) {
		this.templatingClient = templatingClient;
	}

	public Optional<RenderResponse> renderTemplate(final String municipalityId, final RenderRequest renderRequest) {
		try {
			return Optional.of(templatingClient.render(municipalityId, renderRequest));
		} catch (final Exception e) {
			LOGGER.warn("Error while rendering template", e);
			return Optional.empty();
		}
	}
}
