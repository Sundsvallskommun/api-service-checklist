package se.sundsvall.checklist.integration.templating;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.checklist.integration.templating.configuration.TemplatingConfiguration.CLIENT_ID;

import generated.se.sundsvall.templating.RenderRequest;
import generated.se.sundsvall.templating.RenderResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.checklist.integration.templating.configuration.TemplatingConfiguration;

@CircuitBreaker(name = CLIENT_ID)
@FeignClient(name = CLIENT_ID, url = "${integration.templating.url}", configuration = TemplatingConfiguration.class)
public interface TemplatingClient {

	@PostMapping(path = "/{municipalityId}/render", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	RenderResponse render(
		@PathVariable("municipalityId") String municipalityId,
		@RequestBody final RenderRequest request);
}
