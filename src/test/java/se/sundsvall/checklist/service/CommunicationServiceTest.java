package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.EMAIL;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.NO_COMMUNICATION;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.ERROR;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.NOT_SENT;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.integration.db.model.CorrespondenceEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;

@ExtendWith(MockitoExtension.class)
class CommunicationServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";

	@Mock
	private MailHandler mailHandlerMock;

	@Mock
	private EmployeeChecklistRepository employeeChecklistRepositoryMock;

	@InjectMocks
	private CommunicationService service;

	@Captor
	private ArgumentCaptor<EmployeeChecklistEntity> entityCaptor;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(
			employeeChecklistRepositoryMock,
			mailHandlerMock);
	}

	@Test
	void fetchCorrespondence() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var correspondence = CorrespondenceEntity.builder().build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder().withCorrespondence(correspondence).build();

		when(employeeChecklistRepositoryMock.findByIdAndChecklistsMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(employeeChecklistEntity));

		// Act
		final var result = service.fetchCorrespondence(MUNICIPALITY_ID, id);

		// Assert and verify
		assertThat(result).isNotNull();
		verify(employeeChecklistRepositoryMock).findByIdAndChecklistsMunicipalityId(id, MUNICIPALITY_ID);
	}

	@Test
	void fetchCorrespondenceForNonExistingEmployeeChecklist() {
		// Arrange
		final var id = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.fetchCorrespondence(MUNICIPALITY_ID, id));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s not found within municipality %s.".formatted(id, MUNICIPALITY_ID));
		verify(employeeChecklistRepositoryMock).findByIdAndChecklistsMunicipalityId(id, MUNICIPALITY_ID);
	}

	@Test
	void sendEmail() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(EmployeeEntity.builder()
				.withManager(ManagerEntity.builder()
					.build())
				.build())
			.build();

		when(employeeChecklistRepositoryMock.findByIdAndChecklistsMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(employeeChecklistEntity));

		// Act
		service.sendEmail(MUNICIPALITY_ID, id);

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findByIdAndChecklistsMunicipalityId(id, MUNICIPALITY_ID);
		verify(mailHandlerMock).sendEmail(eq(employeeChecklistEntity), any());
	}

	@Test
	void sendEmailForNonExistingEmployeeChecklist() {
		// Arrange
		final var id = UUID.randomUUID().toString();

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.sendEmail(MUNICIPALITY_ID, id));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Employee checklist with id %s not found within municipality %s.".formatted(id, MUNICIPALITY_ID));
		verify(employeeChecklistRepositoryMock).findByIdAndChecklistsMunicipalityId(id, MUNICIPALITY_ID);

	}

	@Test
	void sendEmailForEntity() {
		// Arrange
		final var entity = EmployeeChecklistEntity.builder()
			.withEmployee(EmployeeEntity.builder()
				.withManager(ManagerEntity.builder()
					.build())
				.build())
			.build();

		// Act
		service.sendEmail(entity);

		// Assert and verify
		verify(mailHandlerMock).sendEmail(eq(entity), any());
	}

	@Test
	void fetchManagersToSendMailToWhenRecipientsFound() {
		// Arrange
		final var checklistWithNoCommunication = EmployeeChecklistEntity.builder()
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withCommunicationChannels(Set.of(EMAIL))
					.build())
				.withManager(ManagerEntity.builder()
					.build())
				.build())
			.build();
		final var checklistWithFailedCommunication = EmployeeChecklistEntity.builder()
			.withCorrespondence(CorrespondenceEntity.builder()
				.withCorrespondenceStatus(NOT_SENT)
				.build())
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withCommunicationChannels(Set.of(EMAIL))
					.build())
				.withManager(ManagerEntity.builder()
					.build())
				.build())
			.build();

		when(employeeChecklistRepositoryMock.findAllByChecklistsMunicipalityIdAndCorrespondenceIsNull(MUNICIPALITY_ID)).thenReturn(List.of(checklistWithNoCommunication, checklistWithFailedCommunication));

		// Act
		final var result = service.fetchManagersToSendMailTo(MUNICIPALITY_ID);

		// Assert and verify
		assertThat(result).hasSize(2).satisfiesExactlyInAnyOrder(e -> assertThat(e).isSameAs(checklistWithNoCommunication), e -> assertThat(e).isSameAs(checklistWithFailedCommunication));

		verify(employeeChecklistRepositoryMock).findAllByChecklistsMunicipalityIdAndCorrespondenceIsNull(MUNICIPALITY_ID);
		verify(employeeChecklistRepositoryMock).findAllByChecklistsMunicipalityIdAndCorrespondenceCorrespondenceStatus(MUNICIPALITY_ID, NOT_SENT);
	}

	@Test
	void fetchManagersToSendMailToForOptedOutCompany() {
		// Arrange
		final var checklistWithNoCommunication = EmployeeChecklistEntity.builder()
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withCommunicationChannels(Set.of(NO_COMMUNICATION))
					.build())
				.withManager(ManagerEntity.builder()
					.build())
				.build())
			.build();
		final var checklistWithFailedCommunication = EmployeeChecklistEntity.builder()
			.withCorrespondence(CorrespondenceEntity.builder()
				.withCorrespondenceStatus(NOT_SENT)
				.build())
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withCommunicationChannels(Set.of(NO_COMMUNICATION))
					.build())
				.withManager(ManagerEntity.builder()
					.build())
				.build())
			.build();

		when(employeeChecklistRepositoryMock.findAllByChecklistsMunicipalityIdAndCorrespondenceIsNull(MUNICIPALITY_ID)).thenReturn(List.of(checklistWithNoCommunication, checklistWithFailedCommunication));

		// Act
		final var result = service.fetchManagersToSendMailTo(MUNICIPALITY_ID);

		// Assert and verify
		assertThat(result).isEmpty();

		verify(employeeChecklistRepositoryMock).findAllByChecklistsMunicipalityIdAndCorrespondenceIsNull(MUNICIPALITY_ID);
		verify(employeeChecklistRepositoryMock).findAllByChecklistsMunicipalityIdAndCorrespondenceCorrespondenceStatus(MUNICIPALITY_ID, NOT_SENT);
		verify(employeeChecklistRepositoryMock, times(2)).save(entityCaptor.capture());
		assertThat(entityCaptor.getAllValues()).hasSize(2).allSatisfy(e -> {
			assertThat(e.getCorrespondence().getCorrespondenceStatus()).isEqualTo(CorrespondenceStatus.WILL_NOT_SEND);
		});
	}

	@Test
	void countCorrespondenceWithErrors() {
		// Arrange
		final var errors = 123;
		when(employeeChecklistRepositoryMock.countByCorrespondenceCorrespondenceStatus(ERROR)).thenReturn(errors);

		// Act
		final var result = service.countCorrespondenceWithErrors();

		// Assert and verify

		assertThat(result).isEqualTo(errors);

		verify(employeeChecklistRepositoryMock).countByCorrespondenceCorrespondenceStatus(ERROR);
	}
}
