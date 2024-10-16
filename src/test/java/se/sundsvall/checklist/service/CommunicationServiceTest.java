package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.messaging.DeliveryResult;
import generated.se.sundsvall.messaging.MessageResult;
import generated.se.sundsvall.messaging.MessageStatus;
import generated.se.sundsvall.templating.RenderResponse;
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
class CommunicationServiceTest {

	@Mock
	private MessagingIntegration messagingIntegrationMock;

	@Mock
	private TemplatingIntegration templatingIntegrationMock;

	@Mock
	private EmployeeChecklistRepository employeeChecklistRepositoryMock;

	@InjectMocks
	private CommunicationService service;

	@Test
	void fetchCorrespondence() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var correspondence = CorrespondenceEntity.builder().build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().withCorrespondence(correspondence).build();

		when(employeeChecklistRepositoryMock.findById(id)).thenReturn(Optional.of(employeeChecklistEntity));

		// Act
		final var result = service.fetchCorrespondence(id);

		// Assert and verify
		assertThat(result).isNotNull();
		verify(employeeChecklistRepositoryMock).findById(id);
		verifyNoMoreInteractions(employeeChecklistRepositoryMock);
		verifyNoInteractions(messagingIntegrationMock, templatingIntegrationMock);
	}

	@Test
	void fetchCorrespondenceForNonExistingEmployeeChecklist() {
		// Arrange
		final var id = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.fetchCorrespondence(id));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s not found".formatted(id));
		verify(employeeChecklistRepositoryMock).findById(id);
		verifyNoMoreInteractions(employeeChecklistRepositoryMock);
		verifyNoInteractions(messagingIntegrationMock, templatingIntegrationMock);
	}

	@Test
	void sendEmail() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var email = "some.email@noreply.com";
		final var manager = ManagerEntity.builder().withEmail(email).build();
		final var employee = EmployeeEntity.builder().withManager(manager).build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(employee)
			.build();
		final var output = "output";
		final var renderReponse = new RenderResponse().output(output);
		final var deliveryResult = new DeliveryResult().status(MessageStatus.SENT);
		final var messageId = UUID.randomUUID();
		final var messageResult = new MessageResult()
			.messageId(messageId)
			.deliveries(List.of(deliveryResult));

		when(employeeChecklistRepositoryMock.findById(id)).thenReturn(Optional.of(employeeChecklistEntity));
		when(templatingIntegrationMock.renderTemplate(any())).thenReturn(Optional.of(renderReponse));
		when(messagingIntegrationMock.sendEmail(email, output)).thenReturn(Optional.of(messageResult));

		// Act
		service.sendEmail(id);

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findById(id);
		verify(templatingIntegrationMock).renderTemplate(any());
		verify(messagingIntegrationMock).sendEmail(email, output);
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
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var email = "some.email@noreply.com";
		final var attempts = 123;
		final var correspondence = CorrespondenceEntity.builder().withAttempts(attempts).build();
		final var manager = ManagerEntity.builder().withEmail(email).build();
		final var employee = EmployeeEntity.builder().withManager(manager).build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(employee)
			.withCorrespondence(correspondence)
			.build();
		final var output = "output";
		final var renderReponse = new RenderResponse().output(output);
		final var deliveryResult = new DeliveryResult().status(MessageStatus.SENT);
		final var messageId = UUID.randomUUID();
		final var messageResult = new MessageResult()
			.messageId(messageId)
			.deliveries(List.of(deliveryResult));

		when(employeeChecklistRepositoryMock.findById(id)).thenReturn(Optional.of(employeeChecklistEntity));
		when(templatingIntegrationMock.renderTemplate(any())).thenReturn(Optional.of(renderReponse));
		when(messagingIntegrationMock.sendEmail(email, output)).thenReturn(Optional.of(messageResult));

		// Act
		service.sendEmail(id);

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findById(id);
		verify(templatingIntegrationMock).renderTemplate(any());
		verify(messagingIntegrationMock).sendEmail(email, output);
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
		final var id = UUID.randomUUID().toString();
		final var email = "some.email@noreply.com";
		final var manager = ManagerEntity.builder().withEmail(email).build();
		final var employee = EmployeeEntity.builder().withManager(manager).build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(employee)
			.build();

		when(employeeChecklistRepositoryMock.findById(id)).thenReturn(Optional.of(employeeChecklistEntity));

		// Act
		service.sendEmail(id);

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findById(id);
		verify(templatingIntegrationMock).renderTemplate(any());
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
		final var id = UUID.randomUUID().toString();
		final var email = "some.email@noreply.com";
		final var previousRetries = 123;
		final var correspondence = CorrespondenceEntity.builder().withAttempts(previousRetries).build();
		final var manager = ManagerEntity.builder().withEmail(email).build();
		final var employee = EmployeeEntity.builder().withManager(manager).build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(employee)
			.withCorrespondence(correspondence)
			.build();
		final var output = "output";
		final var renderReponse = new RenderResponse().output(output);

		when(employeeChecklistRepositoryMock.findById(id)).thenReturn(Optional.of(employeeChecklistEntity));
		when(templatingIntegrationMock.renderTemplate(any())).thenReturn(Optional.of(renderReponse));

		// Act
		service.sendEmail(id);

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findById(id);
		verify(templatingIntegrationMock).renderTemplate(any());
		verify(messagingIntegrationMock).sendEmail(email, output);
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

	@Test
	void sendEmailForNonExistingEmployeeChecklist() {
		// Arrange
		final var id = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.sendEmail(id));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s not found".formatted(id));
		verify(employeeChecklistRepositoryMock).findById(id);
		verifyNoMoreInteractions(employeeChecklistRepositoryMock);
		verifyNoInteractions(messagingIntegrationMock, templatingIntegrationMock);

	}
}
