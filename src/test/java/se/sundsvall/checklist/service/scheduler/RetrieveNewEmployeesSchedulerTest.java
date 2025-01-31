package se.sundsvall.checklist.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.integration.db.model.InitiationInfoEntity;
import se.sundsvall.checklist.integration.db.repository.InitiationRepository;
import se.sundsvall.checklist.service.EmployeeChecklistService;
import se.sundsvall.dept44.requestid.RequestId;

@ExtendWith(MockitoExtension.class)
class RetrieveNewEmployeesSchedulerTest {

	@Mock
	private EmployeeChecklistService employeeChecklistServiceMock;

	@Mock
	private ChecklistProperties checklistPropertiesMock;

	@Mock
	private InitiationRepository initiationRepositoryMock;

	@Captor
	private ArgumentCaptor<List<InitiationInfoEntity>> initiationInfoEntityCaptor;

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
		mockStatic(RequestId.class).when(RequestId::get).thenReturn("logId");

		scheduler.execute();

		verify(checklistPropertiesMock, times(2)).managedMunicipalityIds();
		verify(initiationRepositoryMock).saveAll(initiationInfoEntityCaptor.capture());
		var initiationInfoEntities = initiationInfoEntityCaptor.getValue();
		assertThat(initiationInfoEntities).hasSize(2).allSatisfy(initiationInfoEntity -> {
			assertThat(initiationInfoEntity.getMunicipalityId()).isEqualTo(municipalityId);
			assertThat(initiationInfoEntity.getLogId()).isEqualTo("logId");
			assertThat(initiationInfoEntity.getInformation()).isIn("Ok string", "Error string");
			assertThat(initiationInfoEntity.getStatus()).isIn("200 OK", "404 Not Found");
		});
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
