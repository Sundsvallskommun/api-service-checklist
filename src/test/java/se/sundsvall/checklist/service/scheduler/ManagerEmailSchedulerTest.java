package se.sundsvall.checklist.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.service.CommunicationService;

@ExtendWith(MockitoExtension.class)
class ManagerEmailSchedulerTest {
	private static final String MUNICIPALITY_ID = "municipalityId";

	@Mock
	private CommunicationService communicationServiceMock;

	@Mock
	private ChecklistProperties checklistPropertiesMock;

	@InjectMocks
	private ManagerEmailScheduler scheduler;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(checklistPropertiesMock, communicationServiceMock);
	}

	@Test
	void executeWhenReciviersExist() {
		// Arrange
		final var entity = EmployeeChecklistEntity.builder().build();

		when(checklistPropertiesMock.managedMunicipalityIds()).thenReturn(List.of(MUNICIPALITY_ID));
		when(communicationServiceMock.fetchManagersToSendMailTo(MUNICIPALITY_ID)).thenReturn(List.of(entity));

		// Act
		scheduler.execute();

		// Assert and verify
		verify(checklistPropertiesMock, times(2)).managedMunicipalityIds();
		verify(communicationServiceMock).fetchManagersToSendMailTo(MUNICIPALITY_ID);
		verify(communicationServiceMock).sendEmail(entity);
	}

	@Test
	void executeWhenRecieversNotExist() {
		// Arrange
		when(checklistPropertiesMock.managedMunicipalityIds()).thenReturn(List.of(MUNICIPALITY_ID));
		// Act
		scheduler.execute();

		// Assert and verify
		verify(checklistPropertiesMock, times(2)).managedMunicipalityIds();
		verify(communicationServiceMock).fetchManagersToSendMailTo(MUNICIPALITY_ID);
	}

	@Test
	void executeWhenNoManagedMunicipalitiesExists() {
		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> scheduler.execute());

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(e.getMessage()).isEqualTo("Internal Server Error: No managed municipalities was found, please verify service properties.");
		verify(checklistPropertiesMock).managedMunicipalityIds();
	}

	@Test
	void executeWhenSendingEmailThrowsException() {
		// Arrange
		final var entityFail = EmployeeChecklistEntity.builder().build();
		final var entityOk = EmployeeChecklistEntity.builder().build();

		when(checklistPropertiesMock.managedMunicipalityIds()).thenReturn(List.of(MUNICIPALITY_ID));
		when(communicationServiceMock.fetchManagersToSendMailTo(MUNICIPALITY_ID)).thenReturn(List.of(entityFail, entityOk));
		doThrow(Problem.valueOf(Status.BAD_GATEWAY, "Bad to the bone")).when(communicationServiceMock).sendEmail(entityFail);

		// Act
		scheduler.execute();

		// Assert and verify
		verify(checklistPropertiesMock, times(2)).managedMunicipalityIds();
		verify(communicationServiceMock).fetchManagersToSendMailTo(MUNICIPALITY_ID);
		verify(communicationServiceMock).sendEmail(entityFail);
		verify(communicationServiceMock).sendEmail(entityOk);

	}
}
