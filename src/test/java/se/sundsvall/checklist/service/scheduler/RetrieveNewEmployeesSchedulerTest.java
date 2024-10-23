package se.sundsvall.checklist.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.service.EmployeeChecklistService;

@ExtendWith(MockitoExtension.class)
class RetrieveNewEmployeesSchedulerTest {

	@Mock
	private EmployeeChecklistService employeeChecklistServiceMock;

	@Mock
	private ChecklistProperties checklistPropertiesMock;

	@InjectMocks
	private RetrieveNewEmployeesScheduler scheduler;

	@Test
	void execute() {
		// Arrange
		final var municipalityId = "municipalityId";

		when(checklistPropertiesMock.managedMunicipalityIds()).thenReturn(List.of(municipalityId));
		when(employeeChecklistServiceMock.initiateEmployeeChecklists(municipalityId)).thenReturn(EmployeeChecklistResponse.builder()
			.withSummary("summary")
			.withDetails(List.of(
				Detail.builder()
					.withInformation("Ok string")
					.withStatus(Status.OK)
					.build(),
				Detail.builder()
					.withInformation("Error string")
					.withStatus(Status.NOT_FOUND)
					.build()))
			.build());

		scheduler.execute();

		verify(checklistPropertiesMock, times(2)).managedMunicipalityIds();
		verify(employeeChecklistServiceMock).initiateEmployeeChecklists(municipalityId);
		verifyNoMoreInteractions(employeeChecklistServiceMock, checklistPropertiesMock);
	}

	@Test
	void executeWhenNoManagedMunicipalitiesExists() {
		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> scheduler.execute());

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(e.getMessage()).isEqualTo("Internal Server Error: No managed municipalities was found, please verify service properties.");
		verify(checklistPropertiesMock).managedMunicipalityIds();
		verifyNoMoreInteractions(employeeChecklistServiceMock, checklistPropertiesMock);
	}
}
