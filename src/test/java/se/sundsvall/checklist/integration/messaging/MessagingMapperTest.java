package se.sundsvall.checklist.integration.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import se.sundsvall.checklist.integration.db.model.CorrespondenceEntity;
import se.sundsvall.checklist.integration.messaging.configuration.MessagingProperties;

class MessagingMapperTest {

	@Test
	void toEmail() {

		// Arrange
		final var recipient = "someRecipient";
		final var subject = "someSubject";
		final var message = "someMessage";
		final var address = "someAddress";
		final var name = "someName";
		final var replyTo = "someReplyTo";

		final var entity = CorrespondenceEntity.builder().withRecipient(recipient).build();
		final var htmlMessage = Base64.getEncoder().encodeToString(message.getBytes());
		final var sender = new MessagingProperties.Sender(address, name, replyTo);

		// Act
		final var result = MessagingMapper.toEmail(entity.getRecipient(), subject, htmlMessage, sender);

		// Assert and verify
		assertThat(result).satisfies(
			emailRequest -> {
				assertThat(emailRequest.getEmailAddress()).isEqualTo(recipient);
				assertThat(emailRequest.getSubject()).isEqualTo(subject);
				assertThat(emailRequest.getHtmlMessage()).isEqualTo(htmlMessage);
				assertThat(emailRequest.getSender()).satisfies(
					emailSender -> {
						assertThat(emailSender.getAddress()).isEqualTo(address);
						assertThat(emailSender.getName()).isEqualTo(name);
						assertThat(emailSender.getReplyTo()).isEqualTo(replyTo);
					});
			});
	}

}
