package se.sundsvall.checklist.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;

@ExtendWith(MockitoExtension.class)
class LockEmployeeChecklistsSchedulerTest {

	@Mock
	private EmployeeChecklistRepository employeeChecklistRepositoryMock;

	@Mock
	private ChecklistProperties checklistPropertiesMock;

	@InjectMocks
	private LockEmployeeChecklistsScheduler scheduler;

	@Captor
	private ArgumentCaptor<EmployeeChecklistEntity> entityCaptor;

	@Test
	void executeWhenNoLockableEmployeeChecklistsExists() {
		// Arrange
		final var municipalityId = "municipalityId";

		when(checklistPropertiesMock.managedMunicipalityIds()).thenReturn(List.of(municipalityId));

		// Act
		scheduler.execute();

		// Assert and verify
		verify(checklistPropertiesMock, times(2)).managedMunicipalityIds();
		verify(employeeChecklistRepositoryMock).findAllByChecklistMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse(municipalityId, LocalDate.now());
		verifyNoMoreInteractions(employeeChecklistRepositoryMock);
	}

	@Test
	void executeWhenLockableEmployeeChecklistsExists() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var employeeChecklistsId = UUID.randomUUID().toString();
		final var employeeChecklistsEntity = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistsId)
			.build();

		when(checklistPropertiesMock.managedMunicipalityIds()).thenReturn(List.of(municipalityId));
		when(employeeChecklistRepositoryMock.findAllByChecklistMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse(municipalityId, LocalDate.now())).thenReturn(List.of(employeeChecklistsEntity));

		// Act
		scheduler.execute();

		// Assert and verify
		verify(checklistPropertiesMock, times(2)).managedMunicipalityIds();
		verify(employeeChecklistRepositoryMock).findAllByChecklistMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse(municipalityId, LocalDate.now());
		verify(employeeChecklistRepositoryMock).save(entityCaptor.capture());
		verifyNoMoreInteractions(employeeChecklistRepositoryMock, checklistPropertiesMock);

		assertThat(entityCaptor.getValue()).isEqualTo(employeeChecklistsEntity);
		assertThat(entityCaptor.getValue().isLocked()).isTrue();
		verifyNoMoreInteractions(employeeChecklistRepositoryMock, checklistPropertiesMock);
	}

	@Test
	void executeWhenNoManagedMunicipalitiesExists() {
		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> scheduler.execute());

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(e.getMessage()).isEqualTo("Internal Server Error: No managed municipalities was found, please verify service properties.");
		verify(checklistPropertiesMock).managedMunicipalityIds();
		verifyNoMoreInteractions(employeeChecklistRepositoryMock, checklistPropertiesMock);
	}
}
