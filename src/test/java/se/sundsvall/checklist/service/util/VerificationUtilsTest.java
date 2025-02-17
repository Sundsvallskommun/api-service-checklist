package se.sundsvall.checklist.service.util;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.problem.Status.NOT_ACCEPTABLE;
import static org.zalando.problem.Status.NOT_FOUND;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;

class VerificationUtilsTest {

	private static Stream<Arguments> employeeMissingVitalDataProvider() {
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var loginName = "<loginName>";
		final var orgId = 1;
		final var companyId = 2;
		final var formOfEmploymentId = "1";
		final var eventType = "Joiner";
		final var managerUuid = UUID.randomUUID();

		return Stream.of(
			Arguments.of(createEmployee(null, loginName, emailAddress, orgId, companyId, formOfEmploymentId, eventType, true, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the employee does not have any personid.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, null, emailAddress, orgId, companyId, formOfEmploymentId, eventType, true, true, managerUuid),
				"Creation of checklist not possible for employee with personid %s as the employee does not have any loginname.".formatted(employeeUuid)),
			Arguments.of(createEmployee(employeeUuid, loginName, null, orgId, companyId, formOfEmploymentId, eventType, true, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the employee does not have any email address.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, null, companyId, formOfEmploymentId, eventType, true, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the main employment for the employee lacks neccessary department information.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, null, formOfEmploymentId, eventType, true, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the main employment for the employee lacks neccessary company information.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, eventType, null, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the employee does not have any main employment.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, eventType, false, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the employee does not have any main employment.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, eventType, true, true, null),
				"Creation of checklist not possible for employee with loginname %s as the personid is missing for the manager connected to the main employment of the employee.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, eventType, true, false, null),
				"Creation of checklist not possible for employee with loginname %s as the main employment for the employee lacks information about the manager.".formatted(loginName)));
	}

	@ParameterizedTest
	@MethodSource("employeeMissingVitalDataProvider")
	void testEmployeeMissingVitalData(Employee employee, String expectedMessage) {
		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyMandatoryInformation(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo(NOT_FOUND.getReasonPhrase() + ": " + expectedMessage);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"joiner", "Joiner", "JOINER"
	})
	void testValidEmployment(String eventType) {
		final var employee = new Employee()
			.loginname("<loginName>")
			.employments(List.of(new Employment().isMainEmployment(true).formOfEmploymentId("1").eventType(eventType)));

		assertDoesNotThrow(() -> VerificationUtils.verifyValidEmployment(employee));

	}

	@Test
	void testValidEmploymentWhenNoMainEmployment() {
		final var employee = new Employee().loginname("<loginName>");
		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyValidEmployment(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo(NOT_FOUND.getReasonPhrase() + ": Creation of checklist not possible for employee with loginname <loginName> as the employee does not have any main employment.");
	}

	@Test
	void testValidEmploymentWhenNoEventType() {
		final var personId = UUID.randomUUID();
		final var employee = new Employee()
			.personId(personId)
			.employments(List.of(new Employment().isMainEmployment(true)));
		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyValidEmployment(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo(NOT_FOUND.getReasonPhrase()
			+ ": Creation of checklist not possible for employee with personid %s as the main employment for the employee lacks event type information.".formatted(personId.toString()));
	}

	@Test
	void testValidEmploymentWhenNotValidFormOfEmployment() {
		final var employee = new Employee()
			.loginname("<loginName>")
			.employments(List.of(new Employment().isMainEmployment(true).formOfEmploymentId("notValid").eventType("someType")));
		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyValidEmployment(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_ACCEPTABLE);
		assertThat(e.getMessage()).isEqualTo(NOT_ACCEPTABLE.getReasonPhrase()
			+ ": Creation of checklist not possible for employee with loginname <loginName> as the employee does not have a main employment with an employment form that validates for creating an employee checklist.");
	}

	@Test
	void testValidEmploymentWhenNotValidEventType() {
		final var employee = new Employee()
			.loginname("<loginName>")
			.employments(List.of(new Employment().isMainEmployment(true).formOfEmploymentId("1").eventType("notValid")));
		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyValidEmployment(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_ACCEPTABLE);
		assertThat(e.getMessage()).isEqualTo(NOT_ACCEPTABLE.getReasonPhrase()
			+ ": Creation of checklist not possible for employee with loginname <loginName> as the employee does not have a main employment with an event type that validates for creating an employee checklist.");
	}

	@Test
	void testEmptyEmployee() {
		final var employee = new Employee();

		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyMandatoryInformation(employee));
		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		System.out.println(e.getMessage());
		assertThat(e.getMessage()).isEqualTo(NOT_FOUND.getReasonPhrase() + ": Creation of checklist not possible for employee with personid null as "
			+ "the employee does not have any personid, "
			+ "the employee does not have any loginname, "
			+ "the employee does not have any email address and the employee does not have any main employment.");
	}

	@Test
	void testValidEmployee() {
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var loginName = "<loginName>";
		final var orgId = 1;
		final var companyId = 2;
		final var formOfEmploymentId = "1";
		final var eventType = "Joiner";
		final var managerUuid = UUID.randomUUID();
		final var employee = createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, eventType, true, true, managerUuid);

		assertDoesNotThrow(() -> VerificationUtils.verifyMandatoryInformation(employee));
	}

	@Test
	void verifyUnlockedEmployeeChecklistWhenUnlocked() {
		assertDoesNotThrow(() -> VerificationUtils.verifyUnlockedEmployeeChecklist(EmployeeChecklistEntity.builder().build()));
	}

	@Test
	void verifyUnlockedEmployeeChecklistWhenLocked() {
		final var employeeChecklistId = UUID.randomUUID().toString();
		final var employeeChecklist = EmployeeChecklistEntity.builder()
			.withId(employeeChecklistId)
			.withLocked(true)
			.build();

		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyUnlockedEmployeeChecklist(employeeChecklist));

		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Employee checklist with id %s is locked and can not be modified.".formatted(employeeChecklistId));
	}

	private static Employee createEmployee(UUID employeeUuid, String loginName, String emailAddress, Integer orgId, Integer companyId, String formOfEmployment, String eventType, Boolean isMainEmployment, Boolean hasManager, UUID managerUuid) {
		final var employee = new Employee();
		ofNullable(employeeUuid).ifPresent(employee::personId);
		ofNullable(emailAddress).ifPresent(employee::emailAddress);
		ofNullable(loginName).ifPresent(employee::loginname);
		ofNullable(isMainEmployment).ifPresent(v -> {
			employee.employments(List.of(createEmployment(isMainEmployment, orgId, companyId, formOfEmployment, eventType, hasManager, managerUuid)));
		});

		return employee;
	}

	private static Employment createEmployment(Boolean isMainEmployment, Integer orgId, Integer companyId, String formOfEmployment, String eventType, Boolean hasManager, UUID managerId) {
		final var employment = new Employment();

		ofNullable(isMainEmployment).ifPresent(employment::isMainEmployment);
		ofNullable(orgId).ifPresent(employment::orgId);
		ofNullable(companyId).ifPresent(employment::companyId);
		ofNullable(formOfEmployment).ifPresent(employment::formOfEmploymentId);
		ofNullable(eventType).ifPresent(employment::eventType);
		ofNullable(hasManager).ifPresent(create -> {
			if (create) {
				employment.manager(new Manager());
				ofNullable(managerId).ifPresent(p -> employment.getManager().personId(p));
			}

		});

		return employment;
	}
}
