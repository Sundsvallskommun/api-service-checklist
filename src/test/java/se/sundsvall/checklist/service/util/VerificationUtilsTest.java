package se.sundsvall.checklist.service.util;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zalando.problem.Status.NOT_ACCEPTABLE;
import static org.zalando.problem.Status.NOT_FOUND;

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
import se.sundsvall.checklist.service.model.Employee;
import se.sundsvall.checklist.service.model.Employment;
import se.sundsvall.checklist.service.model.Manager;

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
		final var employee = Employee.builder()
			.withLoginname("<loginName>")
			.withMainEmployment(Employment.builder()
				.withIsMainEmployment(true)
				.withFormOfEmploymentId("1")
				.withEventType(eventType)
				.build())
			.build();

		assertDoesNotThrow(() -> VerificationUtils.verifyValidEmployment(employee));
	}

	@Test
	void testValidEmploymentWhenNoMainEmployment() {
		final var employee = Employee.builder()
			.withLoginname("<loginName>")
			.build();

		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyValidEmployment(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo(NOT_FOUND.getReasonPhrase() + ": Creation of checklist not possible for employee with loginname <loginName> as the employee does not have any main employment.");
	}

	@Test
	void testValidEmploymentWhenNoEventType() {
		final var personId = UUID.randomUUID().toString();
		final var employee = Employee.builder()
			.withPersonId(personId)
			.withMainEmployment(Employment.builder()
				.withIsMainEmployment(true)
				.build())
			.build();

		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyValidEmployment(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo(NOT_FOUND.getReasonPhrase()
			+ ": Creation of checklist not possible for employee with personid %s as the main employment for the employee lacks event type information.".formatted(personId));
	}

	@Test
	void testValidEmploymentWhenNoFormOfEmployment() {
		final var personId = UUID.randomUUID().toString();
		final var employee = Employee.builder()
			.withPersonId(personId)
			.withMainEmployment(Employment.builder()
				.withIsMainEmployment(true)
				.withEventType("someType")
				.build())
			.build();

		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyValidEmployment(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo(NOT_FOUND.getReasonPhrase()
			+ ": Creation of checklist not possible for employee with personid %s as the main employment for the employee lacks information regarding form of employment.".formatted(personId));
	}

	@Test
	void testValidEmploymentWhenNotValidFormOfEmployment() {
		final var employee = Employee.builder()
			.withLoginname("<loginName>")
			.withMainEmployment(Employment.builder()
				.withIsMainEmployment(true)
				.withFormOfEmploymentId("notValid")
				.withEventType("someType")
				.build())
			.build();

		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyValidEmployment(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_ACCEPTABLE);
		assertThat(e.getMessage()).isEqualTo(NOT_ACCEPTABLE.getReasonPhrase()
			+ ": Creation of checklist not possible for employee with loginname <loginName> as the employee does not have a main employment with an employment form that validates for creating an employee checklist.");
	}

	@Test
	void testValidEmploymentWhenNotValidEventType() {
		final var employee = Employee.builder()
			.withLoginname("<loginName>")
			.withMainEmployment(Employment.builder()
				.withIsMainEmployment(true)
				.withFormOfEmploymentId("1")
				.withEventType("notValid")
				.build())
			.build();

		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyValidEmployment(employee));

		assertThat(e.getStatus()).isEqualTo(NOT_ACCEPTABLE);
		assertThat(e.getMessage()).isEqualTo(NOT_ACCEPTABLE.getReasonPhrase()
			+ ": Creation of checklist not possible for employee with loginname <loginName> as the employee does not have a main employment with an event type that validates for creating an employee checklist.");
	}

	@Test
	void testEmptyEmployee() {
		final var employee = Employee.builder().build();

		final var e = assertThrows(ThrowableProblem.class, () -> VerificationUtils.verifyMandatoryInformation(employee));
		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo(NOT_FOUND.getReasonPhrase() + ": Creation of checklist not possible for employee with personid null as "
			+ "the employee does not have any personid and the employee does not have any main employment.");
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

	private static Employee createEmployee(UUID employeeUuid, String loginName, String emailAddress, Integer orgId, Integer companyId, String formOfEmployment, String eventType, Boolean hasMainEmployment, Boolean hasManager, UUID managerUuid) {
		final var employee = Employee.builder();
		ofNullable(employeeUuid).map(UUID::toString).ifPresent(employee::withPersonId);
		ofNullable(emailAddress).ifPresent(employee::withEmailAddress);
		ofNullable(loginName).ifPresent(employee::withLoginname);
		ofNullable(hasMainEmployment).ifPresent(v -> {
			employee.withMainEmployment(createEmployment(orgId, companyId, formOfEmployment, eventType, hasManager, managerUuid));
		});

		return employee.build();
	}

	private static Employment createEmployment(Integer orgId, Integer companyId, String formOfEmployment, String eventType, Boolean hasManager, UUID managerId) {
		final var employment = Employment.builder().withIsMainEmployment(true);

		ofNullable(orgId).ifPresent(employment::withOrgId);
		ofNullable(companyId).ifPresent(employment::withCompanyId);
		ofNullable(formOfEmployment).ifPresent(employment::withFormOfEmploymentId);
		ofNullable(eventType).ifPresent(employment::withEventType);
		ofNullable(hasManager).ifPresent(create -> {
			if (create) {
				final var manager = Manager.builder();
				ofNullable(managerId).map(UUID::toString).ifPresent(manager::withPersonId);

				employment.withManager(manager.build());
			}

		});

		return employment.build();
	}
}
