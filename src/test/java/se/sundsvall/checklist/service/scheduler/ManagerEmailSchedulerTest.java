package se.sundsvall.checklist.service.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.EMAIL;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.NO_COMMUNICATION;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.NOT_SENT;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.checklist.integration.db.model.CorrespondenceEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.service.CommunicationService;

@ExtendWith(MockitoExtension.class)
class ManagerEmailSchedulerTest {

	@Mock
	private EmployeeChecklistRepository employeeChecklistRepositoryMock;

	@Mock
	private CommunicationService communicationServiceMock;

	@InjectMocks
	private ManagerEmailScheduler scheduler;

	@Test
	void executeWhenEmployeeChecklistsWithNoPreviousCorrespondenceFound() {
		// Arrange
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withCommunicationChannels(Set.of(EMAIL))
					.build())
				.withManager(ManagerEntity.builder()
					.build())
				.build())
			.build();

		when(employeeChecklistRepositoryMock.findAllByCorrespondenceIsNull()).thenReturn(List.of(employeeChecklistEntity));

		// Act
		scheduler.execute();

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findAllByCorrespondenceIsNull();
		verify(employeeChecklistRepositoryMock).findAllByCorrespondenceCorrespondenceStatus(NOT_SENT);
		verify(communicationServiceMock).sendEmail(employeeChecklistEntity);
		verifyNoMoreInteractions(employeeChecklistRepositoryMock, communicationServiceMock);
	}

	@Test
	void executeWhenEmployeeChecklistsWithPreviousNonSuccessfulCorrespondenceFound() {
		// Arrange
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
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

		when(employeeChecklistRepositoryMock.findAllByCorrespondenceCorrespondenceStatus(NOT_SENT)).thenReturn(List.of(employeeChecklistEntity));

		// Act
		scheduler.execute();

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findAllByCorrespondenceIsNull();
		verify(employeeChecklistRepositoryMock).findAllByCorrespondenceCorrespondenceStatus(NOT_SENT);
		verify(communicationServiceMock).sendEmail(employeeChecklistEntity);
		verifyNoMoreInteractions(employeeChecklistRepositoryMock, communicationServiceMock);
	}

	@Test
	void executeWhenEmployeeChecklistsWithNoPreviousCorrespondenceFoundForOptedOutCompanys() {
		// Arrange
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withCommunicationChannels(Set.of(NO_COMMUNICATION))
					.build())
				.withManager(ManagerEntity.builder()
					.withEmail("manager@email.com")
					.build())
				.build())
			.build();

		when(employeeChecklistRepositoryMock.findAllByCorrespondenceIsNull()).thenReturn(List.of(employeeChecklistEntity));

		// Act
		scheduler.execute();

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findAllByCorrespondenceIsNull();
		verify(employeeChecklistRepositoryMock).findAllByCorrespondenceCorrespondenceStatus(NOT_SENT);
		verify(communicationServiceMock, never()).sendEmail(employeeChecklistEntity);
	}

	@Test
	void executeWhenEmployeeChecklistsWithPreviousNonSuccessfulCorrespondenceFoundForOptedOutCompanys() {
		// Arrange
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withCorrespondence(CorrespondenceEntity.builder()
				.withCorrespondenceStatus(NOT_SENT)
				.build())
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withCommunicationChannels(Set.of(NO_COMMUNICATION))
					.build())
				.withManager(ManagerEntity.builder()
					.withEmail("manager@email.com")
					.build())
				.build())
			.build();

		when(employeeChecklistRepositoryMock.findAllByCorrespondenceCorrespondenceStatus(NOT_SENT)).thenReturn(List.of(employeeChecklistEntity));

		// Act
		scheduler.execute();

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findAllByCorrespondenceIsNull();
		verify(employeeChecklistRepositoryMock).findAllByCorrespondenceCorrespondenceStatus(NOT_SENT);
		verify(communicationServiceMock, never()).sendEmail(employeeChecklistEntity);
	}
}
