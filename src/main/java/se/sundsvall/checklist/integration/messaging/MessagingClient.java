package se.sundsvall.checklist.integration.messaging;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.checklist.integration.messaging.configuration.MessagingConfiguration.CLIENT_ID;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.checklist.integration.messaging.configuration.MessagingConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.messaging.url}", configuration = MessagingConfiguration.class)
public interface MessagingClient {

	/**
	 * Send a single e-mail
	 *
	 * @param  emailRequest containing email information
	 * @return              response containing id and delivery results for sent message
	 */
	@PostMapping(path = "/{municipalityId}/email", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageResult sendEmail(
		@PathVariable("municipalityId") String municipalityId,
		@RequestBody EmailRequest emailRequest);
}
