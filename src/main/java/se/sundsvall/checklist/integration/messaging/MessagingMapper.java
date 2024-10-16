package se.sundsvall.checklist.integration.messaging;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;
import se.sundsvall.checklist.integration.messaging.configuration.MessagingProperties;

public final class MessagingMapper {

	private MessagingMapper() {
		// private constructor
	}

	public static EmailRequest toEmail(final String recipient, final String subject, final String htmlMessage, final MessagingProperties.Sender sender) {
		return new EmailRequest(recipient, subject)
			.htmlMessage(htmlMessage)
			.sender(new EmailSender()
				.address(sender.address())
				.name(sender.name())
				.replyTo(sender.replyTo()));
	}

}
