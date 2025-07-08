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
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.service.EmployeeChecklistService;

@ExtendWith(MockitoExtension.class)
class UpdateEmployeeManagerSchedulerTest {

	@Mock
	private EmployeeChecklistService employeeChecklistServiceMock;

	@Mock
	private ChecklistProperties checklistPropertiesMock;

	@InjectMocks
	private UpdateEmployeeManagerScheduler scheduler;

	@Test
	void execute() {
		// Arrange
		final var municipalityId = "municipalityId";

		when(checklistPropertiesMock.managedMunicipalityIds()).thenReturn(List.of(municipalityId));
		when(employeeChecklistServiceMock.updateManagerInformation(municipalityId, null)).thenReturn(EmployeeChecklistResponse.builder()
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
		verify(employeeChecklistServiceMock).updateManagerInformation(municipalityId, null);
		verifyNoMoreInteractions(employeeChecklistServiceMock, checklistPropertiesMock);
	}

	@Test
	void executeWhenProcessThrowsError() {
		// Arrange
		final var municipalityId1 = "municipalityId1";
		final var municipalityId2 = "municipalityId2";

		when(checklistPropertiesMock.managedMunicipalityIds()).thenReturn(List.of(municipalityId1, municipalityId2));
		when(employeeChecklistServiceMock.updateManagerInformation(municipalityId1, null)).thenThrow(Problem.valueOf(Status.I_AM_A_TEAPOT));

		// Act
		scheduler.execute();

		// Assert and verify
		verify(checklistPropertiesMock, times(2)).managedMunicipalityIds();
		verify(employeeChecklistServiceMock).updateManagerInformation(municipalityId1, null);
		verify(employeeChecklistServiceMock).updateManagerInformation(municipalityId2, null);
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
