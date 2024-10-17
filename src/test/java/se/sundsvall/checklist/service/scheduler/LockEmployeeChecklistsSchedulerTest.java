package se.sundsvall.checklist.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
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

import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;

@ExtendWith(MockitoExtension.class)
class LockEmployeeChecklistsSchedulerTest {

	@Mock
	private EmployeeChecklistRepository employeeChecklistRepositoryMock;

	@Captor
	private ArgumentCaptor<EmployeeChecklistEntity> entityCaptor;

	@InjectMocks
	private LockEmployeeChecklistsScheduler scheduler;

	@Test
	void executeWhenNoLockableEmployeeChecklistsExists() {
		// Act
		scheduler.execute();

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findAllByExpirationDateIsBeforeAndLockedIsFalse(LocalDate.now());
		verifyNoMoreInteractions(employeeChecklistRepositoryMock);
	}

	@Test
	void executeWhenLockableEmployeeChecklistsExists() {
		// Arrange
		final var employeeChecklistsId = UUID.randomUUID().toString();
		final var employeeChecklistsEntity = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistsId)
			.build();

		when(employeeChecklistRepositoryMock.findAllByExpirationDateIsBeforeAndLockedIsFalse(LocalDate.now())).thenReturn(List.of(employeeChecklistsEntity));

		// Act
		scheduler.execute();

		// Assert and verify
		verify(employeeChecklistRepositoryMock).findAllByExpirationDateIsBeforeAndLockedIsFalse(LocalDate.now());
		verify(employeeChecklistRepositoryMock).save(entityCaptor.capture());
		verifyNoMoreInteractions(employeeChecklistRepositoryMock);

		assertThat(entityCaptor.getValue()).isEqualTo(employeeChecklistsEntity);
		assertThat(entityCaptor.getValue().isLocked()).isTrue();
	}
}
