package se.sundsvall.checklist.integration.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.MessageResult;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.checklist.integration.db.model.CorrespondenceEntity;
import se.sundsvall.checklist.integration.messaging.configuration.MessagingProperties;

@ExtendWith(MockitoExtension.class)
class MessagingIntegrationTest {

	private static final String MUNICIPALITY_ID = "municipalityId";

	@Mock
	private MessagingClient messagingClientMock;

	@Mock
	private MessagingProperties messagingProperties;

	@InjectMocks
	private MessagingIntegration messagingIntegration;

	@Test
	void sendEmail() {

		// Arrange
		final var correspondenceEntity = CorrespondenceEntity.builder().withRecipient("someRecipient").build();
		final var subject = "someSubject";
		final var htmlMessage = "someHtmlMessage";
		final var uuid = UUID.randomUUID();
		final var address = "someAddress";
		final var name = "someName";
		final var replyTo = "someReplyTo";
		final var sender = new MessagingProperties.Sender(address, name, replyTo);
		final var email = new MessagingProperties.Email(subject, sender);

		when(messagingClientMock.sendEmail(eq(MUNICIPALITY_ID), any(EmailRequest.class))).thenReturn(new MessageResult().messageId(uuid));
		when(messagingProperties.managerEmail()).thenReturn(email);

		// Act
		final var result = messagingIntegration.sendEmail(MUNICIPALITY_ID, correspondenceEntity.getRecipient(), htmlMessage);

		// Assert and verify
		assertThat(result).isPresent();
		assertThat(result.get().getMessageId()).isNotNull().isEqualTo(uuid);
		verify(messagingClientMock).sendEmail(eq(MUNICIPALITY_ID), any(EmailRequest.class));
		verifyNoMoreInteractions(messagingClientMock);
	}

	@Test
	void sendEmail_noSender() {

		// Arrange
		final var correspondenceEntity = CorrespondenceEntity.builder().withRecipient("someRecipient").build();
		final var subject = "someSubject";
		final var htmlMessage = "someHtmlMessage";
		final var email = new MessagingProperties.Email(subject, null);
		when(messagingProperties.managerEmail()).thenReturn(email);

		// Act
		final var result = messagingIntegration.sendEmail(MUNICIPALITY_ID, correspondenceEntity.getRecipient(), htmlMessage);

		// Assert and verify
		assertThat(result).isEmpty();
		verifyNoInteractions(messagingClientMock);
	}

	@Test
	void sendEmail_noRecipient() {

		// Arrange
		final var correspondenceEntity = CorrespondenceEntity.builder().build();
		final var subject = "someSubject";
		final var htmlMessage = "someHtmlMessage";
		final var address = "someAddress";
		final var name = "someName";
		final var replyTo = "someReplyTo";
		final var sender = new MessagingProperties.Sender(address, name, replyTo);
		final var email = new MessagingProperties.Email(subject, sender);

		when(messagingProperties.managerEmail()).thenReturn(email);

		// Act
		final var result = messagingIntegration.sendEmail(MUNICIPALITY_ID, correspondenceEntity.getRecipient(), htmlMessage);

		// Assert and verify
		assertThat(result).isEmpty();
		verify(messagingClientMock).sendEmail(eq(MUNICIPALITY_ID), any(EmailRequest.class));
		verifyNoMoreInteractions(messagingClientMock);
	}

	@Test
	void sendEmail_noHtmlMessage() {

		// Arrange
		final var correspondenceEntity = CorrespondenceEntity.builder().withRecipient("someRecipient").build();
		final var subject = "someSubject";
		final var address = "someAddress";
		final var name = "someName";
		final var replyTo = "someReplyTo";
		final var sender = new MessagingProperties.Sender(address, name, replyTo);
		final var email = new MessagingProperties.Email(subject, sender);

		when(messagingProperties.managerEmail()).thenReturn(email);

		// Act
		final var result = messagingIntegration.sendEmail(MUNICIPALITY_ID, correspondenceEntity.getRecipient(), null);

		// Assert and verify
		assertThat(result).isEmpty();
		verify(messagingClientMock).sendEmail(eq(MUNICIPALITY_ID), any(EmailRequest.class));
		verifyNoMoreInteractions(messagingClientMock);
	}

	@Test
	void sendEmail_serverError() {

		// Arrange
		final var correspondenceEntity = CorrespondenceEntity.builder().withRecipient("someRecipient").build();
		final var subject = "someSubject";
		final var htmlMessage = "someHtmlMessage";
		final var address = "someAddress";
		final var name = "someName";
		final var replyTo = "someReplyTo";
		final var sender = new MessagingProperties.Sender(address, name, replyTo);
		final var email = new MessagingProperties.Email(subject, sender);

		when(messagingClientMock.sendEmail(eq(MUNICIPALITY_ID), any(EmailRequest.class))).thenThrow(new RuntimeException());
		when(messagingProperties.managerEmail()).thenReturn(email);

		// Act
		final var result = messagingIntegration.sendEmail(MUNICIPALITY_ID, correspondenceEntity.getRecipient(), htmlMessage);

		// Assert and verify
		assertThat(result).isEmpty();
		verify(messagingClientMock).sendEmail(eq(MUNICIPALITY_ID), any(EmailRequest.class));
		verifyNoMoreInteractions(messagingClientMock);
	}

}
