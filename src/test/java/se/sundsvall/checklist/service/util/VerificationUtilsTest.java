package se.sundsvall.checklist.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.problem.Status.NOT_FOUND;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;

class VerificationUtilsTest {

	private static Stream<Arguments> employeeMissingVitalDataProvider() {
		final var emailAddress = "emailAddress";
		final var employeeUuid = UUID.randomUUID();
		final var loginName = "<loginName>";
		final var orgId = 1;
		final var companyId = 2;
		final var formOfEmploymentId = "1";
		final var managerUuid = UUID.randomUUID();

		return Stream.of(
			Arguments.of(createEmployee(null, loginName, emailAddress, orgId, companyId, formOfEmploymentId, true, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the employee does not have any personid.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, null, emailAddress, orgId, companyId, formOfEmploymentId, true, true, managerUuid),
				"Creation of checklist not possible for employee with personid %s as the employee does not have any loginname.".formatted(employeeUuid)),
			Arguments.of(createEmployee(employeeUuid, loginName, null, orgId, companyId, formOfEmploymentId, true, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the employee does not have any email address.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, null, companyId, formOfEmploymentId, true, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the main employment for the employee lacks neccessary department information.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, null, formOfEmploymentId, true, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the main employment for the employee lacks neccessary company information.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, null, true, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the main employment for the employee lacks information about the employment form.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, null, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the employee does not have any main employment.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, false, true, managerUuid),
				"Creation of checklist not possible for employee with loginname %s as the employee does not have any main employment.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, true, true, null),
				"Creation of checklist not possible for employee with loginname %s as the personid is missing for the manager connected to the main employment of the employee.".formatted(loginName)),
			Arguments.of(createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, true, false, null),
				"Creation of checklist not possible for employee with loginname %s as the main employment for the employee lacks information about the manager.".formatted(loginName)));
	}

	@ParameterizedTest
	@MethodSource("employeeMissingVitalDataProvider")
	void testEmployeeMissingVitalData(Employee employee, String expectedMessage) {
		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyMandatoryInformation(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo(NOT_FOUND.getReasonPhrase() + ": " + expectedMessage);
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
		final var managerUuid = UUID.randomUUID();
		final var employee = createEmployee(employeeUuid, loginName, emailAddress, orgId, companyId, formOfEmploymentId, true, true, managerUuid);

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

	private static Employee createEmployee(UUID employeeUuid, String loginName, String emailAddress, Integer orgId, Integer companyId, String formOfEmployment, Boolean isMainEmployment, Boolean hasManager, UUID managerUuid) {
		final var employee = new Employee();
		Optional.ofNullable(employeeUuid).ifPresent(employee::personId);
		Optional.ofNullable(emailAddress).ifPresent(employee::emailAddress);
		Optional.ofNullable(loginName).ifPresent(employee::loginname);
		Optional.ofNullable(isMainEmployment).ifPresent(v -> {
			employee.employments(List.of(createEmployment(isMainEmployment, orgId, companyId, formOfEmployment, hasManager, managerUuid)));
		});

		return employee;
	}

	private static Employment createEmployment(Boolean isMainEmployment, Integer orgId, Integer companyId, String formOfEmployment, Boolean hasManager, UUID managerId) {
		final var employment = new Employment();

		Optional.ofNullable(isMainEmployment).ifPresent(employment::isMainEmployment);
		Optional.ofNullable(orgId).ifPresent(employment::orgId);
		Optional.ofNullable(companyId).ifPresent(employment::companyId);
		Optional.ofNullable(formOfEmployment).ifPresent(employment::formOfEmploymentId);
		Optional.ofNullable(hasManager).ifPresent(create -> {
			if (create) {
				employment.manager(new Manager());
				Optional.ofNullable(managerId).ifPresent(p -> employment.getManager().personId(p));
			}

		});

		return employment;
	}
}
