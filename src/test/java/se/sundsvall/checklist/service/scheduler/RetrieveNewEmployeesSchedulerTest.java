package se.sundsvall.checklist.service.scheduler;

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

import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.service.EmployeeChecklistService;

@ExtendWith(MockitoExtension.class)
class RetrieveNewEmployeesSchedulerTest {

	@Mock
	private EmployeeChecklistService employeeChecklistServiceMock;

	@InjectMocks
	private RetrieveNewEmployeesScheduler scheduler;

	@Test
	void execute() {
		when(employeeChecklistServiceMock.initiateEmployeeChecklists()).thenReturn(EmployeeChecklistResponse.builder()
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

		verify(employeeChecklistServiceMock).initiateEmployeeChecklists();
		verifyNoMoreInteractions(employeeChecklistServiceMock);
	}
}
