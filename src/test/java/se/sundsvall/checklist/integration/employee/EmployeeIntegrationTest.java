package se.sundsvall.checklist.integration.employee;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;

import generated.se.sundsvall.employee.PortalPersonData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;
import se.sundsvall.checklist.TestObjectFactory;
import se.sundsvall.dept44.exception.ClientProblem;

@ExtendWith(MockitoExtension.class)
class EmployeeIntegrationTest {
	private static final String MUNICIPALITY_ID = "2281";
	private static final String PERSON_ID = "personId";
	private static final LocalDate HIRE_DATE_FROM = LocalDate.now();
	private static final String EMAIL = "email@some.com";

	@Mock
	private EmployeeClient employeeClientMock;

	@InjectMocks
	private EmployeeIntegration employeeIntegration;

	private final UUID uuid = UUID.randomUUID();

	@Test
	void verifyCacheAnnotations() throws NoSuchMethodException {
		assertThat(EmployeeIntegration.class.getMethod("getEmployeeByEmail", String.class, String.class).getAnnotation(Cacheable.class).value()).containsExactly("employee");
	}

	@Test
	void testGetEmployeeInformation_shouldReturnListOfEmployees_whenOk() {
		final var response = List.of(TestObjectFactory.generateEmployee(uuid));

		when(employeeClientMock.getEmployeesByPersonId(any(), any())).thenReturn(Optional.of(response));

		final var employeeInformation = employeeIntegration.getEmployeeInformation(MUNICIPALITY_ID, PERSON_ID);

		assertThat(employeeInformation).hasSize(1);
		assertThat(employeeInformation.getFirst().getPersonId()).isEqualTo(uuid.toString());
		verify(employeeClientMock).getEmployeesByPersonId(MUNICIPALITY_ID, PERSON_ID);
	}

	@Test
	void testGetEmployeeInformation_shouldReturnEmptyList_whenNotOk() {
		when(employeeClientMock.getEmployeesByPersonId(any(), any())).thenThrow(new ClientProblem(NOT_FOUND, "Not found"));

		final var employeeInformation = employeeIntegration.getEmployeeInformation(MUNICIPALITY_ID, PERSON_ID);

		assertThat(employeeInformation).isEmpty();
		verify(employeeClientMock).getEmployeesByPersonId(MUNICIPALITY_ID, PERSON_ID);
	}

	@Test
	void getNewEmployees_shouldReturnListOfEmployees_whenOk() {
		final var employees = List.of(TestObjectFactory.generateNewEmployee(uuid));

		when(employeeClientMock.getNewEmployees(any(), any())).thenReturn(Optional.of(employees));

		final var employeeInformation = employeeIntegration.getNewEmployees(MUNICIPALITY_ID, HIRE_DATE_FROM);

		assertThat(employeeInformation).hasSize(1);
		assertThat(employeeInformation.getFirst().getPersonId()).isEqualTo(uuid.toString());
		verify(employeeClientMock).getNewEmployees(MUNICIPALITY_ID, HIRE_DATE_FROM.format(ISO_LOCAL_DATE));
	}

	@Test
	void getNewEmployees_withHireDateFromNull() {
		final var employees = List.of(TestObjectFactory.generateNewEmployee(uuid));

		when(employeeClientMock.getNewEmployees(any(), any())).thenReturn(Optional.of(employees));

		final var employeeInformation = employeeIntegration.getNewEmployees(MUNICIPALITY_ID, null);

		assertThat(employeeInformation).hasSize(1);
		assertThat(employeeInformation.getFirst().getPersonId()).isEqualTo(uuid.toString());
		verify(employeeClientMock).getNewEmployees(MUNICIPALITY_ID, null);
	}

	@Test
	void getNewEmployees_shouldReturnEmptyList_whenNotOk() {
		when(employeeClientMock.getNewEmployees(any(), any())).thenThrow(new ClientProblem(NOT_FOUND, "Not found"));

		final var employeeInformation = employeeIntegration.getNewEmployees(MUNICIPALITY_ID, HIRE_DATE_FROM);

		assertThat(employeeInformation).isEmpty();
		verify(employeeClientMock).getNewEmployees(MUNICIPALITY_ID, HIRE_DATE_FROM.format(ISO_LOCAL_DATE));
	}

	@Test
	void getEmployeeByEmail_shouldReturnEmployee_whenOk() {
		final var portalPersonData = Optional.of(TestObjectFactory.generatePortalPersonData(uuid));

		when(employeeClientMock.getEmployeeByEmail(any(), any())).thenReturn(portalPersonData);

		final var employeeByEmail = employeeIntegration.getEmployeeByEmail(MUNICIPALITY_ID, EMAIL);

		assertThat(employeeByEmail).isPresent();
		assertThat(employeeByEmail.get().getPersonid()).isEqualByComparingTo(uuid);
		verify(employeeClientMock).getEmployeeByEmail(MUNICIPALITY_ID, EMAIL);
	}

	@Test
	void getEmployeeByEmail_shouldReturnEmptyOptional_whenEmpty() {
		final var portalPersonData = Optional.<PortalPersonData>empty();

		when(employeeClientMock.getEmployeeByEmail(any(), any())).thenReturn(portalPersonData);

		final var employeeByEmail = employeeIntegration.getEmployeeByEmail(MUNICIPALITY_ID, EMAIL);

		assertThat(employeeByEmail).isEmpty();
		verify(employeeClientMock).getEmployeeByEmail(MUNICIPALITY_ID, EMAIL);
	}

	@Test
	void getEmployeeByEmail_shouldReturnEmptyOptional_whenException() {
		when(employeeClientMock.getEmployeeByEmail(any(), any())).thenThrow(new ClientProblem(INTERNAL_SERVER_ERROR, "Internal Server Error"));

		final var employeeByEmail = employeeIntegration.getEmployeeByEmail(MUNICIPALITY_ID, EMAIL);

		assertThat(employeeByEmail).isEmpty();
		verify(employeeClientMock).getEmployeeByEmail(MUNICIPALITY_ID, EMAIL);
	}
}
