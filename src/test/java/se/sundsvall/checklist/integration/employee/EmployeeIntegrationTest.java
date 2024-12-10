package se.sundsvall.checklist.integration.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;

import generated.se.sundsvall.employee.PortalPersonData;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.checklist.TestObjectFactory;
import se.sundsvall.dept44.exception.ClientProblem;

@ExtendWith(MockitoExtension.class)
class EmployeeIntegrationTest {

	@Mock
	private EmployeeClient employeeClientMock;

	@InjectMocks
	private EmployeeIntegration employeeIntegration;

	private final UUID uuid = UUID.randomUUID();

	@Test
	void testGetEmployeeInformation_shouldReturnListOfEmployees_whenOk() {
		var response = List.of(TestObjectFactory.generateEmployee(uuid));
		when(employeeClientMock.getEmployeeInformation(anyString())).thenReturn(Optional.of(response));

		var employeeInformation = employeeIntegration.getEmployeeInformation("filterString");

		assertThat(employeeInformation).hasSize(1);
		assertThat(employeeInformation.getFirst().getPersonId()).isEqualByComparingTo(uuid);
		verify(employeeClientMock).getEmployeeInformation("filterString");
	}

	@Test
	void testGetEmployeeInformation_shouldReturnEmptyList_whenNotOk() {
		when(employeeClientMock.getEmployeeInformation(Mockito.anyString())).thenThrow(new ClientProblem(NOT_FOUND, "Not found"));

		var employeeInformation = employeeIntegration.getEmployeeInformation("filterString");

		assertThat(employeeInformation).isEmpty();
		verify(employeeClientMock).getEmployeeInformation("filterString");
	}

	@Test
	void getNewEmployees_shouldReturnListOfEmployees_whenOk() {
		var employees = List.of(TestObjectFactory.generateEmployee(uuid));
		when(employeeClientMock.getNewEmployees(Mockito.anyString())).thenReturn(Optional.of(employees));

		var employeeInformation = employeeIntegration.getNewEmployees("filterString");

		assertThat(employeeInformation).hasSize(1);
		assertThat(employeeInformation.getFirst().getPersonId()).isEqualByComparingTo(uuid);
		verify(employeeClientMock).getNewEmployees("filterString");
	}

	@Test
	void getNewEmployees_shouldReturnEmptyList_whenNotOk() {
		when(employeeClientMock.getNewEmployees(Mockito.anyString())).thenThrow(new ClientProblem(NOT_FOUND, "Not found"));

		var employeeInformation = employeeIntegration.getNewEmployees("filterString");

		assertThat(employeeInformation).isEmpty();
		verify(employeeClientMock).getNewEmployees("filterString");
	}

	@Test
	void getEmployeeByEmail_shouldReturnEmployee_whenOk() {
		var portalPersonData = Optional.of(TestObjectFactory.generatePortalPersonData(uuid));
		when(employeeClientMock.getEmployeeByEmail(Mockito.anyString())).thenReturn(portalPersonData);

		var employeeByEmail = employeeIntegration.getEmployeeByEmail("email");

		assertThat(employeeByEmail).isPresent();
		assertThat(employeeByEmail.get().getPersonid()).isEqualByComparingTo(uuid);
		verify(employeeClientMock).getEmployeeByEmail("email");

	}

	@Test
	void getEmployeeByEmail_shouldReturnEmptyOptional_whenEmpty() {
		var portalPersonData = Optional.<PortalPersonData>empty();
		when(employeeClientMock.getEmployeeByEmail(Mockito.anyString())).thenReturn(portalPersonData);

		var employeeByEmail = employeeIntegration.getEmployeeByEmail("email");

		assertThat(employeeByEmail).isEmpty();
		verify(employeeClientMock).getEmployeeByEmail("email");
	}

	@Test
	void getEmployeeByEmail_shouldReturnEmptyOptional_whenException() {
		when(employeeClientMock.getEmployeeByEmail(Mockito.anyString())).thenThrow(new ClientProblem(INTERNAL_SERVER_ERROR, "Internal Server Error"));

		var employeeByEmail = employeeIntegration.getEmployeeByEmail("email");

		assertThat(employeeByEmail).isEmpty();
		verify(employeeClientMock).getEmployeeByEmail("email");
	}
}
