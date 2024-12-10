package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.messaging.DeliveryResult;
import generated.se.sundsvall.messaging.MessageResult;
import generated.se.sundsvall.messaging.MessageStatus;
import generated.se.sundsvall.templating.RenderResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CorrespondenceEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.messaging.MessagingIntegration;
import se.sundsvall.checklist.integration.templating.TemplatingIntegration;

@ExtendWith(MockitoExtension.class)
class MailHandlerTest {

	@Mock
	private MessagingIntegration messagingIntegrationMock;

	@Mock
	private TemplatingIntegration templatingIntegrationMock;

	@Mock
	private EmployeeChecklistRepository employeeChecklistRepositoryMock;

	@InjectMocks
	private MailHandler mailHandler;

	@Test
	void sendEmail() {
		final var municipalityId = "municipalityId";
		final var email = "some.email@noreply.com";
		final var manager = ManagerEntity.builder().withEmail(email).build();
		final var employee = EmployeeEntity.builder().withManager(manager).build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(employee)
			.withChecklists(List.of(ChecklistEntity.builder()
				.withMunicipalityId(municipalityId)
				.build()))
			.build();
		final var output = "output";
		final var renderReponse = new RenderResponse().output(output);
		final var deliveryResult = new DeliveryResult().status(MessageStatus.SENT);
		final var messageId = UUID.randomUUID();
		final var messageResult = new MessageResult()
			.messageId(messageId)
			.deliveries(List.of(deliveryResult));

		when(templatingIntegrationMock.renderTemplate(eq(municipalityId), any())).thenReturn(Optional.of(renderReponse));
		when(messagingIntegrationMock.sendEmail(municipalityId, email, output)).thenReturn(Optional.of(messageResult));

		// Act
		mailHandler.sendEmail(employeeChecklistEntity, "TEST");

		// Assert and verify
		verify(templatingIntegrationMock).renderTemplate(eq(municipalityId), any());
		verify(messagingIntegrationMock).sendEmail(municipalityId, email, output);
		verify(employeeChecklistRepositoryMock).save(employeeChecklistEntity);
		verifyNoMoreInteractions(employeeChecklistRepositoryMock, messagingIntegrationMock, templatingIntegrationMock);

		assertThat(employeeChecklistEntity.getCorrespondence()).isNotNull()
			.extracting(
				CorrespondenceEntity::getCommunicationChannel,
				CorrespondenceEntity::getCorrespondenceStatus,
				CorrespondenceEntity::getMessageId,
				CorrespondenceEntity::getRecipient,
				CorrespondenceEntity::getAttempts)
			.containsExactly(
				CommunicationChannel.EMAIL,
				CorrespondenceStatus.SENT,
				messageId.toString(),
				email,
				1);
	}

	@Test
	void resendEmail() {
		final var municipalityId = "municipalityId";
		final var email = "some.email@noreply.com";
		final var attempts = 123;
		final var correspondence = CorrespondenceEntity.builder().withAttempts(attempts).build();
		final var manager = ManagerEntity.builder().withEmail(email).build();
		final var employee = EmployeeEntity.builder().withManager(manager).build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(employee)
			.withChecklists(List.of(ChecklistEntity.builder()
				.withMunicipalityId(municipalityId)
				.build()))
			.withCorrespondence(correspondence)
			.build();
		final var output = "output";
		final var renderReponse = new RenderResponse().output(output);
		final var deliveryResult = new DeliveryResult().status(MessageStatus.SENT);
		final var messageId = UUID.randomUUID();
		final var messageResult = new MessageResult()
			.messageId(messageId)
			.deliveries(List.of(deliveryResult));

		when(templatingIntegrationMock.renderTemplate(eq(municipalityId), any())).thenReturn(Optional.of(renderReponse));
		when(messagingIntegrationMock.sendEmail(municipalityId, email, output)).thenReturn(Optional.of(messageResult));

		// Act
		mailHandler.sendEmail(employeeChecklistEntity, "TEST");

		// Assert and verify
		verify(templatingIntegrationMock).renderTemplate(eq(municipalityId), any());
		verify(messagingIntegrationMock).sendEmail(municipalityId, email, output);
		verify(employeeChecklistRepositoryMock).save(employeeChecklistEntity);
		verifyNoMoreInteractions(employeeChecklistRepositoryMock, messagingIntegrationMock, templatingIntegrationMock);

		assertThat(employeeChecklistEntity.getCorrespondence()).isNotNull()
			.extracting(
				CorrespondenceEntity::getCommunicationChannel,
				CorrespondenceEntity::getCorrespondenceStatus,
				CorrespondenceEntity::getMessageId,
				CorrespondenceEntity::getRecipient,
				CorrespondenceEntity::getAttempts)
			.containsExactly(
				CommunicationChannel.EMAIL,
				CorrespondenceStatus.SENT,
				messageId.toString(),
				email,
				attempts + 1);
	}

	@Test
	void sendEmailWhenTemplateDoesNotExist() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var email = "some.email@noreply.com";
		final var manager = ManagerEntity.builder().withEmail(email).build();
		final var employee = EmployeeEntity.builder().withManager(manager).build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(employee)
			.withChecklists(List.of(ChecklistEntity.builder()
				.withMunicipalityId(municipalityId)
				.build()))
			.build();

		// Act
		mailHandler.sendEmail(employeeChecklistEntity, "TEST");

		// Assert and verify
		verify(templatingIntegrationMock).renderTemplate(eq(municipalityId), any());
		verify(employeeChecklistRepositoryMock).save(employeeChecklistEntity);
		verifyNoMoreInteractions(employeeChecklistRepositoryMock, templatingIntegrationMock);
		verifyNoInteractions(messagingIntegrationMock);

		assertThat(employeeChecklistEntity.getCorrespondence()).isNotNull()
			.extracting(
				CorrespondenceEntity::getCommunicationChannel,
				CorrespondenceEntity::getCorrespondenceStatus,
				CorrespondenceEntity::getRecipient,
				CorrespondenceEntity::getAttempts)
			.containsExactly(
				CommunicationChannel.EMAIL,
				CorrespondenceStatus.ERROR,
				email,
				1);
	}

	@Test
	void sendEmailWhenEmailReponseIsNotPresent() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var email = "some.email@noreply.com";
		final var previousRetries = 123;
		final var correspondence = CorrespondenceEntity.builder().withAttempts(previousRetries).build();
		final var manager = ManagerEntity.builder().withEmail(email).build();
		final var employee = EmployeeEntity.builder().withManager(manager).build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(employee)
			.withChecklists(List.of(ChecklistEntity.builder()
				.withMunicipalityId(municipalityId)
				.build()))
			.withCorrespondence(correspondence)
			.build();
		final var output = "output";
		final var renderReponse = new RenderResponse().output(output);

		when(templatingIntegrationMock.renderTemplate(eq(municipalityId), any())).thenReturn(Optional.of(renderReponse));

		// Act
		mailHandler.sendEmail(employeeChecklistEntity, "TEST");

		// Assert and verify
		verify(templatingIntegrationMock).renderTemplate(eq(municipalityId), any());
		verify(messagingIntegrationMock).sendEmail(municipalityId, email, output);
		verify(employeeChecklistRepositoryMock).save(employeeChecklistEntity);
		verifyNoMoreInteractions(employeeChecklistRepositoryMock, messagingIntegrationMock, templatingIntegrationMock);

		assertThat(employeeChecklistEntity.getCorrespondence()).isNotNull()
			.extracting(
				CorrespondenceEntity::getCommunicationChannel,
				CorrespondenceEntity::getCorrespondenceStatus,
				CorrespondenceEntity::getRecipient,
				CorrespondenceEntity::getAttempts)
			.containsExactly(
				CommunicationChannel.EMAIL,
				CorrespondenceStatus.ERROR,
				email,
				previousRetries + 1);
	}
}
