package se.sundsvall.checklist.integration.messaging;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.messaging.MessageResult;
import se.sundsvall.checklist.integration.messaging.configuration.MessagingProperties;

@Component
public class MessagingIntegration {

	private final Logger log = LoggerFactory.getLogger(MessagingIntegration.class.getName());

	private final MessagingProperties messagingProperties;

	private final MessagingClient messagingClient;

	public MessagingIntegration(final MessagingProperties messagingProperties, final MessagingClient messagingClient) {
		this.messagingProperties = messagingProperties;
		this.messagingClient = messagingClient;
	}

	public Optional<MessageResult> sendEmail(final String recipient, final String message) {
		try {
			return Optional.of(messagingClient.sendEmail("2281", MessagingMapper.toEmail(
				recipient,
				messagingProperties.managerEmail().subject(),
				message,
				messagingProperties.managerEmail().sender())));
		} catch (final Exception e) {
			log.warn("Error while sending email", e);
			return Optional.empty();
		}
	}
}
